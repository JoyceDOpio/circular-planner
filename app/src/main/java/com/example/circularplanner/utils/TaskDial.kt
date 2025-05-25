package com.example.circularplanner.utils

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.circularplanner.data.Task
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.state.TaskDialState
import com.example.circularplanner.ui.state.TaskMode
import com.example.circularplanner.ui.state.TaskState
import com.example.circularplanner.ui.state.rememberTaskDialState
import java.time.LocalTime
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

const val DEG_TO_RAD = Math.PI / 180f
const val DEG_OFFSET = -90

enum class AngleMode {
    NONE,
    START,
    END
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TaskDial(
//    viewModel: DataViewModel,
    taskState: TaskState,
    onNavigateToTaskEdit: () -> Unit,
    onNavigateToTaskInfo: () -> Unit,
    tasks: List<Task>,//TODO: a method for retrieving tasks should be provided by a view model
//    removeTask: (UUID) -> Unit//TODO: a method for retrieving tasks should be provided by a view model
//    removeTask: (Task) -> Unit
) {

//    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val textMeasurer = rememberTextMeasurer()
    val taskDialState: TaskDialState = rememberTaskDialState(taskState.activeTimeStart, taskState.activeTimeEnd)

    // Note the start and end angle of where the finger touched the screen to get the supposed new start and end time values of a task
    var startAngle by remember { mutableStateOf(0f) }
    var endAngle by remember { mutableStateOf(0f) }
    var startAngleIsSet by remember { mutableStateOf(false) }
    var endAngleIsSet by remember { mutableStateOf(false) }
    var angleSettingMode by remember { mutableStateOf(AngleMode.START) }
//
//    // New task
//    var taskMode by remember { mutableStateOf(TaskMode.NONE) }
//
    // The width and height of the Canvas
    var width by remember { mutableStateOf(0) }
    var height by remember { mutableStateOf(0) }
    var angle by remember { mutableStateOf(0f) }
    var touchNearTheDialEdge by remember { mutableStateOf(false) }
    var touchInsideTheDial by remember { mutableStateOf(false) }
    var drawNewTaskTimeRange by remember { mutableStateOf(false) }
    var radius by remember { mutableStateOf(0f) }
    var center by remember { mutableStateOf(Offset.Zero) }
    var appliedAngle by remember { mutableStateOf(0f) }
    // Basically the width of the finger touch on the screen
    var touchStroke: Float = 50f
    // The radius of the dial
    var outerRadius by remember { mutableStateOf(0f) }
    // The radius from the center to the clock steps
    var innerRadius by remember { mutableStateOf(0f) }
    // The radius of the clock center (the one that displays time)
    var centerRadius by remember { mutableStateOf(0f) }
//
//    var minuteAngle by remember { mutableStateOf(0f) }
//    var totalMinutes by remember { mutableStateOf(0) }
//
    // Variables to identify whether the dial was touched within an existing task area
    var touchWithinTaskArea by remember { mutableStateOf(true) }
//    var touchedTask by remember { mutableStateOf<Task?>(null) }
//    var changeStartTimeTaskBorder by remember { mutableStateOf(false) }
//    var changeEndTimeTaskBorder by remember { mutableStateOf(false) }
//
//    var settingAngle by remember { mutableStateOf(false) }
//    var settingNewTask by remember { mutableStateOf(true) }
//    var allowedAngleRange by remember { mutableStateOf(Pair(0f,0f)) }
//    var dateTime by remember { mutableStateOf(LocalTime.now()) }
    var clockCenterTime by remember { mutableStateOf(Time(LocalTime.now().hour, LocalTime.now().minute)) }
    // Draw a pointer on the dial to illustrate the finger's movement along the dial edge
    var drawFingerPointer by remember { mutableStateOf(false) }

    fun resetDialParameters() {
        touchNearTheDialEdge = false
        drawNewTaskTimeRange = false
        touchWithinTaskArea = false
//        changeStartTimeTaskBorder = false
//        changeEndTimeTaskBorder = false
//        touchedTask = null
//        settingAngle = false
//        settingNewTask = true
//        action = Action.NONE
        angleSettingMode = AngleMode.NONE
//        taskMode = TaskMode.NONE
    }

    fun resetClockCenter() {
//        drawFingerPointer = false
        clockCenterTime = Time(LocalTime.now().hour, LocalTime.now().minute)
    }

    // Reset the start and end angles of where the finger touched the dial
    fun resetStartAndEndAngles() {
        startAngle = 0f
        endAngle = 0f
        startAngleIsSet = false
        endAngleIsSet = false
    }

    fun reset() {
        resetDialParameters()
        resetClockCenter()
        resetStartAndEndAngles()
    }

    Scaffold (
        content = { padding ->
            Surface(
                modifier = Modifier.padding(padding),
                color = MaterialTheme.colorScheme.background,
            ) {
                Canvas (
                    modifier = Modifier
                        .fillMaxSize()
//                                .fillMaxWidth()
//                                .aspectRatio(1f)
//                                .size(400.dp)
//                                .background(Color.Gray)
                        .onGloballyPositioned {
                            width = it.size.width
                            height = it.size.height
                            center = Offset(width / 2f, height / 2f)
//                                    radius = min(width.toFloat(), height.toFloat()) / 2f - padding - stroke / 2f
//                                    radius = min(width.toFloat(), height.toFloat()) / 2f
                            // The radius of the dial
                            outerRadius = min(width.toFloat(), height.toFloat()) / 2f
                            // The radius from the center to the clock steps
                            innerRadius = outerRadius * .8f
                            // The radius of the clock center (the one that displays time)
                            centerRadius = outerRadius * 0.25f
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { offset ->
                                    val distance = taskDialState.distance(offset, center)
                                    touchInsideTheDial = taskDialState.checkIfTouchInsideDial(
                                        distance,
                                        centerRadius,
                                        innerRadius,
                                        touchStroke
                                    )
                                    angle = taskDialState.angle(center, offset)

                                    if (touchInsideTheDial) {
                                        touchWithinTaskArea = taskDialState.checkIfTouchWithinTaskArea(
                                            angle,
                                            tasks
                                        )

                                        if (touchWithinTaskArea) {
                                            // Show task info
                                            taskState.taskId = taskDialState.touchedTaskId
                                            onNavigateToTaskInfo()
                                        }
                                    } else {
                                        // Cancel everything if touch is outside the dial
                                        reset()
                                    }
                                },
                                onDoubleTap = { offset ->
                                    val distance = taskDialState.distance(offset, center)
                                    touchInsideTheDial = taskDialState.checkIfTouchInsideDial(
                                        distance,
                                        centerRadius,
                                        innerRadius,
                                        touchStroke
                                    )

                                    if (touchInsideTheDial) {
                                        touchWithinTaskArea = taskDialState.checkIfTouchWithinTaskArea(
                                            angle,
                                            tasks
                                        )

                                        if (touchWithinTaskArea) {
                                            // Show task info
                                            taskState.taskId = taskDialState.touchedTaskId
                                            onNavigateToTaskEdit()
                                        }
                                    } else {
                                        // Cancel everything if touch is outside the dial
                                        reset()
                                    }
                                },
                                onLongPress = { offset ->
                                    val distance = taskDialState.distance(offset, center)
                                    touchInsideTheDial = taskDialState.checkIfTouchInsideDial(
                                        distance,
                                        centerRadius,
                                        innerRadius,
                                        touchStroke
                                    )
//                                    touchNearTheDialEdge = checkIfTouchNearDialEdge(distance)

                                    if (touchInsideTheDial) {
                                        touchWithinTaskArea = taskDialState.checkIfTouchWithinTaskArea(
                                            angle,
                                            tasks
                                        )

                                        if (touchWithinTaskArea) {
                                            taskDialState.taskMode = TaskMode.CREATE
                                        }
                                    } else {
                                        // Cancel everything if touch is outside the dial
                                        reset()
                                    }

                                    Log.i("taskState: ", taskState.toString())
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    // Get the starting coordinates and determine if the touch is:
                                    // 1. within the dial
                                    // 2. within any existing task area
                                    val distance = taskDialState.distance(offset, center)
//                                    angle = taskDialState.angle(center, offset, minuteAngle)
                                    angle = taskDialState.angle(center, offset)
//                                    taskDialState.angle = taskDialState.angle(center, offset)

                                    touchNearTheDialEdge = taskDialState.checkIfTouchNearDialEdge(
                                        distance,
                                        innerRadius,
                                        outerRadius,
                                        touchStroke
                                    )

                                    if (touchNearTheDialEdge) {
                                        startAngle = angle

                                        if (taskDialState.taskMode == TaskMode.NONE) {
                                            taskDialState.taskMode  = TaskMode.CREATE
//                                        taskDialState.startAngle = taskDialState.angle
//                                        taskDialState.startAngle = angle
                                        }
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    // If touch is within dial, keep track of the coordinate
                                    val offset = change.position
                                    val distance = taskDialState.distance(offset, center)
                                    touchNearTheDialEdge = taskDialState.checkIfTouchNearDialEdge(
                                        distance,
                                        innerRadius,
                                        outerRadius,
                                        touchStroke
                                    )

                                    if (taskDialState.taskMode == TaskMode.CREATE) {
                                        if (touchNearTheDialEdge) {
//                                            drawNewTaskTimeRange = true
                                            drawNewTaskTimeRange = true
//                                        drawFingerPointer = true
//                                    val currentAngle = angle(center, offset, minuteAngle)
                                            val currentAngle = taskDialState.angle(center, offset)
                                            angle = currentAngle
//                                            angle = currentAngle
//                                            taskDialState.endAngle = currentAngle
//                                            taskDialState.endAngle = angle
                                            endAngle = currentAngle
                                            Log.i("currentAngle", currentAngle.toString())
//                                        Log.i("allowedAngleRange", allowedAngleRange.toString())

//                                    // TODO: Make sure the angle doesn't overlap with an existing task angle
//                                    if (currentAngle > allowedAngleRange.first && currentAngle < allowedAngleRange.second) {
//                                        angle = currentAngle
//                                    }

                                            // TODO: Calculate the time represented by the angle to display it in the clock center


                                            val minute = taskDialState.calculateMinutes(
                                                taskDialState.angleForTimeCalculation(angle),
                                                taskDialState.minuteAngle
                                            )
                                            clockCenterTime =
                                                taskDialState.calculateClockTimeBasedOnMinutesFromStartTime(
                                                    start = taskDialState.activeTimeStart,
                                                    minutes = minute
                                                )
                                        }
                                    }
                                },
                                onDragEnd = {
                                    if (touchNearTheDialEdge) {
                                        // The number of minutes from start active start time
                                        val taskStartTimeMinute = taskDialState.calculateMinutes(
                                            taskDialState.angleForTimeCalculation(startAngle),
                                            taskDialState.minuteAngle
                                        )
                                        // Clock time the number of minutes corresponds to
                                        val clockTaskStartTime =
                                            taskDialState.calculateClockTimeBasedOnMinutesFromStartTime(
                                                taskDialState.activeTimeStart,
                                                taskStartTimeMinute
                                            )
                                        val taskEndTimeMinute = taskDialState.calculateMinutes(
                                            taskDialState.angleForTimeCalculation(endAngle),
                                            taskDialState.minuteAngle
                                        )
                                        val clockTaskEndTime =
                                            taskDialState.calculateClockTimeBasedOnMinutesFromStartTime(
                                                taskDialState.activeTimeStart,
                                                taskEndTimeMinute
                                            )

//                                        viewModel.setTaskStartTime(clockTaskStartTime)
//                                        viewModel.setTaskEndTime(clockTaskEndTime)
                                        taskState.taskStartTime = clockTaskStartTime
                                        taskState.taskEndTime = clockTaskEndTime

                                        onNavigateToTaskEdit()
                                    } else {
                                        reset()
                                    }
                                }
                            )
                        }
                ) {
                    val minutesBetweenHoursAccumulated: Array<Int> = taskDialState.calculateMinutesBetweenHoursAccumulated(
                        taskDialState.activeTimeStart,
                        taskDialState.activeTimeEnd
                    )
//                    minutesBetweenHoursAccumulated.forEach {
//                        Log.i("minutesBetweenHoursAccumulated: ", it.toString())
//                    }
//                    val minutesBetweenHoursAccumulated: Array<Int> = calculateMinuteIntervalsToDraw(activeTimeStart, activeTimeEnd)
//                    val minutesBetweenHoursAccumulated = arrayOf(0, 40, 100, 160, 220, 280, 292)
                    val activeTimeHourSteps: Array<Time> = taskDialState.createClockHoursArray(
                        taskDialState.activeTimeStart,
                        taskDialState.activeTimeEnd
                    )
//                    val activeTimeHourSteps = arrayOf(
//                        Time(6,20),
//                        Time(7,0),
//                        Time(8,0),
//                        Time(9,0),
//                        Time(10,0),
//                        Time(11,0),
//                        Time(11,12)
//                    )
                    outerRadius = min(width, height) / 2f * 0.8f
                    innerRadius = outerRadius * .8f

                    drawHourStepsAndLabels(
                        minutesBetweenHoursAccumulated = minutesBetweenHoursAccumulated,
                        minuteAngle = taskDialState.minuteAngle,
                        innerRadius = innerRadius,
                        outerRadius = outerRadius,
                        activeTimeHourSteps = activeTimeHourSteps,
                        textMeasurer = textMeasurer,
                    )

                    // TODO: Do poprawy
                    drawMinuteSteps(
                        taskDialState.minuteAngle,
                        taskDialState.totalMinutes,
                        minutesBetweenHoursAccumulated,
                        innerRadius,
                        outerRadius
                    )

                    if (drawNewTaskTimeRange) {
                        drawNewTaskArea(
                            startAngle = startAngle,
                            endAngle = endAngle,
                            size = size,
                            outerRadius = outerRadius,
                            sweepAngle = taskDialState.sweepAngle(
                                startAngle,
                                endAngle
                            )
                        )
                    }

                    for (task in tasks) {
                        val startMinute = taskDialState.calculateTotalNumberOfMinutes(
                            taskDialState.activeTimeStart,
                            task.startTime
                        )
                        val endMinute = taskDialState.calculateTotalNumberOfMinutes(
                            taskDialState.activeTimeStart,
                            task.endTime
                        )
                        val taskDuration = taskDialState.calculateTotalNumberOfMinutes(task.startTime, task.endTime)

                        // We have to offset these angles because startMinute * taskDialState.minuteAngle returns a biased angle
                        task.startAngle = taskDialState.offsetAngle(startMinute * taskDialState.minuteAngle)
                        task.endAngle = taskDialState.offsetAngle(endMinute * taskDialState.minuteAngle)
                        task.durationInMinutes = taskDuration

                        drawTask(
                            taskStartAngle = task.startAngle,
                            minuteAngle = taskDialState.minuteAngle,
                            outerRadius = outerRadius,
                            taskDuration = task.durationInMinutes
                        )
                    }

                    if (drawFingerPointer) {
                        drawFingerPointer(
                            outerRadius = outerRadius,
                            touchStroke = touchStroke,
                            angle = angle
                        )
                    }

                    drawClockCenter(
                        textMeasurer = textMeasurer,
                        radius = centerRadius,
                        hourFontSize = 32.sp,
                        minuteFontSize = 16.sp,
                        label = clockCenterTime
                    )
                }


            }
        }
    )
}

fun DrawScope.drawHourStepsAndLabels(
    minutesBetweenHoursAccumulated: Array<Int>,
    minuteAngle: Float,
    innerRadius: Float,
    outerRadius: Float,
    activeTimeHourSteps: Array<Time>,
    textMeasurer: TextMeasurer,
) {
    // Draw hour steps and labels
    for (i in 0..minutesBetweenHoursAccumulated.size - 1) {
        // Draw hour steps
        var stepAngle = minutesBetweenHoursAccumulated[i] * minuteAngle + DEG_OFFSET

        val stepStartOffset = Offset(
            x = center.x + (innerRadius * 1.15 * cos(stepAngle * DEG_TO_RAD)).toFloat(),
            y = center.y + (innerRadius * 1.15 * sin(stepAngle * DEG_TO_RAD)).toFloat()
        )

        val stepEndOffset = Offset(
            x = center.x + (outerRadius * cos(stepAngle * DEG_TO_RAD)).toFloat(),
            y = center.y + (outerRadius * sin(stepAngle * DEG_TO_RAD)).toFloat()
        )

        drawLine(
            Color.LightGray,
            start = stepStartOffset,
            end = stepEndOffset,
            strokeWidth = 2.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Draw hour labels
        val hourStep = activeTimeHourSteps[i]
        val hourLabel = String.format("%d", hourStep.hour)
        val minuteLabel = String.format("%02d", hourStep.minute)
        val hourStepLabel = buildAnnotatedString {
            append(hourLabel)

            // Apply font size and baseline shift for the superscript
            withStyle(style = SpanStyle(
                fontSize = 9.sp,
                baselineShift = BaselineShift.Superscript
            )) {
                append(minuteLabel)
            }
        }

        val hourStepLabelTextLayout = textMeasurer.measure(
            text = hourStepLabel,
            style = TextStyle()
        )

//        val stepsLabelOffset = Offset(
//            x = center.x + (radius - stepsHeight - dialStyle.stepsLabelTopPadding.toPx()) * cos(
//                (stepsAngle + rotation) * (Math.PI / 180)
//            ).toFloat(),
//            y = center.y - (radius - stepsHeight - dialStyle.stepsLabelTopPadding.toPx()) * sin(
//                (stepsAngle + rotation) * (Math.PI / 180)
//            ).toFloat()
//        )
        var stepsHeight = 10.dp.toPx()
//        var stepsHeight = 0.dp.toPx()
        val stepsLabelTopPadding = 12.dp
//        val stepsLabelTopPadding = 0.dp
        // The last label is to be placed above the first
        val labelYOffset = if (i == minutesBetweenHoursAccumulated.size - 1) 25.dp.toPx() else 0.dp.toPx()
        stepAngle = minutesBetweenHoursAccumulated[i] * minuteAngle + DEG_OFFSET
        val stepsLabelOffset = Offset(
            x = center.x + ((outerRadius + stepsLabelTopPadding.toPx() + stepsHeight) * cos(stepAngle * DEG_TO_RAD)).toFloat(),
            y = center.y + ((outerRadius + stepsLabelTopPadding.toPx() + stepsHeight + labelYOffset) * sin(stepAngle * DEG_TO_RAD)).toFloat()
        )

        //subtract the label width and height to position label at the center of the step
        val stepsLabelTopLeft = Offset(
            stepsLabelOffset.x - ((hourStepLabelTextLayout.size.width) / 3f),
            stepsLabelOffset.y - (hourStepLabelTextLayout.size.height / 2f)
        )

        val labelCircleOffset = Offset(
            x = center.x + (outerRadius * cos(stepAngle * DEG_TO_RAD)).toFloat(),
            y = center.y + (outerRadius * sin(stepAngle * DEG_TO_RAD)).toFloat()
        )

//        drawCircle(
//            color = Color.LightGray,
//            center = labelCircleOffset,
//            radius = 16.dp.toPx(),
////            style = Stroke(
////                width = 2f
////            )
//        )

        drawText(
            textMeasurer = textMeasurer,
            text = hourStepLabel,
            topLeft = stepsLabelTopLeft,
//                style = dialStyle.stepsTextStyle
            style = TextStyle()
        )
    }
}

fun DrawScope.drawClockCenter(
    textMeasurer: TextMeasurer,
    radius: Float,
    hourFontSize: TextUnit = 26.sp,
    minuteFontSize: TextUnit = 14.sp,
    label: Time
) {
    // Draw clock center
    drawCircle(
        color = Color.White,
        center = center,
        radius = radius,
    )

    var hourLabel = (label.hour).toString()
    var minuteLabel = String.format("%02d", label.minute)

    val clockHourLabel = buildAnnotatedString {
        withStyle(style = SpanStyle(
            fontSize = hourFontSize,
            fontWeight = FontWeight.Bold
        )) {
            append(hourLabel)
        }

        // Apply font size and baseline shift for the superscript
        withStyle(style = SpanStyle(
            fontSize = minuteFontSize,
            fontWeight = FontWeight.Bold,
            baselineShift = BaselineShift.Superscript
        )) {
            append(minuteLabel)
        }
    }

    val hourStepLabelTextLayout = textMeasurer.measure(
        text = clockHourLabel,
        style = TextStyle()
    )

    var clockCenterLabelOffset = Offset(
        center.x - hourStepLabelTextLayout.size.width / 3f,
        center.y - hourStepLabelTextLayout.size.height / 2f
    )

//    var affirmation = "I can do it"
    // Draw clock center label
    drawText(
        textMeasurer = textMeasurer,
        text = clockHourLabel,
        topLeft = clockCenterLabelOffset,
//                style = dialStyle.stepsTextStyle
        style = TextStyle()
    )
}

fun DrawScope.drawFingerPointer(
    outerRadius: Float,
    touchStroke: Float,
    angle: Float
) {
//    var stepAngle = angle + DEG_OFFSET
    var stepAngle = angle

    val stepStartOffset = Offset(
        x = center.x,
        y = center.y
    )

    val stepEndOffset = Offset(
        x = center.x + (outerRadius * cos(stepAngle * DEG_TO_RAD)).toFloat(),
        y = center.y + (outerRadius * sin(stepAngle * DEG_TO_RAD)).toFloat()
    )

    drawLine(
        Color.DarkGray,
        start = stepStartOffset,
        end = stepEndOffset,
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Draw clock center
    drawCircle(
        color = Color.DarkGray,
//        center = center,
        center = stepEndOffset,
        radius = touchStroke,
    )
}

fun DrawScope.drawMinuteSteps(
    minuteAngle: Float,
    totalMinutes: Int,
    minutesBetweenHoursAccumulated: Array<Int>,
    innerRadius: Float,
    outerRadius: Float
)
{
    // Draw minute (1-, 5-, 10-, 15- or 30-minute) steps
    var minuteStep: Int
    if (minuteAngle >= 5) {
        minuteStep = 1
    } else if (1 <= minuteAngle && minuteAngle < 5) {
        minuteStep = 5
    } else if (0.5 <= minuteAngle && minuteAngle < 1) {
        minuteStep = 15
    } else if (0.25 <= minuteAngle && minuteAngle < 0.5) {
        minuteStep = 15
    } else {
        minuteStep = 30
    }

    for (i in 0..totalMinutes) {
        if (i % minuteStep == 0 && !(i in minutesBetweenHoursAccumulated)) {
            var stepAngle = i * minuteAngle + DEG_OFFSET

            val stepStartOffset = Offset(
                x = center.x + ((innerRadius * 1.15) * cos(stepAngle * DEG_TO_RAD)).toFloat(),
                y = center.y + ((innerRadius * 1.15) * sin(stepAngle * DEG_TO_RAD)).toFloat()
            )

            val stepEndOffset = Offset(
                x = center.x + (outerRadius * cos(stepAngle * DEG_TO_RAD)).toFloat(),
                y = center.y + (outerRadius * sin(stepAngle * DEG_TO_RAD)).toFloat()
            )

            drawLine(
                Color.LightGray,
                start = stepStartOffset,
                end = stepEndOffset,
                strokeWidth = 1.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

fun DrawScope.drawNewTaskArea(
    startAngle: Float,
    endAngle: Float,
//    width: Float,
//    height: Float,
    size: Size,
    outerRadius: Float,
    sweepAngle: Float
) {
    drawArc(
        Color.Red,
        startAngle = startAngle,
        sweepAngle = sweepAngle,
        useCenter = true,
        Offset(size.width / 2 - outerRadius, size.height / 2 - outerRadius),
        size = Size(outerRadius * 2, outerRadius * 2),
        alpha = 0.1f
    )
}

fun DrawScope.drawTask(
    taskStartAngle: Float,
    minuteAngle: Float,
    outerRadius: Float,
    taskDuration: Int
) {
    drawArc(
        Color.Cyan,
        startAngle = taskStartAngle,
        sweepAngle = (taskDuration * minuteAngle).toFloat(),
        useCenter = true,
        Offset(size.width / 2 - outerRadius, size.height / 2 - outerRadius),
        size = Size(outerRadius * 2, outerRadius * 2),
        alpha = 0.1f
    )
}


