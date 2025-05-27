package com.example.circularplanner.utils

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.circularplanner.ui.state.AngleMode
import com.example.circularplanner.ui.state.TaskDialState
import com.example.circularplanner.ui.state.TaskMode
import com.example.circularplanner.ui.state.TaskState
import com.example.circularplanner.ui.state.rememberTaskDialState
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.UUID
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import com.example.circularplanner.utils.TaskDialUtils

const val DEG_TO_RAD = Math.PI / 180f
const val DEG_OFFSET = -90

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TaskDial(
//    viewModel: DataViewModel,
    taskState: TaskState,
    onNavigateToTaskEdit: () -> Unit,
    onNavigateToTaskInfo: () -> Unit,
    tasks: List<Task>,//TODO: a method for retrieving tasks should be provided by a view model,
    getTask: (UUID?) -> Task?,//TODO: a method for retrieving tasks should be provided by a view model,
//    removeTask: (UUID) -> Unit//TODO: a method for retrieving tasks should be provided by a view model
//    removeTask: (Task) -> Unit
//    updateTask: (UUID, String, Time, Time, String, Float, Float, Int) -> Unit
) {

//    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val textMeasurer = rememberTextMeasurer()
//    val taskDialState: TaskDialState = rememberTaskDialState(taskState.activeTimeStart, taskState.activeTimeEnd)

    var activeTimeStart: Time = taskState.activeTimeStart
    var activeTimeEnd: Time = taskState.activeTimeEnd
    var taskMode: TaskMode by remember { mutableStateOf(TaskMode.CREATE) }
    var angleMode: AngleMode by remember { mutableStateOf(AngleMode.NONE) }

    var totalMinutes: Int = TaskDialUtils.calculateTotalNumberOfMinutes(
        Time(
            activeTimeStart.hour,
            activeTimeStart.minute
        ),
        Time(
            activeTimeEnd.hour,
            activeTimeEnd.minute
        )
    )
    var minuteAngle: Float = 360 / totalMinutes.toFloat()

    var touchedTaskId by remember { mutableStateOf<UUID?>(null) }

    // Note the start and end angle of where the finger touched the screen to get the supposed new start and end time values of a task:
    // - values used to trace the finger movement
    var startAngle by remember { mutableStateOf(0f) }
    var endAngle by remember { mutableStateOf(0f) }
//    var startAngleIsSet by remember { mutableStateOf(false) }
//    var endAngleIsSet by remember { mutableStateOf(false) }
//    var angleSettingMode by remember { mutableStateOf(AngleMode.START) }
    // - values used to draw the angles
    var tmpStartAngle by remember { mutableStateOf(0f) }
    var tmpEndAngle by remember { mutableStateOf(0f) }
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
    var touchedTask by remember { mutableStateOf<Task?>(null) }
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

    fun checkIfTouchWithinTaskArea(angle: Float, tasks: List<Task>): Boolean {
        // The task stores the appropriate angle values, i.e. values corresponding to how the circle is drawn (the 0 degree starts at the right-hand side (east) of the circle). We want to 'correct' these angles as if 0 degree starts at the top of the circle (north)
        var isTouchWithinAnyTask: Boolean = false

        for (task in tasks) {
            isTouchWithinAnyTask = TaskDialUtils.checkIfTouchWithinAngleRange(angle, task.startAngle, task.endAngle)

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
//        changeStartTimeTaskBorder = false
//        changeEndTimeTaskBorder = false
//        touchedTask = null
//        settingAngle = false
//        settingNewTask = true
//        action = Action.NONE
//        angleSettingMode = AngleMode.NONE
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
        tmpStartAngle = 0f
        tmpEndAngle = 0f
//        startAngleIsSet = false
//        endAngleIsSet = false
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
                            // Defining time range: TASK CREATION
                            detectDragGesturesAfterLongPress(
                                onDragStart = { offset ->
                                    // Get the starting coordinates and determine if the touch is:
                                    // 1. within the dial
                                    // 2. within any existing task area
                                    val distance = TaskDialUtils.distance(offset, center)
                                    angle = TaskDialUtils.angle(center, offset)

                                    Log.i("taskMode", taskMode.toString())

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
//                                            drawNewTaskTimeRange = true
                                            drawNewTaskTimeRange = true
//                                        drawFingerPointer = true
//                                    val currentAngle = angle(center, offset, minuteAngle)
                                            val currentAngle = TaskDialUtils.angle(center, offset)
                                            angle = currentAngle
//                                            angle = currentAngle
//                                            taskDialState.endAngle = currentAngle
//                                            taskDialState.endAngle = angle
                                            endAngle = currentAngle
                                            tmpEndAngle = endAngle
                                            Log.i("currentAngle", currentAngle.toString())
//                                        Log.i("allowedAngleRange", allowedAngleRange.toString())

//                                    // TODO: Make sure the angle doesn't overlap with an existing task angle
//                                    if (currentAngle > allowedAngleRange.first && currentAngle < allowedAngleRange.second) {
//                                        angle = currentAngle
//                                    }

                                            // TODO: Calculate the time represented by the angle to display it in the clock center


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

//                                        viewModel.setTaskStartTime(clockTaskStartTime)
//                                        viewModel.setTaskEndTime(clockTaskEndTime)
                                            taskState.taskStartTime = clockTaskStartTime
                                            taskState.taskEndTime = clockTaskEndTime

                                            onNavigateToTaskEdit()
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

                                    Log.i("taskMode", taskMode.toString())

                                    if (touchInsideTheDial) {
                                        if (taskMode == TaskMode.CREATE) {
                                            // Check whether the touch is within the ANY task area
//                                            touchWithinTaskArea =
//                                                TaskDialUtils.checkIfTouchWithinTaskArea(
//                                                    angle,
//                                                    tasks
//                                                )

                                            touchWithinTaskArea =
                                                checkIfTouchWithinTaskArea(
                                                    angle,
                                                    tasks
                                                )

                                            if (touchWithinTaskArea) {
                                                // Show task info
                                                taskState.taskId = touchedTaskId
                                                onNavigateToTaskInfo()
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
                                                // Start angle carries now the supposedly new value for start time of the given task, the end angle the value for the end time, respectively. Update the task:
                                                val task = getTask(touchedTaskId)

                                                // TODO: Move task update method to View Model
                                                if (task != null) {
                                                    // - start time
                                                    var minute = TaskDialUtils.calculateMinutes(
                                                        TaskDialUtils.angleForTimeCalculation(
                                                            tmpStartAngle
                                                        ),
                                                        minuteAngle
                                                    )
                                                    var clockTime =
                                                        TaskDialUtils.calculateClockTimeBasedOnMinutesFromStartTime(
                                                            start = activeTimeStart,
                                                            minutes = minute
                                                        )
                                                    task.startTime = clockTime

                                                    // - end time
                                                    minute = TaskDialUtils.calculateMinutes(
                                                        TaskDialUtils.angleForTimeCalculation(
                                                            tmpEndAngle
                                                        ),
                                                        minuteAngle
                                                    )
                                                    clockTime =
                                                        TaskDialUtils.calculateClockTimeBasedOnMinutesFromStartTime(
                                                            start = activeTimeStart,
                                                            minutes = minute
                                                        )
                                                    task.endTime = clockTime

                                                    // - start angle
                                                    task.startAngle = tmpStartAngle

                                                    // - end time
                                                    task.endAngle = tmpEndAngle

                                                    // - duration (in minutes)
                                                    task.durationInMinutes =
                                                        TaskDialUtils.calculateTotalNumberOfMinutes(
                                                            task.startTime,
                                                            task.endTime
                                                        )
                                                }

                                                // Switch the task mode to CREATE and reset
                                                taskMode = TaskMode.CREATE
                                                touchedTask = null
                                                reset()
                                            }
                                        }
                                    } else {
                                        // Cancel everything if touch is outside the dial
                                        reset()
                                    }
                                },
                                onDoubleTap = { offset ->
                                    // Engage only if task mode is CREATE
//                                    Log.i("taskMode", taskMode.toString())

                                    val distance = TaskDialUtils.distance(offset, center)
                                    touchInsideTheDial = TaskDialUtils.checkIfTouchInsideDial(
                                        distance,
                                        centerRadius,
                                        innerRadius,
                                        touchStroke
                                    )

                                    if (touchInsideTheDial) {
                                        // Check if touch is within ANY task area
                                        touchWithinTaskArea =
                                            checkIfTouchWithinTaskArea(
                                                angle,
                                                tasks
                                            )

                                        if (touchWithinTaskArea) {
                                            taskMode = TaskMode.EDIT
                                            touchedTask = getTask(touchedTaskId)

                                            if (touchedTask != null) {
                                                tmpStartAngle = touchedTask!!.startAngle
                                                tmpEndAngle = touchedTask!!.endAngle

                                                // Draw this task separately than other tasks
                                                drawNewTaskTimeRange = true
                                            }
                                        }
                                    } else {
                                        // Cancel everything if touch is outside the dial
                                        reset()
                                    }

//                                    if (taskDialState.taskMode == TaskMode.CREATE) {
//                                        val distance = taskDialState.distance(offset, center)
//                                        touchInsideTheDial = taskDialState.checkIfTouchInsideDial(
//                                            distance,
//                                            centerRadius,
//                                            innerRadius,
//                                            touchStroke
//                                        )
//
//                                        if (touchInsideTheDial) {
//                                            // Check if touch is within ANY task area
//                                            touchWithinTaskArea =
//                                                taskDialState.checkIfTouchWithinTaskArea(
//                                                    angle,
//                                                    tasks
//                                                )
//
//                                            if (touchWithinTaskArea) {
//                                                taskDialState.taskMode = TaskMode.EDIT
//                                                touchedTask = getTask(taskDialState.touchedTaskId)
//
//                                                if (touchedTask != null) {
//                                                    tmpStartAngle = touchedTask!!.startAngle
//                                                    tmpEndAngle = touchedTask!!.endAngle
//
//                                                    // Draw this task separately than other tasks
//                                                    drawNewTaskTimeRange = true
//                                                }
//                                            }
//                                        } else {
//                                            // Cancel everything if touch is outside the dial
//                                            reset()
//                                        }
//                                    }
                                },
//                                // Long press is within a task area triggers task EDITING <- SEEMS TO COLLIDE WITH detectDragGesturesAfterLongPress
//                                onLongPress = { offset ->
//                                    // Engage only if task mode is CREATE
//                                    Log.i("taskMode", taskDialState.taskMode.toString())
//
//                                    if (taskDialState.taskMode == TaskMode.CREATE) {
//                                        val distance = taskDialState.distance(offset, center)
//                                        touchInsideTheDial = taskDialState.checkIfTouchInsideDial(
//                                            distance,
//                                            centerRadius,
//                                            innerRadius,
//                                            touchStroke
//                                        )
//
//                                        if (touchInsideTheDial) {
//                                            // Check if touch is within ANY task area
//                                            touchWithinTaskArea =
//                                                taskDialState.checkIfTouchWithinTaskArea(
//                                                    angle,
//                                                    tasks
//                                                )
//
//                                            if (touchWithinTaskArea) {
//                                                taskDialState.taskMode = TaskMode.EDIT
//                                                touchedTask = getTask(taskDialState.touchedTaskId)
//
//                                                if (touchedTask != null) {
//                                                    tmpStartAngle = touchedTask!!.startAngle
//                                                    tmpEndAngle = touchedTask!!.endAngle
//
//                                                    // Draw this task separately than other tasks
//                                                    drawNewTaskTimeRange = true
//                                                }
//                                            }
//                                        } else {
//                                            // Cancel everything if touch is outside the dial
//                                            reset()
//                                        }
//                                    }
//                                }
                            )
                        }
                        .pointerInput(Unit) {
                            // Defining time range: TASK EDITING
                            detectDragGestures(
                                onDragStart = { offset ->
//                                    Log.i("taskMode", taskMode.toString())
//                                    // Get the starting coordinates and determine if the touch is:
//                                    // 1. within the dial
//                                    // 2. within any existing task area
                                    val distance = TaskDialUtils.distance(offset, center)
//
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
                                    Log.i("taskMode", taskMode.toString())
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

                                            Log.i("angle", angle.toString())
                                            Log.i("angleMode", angleMode.toString())
                                            Log.i("tmpStartAngle", tmpStartAngle.toString())
                                            Log.i("tmpEndAngle", tmpEndAngle.toString())
//                                            // Check if touch is within GIVEN task
//                                            touchWithinTaskArea =
//                                                taskDialState.checkIfTouchWithinTaskArea(
//                                                    angle,
//                                                    tasks
//                                                )

                                            // If we don't know which time boundary of the given task we are setting
                                            if (angleMode == AngleMode.NONE) {
                                                Log.i("angleEqualsTmpEndAngle", (angle == tmpEndAngle).toString())
                                                Log.i("angleEqualsTmpStartAngle", (angle == tmpStartAngle).toString())
                                                // Determine which time boundary of the given task we are setting: start or end time
                                                // It seems that an angle == tmpStartAngle comparison does not cut it - the app is not able to detect the moment when the two angles are equal
//                                                if (angle == tmpStartAngle) {
                                                if (angle in (tmpStartAngle - touchStroke/2f)..(tmpStartAngle + touchStroke/2f)) {
                                                    angleMode = AngleMode.START
//                                                } else if (angle == tmpEndAngle) {
                                                } else if (angle in (tmpEndAngle - touchStroke/2f)..(tmpEndAngle + touchStroke/2f)) {
                                                    angleMode = AngleMode.END
                                                }
                                            }
                                            // If we do know which time boundary of the given task we are setting
                                            else {
//                                                // Draw the area
//                                                drawNewTaskTimeRange = true

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
                    val minutesBetweenHoursAccumulated: Array<Int> = TaskDialUtils.calculateMinutesBetweenHoursAccumulated(
                        activeTimeStart,
                        activeTimeEnd
                    )
//                    minutesBetweenHoursAccumulated.forEach {
//                        Log.i("minutesBetweenHoursAccumulated: ", it.toString())
//                    }
//                    val minutesBetweenHoursAccumulated: Array<Int> = calculateMinuteIntervalsToDraw(activeTimeStart, activeTimeEnd)
//                    val minutesBetweenHoursAccumulated = arrayOf(0, 40, 100, 160, 220, 280, 292)
                    val activeTimeHourSteps: Array<Time> = TaskDialUtils.createClockHoursArray(
                        activeTimeStart,
                        activeTimeEnd
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
                        minuteAngle = minuteAngle,
                        innerRadius = innerRadius,
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

//                        if (taskDialState.taskMode == TaskMode.CREATE) {
//                            tmpStartAngle = startAngle
//                            tmpEndAngle = endAngle
//                        } else if (taskDialState.taskMode == TaskMode.EDIT) {
//                            // If we know which time bound we are editing: start or end
//                            if (taskDialState.angleMode == AngleMode.START) {
//                                tmpStartAngle = startAngle
//                            } else if (taskDialState.angleMode == AngleMode.END) {
//                                tmpStartAngle = startAngle
//                            }
//                        }

                        drawNewTaskArea(
//                            startAngle = startAngle,
//                            endAngle = endAngle,
                            startAngle = tmpStartAngle,
                            endAngle = tmpEndAngle,
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
                        val startMinute = TaskDialUtils.calculateTotalNumberOfMinutes(
                            activeTimeStart,
                            task.startTime
                        )
                        val endMinute = TaskDialUtils.calculateTotalNumberOfMinutes(
                            activeTimeStart,
                            task.endTime
                        )
                        val taskDuration = TaskDialUtils.calculateTotalNumberOfMinutes(task.startTime, task.endTime)

                        // We have to offset these angles because startMinute * taskDialState.minuteAngle returns a biased angle
                        task.startAngle = TaskDialUtils.offsetAngle(startMinute * minuteAngle)
                        task.endAngle = TaskDialUtils.offsetAngle(endMinute * minuteAngle)
                        task.durationInMinutes = taskDuration

                        // Don't draw the task whose time bounds are being edited
                        if (!(task.id == touchedTask?.id && taskMode == TaskMode.EDIT)) {
                            drawTask(
                                taskStartAngle = task.startAngle,
                                minuteAngle = minuteAngle,
                                outerRadius = outerRadius,
                                taskDuration = task.durationInMinutes,
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


