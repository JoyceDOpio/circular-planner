package com.example.circularplanner.ui.navigation

sealed class Routes(val route: String) {
    object TaskDisplay: Routes("taskDisplay")
    object TaskEdit: Routes("taskEdit")
    object ActiveTimeSetUp: Routes("activeTimeSetUp")
}