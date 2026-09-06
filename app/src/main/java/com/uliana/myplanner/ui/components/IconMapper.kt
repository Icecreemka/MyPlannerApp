package com.uliana.myplanner.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

fun iconFor(key: String): ImageVector = when (key) {
    "work" -> Icons.Filled.Work
    "study" -> Icons.Filled.School
    "home" -> Icons.Filled.Home
    "health" -> Icons.Filled.Favorite
    "rest" -> Icons.Filled.SelfImprovement
    "scenario" -> Icons.Filled.AutoAwesome
    "laundry" -> Icons.Filled.LocalLaundryService
    "food" -> Icons.Filled.Restaurant
    "sport" -> Icons.Filled.FitnessCenter
    else -> Icons.Filled.Eco
}

val AVAILABLE_ICON_KEYS = listOf(
    "work", "study", "home", "health", "rest", "laundry", "food", "sport", "scenario", "leaf"
)
