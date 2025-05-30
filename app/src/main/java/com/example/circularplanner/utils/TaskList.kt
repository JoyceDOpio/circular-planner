package com.example.circularplanner.utils

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.circularplanner.data.Task
import java.util.UUID

@Composable
fun TaskList(
//    viewModel: DataViewModel,
    onNavigateToTaskInfo: (String?) -> Unit,
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
                        horizontal = 6.dp,
                        vertical = 3.dp,
                    )
                    .clip(RoundedCornerShape(15.dp)),
                item = task,
                onDelete = removeTask
            ){
                task -> TaskListItem(
//                    viewModel = viewModel,
                    task = task,
                    onNavigateToTaskInfo = onNavigateToTaskInfo,
//                    getTask = getTask,
                    removeTask = removeTask,
                )
            }
        }
    }
}