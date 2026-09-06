@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.uliana.myplanner.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uliana.myplanner.R
import com.uliana.myplanner.ui.components.ScrollableChipRow
import com.uliana.myplanner.ui.components.WheelTimePicker
import com.uliana.myplanner.ui.rememberRepository
import com.uliana.myplanner.ui.theme.CategoryPalette
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val DEFAULT_REMINDER_PRESETS = listOf(5, 10, 15, 30, 60)

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val repo = rememberRepository()
    val app = LocalContext.current.applicationContext as android.app.Application
    val factory = remember { viewModelFactory { initializer { SettingsViewModel(app, repo) } } }
    val viewModel: SettingsViewModel = viewModel(factory = factory)

    val sleep by viewModel.sleepSchedule.collectAsState()
    val defaultReminder by viewModel.defaultReminderMinutes.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryColor by remember { mutableStateOf(CategoryPalette.first()) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.settings_title)) }, navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back)) } })
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(horizontal = 20.dp).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.settings_sleep_title), style = MaterialTheme.typography.titleLarge)
            Text(
                stringResource(R.string.settings_sleep_desc),
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.settings_sleep_toggle))
                Switch(checked = sleep.enabled, onCheckedChange = { viewModel.updateSleepSchedule(sleep.copy(enabled = it)) })
            }
            if (sleep.enabled) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { showStartPicker = true }) {
                        Column { Text(stringResource(R.string.settings_sleep_start_label), style = MaterialTheme.typography.labelSmall); Text(sleep.sleepStart.format(DateTimeFormatter.ofPattern("HH:mm"))) }
                    }
                    OutlinedButton(onClick = { showEndPicker = true }) {
                        Column { Text(stringResource(R.string.settings_sleep_end_label), style = MaterialTheme.typography.labelSmall); Text(sleep.sleepEnd.format(DateTimeFormatter.ofPattern("HH:mm"))) }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.settings_reminders_title), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            DefaultReminderPicker(current = defaultReminder, onChange = viewModel::updateDefaultReminder)

            Spacer(Modifier.height(24.dp))
            Text(stringResource(R.string.settings_categories_title), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            categories.forEach { cat ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(14.dp).clip(CircleShape).background(Color(android.graphics.Color.parseColor(cat.colorHex))))
                        Spacer(Modifier.width(10.dp))
                        Text(cat.name)
                    }
                    IconButton(onClick = { viewModel.deleteCategory(cat) }) { Icon(Icons.Filled.DeleteOutline, contentDescription = stringResource(R.string.settings_delete_category_desc)) }
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = newCategoryName, onValueChange = { newCategoryName = it },
                label = { Text(stringResource(R.string.settings_new_category_label)) }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoryPalette.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(
                                if (color == newCategoryColor)
                                    Modifier.border(2.dp, Color.Black, CircleShape)
                                else Modifier
                            )
                            .clickable { newCategoryColor = color }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    if (newCategoryName.isNotBlank()) {
                        val hex = String.format("#%06X", 0xFFFFFF and newCategoryColor.toArgb())
                        viewModel.addCategory(newCategoryName, hex, "leaf")
                        newCategoryName = ""
                    }
                },
                enabled = newCategoryName.isNotBlank()
            ) { Text(stringResource(R.string.settings_add_category_action)) }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showStartPicker) {
        SleepTimeDialog(sleep.sleepStart, title = stringResource(R.string.settings_sleep_start_title), onDismiss = { showStartPicker = false }) {
            viewModel.updateSleepSchedule(sleep.copy(sleepStart = it)); showStartPicker = false
        }
    }
    if (showEndPicker) {
        SleepTimeDialog(sleep.sleepEnd, title = stringResource(R.string.settings_sleep_end_title), onDismiss = { showEndPicker = false }) {
            viewModel.updateSleepSchedule(sleep.copy(sleepEnd = it)); showEndPicker = false
        }
    }
}

@Composable
private fun DefaultReminderPicker(current: Int, onChange: (Int) -> Unit) {
    var customText by remember(current) {
        mutableStateOf(if (current !in DEFAULT_REMINDER_PRESETS) current.toString() else "")
    }
    var showCustomField by remember(current) { mutableStateOf(current !in DEFAULT_REMINDER_PRESETS) }

    ScrollableChipRow(items = DEFAULT_REMINDER_PRESETS) { minutes ->
        FilterChip(
            selected = current == minutes,
            onClick = { showCustomField = false; onChange(minutes) },
            label = { Text(stringResource(R.string.minutes_suffix, minutes)) }
        )
    }
    Spacer(Modifier.height(8.dp))
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = showCustomField, onClick = { showCustomField = true }, label = { Text(stringResource(R.string.minutes_custom_option)) })
        if (showCustomField) {
            OutlinedTextField(
                value = customText,
                onValueChange = { text ->
                    val digits = text.filter { it.isDigit() }.take(4)
                    customText = digits
                    digits.toIntOrNull()?.let { if (it > 0) onChange(it) }
                },
                label = { Text(stringResource(R.string.minutes_label)) },
                modifier = Modifier.width(140.dp),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
            )
        }
    }
}

@Composable
private fun SleepTimeDialog(initial: LocalTime, title: String, onDismiss: () -> Unit, onConfirm: (LocalTime) -> Unit) {
    var picked by remember(initial) { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        confirmButton = { TextButton(onClick = { onConfirm(picked) }) { Text(stringResource(R.string.common_done)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } },
        text = { WheelTimePicker(initial = initial, onChange = { picked = it }) }
    )
}
