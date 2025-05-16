package com.example.circularplanner.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.circularplanner.data.Time
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

data class DataUiState (
    val activeTimeStart: Time? = null,
    val activeTimeEnd: Time? = null,
    val taskId: UUID? = null
)

class DataViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(DataUiState())
    val uiState: StateFlow<DataUiState> = _uiState.asStateFlow()

    fun setActiveTime(startTime: Time, endTime: Time) {
        _uiState.update {
            currentState -> currentState.copy(
                activeTimeStart = startTime,
                activeTimeEnd = endTime
            )
        }
    }

    fun setTaskId(id: UUID?) {
        _uiState.update {
            currentState -> currentState.copy(
                taskId = id
            )
        }
    }


    fun reset() {
        _uiState.update {
                currentState -> currentState.copy(
            activeTimeStart = null,
            activeTimeEnd = null
        )
        }
    }
}