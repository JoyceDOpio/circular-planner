package com.example.circularplanner.utils

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.example.circularplanner.R
import com.example.circularplanner.ui.state.TaskState
import com.example.circularplanner.ui.viewmodel.DataViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppBar(
//    viewModel: DataViewModel,
    modifier: Modifier = Modifier,
    taskState: TaskState,
    onNavigateToTaskEdit: () -> Unit,
    isList: Boolean,
    onLayoutChangeRequested: () -> Unit,
    title: String
) {
    TopAppBar(
        title = { Text(title) },
        actions = {
            IconButton(
                onClick = {
//                    viewModel.setTaskId(null)
                    taskState.taskId = null
                    onNavigateToTaskEdit()
                }
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.add_circle_24dp_5f6368_fill0_wght400_grad0_opsz24),
                    contentDescription = "Add task",
                    modifier = Modifier.fillMaxSize(0.8F)
                )
            }

            IconButton(onClick = {
                onLayoutChangeRequested()
            }) {
                if (!isList) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.list_24dp_5f6368_fill0_wght400_grad0_opsz24),
                        contentDescription = "List view",
                        modifier = Modifier.fillMaxSize(0.8F)
                    )
                }
                else {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.data_usage_24dp_5f6368_fill0_wght400_grad0_opsz24),
                        contentDescription = "Dial view",
                        modifier = Modifier.fillMaxSize(0.8F)
                    )
                }
            }

//            IconButton(onClick = {
//                //TODO: Open calendar
//            }) {
//                Icon(
//                    imageVector = ImageVector.vectorResource(id = R.drawable.calendar_month_24dp_5f6368_fill0_wght400_grad0_opsz24),
//                    contentDescription = "Calendar",
//                    modifier = Modifier.fillMaxSize(0.8F)
//                )
//            }
        },
        colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.primaryContainer),
    )
}