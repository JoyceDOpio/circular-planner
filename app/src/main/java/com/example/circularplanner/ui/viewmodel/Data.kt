package com.example.circularplanner.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

data class DataUiState (
    val date: LocalDate = LocalDate.now(),
)

class DataViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DataUiState())
    val uiState: StateFlow<DataUiState> = _uiState.asStateFlow()

    fun setDate(date: LocalDate) {
        _uiState.update {
            currentState -> currentState.copy(
                date = date
            )
        }
    }
}