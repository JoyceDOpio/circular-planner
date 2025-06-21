package com.example.circularplanner.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.circularplanner.R
import com.example.circularplanner.ui.viewmodel.TaskDisplayUiState
import com.example.circularplanner.utils.TaskDialUtils

@Composable
fun TaskInfoScreen(
    modifier: Modifier = Modifier,
    uiState: TaskDisplayUiState,
    onCancel: () -> Unit,
    onNavigateToTaskEdit: (String?) -> Unit,
    reset: () -> Unit
) {
    val taskDetails = uiState.taskDetails

    Row (
        modifier = modifier
            .padding(vertical = 10.dp)
            .fillMaxSize(),
        verticalAlignment = Alignment.Top
    ) {
        Column (
            modifier = modifier
                .padding(horizontal = 10.dp)
                .fillMaxWidth(0.85f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Row (
                modifier = modifier
                    .padding(top = 10.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = taskDetails.title!!,
                    modifier = modifier.weight(2f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Column (
                modifier = modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                if (taskDetails != null) {
                    // Time range
                    val timeRangeText = String.format("%d:%02d - %d:%02d", taskDetails?.startTime!!.hour ?:0, taskDetails?.startTime!!.minute, taskDetails?.endTime!!.hour, taskDetails?.endTime!!.minute)
                    Text(
                        text = timeRangeText,
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp
                    )

                    // Time duration
                    val totalMinutes = TaskDialUtils.calculateTotalNumberOfMinutes(taskDetails.startTime, taskDetails.endTime)
                    val hours = totalMinutes / TaskDialUtils.MINUTES_IN_HOUR
                    val minutes = totalMinutes % TaskDialUtils.MINUTES_IN_HOUR
                    val timeDurationText = if (hours == 0) {
                        String.format("%01d min", minutes)
                    } else if (minutes == 0) {
                        if (hours > 1) {
                            String.format("%01d hours", hours)
                        } else {
                            String.format("%01d hour", hours)
                        }
                    } else {
                        if (hours > 1) {
                            String.format("%01d hours %01d min", hours, minutes)
                        } else {
                            String.format("%01d hour %01d min", hours, minutes)
                        }
                    }
                    Text(
                        text = timeDurationText,
                        fontWeight = FontWeight.Thin,
                        fontSize = 18.sp
                    )
                }
            }

            Row (
                modifier = modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Description
                Text(
                    text = taskDetails.description,
                    modifier = modifier.verticalScroll(rememberScrollState()),
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Justify,
                )
            }
        }

        VerticalDivider(
            modifier = Modifier
                .fillMaxHeight(),
            thickness = 2.dp,
        )

        Column (
            modifier = modifier
                .fillMaxWidth(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Close button
            IconButton(onClick = {
                // Navigate to previous stack entry
                reset()
                onCancel()
            }) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.cancel_24dp_5f6368_fill0_wght400_grad0_opsz24),
                    contentDescription = "Open time picker",
                    modifier = Modifier.fillMaxSize(0.8F)
                )
            }

            // Edit button
            IconButton(onClick = {
                onNavigateToTaskEdit(taskDetails.id.toString())
            }) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.edit_24dp_5f6368_fill0_wght400_grad0_opsz24),
                    contentDescription = "Open time picker",
                    modifier = Modifier.fillMaxSize(0.8F)
                )
            }
        }
    }
}