package com.glucocontrol.presentation.screen.chart

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glucocontrol.domain.model.GlucoseRange
import com.glucocontrol.domain.usecase.query.GetMonthlyReadingsUseCase
import com.glucocontrol.domain.usecase.query.GetWeeklyReadingsUseCase
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

enum class ChartPeriod { WEEKLY, MONTHLY }

data class ChartPoint(val date: LocalDate, val avgMgDl: Float)

data class ChartUiState(
    val points: List<ChartPoint> = emptyList(),
    val period: ChartPeriod = ChartPeriod.WEEKLY,
    val referenceDate: LocalDate = LocalDate.now(),
    val glucoseRange: GlucoseRange = GlucoseRange(),
    val isLoading: Boolean = true,
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ChartViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getWeeklyReadingsUseCase: GetWeeklyReadingsUseCase,
    private val getMonthlyReadingsUseCase: GetMonthlyReadingsUseCase,
) : ViewModel() {

    // Lee el periodo inicial del argumento de navegación; usa WEEKLY como fallback seguro
    private val initialPeriod = runCatching {
        ChartPeriod.valueOf(savedStateHandle.get<String>("period") ?: "WEEKLY")
    }.getOrDefault(ChartPeriod.WEEKLY)

    private val periodFlow = MutableStateFlow(initialPeriod)
    private val referenceDateFlow = MutableStateFlow(LocalDate.now())

    private val _uiState = MutableStateFlow(ChartUiState(period = initialPeriod))
    val uiState: StateFlow<ChartUiState> = _uiState

    init {
        viewModelScope.launch {
            combine(periodFlow, referenceDateFlow) { p, d -> p to d }
                .flatMapLatest { (period, date) ->
                    when (period) {
                        ChartPeriod.WEEKLY -> getWeeklyReadingsUseCase(date)
                        ChartPeriod.MONTHLY -> getMonthlyReadingsUseCase(date.year, date.month)
                    }
                }.collect { readings ->
                    // Agregar lecturas por media diaria para no saturar el eje X
                    val points = readings
                        .groupBy { it.date }
                        .entries
                        .sortedBy { it.key }
                        .map { (date, dayReadings) ->
                            ChartPoint(
                                date = date,
                                avgMgDl = dayReadings.map { it.valueMgDl }.average().toFloat(),
                            )
                        }
                    _uiState.update { it.copy(points = points, isLoading = false) }
                }
        }
    }

    fun setPeriod(period: ChartPeriod) {
        periodFlow.value = period
        _uiState.update { it.copy(period = period, isLoading = true) }
    }

    fun previousPeriod() {
        val newDate =
            when (periodFlow.value) {
                ChartPeriod.WEEKLY -> referenceDateFlow.value.minusWeeks(1)
                ChartPeriod.MONTHLY -> referenceDateFlow.value.minusMonths(1)
            }
        referenceDateFlow.value = newDate
        _uiState.update { it.copy(referenceDate = newDate, isLoading = true) }
    }

    fun nextPeriod() {
        val newDate =
            when (periodFlow.value) {
                ChartPeriod.WEEKLY -> referenceDateFlow.value.plusWeeks(1)
                ChartPeriod.MONTHLY -> referenceDateFlow.value.plusMonths(1)
            }
        if (newDate.isAfter(LocalDate.now())) return
        referenceDateFlow.value = newDate
        _uiState.update { it.copy(referenceDate = newDate, isLoading = true) }
    }
}
