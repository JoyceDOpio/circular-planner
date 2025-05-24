package com.example.circularplanner.ui.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.example.circularplanner.data.Time
import java.util.UUID

@Stable
class TaskState() {
    var activeTimeStart: Time = Time(0, 0)
    var activeTimeEnd: Time = Time(0, 0)
    var taskId: UUID? = null
    var taskStartTime: Time = Time(0, 0)
    var taskEndTime: Time = Time(0, 0)
}

@Composable
fun rememberTaskState(): TaskState {
    return remember () { TaskState() }
}