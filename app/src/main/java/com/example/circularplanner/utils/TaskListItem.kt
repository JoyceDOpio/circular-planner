package com.example.circularplanner.utils

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.circularplanner.data.Task
import com.example.circularplanner.ui.viewmodel.DataViewModel
import java.util.UUID

@Composable
fun TaskListItem(
    modifier: Modifier = Modifier,
    viewModel: DataViewModel,
    onNavigateToTaskEdit: () -> Unit,
    taskId: UUID,
    getTask: (UUID) -> Task?,
//    removeTask: (UUID) -> Unit,
    removeTask: (Task) -> Unit,
){
    val task = getTask(taskId)

    Card(
        modifier = modifier
            .clickable {
                viewModel.setTaskId(taskId)
                onNavigateToTaskEdit()
            }
//            .padding(8.dp)
//            .fillMaxWidth(),
            .fillMaxSize(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface),
    ) {
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(10.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            Column (
//                modifier = Modifier.fillMaxWidth(0.28f)
            ) {
                Text(
                    text = String.format("%d:%02d-%d:%02d", task!!.startTime.hour, task!!.startTime.minute, task!!.endTime.hour, task!!.endTime.minute),
//                    modifier = Modifier.padding(top = 4.dp),
                )

            }

            VerticalDivider(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 8.dp),
                thickness = 2.dp,
            )

            Column (

            ) {
                Text(
                    text = task!!.title,
//                    modifier = Modifier.padding(end = 16.dp),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = task!!.description,
//                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}