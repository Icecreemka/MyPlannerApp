@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.uliana.myplanner.ui.scenario

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.uliana.myplanner.R
import com.uliana.myplanner.ui.components.ScrollableChipRow
import com.uliana.myplanner.ui.components.WheelTimePicker
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Выбор даты и времени запуска сценария — с быстрыми вариантами ("сейчас", "через 30 мин")
 * и полноценным календарём + колесом времени, если нужно любое другое время.
 */
@Composable
fun RunScenarioSheet(
    scenarioName: String,
    onDismiss: () -> Unit,
    onConfirm: (LocalDateTime) -> Unit
) {
    val now = remember { LocalDateTime.now() }
    var date by remember { mutableStateOf(now.toLocalDate()) }
    var time by remember { mutableStateOf(now.toLocalTime().withSecond(0).withNano(0)) }
    var showDatePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp).navigationBarsPadding()) {
            Text(stringResource(R.string.run_scenario_title, scenarioName), style = MaterialTheme.typography.titleLarge)
            Text(
                stringResource(R.string.run_scenario_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            Text(stringResource(R.string.run_scenario_quick_pick), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            ScrollableChipRow(items = listOf(0, 15, 30, 60)) { minutesFromNow ->
                AssistChip(
                    onClick = {
                        val target = now.plusMinutes(minutesFromNow.toLong())
                        date = target.toLocalDate()
                        time = target.toLocalTime().withSecond(0).withNano(0)
                    },
                    label = {
                        Text(
                            if (minutesFromNow == 0) stringResource(R.string.run_scenario_now)
                            else stringResource(R.string.run_scenario_in_minutes, minutesFromNow)
                        )
                    }
                )
            }

            Spacer(Modifier.height(20.dp))
            Text(stringResource(R.string.label_date), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            OutlinedButton(onClick = { showDatePicker = true }) {
                Text(date.format(DateTimeFormatter.ofPattern("d MMMM yyyy")))
            }

            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.label_start_time), style = MaterialTheme.typography.titleMedium)
            WheelTimePicker(initial = time, onChange = { time = it })

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { onConfirm(LocalDateTime.of(date, time)) },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.run_scenario_action)) }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        date = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.common_done)) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.common_cancel)) } }
        ) { DatePicker(state = state) }
    }
}
