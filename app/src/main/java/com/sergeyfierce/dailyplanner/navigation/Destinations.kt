package com.sergeyfierce.dailyplanner.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.ui.graphics.vector.ImageVector
import com.sergeyfierce.dailyplanner.R

sealed class Destination(
    val route: String,
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    data object Calendar : Destination("calendar", R.string.nav_calendar, Icons.Filled.CalendarMonth)
    data object Notes : Destination("notes", R.string.nav_notes, Icons.Filled.StickyNote2)
    data object Settings : Destination("settings", R.string.nav_settings, Icons.Filled.Settings)
}

val primaryDestinations = listOf(Destination.Calendar, Destination.Notes, Destination.Settings)
