package com.example.circularplanner.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.circularplanner.data.Task
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.viewmodel.TaskDisplayUiState
import com.example.circularplanner.utils.AngleMode
import com.example.circularplanner.utils.TaskDialUtils
import com.example.circularplanner.utils.TaskDialUtils.DEG_OFFSET
import com.example.circularplanner.utils.TaskDialUtils.DEG_TO_RAD
import com.example.circularplanner.utils.TaskMode
import java.time.LocalTime
import java.util.UUID
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

const val TOUCH_STROKE = 50f

@Composable
fun TaskDial(
    uiState: TaskDisplayUiState,
    onNavigateToTaskEdit: (String?) -> Unit,
    onNavigateToTaskInfo: (String?) -> Unit,
    onPressActiveTime: () -> Unit,
    onSetTaskEndTime: (Time) -> Unit,
    onSetTaskStartTime: (Time) -> Unit,
    saveTask: () -> Unit,
    selectTask: (UUID?) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()
    val activeTimeStart: Time = uiState.dayDetails.activeTimeStart
    val activeTimeEnd: Time = uiState.dayDetails.activeTimeEnd
    var taskMode: TaskMode by remember { mutableStateOf(TaskMode.CREATE) }
    var angleMode: AngleMode by remember { mutableStateOf(AngleMode.NONE) }
    val activeTimeColor = MaterialTheme.colorScheme.primary

    val totalMinutes: Int = TaskDialUtils.calculateTotalNumberOfMinutes(
        Time(
            activeTimeStart.hour,
            activeTimeStart.minute
        ),
        Time(
            activeTimeEnd.hour,
            activeTimeEnd.minute
        )
    )
    val minuteAngle: Float = 360 / totalMinutes.toFloat()

    // Note the start and end angle of where the finger touched the screen to get the supposed new start and end time values of a task:
    // - values used to trace the finger movement
    var startAngle by remember { mutableStateOf(0f) }
    var endAngle by remember { mutableStateOf(0f) }
    // - values used to draw the angles
    var tmpStartAngle by remember { mutableStateOf(0f) }
    var tmpEndAngle by remember { mutableStateOf(0f) }

    // The width and height of the Canvas
    var width by remember { mutableStateOf(0) }
    var height by remember { mutableStateOf(0) }
    var angle by remember { mutableStateOf(0f) }
    var touchNearTheDialEdge by remember { mutableStateOf(false) }
    var touchInsideTheDial by remember { mutableStateOf(false) }
    var drawNewTaskTimeRange by remember { mutableStateOf(false) }
    var center by remember { mutableStateOf(Offset.Zero) }

    // Basically the width of the finger touch on the screen
    var touchStroke: Float = TOUCH_STROKE

    // The radius of the dial
    var outerRadius by remember { mutableStateOf(0f) }
    // The radius from the center to the clock steps
    var innerRadius by remember { mutableStateOf(0f) }
    // The radius of the clock center (the one that displays time)
    var centerRadius by remember { mutableStateOf(0f) }

    // Variables to identify whether the dial was touched within an existing task area
    var touchWithinTaskArea by remember { mutableStateOf(true) }
    var touchedTaskId by remember { mutableStateOf<UUID?>(null) }
    var touchedTask = uiState.taskDetails

    var clockCenterTime by remember { mutableStateOf(Time(LocalTime.now().hour, LocalTime.now().minute)) }
    // Draw a pointer on the dial to illustrate the finger's movement along the dial edge
    var drawFingerPointer by remember { mutableStateOf(false) }

    val tasks = uiState.dayDetails.tasks

    var touchWithinActiveTime by remember { mutableStateOf(false) }

    fun checkIfTouchWithinTaskArea(angle: Float, tasks: List<Task>): Boolean {
        // The task stores the appropriate angle values, i.e. values corresponding to how the circle is drawn (the 0 degree starts at the right-hand side (east) of the circle). We want to 'correct' these angles as if 0 degree starts at the top of the circle (north)
        var isTouchWithinAnyTask: Boolean = false

        for (task in tasks) {
            val taskStartAngle = TaskDialUtils.calculateTaskAngle (
                activeTimeStart,
                task.startTime,
                minuteAngle
            )
            val taskEndAngle = TaskDialUtils.calculateTaskAngle (
                activeTimeStart,
                task.endTime,
                minuteAngle
            )
            isTouchWithinAnyTask =
                TaskDialUtils.checkIfTouchWithinAngleRange(angle, taskStartAngle, taskEndAngle)

            if (isTouchWithinAnyTask) {
                touchedTaskId = task.id

                return isTouchWithinAnyTask
            }
        }

        return isTouchWithinAnyTask
    }

    fun resetDialParameters() {
        touchNearTheDialEdge = false
        drawNewTaskTimeRange = false
        touchWithinTaskArea = false
        touchedTaskId = null
    }

    fun resetClockCenter() {
//        drawFingerPointer = false
        clockCenterTime = Time(LocalTime.now().hour, LocalTime.now().minute)
    }

    // Reset the start and end angles of where the finger touched the dial
    fun resetStartAndEndAngles() {
        startAngle = 0f
        endAngle = 0f
        tmpStartAngle = 0f
        tmpEndAngle = 0f
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
                        .onGloballyPositioned {
                            width = it.size.width
                            height = it.size.height
                            center = Offset(width / 2f, height / 2f)
                            // The radius of the dial
                            outerRadius = min(width.toFloat(), height.toFloat()) / 2f
                            // The radius from the center to the clock steps
                            innerRadius = outerRadius * .8f
                            // The radius of the clock center (the one that displays time)
                            centerRadius = outerRadius * 0.25f
                        }
                        .pointerInput(Unit) {
                            // Defining time range: TASK CREATION
                            detectDragGesturesAfterLongPress(
                                onDragStart = { offset ->
                                    // Get the starting coordinates and determine if the touch is:
                                    // 1. within the dial
                                    // 2. within any existing task area
                                    val distance = TaskDialUtils.distance(offset, center)
                                    angle = TaskDialUtils.angle(center, offset)

                                    if (taskMode == TaskMode.CREATE) {
                                        touchNearTheDialEdge =
                                            TaskDialUtils.checkIfTouchNearDialEdge(
                                                distance,
                                                innerRadius,
                                                outerRadius,
                                                touchStroke
                                            )

                                        if (touchNearTheDialEdge) {
                                            startAngle = angle
                                            tmpStartAngle = startAngle
                                        }
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    if (taskMode == TaskMode.CREATE) {
                                        // If touch is within dial, keep track of the coordinate
                                        val offset = change.position
                                        val distance = TaskDialUtils.distance(offset, center)
                                        touchNearTheDialEdge =
                                            TaskDialUtils.checkIfTouchNearDialEdge(
                                                distance,
                                                innerRadius,
                                                outerRadius,
                                                touchStroke
                                            )

                                        if (touchNearTheDialEdge) {
                                            drawNewTaskTimeRange = true
//                                        drawFingerPointer = true
                                            val currentAngle = TaskDialUtils.angle(center, offset)
                                            angle = currentAngle
                                            endAngle = currentAngle
                                            tmpEndAngle = endAngle

                                            // Calculate the time represented by the angle to display it in the clock center
                                            val minute = TaskDialUtils.calculateMinutes(
                                                TaskDialUtils.angleForTimeCalculation(angle),
                                                minuteAngle
                                            )
                                            clockCenterTime =
                                                TaskDialUtils.calculateClockTimeBasedOnMinutesFromStartTime(
                                                    start = activeTimeStart,
                                                    minutes = minute
                                                )
                                        }
                                    }
                                },
                                onDragEnd = {
                                    if (taskMode == TaskMode.CREATE) {
                                        if (touchNearTheDialEdge) {
                                            // The number of minutes from start active start time
                                            val taskStartTimeMinute =
                                                TaskDialUtils.calculateMinutes(
                                                    TaskDialUtils.angleForTimeCalculation(startAngle),
                                                    minuteAngle
                                                )
                                            // Clock time the number of minutes corresponds to
                                            val clockTaskStartTime =
                                                TaskDialUtils.calculateClockTimeBasedOnMinutesFromStartTime(
                                                    activeTimeStart,
                                                    taskStartTimeMinute
                                                )
                                            val taskEndTimeMinute = TaskDialUtils.calculateMinutes(
                                                TaskDialUtils.angleForTimeCalculation(endAngle),
                                                minuteAngle
                                            )
                                            val clockTaskEndTime =
                                                TaskDialUtils.calculateClockTimeBasedOnMinutesFromStartTime(
                                                    activeTimeStart,
                                                    taskEndTimeMinute
                                                )

                                            onSetTaskStartTime(clockTaskStartTime)
                                            onSetTaskEndTime(clockTaskEndTime)

                                            onNavigateToTaskEdit(
                                                null
                                            )
                                        }
                                    } else {
                                        reset()
                                    }
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { offset ->
                                    val distance = TaskDialUtils.distance(offset, center)
                                    touchInsideTheDial = TaskDialUtils.checkIfTouchInsideDial(
                                        distance,
                                        centerRadius,
                                        innerRadius,
                                        touchStroke
                                    )
                                    angle = TaskDialUtils.angle(center, offset)

                                    if (touchInsideTheDial) {
                                        if (taskMode == TaskMode.CREATE) {
                                            // Check whether the touch is within the ANY task area
                                            touchWithinTaskArea =
                                                checkIfTouchWithinTaskArea(
                                                    angle,
                                                    tasks
                                                )

                                            if (touchWithinTaskArea) {
                                                selectTask(touchedTaskId)
                                                // Show task info
                                                onNavigateToTaskInfo(touchedTaskId.toString())
                                            }
                                        } else if (taskMode == TaskMode.EDIT) {
                                            // Check whether the touch is within the GIVEN task area
                                            touchWithinTaskArea =
                                                TaskDialUtils.checkIfTouchWithinAngleRange(
                                                    angle,
                                                    tmpStartAngle,
                                                    tmpEndAngle
                                                )

                                            if (touchWithinTaskArea) {
                                                selectTask(touchedTaskId)
                                                // Start angle carries now the supposedly new value for start time of the given task, the end angle the value for the end time, respectively. Update the task:
                                                // - start time
                                                val startMinute = TaskDialUtils.calculateMinutes(
                                                    TaskDialUtils.angleForTimeCalculation(
                                                        tmpStartAngle
                                                    ),
                                                    minuteAngle
                                                )
                                                val clockTaskStartTime =
                                                    TaskDialUtils.calculateClockTimeBasedOnMinutesFromStartTime(
                                                        start = activeTimeStart,
                                                        minutes = startMinute
                                                    )
                                                // - end time
                                                val endMinute = TaskDialUtils.calculateMinutes(
                                                    TaskDialUtils.angleForTimeCalculation(
                                                        tmpEndAngle
                                                    ),
                                                    minuteAngle
                                                )
                                                val clockTaskEndTime =
                                                    TaskDialUtils.calculateClockTimeBasedOnMinutesFromStartTime(
                                                        start = activeTimeStart,
                                                        minutes = endMinute
                                                    )

                                                onSetTaskStartTime(clockTaskStartTime)
                                                onSetTaskEndTime(clockTaskEndTime)
                                                saveTask()

                                                // Switch the task mode to CREATE and reset
                                                taskMode = TaskMode.CREATE
                                                reset()
                                            }
                                        }
                                    } else {
                                        // Cancel everything if touch is outside the dial
                                        taskMode = TaskMode.CREATE
                                        reset()

                                        // Check if the touch was within the active time
                                        var stepAngle = 0 * minuteAngle + DEG_OFFSET
                                        val minutesBetweenHoursAccumulated: Array<Int> =
                                            TaskDialUtils.calculateMinutesBetweenHoursAccumulated(
                                                activeTimeStart,
                                                activeTimeEnd
                                            )
                                        val activeTimeHourSteps: Array<Time> = TaskDialUtils.createClockHoursArray(
                                            activeTimeStart,
                                            activeTimeEnd
                                        )
                                        var hourStep = activeTimeHourSteps[0]
                                        // Let only the first and last label display the hour and minute values - other labels will display only the hour value
                                        val hourStepLabel = buildAnnotatedString {
                                            append(String.format("%d:%02d", hourStep.hour, hourStep.minute))
                                            appendLine()
                                            hourStep = activeTimeHourSteps[minutesBetweenHoursAccumulated.size - 1]
                                            append(String.format("%d:%02d", hourStep.hour, hourStep.minute))
                                        }
                                        val hourStepLabelTextLayout = textMeasurer.measure(
                                            text = hourStepLabel,
                                            style = TextStyle(textAlign = TextAlign.Center)
                                        )

                                        var circleCenterOffset = Offset(
                                            x = center.x + (outerRadius * cos(stepAngle * DEG_TO_RAD)).toFloat(),
                                            y = center.y + (outerRadius * sin(stepAngle * DEG_TO_RAD)).toFloat()
                                        )

                                        circleCenterOffset = Offset(
                                            x = circleCenterOffset.x,
                                            y = circleCenterOffset.y - (hourStepLabelTextLayout.size.height * 1.2f)
                                        )

                                        touchWithinActiveTime = TaskDialUtils.checkIfTouchWithinActiveTime(
                                            offset = offset,
                                            activeTimeCenter = circleCenterOffset,
                                            activeTimeRadius = hourStepLabelTextLayout.size.width * 0.8f,
                                            touchStroke = touchStroke
                                        )

                                        if (touchWithinActiveTime) {
                                            // Show active time setup
                                            onPressActiveTime()
                                        }
                                    }
                                },
                                onDoubleTap = { offset ->
                                    val distance = TaskDialUtils.distance(offset, center)
                                    touchInsideTheDial = TaskDialUtils.checkIfTouchInsideDial(
                                        distance,
                                        centerRadius,
                                        innerRadius,
                                        touchStroke
                                    )
                                    angle = TaskDialUtils.angle(center, offset)

                                    if (touchInsideTheDial) {
                                        // Check if touch is within ANY task area
                                        touchWithinTaskArea =
                                            checkIfTouchWithinTaskArea(
                                                angle,
                                                tasks
                                            )

                                        if (touchWithinTaskArea) {
                                            taskMode = TaskMode.EDIT
                                            selectTask(touchedTaskId)

                                            val taskStartAngle = TaskDialUtils.calculateTaskAngle(
                                                activeTimeStart,
                                                touchedTask.startTime,
                                                minuteAngle
                                            )
                                            val taskEndAngle = TaskDialUtils.calculateTaskAngle(
                                                activeTimeStart,
                                                touchedTask.endTime,
                                                minuteAngle
                                            )
                                            tmpStartAngle = taskStartAngle
                                            tmpEndAngle = taskEndAngle

                                            // Draw this task separately than other tasks
                                            drawNewTaskTimeRange = true
                                        }
                                    } else {
                                        // Cancel everything if touch is outside the dial
                                        reset()
                                    }
                                },
                            )
                        }
                        .pointerInput(Unit) {
                            // Defining time range: TASK EDITING
                            detectDragGestures(
                                onDragStart = { offset ->
                                    // Get the starting coordinates and determine if the touch is:
                                    // 1. within the dial
                                    // 2. within any existing task area
                                    val distance = TaskDialUtils.distance(offset, center)
                                    touchNearTheDialEdge = TaskDialUtils.checkIfTouchNearDialEdge(
                                        distance,
                                        innerRadius,
                                        outerRadius,
                                        touchStroke
                                    )

                                    if (touchNearTheDialEdge) {
                                        val currentAngle = TaskDialUtils.angle(center, offset)
                                        angle = currentAngle
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    // Engage only if in task EDITING mode
                                    if (taskMode == TaskMode.EDIT) {
                                        // If touch is within dial, keep track of the coordinate
                                        val offset = change.position
                                        val distance = TaskDialUtils.distance(offset, center)

                                        touchNearTheDialEdge =
                                            TaskDialUtils.checkIfTouchNearDialEdge(
                                                distance,
                                                innerRadius,
                                                outerRadius,
                                                touchStroke
                                            )

                                        if (touchNearTheDialEdge) {
                                            val currentAngle = TaskDialUtils.angle(center, offset)
                                            angle = currentAngle

                                            // If we don't know which time boundary of the given task we are setting
                                            if (angleMode == AngleMode.NONE) {
                                                // Determine which time boundary of the given task we are setting: start or end time
                                                // It seems that an angle == tmpStartAngle comparison does not cut it - the app is not able to detect the moment when the two angles are equal
                                                if (angle in (tmpStartAngle - touchStroke/2f)..(tmpStartAngle + touchStroke/2f)) {
                                                    angleMode = AngleMode.START
                                                } else if (angle in (tmpEndAngle - touchStroke/2f)..(tmpEndAngle + touchStroke/2f)) {
                                                    angleMode = AngleMode.END
                                                }
                                            }
                                            // If we do know which time boundary of the given task we are setting
                                            else {
                                                // If we are setting the start time of the given task
                                                if (angleMode == AngleMode.START) {
                                                    startAngle = angle
                                                    tmpStartAngle = startAngle
                                                }
                                                // If we are setting the end time of the given task
                                                else if (angleMode == AngleMode.END) {
                                                    endAngle = angle
                                                    tmpEndAngle = endAngle
                                                }

                                                // Show the clock time the angle corresponds to
                                                val minute = TaskDialUtils.calculateMinutes(
                                                    TaskDialUtils.angleForTimeCalculation(angle),
                                                    minuteAngle
                                                )
                                                clockCenterTime =
                                                    TaskDialUtils.calculateClockTimeBasedOnMinutesFromStartTime(
                                                        start = activeTimeStart,
                                                        minutes = minute
                                                    )
                                            }
                                        }
                                    }

                                },
                                onDragEnd = {
                                    if (touchNearTheDialEdge) {
                                        // Reset the angle mode
                                        angleMode = AngleMode.NONE
                                        // Set new startAngle and endAngle values
                                        startAngle = tmpStartAngle
                                        endAngle = tmpEndAngle
                                    } else {
                                        reset()
                                    }
                                }
                            )
                        }
                ) {
                    val minutesBetweenHoursAccumulated: Array<Int> =
                        TaskDialUtils.calculateMinutesBetweenHoursAccumulated(
                            activeTimeStart,
                            activeTimeEnd
                        )
                    val activeTimeHourSteps: Array<Time> = TaskDialUtils.createClockHoursArray(
                        activeTimeStart,
                        activeTimeEnd
                    )
                    outerRadius = min(width, height) / 2f * 0.8f
                    innerRadius = outerRadius * .8f

                    drawHourStepsAndLabels(
                        activeTimeColor = activeTimeColor,
                        minutesBetweenHoursAccumulated = minutesBetweenHoursAccumulated,
                        minuteAngle = minuteAngle,
                        outerRadius = outerRadius,
                        activeTimeHourSteps = activeTimeHourSteps,
                        textMeasurer = textMeasurer,
                    )

                    // TODO: Do poprawy
                    drawMinuteSteps(
                        minuteAngle,
                        totalMinutes,
                        minutesBetweenHoursAccumulated,
                        innerRadius,
                        outerRadius
                    )

                    if (drawNewTaskTimeRange) {

                        drawNewTaskArea(
                            startAngle = tmpStartAngle,
                            size = size,
                            outerRadius = outerRadius,
                            sweepAngle = TaskDialUtils.sweepAngle(
                                tmpStartAngle,
                                tmpEndAngle
                            ),
                            color = Color.Red
                        )
                    }

                    for (task in tasks) {
                        // We have to offset these angles because startMinute * taskDialState.minuteAngle returns a biased angle
                        val taskStartAngle = TaskDialUtils.calculateTaskAngle (
                            activeTimeStart,
                            task.startTime,
                            minuteAngle
                        )
                        val taskDuration = TaskDialUtils.calculateTotalNumberOfMinutes(
                            task.startTime,
                            task.endTime
                        )

                        // Don't draw the task whose time bounds are being edited
                        if (!(task.id == touchedTask?.id && taskMode == TaskMode.EDIT)) {
                            drawTask(
                                taskStartAngle = taskStartAngle,
                                minuteAngle = minuteAngle,
                                outerRadius = outerRadius,
                                taskDuration = taskDuration,
                                color = Color.Cyan
                            )
                        }
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
                        fontSize = 32.sp,
                        label = clockCenterTime
                    )
                }
            }
        }
    )
}

fun DrawScope.drawClockCenter(
    textMeasurer: TextMeasurer,
    radius: Float,
    fontSize: TextUnit = 26.sp,
    label: Time
) {
    // Draw clock center
    drawCircle(
        color = Color.White,
        center = center,
        radius = radius,
    )

    val clockHourLabel = buildAnnotatedString {
        withStyle(style = SpanStyle(
            fontSize = fontSize,
            fontWeight = FontWeight.Bold
        )) {
            append(String.format("%d:%02d", label.hour, label.minute))
        }
    }

    val hourStepLabelTextLayout = textMeasurer.measure(
        text = clockHourLabel,
        style = TextStyle()
    )

    var clockCenterLabelOffset = Offset(
        center.x - hourStepLabelTextLayout.size.width / 2f,
        center.y - hourStepLabelTextLayout.size.height / 2f
    )

    // Draw clock center label
    drawText(
        textMeasurer = textMeasurer,
        text = clockHourLabel,
        topLeft = clockCenterLabelOffset,
        style = TextStyle()
    )
}

fun DrawScope.drawFingerPointer(
    outerRadius: Float,
    touchStroke: Float,
    angle: Float
) {
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
        center = stepEndOffset,
        radius = touchStroke,
    )
}

fun DrawScope.drawHourStepsAndLabels(
    activeTimeColor: Color,
    minutesBetweenHoursAccumulated: Array<Int>,
    minuteAngle: Float,
    outerRadius: Float,
    activeTimeHourSteps: Array<Time>,
    textMeasurer: TextMeasurer,
) {
    val textStyle = TextStyle(
        textAlign = TextAlign.Center
    )
    val activeTimeTextStyle = textStyle.copy(
        color = Color.White
    )

    // Draw hour steps and labels
    for (i in 0..(minutesBetweenHoursAccumulated.size - 2)) {
        // Draw hour steps
        var stepAngle = minutesBetweenHoursAccumulated[i] * minuteAngle + DEG_OFFSET

        // Draw hour labels
        var hourStep = activeTimeHourSteps[i]
        // Let only the first and last label display the hour and minute values - other labels will display only the hour value
        val hourStepLabel = buildAnnotatedString {
            if (i == 0) {
                append(String.format("%d:%02d", hourStep.hour, hourStep.minute))
                appendLine()
                hourStep = activeTimeHourSteps[minutesBetweenHoursAccumulated.size - 1]
                append(String.format("%d:%02d", hourStep.hour, hourStep.minute))
            } else {
                append(String.format("%d", hourStep.hour))
            }
        }
        val hourStepLabelTextLayout = textMeasurer.measure(
            text = hourStepLabel,
            style = textStyle
        )

        // Calculate the angle of the step
        stepAngle = minutesBetweenHoursAccumulated[i] * minuteAngle + DEG_OFFSET
        var stepLabelOffset = Offset(
            x = center.x + (outerRadius * cos(stepAngle * DEG_TO_RAD)).toFloat(),
            y = center.y + (outerRadius * sin(stepAngle * DEG_TO_RAD)).toFloat()
        )
        // Subtract the label width and height to position label at the center of the step
        stepLabelOffset = Offset(
            // Subtracting from the x moves the element to the left
            stepLabelOffset.x - ((hourStepLabelTextLayout.size.width) / 2f),
            // Subtracting from the y moves the element up
            stepLabelOffset.y - (hourStepLabelTextLayout.size.height / 2f)
        )

        if (i == 0) {
            stepLabelOffset = Offset(
                stepLabelOffset.x,
                stepLabelOffset.y - (hourStepLabelTextLayout.size.height * 1.2f)
            )

            var circleCenterOffset = Offset(
                x = center.x + (outerRadius * cos(stepAngle * DEG_TO_RAD)).toFloat(),
                y = center.y + (outerRadius * sin(stepAngle * DEG_TO_RAD)).toFloat()
            )

            // Draw a circle only to mark the start/end of active time
            drawCircle(
                color = Color.LightGray,
                radius = 15f,
                center = circleCenterOffset
            )

            circleCenterOffset = Offset(
                x = circleCenterOffset.x,
                y = circleCenterOffset.y - (hourStepLabelTextLayout.size.height * 1.2f)
            )

            // Draw a circle around the active time start and end labels
            drawCircle(
//                color = Color.LightGray,
                color = activeTimeColor,
                radius = hourStepLabelTextLayout.size.width * 0.8f,
                center = circleCenterOffset,
//                style = Stroke(width = 5f)
            )
        }

        drawText(
            textMeasurer = textMeasurer,
            text = hourStepLabel,
            topLeft = stepLabelOffset,
            style = if (i == 0 ) activeTimeTextStyle else textStyle
        )
    }
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
    minuteStep = if (minuteAngle >= 5) {
        1
    } else if (1 <= minuteAngle && minuteAngle < 5) {
        5
    } else if (0.5 <= minuteAngle && minuteAngle < 1) {
        15
    } else if (0.25 <= minuteAngle && minuteAngle < 0.5) {
        15
    } else {
        30
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

            drawCircle(
                color = Color.LightGray,
                radius = 5f,
                center = stepEndOffset,
            )
        }
    }
}

fun DrawScope.drawNewTaskArea(
    startAngle: Float,
    size: Size,
    outerRadius: Float,
    sweepAngle: Float,
    color: Color
) {
    drawArc(
        color = color,
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
    taskDuration: Int,
    color: Color
) {
    drawArc(
        color = color,
        startAngle = taskStartAngle,
        sweepAngle = (taskDuration * minuteAngle).toFloat(),
        useCenter = true,
        Offset(size.width / 2 - outerRadius, size.height / 2 - outerRadius),
        size = Size(outerRadius * 2, outerRadius * 2),
        alpha = 0.1f
    )
}


