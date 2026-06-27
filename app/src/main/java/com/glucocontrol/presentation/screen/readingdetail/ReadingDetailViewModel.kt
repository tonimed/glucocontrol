package com.glucocontrol.presentation.screen.readingdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glucocontrol.domain.model.GlucoseRange
import com.glucocontrol.domain.model.GlucoseReading
import com.glucocontrol.domain.usecase.reading.GetReadingByIdUseCase
import com.glucocontrol.domain.usecase.reading.GetReadingsByDateRangeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReadingDetailUiState(
    val reading: GlucoseReading? = null,
    val period: DetailPeriod = DetailPeriod.THREE_DAYS,
    val periodReadings: List<GlucoseReading> = emptyList(),
    val glucoseRange: GlucoseRange = GlucoseRange(),
    val isLoading: Boolean = true,
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ReadingDetailViewModel
@Inject
constructor(
    savedStateHandle: SavedStateHandle,
    private val getReadingByIdUseCase: GetReadingByIdUseCase,
    private val getReadingsByDateRangeUseCase: GetReadingsByDateRangeUseCase,
) : ViewModel() {

    private val readingId: Long = checkNotNull(savedStateHandle["readingId"])
    private val detailPeriodFlow = MutableStateFlow(DetailPeriod.THREE_DAYS)

    private val _uiState = MutableStateFlow(ReadingDetailUiState())
    val uiState: StateFlow<ReadingDetailUiState> = _uiState

    init {
        viewModelScope.launch {
            val reading = getReadingByIdUseCase(readingId) ?: run {
                // La lectura fue eliminada mientras se navegaba a esta pantalla
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }
            _uiState.update { it.copy(reading = reading) }

            // flatMapLatest cancela la consulta anterior al cambiar el periodo
            detailPeriodFlow
                .flatMapLatest { period ->
                    val (from, to) = period.dateRange(reading.date)
                    getReadingsByDateRangeUseCase(from, to)
                }
                .collect { periodReadings ->
                    _uiState.update { it.copy(periodReadings = periodReadings, isLoading = false) }
                }
        }
    }

    fun setPeriod(period: DetailPeriod) {
        detailPeriodFlow.value = period
        _uiState.update { it.copy(period = period) }
    }
}
