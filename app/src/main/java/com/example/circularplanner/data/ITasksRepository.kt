package com.example.circularplanner.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

// Repository that provides insert, update, delete, and retrieve of [Task] from a given data source.
interface ITasksRepository {
    fun getAllTasksPerDayStream(date: String): Flow<List<Task>>

    fun getTaskStream(id: UUID): Flow<Task?>

    suspend fun insertTask(task: Task): Long

    suspend fun deleteTask(task: Task)

    suspend fun updateTask(task: Task): Int
}