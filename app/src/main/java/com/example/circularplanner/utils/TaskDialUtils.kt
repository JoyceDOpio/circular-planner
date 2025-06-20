package com.example.circularplanner.utils

import androidx.compose.ui.geometry.Offset
import com.example.circularplanner.data.Time
import kotlin.math.atan2
import kotlin.math.sqrt

// Are we creating a new task or editing an existing task
enum class TaskMode {
    CREATE,
    EDIT
}

// Are we setting the start angle (start time) or end angle (end time) of a given task
enum class AngleMode {
    NONE,
    START,
    END
}

object TaskDialUtils {
    const val DEG_TO_RAD = Math.PI / 180f
    const val DEG_OFFSET = -90
    const val MINUTES_IN_HOUR = 60

    // Calculate the exact angle on the circle
    fun angle(center: Offset, offset: Offset): Float {
        val rad = atan2(offset.y - center.y, offset.x - center.x)
        var deg = Math.toDegrees(rad.toDouble())
        return if (deg >= 0) deg.toFloat() else (deg + 360).toFloat()
    }

    // We use this to calculate the clock time
    fun angleForTimeCalculation(angle: Float): Float {
        if (angle in 270f..360f) {
            return angle - 270f
        } else {
            return angle + 90f
        }
    }

    // Calculate the number of hour points between the start- and end time, e.g. between 6:20 AM and 11:12 AM there are 5 hour points: 7:00, 8:00, 9:00, 10:00 and 11:00.
    fun calculateClockHoursBetween (start: Time, end: Time): Int {
        return end.hour.minus(start.hour)
    }

    fun calculateClockTimeBasedOnMinutesFromStartTime (start: Time, minutes: Int): Time {
        var hour: Int = start.hour
        var minute: Float
        var totalMinutes = start.minute + minutes

        hour += (totalMinutes / 60)
        minute = totalMinutes % 60f

        return Time(hour, minute.toInt())
    }

    // Gives the amount of minutes the angle corresponds to
    fun calculateMinutes(angle: Float, minuteAngle: Float): Int {
        return (angle / minuteAngle).toInt()
    }

    // Calculate minutes between adjacent hours, for example between 6:20 AM and 11:12 AM an array of [
    // 40 (number of minutes between 6:20 and 7:00),
    // 60 (number of minutes between 7:00 and 8:00),
    // 60 (number of minutes between 8:00 and 9:00),
    // 60 (number of minutes between 9:00 and 10:00),
    // 60 (number of minutes between 10:00 and 11:00),
    // 12 (number of minutes between 11:00 and 11:12)
    // ] will be returned
    fun calculateMinutesBetweenHours (start: Time, end: Time): Array<Int> {
        var minutes = emptyArray<Int>()

        val numberOfClockHoursBetween: Int = calculateClockHoursBetween(start, end)

        minutes += (60 - start.minute)

        if (numberOfClockHoursBetween > 0) {
            for (i in 1..(numberOfClockHoursBetween - 1)) {
                minutes += 60
            }
        }
        if (end.minute != 0) {
            minutes += end.minute
        }

        return minutes
    }

    // Calculates an array of minutes, for example between 6:20 AM and 11:12 AM an array of [0, 40, 100, 160, 220, 280, 292] will be returned
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

    // Calculate the angle the given time corresponds to on the dial
    fun calculateTaskAngle (activeTimeStart: Time, taskTime: Time, minuteAngle: Float): Float {
        // Number of minutes the task time corresponds to counting from the active time start
        val minute = calculateTotalNumberOfMinutes(
            activeTimeStart,
            taskTime
        )
        // We have to offset these angles because startMinute * taskDialState.minuteAngle returns a biased angle
        val taskAngle = offsetAngle(minute * minuteAngle)

        return taskAngle
    }

    fun calculateTotalNumberOfMinutes (start: Time, end: Time): Int {
        var minutes: Int = 0
        val numberOfClockHoursBetween: Int = calculateClockHoursBetween(start, end)

        if (numberOfClockHoursBetween > 1) {
            minutes += (60 - start.minute)
            for (i in 1..(numberOfClockHoursBetween - 1)) {
                minutes += 60
            }
            minutes += if (end.minute == 0) 0 else end.minute
        } else if (numberOfClockHoursBetween == 1) {
            minutes += (60 - start.minute)
            minutes += if (end.minute == 0) 0 else end.minute
        } else {
            minutes += (end.minute.minus(start.minute))
        }

        return minutes
    }

    fun checkIfTouchInsideDial(distance: Float, centerRadius: Float, innerRadius: Float, touchStroke: Float): Boolean {
        if (distance >= centerRadius - touchStroke / 2f && distance <= innerRadius + touchStroke * 2f) {
            return true
        } else {
            return false
        }
    }

    fun checkIfTouchNearDialEdge(distance: Float, innerRadius: Float, outerRadius: Float, touchStroke: Float): Boolean {
        if (distance >= innerRadius - touchStroke / 2f && distance <= outerRadius + touchStroke * 2f) {
            return true
        } else {
            return false
        }
    }

    fun checkIfTouchWithinActiveTime(
        offset: Offset,
        activeTimeCenter: Offset,
        activeTimeRadius: Float,
        touchStroke: Float
    ): Boolean {
        val distance = distance(offset, activeTimeCenter)
        if (distance <= activeTimeRadius + touchStroke * 2f) {
            return true
        }

        return false
    }

    fun checkIfTouchWithinAngleRange(angle: Float, startAngle: Float, endAngle: Float): Boolean {
        // The task stores the appropriate angle values, i.e. values corresponding to how the circle is drawn (the 0 degree starts at the right-hand side (east) of the circle). We want to 'correct' these angles as if 0 degree starts at the top of the circle (north)
        val angleCorrected = mapAngle270To0Degree(angle)
        var startAngleCorrected = mapAngle270To0Degree(startAngle)
        var endAngleCorrected = mapAngle270To0Degree(endAngle)

        if (angleCorrected in startAngleCorrected..endAngleCorrected) {
            return true
        }

        return false
    }

    fun createClockHoursArray (start: Time, end: Time): Array<Time> {
        var hours: Array<Time> = emptyArray()
        val numberOfClockHoursBetween: Int = calculateClockHoursBetween(start, end)

        hours += start
        if (numberOfClockHoursBetween > 0) {
            for (i in 1..numberOfClockHoursBetween) {
                val hour: Int = start.hour + i
                hours += Time(hour, 0)
            }
        }
        if (end.minute > 0) {
            hours += end
        }

        return hours
    }

    fun distance(first: Offset, second: Offset): Float {
        return sqrt((first.x - second.x).square() + (first.y - second.y).square())
    }

    fun Float.square(): Float {
        return this * this
    }

    // We want the 0 degree angle to correspond to 270 degree (the north of the circle, and not east). We use this for example to draw the clock upright. The purpose of this offset is to TURN the circle LEFT (ANTI-CLOCKWISE) by 90 degrees
    fun offsetAngle(angle: Float): Float {
        var correctedAngle: Float = angle + DEG_OFFSET

        if (correctedAngle < 0) {
            correctedAngle += 360
        }

        return correctedAngle
    }

    // We want the 0 degree angle to correspond to 270 degree (the north of the circle, and not east). We map an angle to its corresponding value if the circle actually started with 0 degrees at the north of the circle
    fun mapAngle270To0Degree(angle: Float): Float {
        var correctedAngle: Float = angle - 270f

        if (correctedAngle < 0) {
            correctedAngle += 360
        }

        return correctedAngle
    }

    fun sweepAngle(start: Float, end: Float): Float {
        return if (start < end) {
            (end - start).toFloat()
        } else {
            (360 - start + end).toFloat()
        }
    }
}
