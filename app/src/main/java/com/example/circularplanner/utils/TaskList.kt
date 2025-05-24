package com.example.circularplanner.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.circularplanner.data.Task
import com.example.circularplanner.ui.state.TaskState
import com.example.circularplanner.ui.viewmodel.DataViewModel
import java.util.UUID

@Composable
fun TaskList(
//    viewModel: DataViewModel,
    taskState: TaskState,
    onNavigateToTaskEdit: () -> Unit,
    tasks: List<Task>,
    getTask: (UUID) -> Task?,
    removeTask: (Task) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(
            items = tasks,
            key = { it.id }
        ) { task ->
            SwipeToDeleteContainer(
                modifier = Modifier
                    .padding(
                        horizontal = 8.dp,
                        vertical = 4.dp,
                    )
                    .clip(RoundedCornerShape(15.dp)),
                item = task,
                onDelete = removeTask
            ){
//                task -> Text(
//                    text = task.title,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .background(MaterialTheme.colorScheme.background)
//                        .padding(16.dp)
//                )
                task -> TaskListItem(
//                    viewModel = viewModel,
                    taskState = taskState,
                    onNavigateToTaskEdit = onNavigateToTaskEdit,
                    taskId = task.id,
                    getTask = getTask,
                    removeTask = removeTask,
                )
            }
        }
    }
}