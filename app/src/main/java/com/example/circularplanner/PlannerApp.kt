package com.example.circularplanner

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.circularplanner.ui.navigation.Navigation

@Composable
fun PlannerApp(
    navController: NavHostController = rememberNavController()
) {
    Navigation(
        navController = navController,
    )
}