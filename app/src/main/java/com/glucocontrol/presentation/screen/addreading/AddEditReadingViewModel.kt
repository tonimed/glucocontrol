package com.glucocontrol.presentation.screen.addreading

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glucocontrol.domain.model.GlucoseReading
import com.glucocontrol.domain.model.ReadingTag
import com.glucocontrol.domain.usecase.reading.AddReadingUseCase
import com.glucocontrol.domain.usecase.reading.GetReadingByIdUseCase
import com.glucocontrol.domain.usecase.reading.UpdateReadingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class AddEditUiState(
    val date: LocalDate = LocalDate.now(),
    val time: LocalTime? = null,
    val valueMgDl: String = "",
    val tag: ReadingTag = ReadingTag.OTRO,
    val notes: String = "",
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val savedSuccessfully: Boolean = false,
)

@HiltViewModel
class AddEditReadingViewModel
@Inject
constructor(
    savedStateHandle: SavedStateHandle,
    private val addUseCase: AddReadingUseCase,
    private val updateUseCase: UpdateReadingUseCase,
    private val getByIdUseCase: GetReadingByIdUseCase,
) : ViewModel() {
    // readingId = -1 → nueva lectura; > 0 → editar lectura existente
    private val readingId: Long = savedStateHandle["readingId"] ?: -1L

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState

    init {
        if (readingId > 0) {
            viewModelScope.launch {
                val reading = getByIdUseCase(readingId)
                if (reading != null) {
                    _uiState.update {
                        it.copy(
                            date = reading.date,
                            time = reading.time,
                            valueMgDl = reading.valueMgDl.toString(),
                            tag = reading.tag,
                            notes = reading.notes ?: "",
                            isEditing = true,
                        )
                    }
                }
            }
        }
    }

    fun onDateChanged(date: LocalDate) {
        _uiState.update { it.copy(date = date, error = null) }
    }

    fun onTimeChanged(time: LocalTime?) {
        _uiState.update { it.copy(time = time) }
    }

    fun onValueChanged(value: String) {
        _uiState.update { it.copy(valueMgDl = value.filter { c -> c.isDigit() }, error = null) }
    }

    fun onTagChanged(tag: ReadingTag) {
        _uiState.update { it.copy(tag = tag) }
    }

    fun onNotesChanged(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun save() {
        val state = _uiState.value
        val value = state.valueMgDl.toIntOrNull()
        if (value == null) {
            _uiState.update { it.copy(error = "Introduce un valor numérico válido") }
            return
        }
        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            try {
                val reading =
                    GlucoseReading(
                        id = if (state.isEditing) readingId else 0L,
                        date = state.date,
                        time = state.time,
                        valueMgDl = value,
                        tag = state.tag,
                        notes = state.notes.trim().ifBlank { null },
                    )
                if (state.isEditing) updateUseCase(reading) else addUseCase(reading)
                _uiState.update { it.copy(isSaving = false, savedSuccessfully = true) }
            } catch (e: IllegalArgumentException) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }
}
