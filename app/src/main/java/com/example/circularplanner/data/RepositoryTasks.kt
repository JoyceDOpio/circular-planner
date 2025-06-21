package com.example.circularplanner.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class RepositoryTasks(private val taskDao: DaoTask) : ITasksRepository {
    override fun getAllTasksPerDayStream(date: String): Flow<List<Task>> {
        return taskDao.getAllTasks(date)
    }

    override fun getTaskStream(id: UUID): Flow<Task?> {
        return taskDao.getTask(id)
    }

    override suspend fun insertTask(task: Task): Long {
        return taskDao.insert(task)
    }

    override suspend fun deleteTask(task: Task) {
        return taskDao.delete(task)
    }

    override suspend fun updateTask(task: Task): Int {
        return taskDao.update(task)
    }
}