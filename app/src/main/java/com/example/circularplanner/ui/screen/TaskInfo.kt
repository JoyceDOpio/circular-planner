package com.example.circularplanner.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.circularplanner.R
import com.example.circularplanner.data.Task
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.common.TimePickerDialog
import com.example.circularplanner.ui.state.TaskState
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
object TaskInfo

@Composable
fun TaskInfoScreen(
    modifier: Modifier = Modifier,
//    viewModel: DataViewModel,
    taskState: TaskState,
    onCancel: () -> Unit,
    onNavigateToTaskEdit: () -> Unit,
    getTask: (UUID) -> Task?,
) {
    val taskId = taskState.taskId//TODO: taskId MUSI być przekazywany jako parametr nawigacji
    var task: Task? = null

    if (taskId != null) {
        task = getTask(taskId)
//            taskState.taskId = null//TODO: taskId musi być resetowany, ale zmiana stanu state w tym miejscu przerywa działanie aplikacji
    } else {}

    Column (
        modifier = modifier
            .padding(10.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row (
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Edit button
            IconButton(onClick = {
                onNavigateToTaskEdit()
            }) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.edit_24dp_5f6368_fill0_wght400_grad0_opsz24),
                    contentDescription = "Open time picker",
                    modifier = Modifier.fillMaxSize(0.8F)
                )
            }

            // Close button
            IconButton(onClick = {
                // Navigate to previous stack entry
                onCancel()
            }) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.cancel_24dp_5f6368_fill0_wght400_grad0_opsz24),
                    contentDescription = "Open time picker",
                    modifier = Modifier.fillMaxSize(0.8F)
                )
            }
        }

        Row (
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = task?.title!!,
                modifier = modifier.weight(2f),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        Row (
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time scope and time duration //TODO: Add time duration
            Text(text = String.format("%d:%02d - %d:%02d", taskState.taskStartTime.hour, taskState.taskStartTime.minute, taskState.taskEndTime.hour, taskState.taskEndTime.minute), fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        Row (
            modifier = modifier
                .fillMaxWidth(),
//                .padding(vertical = 5.dp)
//                .clip(RoundedCornerShape(5.dp))
//                .border(width = 2.dp, color = Color.LightGray),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Description
            Text(
                text = task?.description!!,
                fontWeight = FontWeight.Normal
            )
        }
    }
}