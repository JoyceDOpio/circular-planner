package com.example.circularplanner

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.circularplanner.ui.theme.CircularPlannerTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()

        setContent {
            CircularPlannerTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Navigation(
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    PlannerApp()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DayPreview() {
    CircularPlannerTheme {
        PlannerApp()
    }
}
