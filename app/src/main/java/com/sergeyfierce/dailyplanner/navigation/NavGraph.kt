package com.sergeyfierce.dailyplanner.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sergeyfierce.dailyplanner.feature.calendar.CalendarScreen
import com.sergeyfierce.dailyplanner.feature.calendar.CalendarViewModel
import com.sergeyfierce.dailyplanner.feature.notes.NotesScreen
import com.sergeyfierce.dailyplanner.feature.settings.SettingsScreen

@Composable
fun DailyPlannerNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Destination.Calendar.route,
        modifier = modifier
    ) {
        composable(Destination.Calendar.route) {
            val viewModel: CalendarViewModel = viewModel()
            CalendarScreen(viewModel = viewModel)
        }
        composable(Destination.Notes.route) {
            NotesScreen()
        }
        composable(Destination.Settings.route) {
            SettingsScreen()
        }
    }
}
