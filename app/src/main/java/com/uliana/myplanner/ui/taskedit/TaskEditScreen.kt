@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.uliana.myplanner.ui.taskedit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uliana.myplanner.R
import com.uliana.myplanner.data.*
import com.uliana.myplanner.notifications.ReminderScheduler
import com.uliana.myplanner.ui.components.ScrollableChipRow
import com.uliana.myplanner.ui.components.WheelDurationPicker
import com.uliana.myplanner.ui.components.WheelTimePicker
import com.uliana.myplanner.ui.components.iconFor
import com.uliana.myplanner.ui.rememberRepository
import com.uliana.myplanner.ui.scenario.RunScenarioSheet
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val REMINDER_PRESETS = listOf(5, 10, 15, 30, 60, 120)

@Composable
fun TaskEditScreen(
    taskId: Long?,
    initialDate: LocalDate?,
    onBack: () -> Unit
) {
    val repo = rememberRepository()
    val app = LocalContext.current.applicationContext as android.app.Application
    val factory = remember { viewModelFactory { initializer { TaskEditViewModel(app, repo, taskId, initialDate) } } }
    val viewModel: TaskEditViewModel = viewModel(factory = factory)
    val scope = rememberCoroutineScope()

    val state by viewModel.state.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val scenarios by repo.scenarios.collectAsState(initial = emptyList())

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    var showDurationPicker by remember { mutableStateOf(false) }
    var showRepeatSheet by remember { mutableStateOf(false) }
    var dismissedScenarioSuggestionId by remember { mutableStateOf<Long?>(null) }
    var scenarioToRun by remember { mutableStateOf<ScenarioWithSteps?>(null) }

    val matchingScenario = remember(state.title, scenarios) {
        val query = state.title.trim()
        if (query.length < 3) null
        else scenarios.find { it.scenario.name.trim().startsWith(query, ignoreCase = true) }
    }
    val showSuggestion = state.isNew && matchingScenario != null && matchingScenario.scenario.id != dismissedScenarioSuggestionId

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isNew) stringResource(R.string.task_edit_new_title) else stringResource(R.string.task_edit_title)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back)) } },
                actions = {
                    if (!state.isNew) {
                        IconButton(onClick = { viewModel.delete(onBack) }) { Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.common_delete)) }
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = { viewModel.save(onBack) },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    enabled = state.title.isNotBlank() && !state.isSaving
                ) { Text(stringResource(R.string.common_save)) }
            }
        }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(horizontal = 20.dp).verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.title, onValueChange = viewModel::updateTitle,
                label = { Text(stringResource(R.string.task_edit_title_label)) }, modifier = Modifier.fillMaxWidth()
            )

            if (showSuggestion && matchingScenario != null) {
                Spacer(Modifier.height(10.dp))
                ScenarioSuggestionCard(
                    scenarioName = matchingScenario.scenario.name,
                    onRun = { scenarioToRun = matchingScenario },
                    onDismiss = { dismissedScenarioSuggestionId = matchingScenario.scenario.id }
                )
            }

            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = state.description, onValueChange = viewModel::updateDescription,
                label = { Text(stringResource(R.string.label_note_optional)) }, modifier = Modifier.fillMaxWidth(), minLines = 2
            )

            Spacer(Modifier.height(18.dp))
            Text(stringResource(R.string.label_category), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            ScrollableChipRow(items = categories) { cat ->
                FilterChip(
                    selected = state.categoryId == cat.id,
                    onClick = { viewModel.updateCategory(if (state.categoryId == cat.id) null else cat.id) },
                    label = { Text(cat.name) },
                    leadingIcon = { Icon(iconFor(cat.icon), null, tint = Color(android.graphics.Color.parseColor(cat.colorHex))) }
                )
            }

            Spacer(Modifier.height(18.dp))
            Text(stringResource(R.string.label_date), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            DatePickerField(date = state.date, onDateChange = viewModel::updateDate)

            Spacer(Modifier.height(18.dp))
            Text(stringResource(R.string.task_edit_time_label), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TimeField(stringResource(R.string.task_edit_start_label), state.startTime) { showStartPicker = true }
                TimeField(stringResource(R.string.task_edit_end_label), state.endTime) { showEndPicker = true }
            }
            Spacer(Modifier.height(8.dp))
            DurationField(minutes = state.durationMinutes, onClick = { showDurationPicker = true })

            Spacer(Modifier.height(18.dp))
            Text(stringResource(R.string.task_edit_repeat_label), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            OutlinedButton(onClick = { showRepeatSheet = true }, modifier = Modifier.fillMaxWidth()) {
                Text(repeatSummary(state.repeatRule))
            }

            Spacer(Modifier.height(18.dp))
            Text(stringResource(R.string.task_edit_reminder_label), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            ReminderPicker(state.reminderMinutesBefore, viewModel::updateReminder)

            Spacer(Modifier.height(24.dp))
        }
    }

    if (showStartPicker) {
        TimePickerDialogWheel(
            initial = state.startTime,
            title = stringResource(R.string.task_edit_start_label),
            onDismiss = { showStartPicker = false },
            onConfirm = { viewModel.updateStartTime(it); showStartPicker = false }
        )
    }
    if (showEndPicker) {
        TimePickerDialogWheel(
            initial = state.endTime,
            title = stringResource(R.string.task_edit_end_label),
            onDismiss = { showEndPicker = false },
            onConfirm = { viewModel.updateEndTime(it); showEndPicker = false }
        )
    }
    if (showDurationPicker) {
        DurationPickerDialog(
            initialMinutes = state.durationMinutes,
            onDismiss = { showDurationPicker = false },
            onConfirm = { viewModel.updateDuration(it); showDurationPicker = false }
        )
    }
    if (showRepeatSheet) {
        RepeatRuleSheet(
            initial = state.repeatRule,
            onDismiss = { showRepeatSheet = false },
            onConfirm = { viewModel.updateRepeatRule(it); showRepeatSheet = false }
        )
    }

    scenarioToRun?.let { scenario ->
        RunScenarioSheet(
            scenarioName = scenario.scenario.name,
            onDismiss = { scenarioToRun = null },
            onConfirm = { startAt ->
                scope.launch {
                    val ids = repo.runScenario(scenario.scenario.id, startAt)
                    ids.forEach { id ->
                        val task = AppDatabase.getInstance(app).taskDao().getById(id)
                        if (task != null) ReminderScheduler.scheduleNextForTask(app, task)
                    }
                    scenarioToRun = null
                    onBack()
                }
            }
        )
    }
}

@Composable
private fun ScenarioSuggestionCard(scenarioName: String, onRun: () -> Unit, onDismiss: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.scenario_suggestion_title, scenarioName),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    stringResource(R.string.scenario_suggestion_question),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onRun, contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)) {
                        Text(stringResource(R.string.scenario_suggestion_run), style = MaterialTheme.typography.labelSmall)
                    }
                    TextButton(onClick = onDismiss, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)) {
                        Text(stringResource(R.string.scenario_suggestion_dismiss), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeField(label: String, time: LocalTime, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick) {
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(time.format(DateTimeFormatter.ofPattern("HH:mm")), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun DurationField(minutes: Long, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            Text(stringResource(R.string.label_duration), style = MaterialTheme.typography.labelSmall)
            Text("${minutes / 60} ${stringResource(R.string.unit_hours)} ${minutes % 60} ${stringResource(R.string.unit_minutes)}", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun ReminderPicker(current: Int?, onChange: (Int?) -> Unit) {
    var customText by remember(current) {
        mutableStateOf(if (current != null && current !in REMINDER_PRESETS) current.toString() else "")
    }
    var showCustomField by remember(current) { mutableStateOf(current != null && current !in REMINDER_PRESETS) }
    val noReminderLabel = stringResource(R.string.reminder_none)

    ScrollableChipRow(items = listOf<Int?>(null) + REMINDER_PRESETS) { minutes ->
        FilterChip(
            selected = current == minutes,
            onClick = { showCustomField = false; onChange(minutes) },
            label = { Text(if (minutes == null) noReminderLabel else stringResource(R.string.minutes_suffix, minutes)) }
        )
    }
    Spacer(Modifier.height(8.dp))
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = showCustomField,
            onClick = { showCustomField = true },
            label = { Text(stringResource(R.string.minutes_custom_option)) }
        )
        if (showCustomField) {
            OutlinedTextField(
                value = customText,
                onValueChange = { text ->
                    val digits = text.filter { it.isDigit() }.take(4)
                    customText = digits
                    digits.toIntOrNull()?.let { onChange(it) }
                },
                label = { Text(stringResource(R.string.reminder_before_start_label)) },
                modifier = Modifier.width(160.dp),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
            )
        }
    }
}

@Composable
private fun DatePickerField(date: LocalDate, onDateChange: (LocalDate) -> Unit) {
    var show by remember { mutableStateOf(false) }
    OutlinedButton(onClick = { show = true }) {
        Text(date.format(DateTimeFormatter.ofPattern("d MMMM yyyy")))
    }
    if (show) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { show = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        val newDate = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        onDateChange(newDate)
                    }
                    show = false
                }) { Text(stringResource(R.string.common_done)) }
            },
            dismissButton = { TextButton(onClick = { show = false }) { Text(stringResource(R.string.common_cancel)) } }
        ) { DatePicker(state = state) }
    }
}

@Composable
private fun TimePickerDialogWheel(initial: LocalTime, title: String, onDismiss: () -> Unit, onConfirm: (LocalTime) -> Unit) {
    var picked by remember(initial) { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        confirmButton = { TextButton(onClick = { onConfirm(picked) }) { Text(stringResource(R.string.common_done)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } },
        text = { WheelTimePicker(initial = initial, onChange = { picked = it }) }
    )
}

@Composable
private fun DurationPickerDialog(initialMinutes: Long, onDismiss: () -> Unit, onConfirm: (Long) -> Unit) {
    var picked by remember(initialMinutes) { mutableStateOf(initialMinutes) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.label_duration)) },
        confirmButton = { TextButton(onClick = { onConfirm(picked) }) { Text(stringResource(R.string.common_done)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } },
        text = { WheelDurationPicker(initialMinutes = initialMinutes, onChange = { picked = it }) }
    )
}

@Composable
private fun repeatSummary(rule: RepeatRule): String = when (rule.type) {
    RepeatType.NONE -> stringResource(R.string.repeat_none)
    RepeatType.INTERVAL -> stringResource(R.string.repeat_every, rule.intervalAmount, unitName(rule.intervalUnit))
    RepeatType.WEEKLY -> stringResource(R.string.repeat_weekly, rule.daysOfWeek.joinToString { it.name.take(2) })
    RepeatType.MONTHLY -> stringResource(R.string.repeat_monthly)
    RepeatType.YEARLY -> stringResource(R.string.repeat_yearly)
}

@Composable
private fun unitName(u: IntervalUnit) = when (u) {
    IntervalUnit.MINUTES -> stringResource(R.string.unit_minutes)
    IntervalUnit.HOURS -> stringResource(R.string.unit_hours)
    IntervalUnit.DAYS -> stringResource(R.string.unit_days)
    IntervalUnit.WEEKS -> stringResource(R.string.unit_weeks)
}
