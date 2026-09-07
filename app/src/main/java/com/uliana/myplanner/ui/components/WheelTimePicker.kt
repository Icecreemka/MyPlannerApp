package com.uliana.myplanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.time.LocalTime
import kotlin.math.abs
import kotlin.math.round

@Composable
private fun WheelColumn(
    values: List<Int>,
    selectedValue: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    itemHeight: Dp = 44.dp,
    visibleCount: Int = 5,
    label: (Int) -> String = { it.toString().padStart(2, '0') }
) {
    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeight.toPx() }
    val listState = rememberLazyListState(values.indexOf(selectedValue).coerceAtLeast(0))
    val sidePadding = itemHeight * (visibleCount / 2)

    fun exactCenteredIndex(): Float =
        (listState.firstVisibleItemIndex * itemHeightPx + listState.firstVisibleItemScrollOffset) / itemHeightPx

    val centeredIndex by remember {
        derivedStateOf { round(exactCenteredIndex()).toInt().coerceIn(0, values.lastIndex) }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.isScrollInProgress }
            .collect { inProgress ->
                if (inProgress) return@collect
                val exact = exactCenteredIndex()
                val target = round(exact).toInt().coerceIn(0, values.lastIndex)

                values.getOrNull(target)?.let { value ->
                    if (value != selectedValue) onValueChange(value)
                }
                if (abs(exact - target) > 0.01f) {
                    listState.animateScrollToItem(target)
                }
            }
    }

    Box(modifier = modifier.height(itemHeight * visibleCount)) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .height(itemHeight)
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
        )
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = sidePadding)
        ) {
            itemsIndexed(values) { index, value ->
                val isCentered = index == centeredIndex
                Box(
                    modifier = Modifier.height(itemHeight).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label(value),
                        style = if (isCentered) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleMedium,
                        fontWeight = if (isCentered) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCentered) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                    )
                }
            }
        }
    }
}

@Composable
fun WheelDurationPicker(
    initialMinutes: Long,
    onChange: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val initialHours = (initialMinutes / 60).toInt().coerceIn(0, 12)
    val initialRemainderMinutes = (initialMinutes % 60).toInt().let { m -> (m / 5) * 5 }
    var hours by remember(initialMinutes) { mutableStateOf(initialHours) }
    var minutes by remember(initialMinutes) { mutableStateOf(initialRemainderMinutes) }
    val hoursSuffix = stringResource(com.uliana.myplanner.R.string.unit_hours)
    val minutesSuffix = stringResource(com.uliana.myplanner.R.string.unit_minutes)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        WheelColumn(
            values = (0..12).toList(),
            selectedValue = hours,
            onValueChange = { hours = it; onChange((hours * 60 + minutes).toLong().coerceAtLeast(5)) },
            label = { "$it $hoursSuffix" },
            modifier = Modifier.weight(1f)
        )
        WheelColumn(
            values = (0..55 step 5).toList(),
            selectedValue = minutes,
            onValueChange = { minutes = it; onChange((hours * 60 + minutes).toLong().coerceAtLeast(5)) },
            label = { "$it $minutesSuffix" },
            modifier = Modifier.weight(1f)
        )
    }
}
@Composable
fun WheelTimePicker(
    initial: LocalTime,
    onChange: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    var hour by remember(initial) { mutableStateOf(initial.hour) }
    var minute by remember(initial) { mutableStateOf(initial.minute) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        WheelColumn(
            values = (0..23).toList(),
            selectedValue = hour,
            onValueChange = { hour = it; onChange(LocalTime.of(hour, minute)) },
            modifier = Modifier.weight(1f)
        )
        Text(
            ":",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        WheelColumn(
            values = (0..59).toList(),
            selectedValue = minute,
            onValueChange = { minute = it; onChange(LocalTime.of(hour, minute)) },
            modifier = Modifier.weight(1f)
        )
    }
}
