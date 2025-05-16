package com.example.circularplanner.data

import java.util.UUID

data class Task (
    var title: String,
    var startTime: Time,
    var endTime: Time,
    val id: UUID = UUID.randomUUID(),
    var description: String = ""
) {
    var startAngle: Float = 0f
    var endAngle: Float = 0f
    var durationInMinutes: Int = 0

    fun compareTo(task: Task): Int {
        //TODO: handle case when task is null

        // return:
        // -1 if the task starts earlier than the task it is compared to
        // 0 the tasks have the same starting time
        // 1 if the task starts later than the task it is compared to
        if (this.startTime.hour!! < task.startTime.hour!!) {
            return -1
        } else if (this.startTime.hour!! > task.startTime.hour!!) {
            return 1
        } else {
            if (this.startTime.minute!! < task.startTime.minute!!) {
                return -1
            } else if (this.startTime.minute!! > task.startTime.minute!!) {
                return 1
            } else {
                return 0
            }
        }
    }

    fun setTitleValue(value: String) {
        if (value != "") {
            title = value
        }
    }
}