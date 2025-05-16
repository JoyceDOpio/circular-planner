package com.example.circularplanner.data

data class Time(var hour: Int = 0, var minute: Int = 0) {
//    var hour: Int? = null
//    var minute: Int? = null

    fun setHourValue(value: Int) {
        if (value in 0..24) {
            hour = value
        }
    }

    fun setMinuteValue(value: Int) {
        if (value in 0..59) {
            minute = value
        }
    }

    fun getHourValue(): Int? {
        return hour
    }

    fun getMinuteValue(): Int? {
        return minute
    }
}