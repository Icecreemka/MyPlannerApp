package com.uliana.myplanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.uliana.myplanner.ui.theme.Parchment
import com.uliana.myplanner.ui.theme.ParchmentDark
import com.uliana.myplanner.ui.theme.SageLight

@Composable
fun GardenBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(SageLight.copy(alpha = 0.35f), Parchment, ParchmentDark),
                    start = Offset(0f, 0f),
                    end = Offset(0f, 1600f)
                )
            )
    ) {
        content()
    }
}
