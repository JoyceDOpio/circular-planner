package com.example.circularplanner.ui.component

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
import com.example.circularplanner.ui.viewmodel.TaskDisplayUiState

@Composable
fun TaskList(
    uiState: TaskDisplayUiState,
    onNavigateToTaskInfo: (String?) -> Unit,
    removeTask: (Task) -> Unit
) {
    // Sorts tasks according to their start time
    val TaskSortingComparator = Comparator <Task> { first, second ->
        if (first.startTime.hour < second.startTime.hour) {
            -1
        } else if (first.startTime.hour > second.startTime.hour) {
            1
        } else {
            if (first.startTime.minute < second.startTime.minute) {
                -1
            } else if (first.startTime.minute > second.startTime.minute) {
                1
            } else {
                0
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(
            items = uiState.dayDetails.tasks.sortedWith(TaskSortingComparator),
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
                    task = task,
                    onNavigateToTaskInfo = onNavigateToTaskInfo,
                )
            }
        }
    }
}