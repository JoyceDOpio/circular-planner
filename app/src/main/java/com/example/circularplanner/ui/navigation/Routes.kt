package com.example.circularplanner.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object ActiveTimeSetUp

@Serializable
object TaskDisplayRoute

@Serializable
data class TaskEditRoute(val id: String?)

@Serializable
data class TaskInfoRoute(val id: String)