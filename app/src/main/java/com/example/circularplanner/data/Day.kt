package com.example.circularplanner.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import java.util.Date

class Day(date: Date) {
    val date = date
    val tasks = emptyList<Task>()
}