package com.example.circularplanner.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.circularplanner.PlannerApplication
import com.example.circularplanner.data.Day
import com.example.circularplanner.data.IDaysRepository
import com.example.circularplanner.data.ITasksRepository
import com.example.circularplanner.data.Task
import com.example.circularplanner.data.Time
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import kotlin.String

data class TaskDisplayUiState (
    val selectedDate: LocalDate = LocalDate.now(),
    val dayDetails: DayDetails = DayDetails(),
    val taskDetails: TaskDetails = TaskDetails(),
    val isActiveTimeValid: Boolean = false,
//    val isTaskTimeValid: Boolean = false
    val isList: Boolean = false,
    val isActiveTimeSetUp: Boolean = false
)

typealias Tasks = List<Task>
data class DayDetails (
    val date: LocalDate = LocalDate.now(),
    val activeTimeStart: Time = Time(6, 0),
    val activeTimeEnd: Time = Time(22, 0),
    val tasks: Tasks = emptyList()
)

data class TaskDetails (
    val id: UUID? = null,
    val date: LocalDate = LocalDate.now(),
    var title: String = "",
    var startTime: Time = Time(0, 0),
    var endTime: Time = Time(0, 0),
    var description: String = ""
)

class TaskDisplayViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val daysRepository: IDaysRepository,
    private val tasksRepository: ITasksRepository
) : ViewModel() {
    companion object {
        private const val MILLS = 5_000L

        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                // Get the Application object from extras
                val application = checkNotNull(extras[APPLICATION_KEY])
                // Create a SavedStateHandle for this ViewModel from extras
                val savedStateHandle = extras.createSavedStateHandle()

                return TaskDisplayViewModel(
                    savedStateHandle,
                    (application as PlannerApplication).container.daysRepository,
                    (application as PlannerApplication).container.tasksRepository,
                ) as T
            }
        }
    }

    val _uiState: MutableStateFlow<TaskDisplayUiState> = MutableStateFlow(TaskDisplayUiState())
    val uiState: StateFlow<TaskDisplayUiState> = _uiState.asStateFlow()

    // Initialize data in ViewModel
    init {
        loadDay()
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            tasksRepository.deleteTask(task)
        }
    }

    fun getTask(id: UUID?) {
        viewModelScope.launch {
            // If we're looking for a specific task
            if (id != null) {
                tasksRepository
                    .getTaskStream(id)
                    .map() {
                            task ->
                        TaskDetails(
                            id = task?.id,
                            date = task?.date!!,
                            title = task.title,
                            startTime = task.startTime,
                            endTime = task.endTime,
                            description = task.description
                        )
                    }
                    .collect { newTaskDetails ->
                        _uiState.update {
                            it.copy(
                                taskDetails = newTaskDetails
                            )
                        }
                    }
            }
            // Else, reset task details except for the date
            else {
                resetTaskDetails()
            }
        }
    }

    fun loadDay() {
        viewModelScope.launch {
            val date = _uiState.value.selectedDate
            val dayFlow = daysRepository.getDayStream(date.toString())
            val tasksFlow = tasksRepository.getAllTasksPerDayStream(date.toString())

            combine(
                flow = dayFlow,
                flow2 = tasksFlow
            ) { day, tasks ->
                DayDetails(
                    date = date,
                    activeTimeStart = day?.activeTimeStart ?: Time(6, 0),
                    activeTimeEnd = day?.activeTimeEnd ?: Time(22, 0),
                    tasks = tasks
                )
            }
                .collect { newDayDetails ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            dayDetails = newDayDetails
                        )
                    }
                }
        }
    }

    fun resetTaskDetails() {
        _uiState.update { currentState ->
            currentState.copy(
                taskDetails = TaskDetails(
                    date = currentState.taskDetails.date
                )
            )
        }
    }

    fun saveDay() {
        viewModelScope.launch {
            daysRepository.insertDay(uiState.value.dayDetails.toDay())
        }
    }

    fun saveTask() {
        viewModelScope.launch {
            // If tasks exists, update it
            val taskId = _uiState.value.taskDetails.id

            if (taskId != null) {
                val task = _uiState.value.dayDetails.tasks.find { task -> task.id == taskId }

                if (task != null) {
                    val updatedTask = _uiState.value.taskDetails.toTask().copy(
                        id = taskId
                    )
                    val numberOfUpdatedRows = tasksRepository.updateTask(updatedTask)

                    if (numberOfUpdatedRows > 0) {
                        resetTaskDetails()
                    }
                }
            }
            // Else, save the new task
            else {
                // There are no tasks for the given day, save the day.
                if (uiState.value.dayDetails.tasks.size == 0) {
                    daysRepository.insertDay(uiState.value.dayDetails.toDay())
                }

                val rowId = tasksRepository.insertTask(uiState.value.taskDetails.toTask())

                if (rowId is Long) {
                    resetTaskDetails()
                }
            }
        }
    }

    fun selectTask(id: UUID?) {
        if (id != null) {
            val task = _uiState.value.dayDetails.tasks.find { task -> task.id == id }
            if (task != null) {
                _uiState.update {
                    it.copy(
                        taskDetails = it.taskDetails.copy(
                            id = task.id,
                            date = task.date,
                            title = task.title,
                            startTime = task.startTime,
                            endTime = task.endTime,
                            description = task.description
                        )
                    )
                }
            }
        } else {
            resetTaskDetails()
        }
    }

    fun setActiveTimeEnd(time: Time) {
        _uiState.update {
            currentState -> currentState.copy(
                dayDetails = currentState.dayDetails.copy(activeTimeEnd = time)
            )
        }
    }

    fun setActiveTimeStart(time: Time) {
        _uiState.update {
            currentState -> currentState.copy(
                dayDetails = currentState.dayDetails.copy(activeTimeStart = time)
            )
        }
    }

    fun setActiveTimeIsValid(isValid: Boolean) {
        _uiState.update {
            currentState -> currentState.copy(
                isActiveTimeValid = isValid
            )
        }
    }

    fun setIsActiveTimeSetUp(value: Boolean) {
        _uiState.update {
            it.copy(
                isActiveTimeSetUp =  value
            )
        }
    }

    fun setIsList(value: Boolean) {
        _uiState.update {
            it.copy(
                isList = value
            )
        }
    }

    fun setSelectedDate(date: LocalDate) {
        _uiState.update {
            it.copy(
                selectedDate = date,
                taskDetails = it.taskDetails.copy(
                    date = date
                )
            )
        }
        loadDay()
    }

    fun setTaskDescription (description: String) {
        _uiState.update { currentState ->
            currentState.copy(
                taskDetails = currentState.taskDetails.copy(
                    description = description
                )
            )
        }
    }

    fun setTaskEndTime(time: Time) {
        _uiState.update {
            currentState -> currentState.copy(
                taskDetails = currentState.taskDetails.copy(
                    endTime = time
                )
            )
        }
    }

    fun setTaskStartTime(time: Time) {
        _uiState.update {
            currentState -> currentState.copy(
                taskDetails = currentState.taskDetails.copy(
                    startTime = time
                )
            )
        }
    }

    fun setTaskTitle (title: String) {
        _uiState.update { currentState ->
            currentState.copy(
                taskDetails = currentState.taskDetails.copy(
                    title = title
                )
            )
        }
    }

    fun updateTasks(date: LocalDate) {
        viewModelScope.launch {
            tasksRepository.getAllTasksPerDayStream(date.toString())
                .collect { tasks ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            dayDetails = currentState.dayDetails.copy(
                                tasks = tasks
                            )
                        )
                    }
                }
        }
    }
}

// Extension function to convert [DayDetails] to [Day].
fun Day.toDayDetails(tasks: List<Task>): DayDetails = DayDetails(
    date = date,
    activeTimeStart = activeTimeStart,
    activeTimeEnd = activeTimeEnd,
    tasks = tasks
)

fun Day.toDayUiState(tasks: List<Task>): TaskDisplayUiState = TaskDisplayUiState(
    dayDetails = this.toDayDetails(tasks),
    selectedDate = this.date,
)

fun DayDetails.toDay(): Day = Day(
    date = this.date,
    activeTimeStart = this.activeTimeStart,
    activeTimeEnd = this.activeTimeEnd
)

fun TaskDetails.toTask(): Task = Task(
    date = this.date!!,
    title = this.title,
    startTime = this.startTime,
    endTime = this.endTime,
    description = this.description
)