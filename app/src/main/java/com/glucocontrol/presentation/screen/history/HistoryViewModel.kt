package com.glucocontrol.presentation.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glucocontrol.domain.model.GlucoseRange
import com.glucocontrol.domain.model.GlucoseReading
import com.glucocontrol.domain.usecase.query.GetDailyReadingsUseCase
import com.glucocontrol.domain.usecase.query.GetMonthlyReadingsUseCase
import com.glucocontrol.domain.usecase.query.GetWeeklyReadingsUseCase
import com.glucocontrol.domain.usecase.reading.DeleteReadingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class HistoryPeriod { DAILY, WEEKLY, MONTHLY }

data class HistoryUiState(
    val groupedReadings: Map<LocalDate, List<GlucoseReading>> = emptyMap(),
    val period: HistoryPeriod = HistoryPeriod.WEEKLY,
    val referenceDate: LocalDate = LocalDate.now(),
    val glucoseRange: GlucoseRange = GlucoseRange(),
    val isLoading: Boolean = true,
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel
@Inject
constructor(
    private val getDailyReadingsUseCase: GetDailyReadingsUseCase,
    private val getWeeklyReadingsUseCase: GetWeeklyReadingsUseCase,
    private val getMonthlyReadingsUseCase: GetMonthlyReadingsUseCase,
    private val deleteReadingUseCase: DeleteReadingUseCase,
) : ViewModel() {
    private val periodFlow = MutableStateFlow(HistoryPeriod.WEEKLY)
    private val referenceDateFlow = MutableStateFlow(LocalDate.now())

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState

    init {
        viewModelScope.launch {
            // flatMapLatest cancela la suscripción anterior cuando cambia periodo o fecha
            combine(periodFlow, referenceDateFlow) { p, d -> p to d }
                .flatMapLatest { (period, date) ->
                    when (period) {
                        HistoryPeriod.DAILY -> getDailyReadingsUseCase(date)
                        HistoryPeriod.WEEKLY -> getWeeklyReadingsUseCase(date)
                        HistoryPeriod.MONTHLY -> getMonthlyReadingsUseCase(date.year, date.month)
                    }
                }.collect { readings ->
                    _uiState.update { state ->
                        state.copy(
                            groupedReadings = readings.groupBy { it.date }.toSortedMap(reverseOrder()),
                            isLoading = false,
                        )
                    }
                }
        }
    }

    fun setPeriod(period: HistoryPeriod) {
        periodFlow.value = period
        _uiState.update { it.copy(period = period, isLoading = true) }
    }

    fun previousPeriod() {
        val newDate =
            when (periodFlow.value) {
                HistoryPeriod.DAILY -> referenceDateFlow.value.minusDays(1)
                HistoryPeriod.WEEKLY -> referenceDateFlow.value.minusWeeks(1)
                HistoryPeriod.MONTHLY -> referenceDateFlow.value.minusMonths(1)
            }
        referenceDateFlow.value = newDate
        _uiState.update { it.copy(referenceDate = newDate, isLoading = true) }
    }

    fun nextPeriod() {
        val newDate =
            when (periodFlow.value) {
                HistoryPeriod.DAILY -> referenceDateFlow.value.plusDays(1)
                HistoryPeriod.WEEKLY -> referenceDateFlow.value.plusWeeks(1)
                HistoryPeriod.MONTHLY -> referenceDateFlow.value.plusMonths(1)
            }
        if (newDate.isAfter(LocalDate.now())) return
        referenceDateFlow.value = newDate
        _uiState.update { it.copy(referenceDate = newDate, isLoading = true) }
    }

    fun deleteReading(id: Long) {
        viewModelScope.launch { deleteReadingUseCase(id) }
    }
}
