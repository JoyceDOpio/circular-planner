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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.circularplanner.data.Task
import com.example.circularplanner.data.Time
import com.example.circularplanner.ui.navigation.Routes
import com.example.circularplanner.ui.viewmodel.DataViewModel
import java.time.LocalTime
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt


const val DEG_TO_RAD = Math.PI / 180f
const val DEG_OFFSET = -90

enum class AngleMode {
    NONE,
    START,
    END
}

enum class TaskMode {
    CREATE,
    EDIT,
    VIEW,
    NONE
}

enum class Action {
    NONE,
    NEW_TASK,
    EXISTING_TASK
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TaskDial(
    viewModel: DataViewModel,
    onNavigateToTaskEdit: () -> Unit,
    tasks: List<Task>,
//    setNewTaskStartTime: (Time) -> Unit,
//    setNewTaskEndTime: (Time) -> Unit,
//    removeTask: (UUID) -> Unit
//    removeTask: (Task) -> Unit
) {

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val textMeasurer = rememberTextMeasurer()

    val activeTimeStart = uiState.activeTimeStart
    val activeTimeEnd = uiState.activeTimeEnd

    // Note the start and end angle of where the finger touched the screen to get the supposed new start and end time values of a task
    var startAngle by remember { mutableStateOf(0f) }
    var endAngle by remember { mutableStateOf(0f) }
    var startAngleIsSet by remember { mutableStateOf(false) }
    var endAngleIsSet by remember { mutableStateOf(false) }
    var angleSettingMode by remember { mutableStateOf(AngleMode.START) }

    // New task
    var taskState by remember { mutableStateOf(TaskMode.NONE) }

    // The width and height of the Canvas
    var width by remember { mutableStateOf(0) }
    var height by remember { mutableStateOf(0) }
    var angle by remember { mutableStateOf(0f) }
    var touchNearTheDialEdge by remember { mutableStateOf(false) }
    var touchInsideTheDial by remember { mutableStateOf(false) }
    var drawNewTaskTimeRange by remember {
        mutableStateOf(false)
    }
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

    var minuteAngle by remember { mutableStateOf(0f) }
    var totalMinutes by remember { mutableStateOf(0) }

    // Variables to identify whether the dial was touched within an existing task area
    var touchWithinTaskArea by remember { mutableStateOf(false) }
    var touchedTask by remember { mutableStateOf<Task?>(null) }
    var changeStartTimeTaskBorder by remember { mutableStateOf(false) }
    var changeEndTimeTaskBorder by remember { mutableStateOf(false) }

    var settingAngle by remember { mutableStateOf(false) }
    var settingNewTask by remember { mutableStateOf(true) }
    var allowedAngleRange by remember { mutableStateOf(Pair(0f,0f)) }
    var dateTime by remember { mutableStateOf(LocalTime.now()) }
    var clockCenterTime by remember { mutableStateOf(Time(LocalTime.now().hour, LocalTime.now().minute)) }
    // Draw a pointer on the dial to illustrate the finger's movement along the dial edge
    var drawFingerPointer by remember { mutableStateOf(false) }

    var action by remember { mutableStateOf(Action.NONE) }

//    totalMinutes = calculateTotalNumberOfMinutes(
//        Time(
//        activeTimeStartPicker.hour,
//        activeTimeStartPicker.minute
//    ), Time(
//        activeTimeEndPicker.hour,
//        activeTimeEndPicker.minute
//        ))
    totalMinutes = 292
    minuteAngle = 360 / totalMinutes.toFloat()

    fun checkIfTouchInsideDial(distance: Float): Boolean {
        if (distance >= centerRadius - touchStroke / 2f && distance <= innerRadius + touchStroke * 2f) {
            return true
        } else {
            return false
        }
    }

    fun checkIfTouchNearDialEdge(distance: Float): Boolean {
        if (distance >= innerRadius - touchStroke / 2f && distance <= outerRadius + touchStroke * 2f) {
            return true
        } else {
            return false
        }
    }

    fun checkIfTouchWithinTaskArea(angle: Float): Boolean {
        for (task in tasks) {
            if (angle in task.startAngle..task.endAngle) {
                touchedTask = task
                return true
            }
        }

        return false
    }

    fun getMinimumAndMaximumAllowedAngleRange(): Pair<Float, Float> {
        var first = 0f
        var second = 360f
        var taskBefore: Task? = null
        var taskAfter: Task? = null

        if (touchedTask != null) {
            for (i in 0..tasks.size - 1) {
                if (tasks[i] == touchedTask) {
                    if (i > 0) {
                        taskBefore = tasks[i - 1]
                        first = taskBefore.endAngle
                    }
                    if (i < tasks.size - 1){
                        taskAfter = tasks [i + 1]
                        second = taskAfter.startAngle
                    }
                    break
                }
            }
        } else {
            for (task in tasks) {
                if (angle >= task.endAngle) {
                    taskBefore = task
                    first = taskBefore.endAngle
                } else if (angle <= task.startAngle) {
                    taskAfter = task
                    second = taskAfter.startAngle
                    break
                }
            }
        }



        return Pair(correctAngle(first), correctAngle(second))
    }

    fun getTouchedTask(angle: Float): Task? {
        for (task in tasks) {
            if (angle in task.startAngle..task.endAngle) {
                return task
            }
        }

        return null
    }

    fun resetDialParameters() {
        touchNearTheDialEdge = false
        drawNewTaskTimeRange = false
        touchWithinTaskArea = false
        changeStartTimeTaskBorder = false
        changeEndTimeTaskBorder = false
        touchedTask = null
        settingAngle = false
        settingNewTask = true
        action = Action.NONE
        angleSettingMode = AngleMode.NONE
        taskState = TaskMode.NONE
    }

    fun resetClockCenter() {
        drawFingerPointer = false
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
//                Canvas(modifier = Modifier
//                    .fillMaxSize()
////                                .fillMaxWidth()
////                                .aspectRatio(1f)
////                                .size(400.dp)
////                                .background(Color.Gray)
//                    .onGloballyPositioned {
//                        width = it.size.width
//                        height = it.size.height
//                        center = Offset(width / 2f, height / 2f)
////                                    radius = min(width.toFloat(), height.toFloat()) / 2f - padding - stroke / 2f
////                                    radius = min(width.toFloat(), height.toFloat()) / 2f
//                        outerRadius = min(width.toFloat(), height.toFloat()) / 2f
//                        innerRadius = outerRadius * .8f
//                        centerRadius = outerRadius * 0.25f
//                    }
//                    .pointerInteropFilter {
//                        val x = it.x
//                        val y = it.y
//                        val offset = Offset(x, y)
//                        when (it.action) {
//
//                            MotionEvent.ACTION_OUTSIDE -> {
//                                reset()
//                            }
//
//                            MotionEvent.ACTION_DOWN -> {
//                                val distance = distance(offset, center)
////                                val angle = angle(center, offset, minuteAngle)
//                                angle = angle(center, offset, minuteAngle)
//
//                                touchNearTheDialEdge = checkIfTouchNearDialEdge(distance)
//                                touchWithinTaskArea = checkIfTouchWithinTaskArea(startAngle)
//                                touchInsideTheDial = checkIfTouchInsideDial(distance)
//
//                                if (touchNearTheDialEdge) {
////                                    startAngle = angle
//                                    settingAngle = true
////                                    allowedAngleRange = getMinimumAndMaximumAllowedAngleRange()
//
//
//                                    if (touchWithinTaskArea) {
//                                        settingNewTask = false
//                                        action = Action.EXISTING_TASK
//                                        touchedTask = getTouchedTask(angle)
//                                    } else {
//                                        action = Action.NEW_TASK
//                                        drawFingerPointer = true
//                                        allowedAngleRange = getMinimumAndMaximumAllowedAngleRange()
//                                    }
//
//                                } else if (touchInsideTheDial) {
//                                    if (touchWithinTaskArea) {
//                                        action = Action.EXISTING_TASK
//                                        touchedTask = getTouchedTask(angle)
//                                    }
//                                } else {
//                                    // Cancel everything if touch is outside the dial
//                                    reset()
//                                }
//                            }
//
//                            MotionEvent.ACTION_MOVE -> {
//                                val distance = distance(offset, center)
//                                touchNearTheDialEdge = checkIfTouchNearDialEdge(distance)
//
//                                if (touchNearTheDialEdge) {
//                                    if (action == Action.NEW_TASK) {
//                                        if (angleSettingMode == AngleMode.END) {
//                                            drawNewTaskTimeRange = true
//                                        }
//                                        val currentAngle = angle(center, offset, minuteAngle)
//                                        // TODO: Make sure the angle doesn't overlap with an existing task angle
//                                        if (currentAngle > allowedAngleRange.first && currentAngle < allowedAngleRange.second) {
//                                            angle = currentAngle
//                                        }
//
//                                        // TODO: Calculate the time represented by the angle to display it in the clock center
//                                        val minute = assignMinuteToAngle(angle, minuteAngle)
//                                        clockCenterTime = calculateClockTimeBasedOnMinutesFromStartTime(activeTimeStart, minute)
//                                    }
//                                }
//                            }
//
//                            MotionEvent.ACTION_UP -> {
//                                // If the user moved the finger around the dial it means he wants to set an angle
//                                if (settingAngle) {
//                                    if (angleSettingMode == AngleMode.START) {
//                                        startAngle = angle
//                                        startAngleIsSet = true
//                                    } else if (angleSettingMode == AngleMode.END) {
//                                        endAngle = angle
//                                        endAngleIsSet = true
//                                    }
//
//                                    // If we are setting time boundaries for a new task
////                                    if (settingNewTask) {
//                                    if (action == Action.NEW_TASK) {
//                                        if (startAngleIsSet && endAngleIsSet) {
//                                            // Map the start and end angles to the corresponding minute values on the dial
//                                            val startMinute =
//                                                assignMinuteToAngle(startAngle, minuteAngle)
//                                            val endMinute =
//                                                assignMinuteToAngle(endAngle, minuteAngle)
//
//                                            Log.d("startMinute", "startMinute: $startMinute")
//                                            Log.d("endMinute", "endMinute: $endMinute")
//
//                                            val startClockTime =
//                                                calculateClockTimeBasedOnMinutesFromStartTime(
//                                                    activeTimeStart,
//                                                    startMinute
//                                                )
//                                            val endClockTime =
//                                                calculateClockTimeBasedOnMinutesFromStartTime(
//                                                    activeTimeStart,
//                                                    endMinute
//                                                )
//
//                                            setNewTaskStartTime(startClockTime)
//                                            setNewTaskEndTime(endClockTime)
//
//                                            resetStartAndEndAngles()
//
//                                            navController.navigate(
//                                                route = "${Routes.TaskEditScreen.route}?id=${""}"
//                                            ) {
//                                                popUpTo(Routes.Home.route)
//                                            }
//                                        }
//                                    }
//                                } else {
//                                    // The user didn't drag the finger around the dial edge (or at all). He just tapped the dial
//                                    if (touchInsideTheDial && touchWithinTaskArea) {
//                                        navController.navigate(
//                                            route = "${Routes.TaskEditScreen.route}?id=${touchedTask!!.id}"
//                                        ) {
//                                            popUpTo(Routes.Home.route)
//                                        }
//                                    }
//                                }
//
//                                resetDialParameters()
//                            }
//
//                            else -> return@pointerInteropFilter false
//                        }
//                        return@pointerInteropFilter true
//                    }
//                ) {
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
                            outerRadius = min(width.toFloat(), height.toFloat()) / 2f
                            innerRadius = outerRadius * .8f
                            centerRadius = outerRadius * 0.25f
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = { offset ->
                                    val distance = distance(offset, center)
                                    touchInsideTheDial = checkIfTouchInsideDial(distance)

                                    if (touchInsideTheDial) {
                                        // Show task info or open task creation screen
                                    } else {
                                        // Cancel everything if touch is outside the dial
                                        reset()
                                    }
                                },
//                                onDoubleTap = { offset ->
//                                    val distance = distance(offset, center)
//                                    touchInsideTheDial = checkIfTouchInsideDial(distance)
//
//                                    if (touchInsideTheDial) {
//
//                                    } else {
//                                        // Cancel everything if touch is outside the dial
//                                        reset()
//                                    }
//                                },
//                                onLongPress = { offset ->
//                                    val distance = distance(offset, center)
////                                    touchInsideTheDial = checkIfTouchInsideDial(distance)
//                                    touchNearTheDialEdge = checkIfTouchNearDialEdge(distance)
//
//                                    if (touchNearTheDialEdge) {
//                                        // Active creation of a new task
//                                        taskState  = TaskMode.CREATE
//                                    } else {
//                                        // Cancel everything if touch is outside the dial
//                                        reset()
//                                    }
//
//                                    Log.i("taskState: ", taskState.toString())
//                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    // Get the starting coordinates and determine if the touch is:
                                    // 1. within the dial
                                    // 2. within any existing task area
                                    val distance = distance(offset, center)
//                                angle = angle(center, offset, minuteAngle)
                                    angle = angle(center, offset)

                                    touchNearTheDialEdge = checkIfTouchNearDialEdge(distance)

                                    if (touchNearTheDialEdge) {
                                        taskState  = TaskMode.CREATE
                                        startAngle = angle
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    // If touch is within dial, keep track of the coordinate
                                    val offset = change.position
                                    val distance = distance(offset, center)
                                touchNearTheDialEdge = checkIfTouchNearDialEdge(distance)

                                    if (taskState == TaskMode.CREATE) {
                                        if (touchNearTheDialEdge) {
                                            drawNewTaskTimeRange = true
//                                        drawFingerPointer = true
//                                    val currentAngle = angle(center, offset, minuteAngle)
                                            val currentAngle = angle(center, offset)
                                            angle = currentAngle
                                            endAngle = angle
                                            Log.i("currentAngle", currentAngle.toString())
//                                        Log.i("allowedAngleRange", allowedAngleRange.toString())

//                                    // TODO: Make sure the angle doesn't overlap with an existing task angle
//                                    if (currentAngle > allowedAngleRange.first && currentAngle < allowedAngleRange.second) {
//                                        angle = currentAngle
//                                    }

                                            // TODO: Calculate the time represented by the angle to display it in the clock center


                                            val activeTimeStart = uiState.activeTimeStart
//                                        val activeTimeStart = Time(
//                                            activeTimeStartPicker.hour,
//                                            activeTimeStartPicker.minute
//                                        )

//                                    Log.i("activeTimeStart", activeTimeStart.toString())
                                            val minute = calculateMinutes(
                                                angleForTimeCalculation(angle),
                                                minuteAngle
                                            )
                                            clockCenterTime =
                                                calculateClockTimeBasedOnMinutesFromStartTime(
                                                    activeTimeStart!!,
                                                    minute
                                                )
                                        }
                                    }
                                },
                                onDragEnd = {
                                    if (touchNearTheDialEdge) {
                                        val taskStartTimeMinute = calculateMinutes(
                                            angleForTimeCalculation(startAngle),
                                            minuteAngle
                                        )
                                        val clockTaskStartTime =
                                            calculateClockTimeBasedOnMinutesFromStartTime(
                                                activeTimeStart!!,
                                                taskStartTimeMinute
                                            )
                                        val taskEndTimeMinute = calculateMinutes(
                                            angleForTimeCalculation(endAngle),
                                            minuteAngle
                                        )
                                        val clockTaskEndTime =
                                            calculateClockTimeBasedOnMinutesFromStartTime(
                                                activeTimeStart!!,
                                                taskEndTimeMinute
                                            )

                                        viewModel.setTaskStartTime(clockTaskStartTime)
                                        viewModel.setTaskEndTime(clockTaskEndTime)

                                        onNavigateToTaskEdit()
                                    } else {
                                        reset()
                                    }
                                }
                            )
                        }
                ) {
                    //    val activeTimeStart: Time = activeTimeStart
                    //    val activeTimeEnd: Time = activeTimeEnd
                    val activeTimeStart: Time = Time(6,20)
                    val activeTimeEnd: Time = Time(11,12)


                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    //    val minutesBetweenHoursAccumulated: Array<Int> = calculateMinuteIntervalsToDraw(activeTimeStart, activeTimeEnd)
                    val minutesBetweenHoursAccumulated = arrayOf(0, 40, 100, 160, 220, 280, 292)
                    //    val activeTimeHourSteps: Array<Time> = createHoursArray(activeTimeStart, activeTimeEnd)
                    val activeTimeHourSteps = arrayOf(
                        Time(6,20),
                        Time(7,0),
                        Time(8,0),
                        Time(9,0),
                        Time(10,0),
                        Time(11,0),
                        Time(11,12)
                    )

//                                val totalMinutes = calculateTotalNumberOfMinutes(activeTimeStart, activeTimeEnd)
//                                val totalMinutes = 292
//                                minuteAngle = 360 / totalMinutes.toDouble()
                    //    val minuteAngle = 1.232876712
                    Log.d("minuteAngle", "minuteAngle: $minuteAngle")
//                                val center = width / 2
//                                val outerRadius = center * .8f
//                                val innerRadius = outerRadius * .8f
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

                    drawMinuteSteps(
                        minuteAngle,
                        totalMinutes,
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
                        )
                    }

                    for (task in tasks) {
                        drawTask(
                            activeTimeStart = activeTimeStart,
                            task = task,
                            minuteAngle = minuteAngle,
                            outerRadius = outerRadius
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

//fun angle(center: Offset, offset: Offset): Float {
//    val rad = atan2(center.y - offset.y, center.x - offset.x)
//    val deg = Math.toDegrees(rad.toDouble())
//    return deg.toFloat()
//}

// Calculate the exact angle on the circle
fun angle(center: Offset, offset: Offset): Float {
    val rad = atan2(offset.y - center.y, offset.x - center.x)
    var deg = Math.toDegrees(rad.toDouble())
    return if (deg >= 0) deg.toFloat() else (deg + 360).toFloat()
}

//fun angle(center: Offset, offset: Offset): Float {
//    val rad = atan2(center.y - offset.y, offset.x - center.x)
//    var deg = Math.toDegrees(rad.toDouble())
//    return if (deg <= 180) deg.toFloat() else (deg - 360).toFloat()
//}

//fun angle(center: Offset, offset: Offset, minuteAngle: Float): Float {
////    val rad = atan2(center.y - offset.y, center.x - offset.x)
//    val rad = atan2(offset.y - center.y, offset.x - center.x)
//    var deg = Math.toDegrees(rad.toDouble()).toFloat()
//
//
//    if (deg < 0) {
////        deg = 360 + Math.toDegrees(rad.toDouble())
//        deg += 360
//    }


//    return calculateAngle(deg.toFloat(), minuteAngle)
////    return roundToMinuteAngle(correctAngle(deg), minuteAngle)
////    return deg
//}

// Gives the amount of minutes the angle corresponds to
fun calculateMinutes(angle: Float, minuteAngle: Float): Int {
    return (angle / minuteAngle).toInt()
}

// Because we want the 0 degree angle to correspond to 270 degree (the north of the circle, and not east). We use this to draw the clock upright
fun correctAngle(angle: Float): Float {
    var correctedAngle: Float = angle + DEG_OFFSET

    if (correctedAngle < 0) {
        correctedAngle += 360
    }

    return correctedAngle
}

fun decorrectAngle(angle: Float): Float {
    var correctedAngle: Float = angle - DEG_OFFSET

    if (correctedAngle < 0) {
        correctedAngle += 360
    }

    return correctedAngle
}

// We use this to calculate the clock time
fun angleForTimeCalculation(angle: Float): Float {
    if (angle in 270f..360f) {
        return angle - 270f
    } else {
        return angle + 90f
    }
}

fun openTaskEditScreen(navController: NavHostController) {
    val startTime = Time(0, 0)
    val endTime = Time(0, 0)

    navController.navigate(Routes.TaskEdit.route){
        popUpTo(Routes.TaskDisplay.route)
    }
}

// Round the exact angle to the minute angle
fun calculateAngle(angle: Float, minuteAngle: Float): Float {
    var minute = calculateMinutes(angle, minuteAngle)

    return minute * minuteAngle
}

//fun calculateAngle(minute: Int, minuteAngle: Float): Float {
//    return minute * minuteAngle
//}

fun calculateClockHoursBetween (start: Time, end: Time): Int {
    return end.hour!!.minus(start.hour!!)
}

fun calculateMinutesBetweenHours (start: Time, end: Time): Array<Int> {
    var minutes = emptyArray<Int>()

    val numberOfClockHoursBetween: Int = calculateClockHoursBetween(start, end)

    minutes += (60 - start.minute!!)
    
    if (numberOfClockHoursBetween > 0) {
        // TODO: secure null values
        for (i in 1..(numberOfClockHoursBetween - 1)) {
            minutes += 60
        }
        minutes += if (end.minute == 0) 60 else end.minute!!
    }

    return minutes
}

fun calculateMinutesBetweenHoursAccumulated (start: Time, end: Time): Array<Int> {
    var minutesBetweenHoursAccumulated: Array<Int> = emptyArray()
    val minutesBetweenHours: Array<Int> = calculateMinutesBetweenHours(start, end)
    var minutesTotal = 0

    minutesBetweenHoursAccumulated += minutesTotal
    for (minutes in minutesBetweenHours) {
        minutesTotal += minutes
        minutesBetweenHoursAccumulated += minutesTotal
    }

    return minutesBetweenHoursAccumulated
}

fun calculateClockTimeBasedOnMinutesFromStartTime (start: Time, minutes: Int): Time {
    var hour: Int = start.hour
    var minute: Float

    var totalMinutes = start.minute + minutes

    hour += (totalMinutes / 60)

    minute = totalMinutes % 60f

//    Log.d("totalMinutes", "totalMinutes: $totalMinutes")
//    Log.d("totalMinutes_%_60f", "totalMinutes % 60f: ${totalMinutes % 60f}")
//    Log.d("totalMinutes_/_60f", "totalMinutes / 60f: ${totalMinutes / 60f}")
//    Log.d("totalMinutes_/_60", "totalMinutes / 60: ${totalMinutes / 60}")
//
//    Log.d("hour", "hour: $hour")
//    Log.d("minute", "minute: $minute")

    return Time(hour, minute.toInt())
}

fun calculateTotalNumberOfMinutes (start: Time, end: Time): Int {
    var minutes: Int = 0
    val numberOfClockHoursBetween: Int = calculateClockHoursBetween(start, end)

    // TODO: secure null values
    if (numberOfClockHoursBetween > 1) {
        minutes += (60 - start.minute!!)
        for (i in 1..(numberOfClockHoursBetween - 1)) {
            minutes += 60
        }
        minutes += if (end.minute == 0) 0 else end.minute!!
    } else if (numberOfClockHoursBetween == 1) {
        minutes += (60 - start.minute!!)
        minutes += if (end.minute == 0) 0 else end.minute!!
    } else {
        minutes += (end.minute?.minus(start.minute!!)!!)
    }

    return minutes
}

fun createClockHoursArray (start: Time, end: Time): Array<Time> {
    var hours: Array<Time> = emptyArray()
    val numberOfClockHoursBetween: Int = calculateClockHoursBetween(start, end)

    hours += start
    if (numberOfClockHoursBetween > 0) {
        for (i in 1..numberOfClockHoursBetween) {
            val hour: Int = start.hour!!
            hours += Time(hour, 0)
        }
    }
    hours += end

    return hours
}

fun distance(first: Offset, second: Offset): Float {
    return sqrt((first.x - second.x).square() + (first.y - second.y).square())
}

fun Float.square(): Float {
    return this * this
}

fun sweepAngle(start: Float, end: Float): Float {
    if (start < end) {
        return (end - start).toFloat()
    } else {
        return (360 - start + end).toFloat()
    }
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

    var affirmation = "I can do it"
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
) {
    drawArc(
        Color.Red,
        startAngle = startAngle,
        sweepAngle = sweepAngle(startAngle, endAngle),
        useCenter = true,
        Offset(size.width / 2 - outerRadius, size.height / 2 - outerRadius),
        size = Size(outerRadius * 2, outerRadius * 2),
        alpha = 0.1f
    )
}

fun DrawScope.drawTask(
    activeTimeStart: Time,
    task: Task,
    minuteAngle: Float,
    outerRadius: Float
) {
    // Minutes from the active time start
    val startMinute: Int = calculateTotalNumberOfMinutes(activeTimeStart, task.startTime)
    val endMinute: Int = calculateTotalNumberOfMinutes(activeTimeStart, task.endTime)
    val taskDurationInMinutes: Int = calculateTotalNumberOfMinutes(task.startTime, task.endTime)
    Log.i("startMinute", "startMinute: $startMinute")
    Log.i("taskDurationInMinutes", "taskDurationInMinutes: $taskDurationInMinutes")

    task.startAngle = correctAngle((startMinute * minuteAngle).toFloat())
    task.endAngle = correctAngle((endMinute * minuteAngle).toFloat())
//    task.startAngle = (startMinute * minuteAngle).toFloat()
//    task.endAngle = (endMinute * minuteAngle).toFloat()
    task.durationInMinutes = taskDurationInMinutes

    drawArc(
        Color.Cyan,
        startAngle = task.startAngle,
        sweepAngle = (taskDurationInMinutes * minuteAngle).toFloat(),
        useCenter = true,
        Offset(size.width / 2 - outerRadius, size.height / 2 - outerRadius),
        size = Size(outerRadius * 2, outerRadius * 2),
        alpha = 0.1f
    )
}


