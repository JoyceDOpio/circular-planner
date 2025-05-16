package com.example.circularplanner.utils

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import com.example.circularplanner.data.Task
import com.example.circularplanner.ui.viewmodel.DataViewModel
import java.util.UUID

@Composable
fun TaskList(
    viewModel: DataViewModel,
    onNavigateToTaskEdit: () -> Unit,
    tasks: List<Task>,
    getTask: (UUID) -> Task?,
    removeTask: (UUID) -> Unit
) {
    LazyColumn {
        items(tasks) { task ->
            TaskListItem(
                viewModel = viewModel,
                onNavigateToTaskEdit = onNavigateToTaskEdit,
                taskId = task.id,
                getTask = getTask,
                removeTask = removeTask
            )
        }
    }
}