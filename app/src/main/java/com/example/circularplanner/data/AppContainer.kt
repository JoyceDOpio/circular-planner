package com.example.circularplanner.data

import android.content.Context

// A container is an object that contains the dependencies that the app requires
interface AppContainer {
    val daysRepository: IDaysRepository
    val tasksRepository: ITasksRepository
}

class AppDataContainer(
    private val context: Context
) : AppContainer {
    override val daysRepository: IDaysRepository by lazy {
        RepositoryDays(OfflineDatabase.getDatabase(context).dayDao())
    }

    override val tasksRepository: ITasksRepository by lazy {
        RepositoryTasks(OfflineDatabase.getDatabase(context).taskDao())
    }
}