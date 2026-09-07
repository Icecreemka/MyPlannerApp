package com.uliana.myplanner.ui.finance

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PiggyBank(
    progress: Float,
    color: Color,
    coinDropTrigger: Int,
    breakTrigger: Boolean,
    onBreakFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale = remember { Animatable(1f) }
    val rotation = remember { Animatable(0f) }
    val alpha = remember { Animatable(1f) }
    var showShards by remember { mutableStateOf(false) }
    val shardProgress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(breakTrigger) {
        if (breakTrigger) {
            showShards = true
            scope.launch { shardProgress.animateTo(1f, tween(500, easing = LinearOutSlowInEasing)) }
            scale.animateTo(1.18f, tween(140))
            launch { rotation.animateTo(14f, tween(260)) }
            scale.animateTo(0f, tween(260))
            alpha.snapTo(0f)
            delay(350)
            onBreakFinished()
        }
    }

    Box(modifier = modifier.size(96.dp), contentAlignment = Alignment.Center) {

        if (showShards) {
            val shardCount = 8
            repeat(shardCount) { i ->
                val angle = (360f / shardCount) * i
                val distance = 46f * shardProgress.value
                val rad = Math.toRadians(angle.toDouble())
                val x = (distance * kotlin.math.cos(rad)).toFloat()
                val y = (distance * kotlin.math.sin(rad)).toFloat()
                Box(
                    modifier = Modifier
                        .offset(x = x.dp, y = y.dp)
                        .size(8.dp)
                        .graphicsLayer { this.alpha = 1f - shardProgress.value }
                        .clip(CircleShape)
                ) {
                    Icon(Icons.Filled.Circle, contentDescription = null, tint = color)
                }
            }
        }

        Box(
            modifier = Modifier
                .size(72.dp)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    rotationZ = rotation.value
                    this.alpha = alpha.value
                }
        ) {
            Icon(
                imageVector = Icons.Filled.Savings,
                contentDescription = null,
                tint = color.copy(alpha = 0.22f),
                modifier = Modifier.size(72.dp)
            )
            Icon(
                imageVector = Icons.Filled.Savings,
                contentDescription = stringResource(com.uliana.myplanner.R.string.piggy_bank_desc),
                tint = color,
                modifier = Modifier
                    .size(72.dp)
                    .drawWithContent {
                        clipRect(top = size.height * (1f - progress.coerceIn(0f, 1f))) {
                            this@drawWithContent.drawContent()
                        }
                    }
            )
        }

        if (coinDropTrigger > 0) {
            key(coinDropTrigger) {
                CoinDrop(color = color)
            }
        }
    }
}

@Composable
private fun CoinDrop(color: Color) {
    val coinCount = 5
    repeat(coinCount) { index ->
        val offsetY = remember { Animatable(-70f) }
        val alpha = remember { Animatable(1f) }
        val xJitter = remember { (-18..18).random().toFloat() }

        LaunchedEffect(Unit) {
            delay(index * 60L)
            launch {
                offsetY.animateTo(28f, tween(420, easing = LinearOutSlowInEasing))
            }
            delay(300)
            alpha.animateTo(0f, tween(200))
        }

        Box(
            modifier = Modifier
                .offset(x = xJitter.dp, y = offsetY.value.dp)
                .size(10.dp)
                .graphicsLayer { this.alpha = alpha.value }
                .clip(CircleShape)
        ) {
            Icon(Icons.Filled.Circle, contentDescription = null, tint = Color(0xFFD4A76A))
        }
    }
}
