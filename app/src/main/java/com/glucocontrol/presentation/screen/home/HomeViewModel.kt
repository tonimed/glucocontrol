package com.glucocontrol.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glucocontrol.domain.model.GlucoseRange
import com.glucocontrol.domain.model.GlucoseReading
import com.glucocontrol.domain.usecase.query.GetDailyReadingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val todayReadings: List<GlucoseReading> = emptyList(),
    val glucoseRange: GlucoseRange = GlucoseRange(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class HomeViewModel @Inject constructor(private val getDailyReadingsUseCase: GetDailyReadingsUseCase) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init {
        viewModelScope.launch {
            getDailyReadingsUseCase(LocalDate.now())
                .collect { readings ->
                    _uiState.update { it.copy(todayReadings = readings, isLoading = false) }
                }
        }
    }
}
