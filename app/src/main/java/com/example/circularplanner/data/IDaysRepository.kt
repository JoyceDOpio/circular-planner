package com.example.circularplanner.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface IDaysRepository {
    fun getDayStream(date: String): Flow<Day?>

    suspend fun insertDay(day: Day)

    suspend fun deleteDay(day: Day)

    suspend fun updateDay(day: Day)
}