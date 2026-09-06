@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class, androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.uliana.myplanner.ui.backlog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uliana.myplanner.R
import com.uliana.myplanner.data.BacklogTaskEntity
import com.uliana.myplanner.data.Category
import com.uliana.myplanner.ui.components.ScrollableChipRow
import com.uliana.myplanner.ui.components.WheelDurationPicker
import com.uliana.myplanner.ui.components.WheelTimePicker
import com.uliana.myplanner.ui.components.iconFor
import com.uliana.myplanner.ui.rememberRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
private fun rememberBacklogViewModel(): BacklogViewModel {
    val repo = rememberRepository()
    val app = LocalContext.current.applicationContext as android.app.Application
    val factory = remember { viewModelFactory { initializer { BacklogViewModel(app, repo) } } }
    return viewModel(factory = factory)
}

@Composable
fun BacklogScreen() {
    val viewModel = rememberBacklogViewModel()
    val tasks by viewModel.backlogTasks.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var showAddSheet by remember { mutableStateOf(false) }
    var scheduleTarget by remember { mutableStateOf<BacklogTaskEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSheet = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.backlog_fab_add_desc))
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text(stringResource(R.string.backlog_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    stringResource(R.string.backlog_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (tasks.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Grain, contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            stringResource(R.string.backlog_empty_title),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 40.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalItemSpacing = 12.dp,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        val category = categories.find { it.id == task.categoryId }
                        SeedCard(
                            task = task,
                            category = category,
                            onPlant = { scheduleTarget = task },
                            onDelete = { viewModel.deleteTask(task) }
                        )
                    }
                }
            }
        }
    }

    if (showAddSheet) {
        AddBacklogTaskSheet(
            categories = categories,
            onDismiss = { showAddSheet = false },
            onConfirm = { title, description, categoryId, duration ->
                viewModel.addTask(title, description, categoryId, duration)
                showAddSheet = false
            }
        )
    }

    scheduleTarget?.let { task ->
        SendToScheduleSheet(
            task = task,
            onDismiss = { scheduleTarget = null },
            onConfirm = { start, duration ->
                viewModel.sendToSchedule(task, start, duration) { scheduleTarget = null }
            }
        )
    }
}

@Composable
private fun SeedCard(
    task: BacklogTaskEntity,
    category: Category?,
    onPlant: () -> Unit,
    onDelete: () -> Unit
) {
    val accentColor = category?.let { Color(android.graphics.Color.parseColor(it.colorHex)) }
        ?: MaterialTheme.colorScheme.primary

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
        color = androidx.compose.ui.graphics.lerp(MaterialTheme.colorScheme.surface, accentColor, 0.10f),
        tonalElevation = 2.dp
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(26.dp).clip(CircleShape).background(accentColor.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = category?.let { iconFor(it.icon) } ?: Icons.Filled.Grain,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = accentColor
                        )
                    }
                    if (category != null) {
                        Spacer(Modifier.width(8.dp))
                        Text(category.name, style = MaterialTheme.typography.labelSmall, color = accentColor)
                    }
                }
                Icon(
                    imageVector = Icons.Filled.Grain,
                    contentDescription = stringResource(R.string.backlog_seed_not_planted_desc),
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                task.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (task.description.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilledTonalButton(onClick = onPlant, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                    Icon(Icons.Filled.Grass, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.backlog_action_add_to_plan), style = MaterialTheme.typography.labelSmall)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Filled.DeleteOutline, contentDescription = stringResource(R.string.common_delete), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun AddBacklogTaskSheet(
    categories: List<Category>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String, categoryId: Long?, durationMinutes: Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf<Long?>(null) }
    var duration by remember { mutableStateOf(30L) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp).navigationBarsPadding()) {
            Text(stringResource(R.string.backlog_add_task_title), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text(stringResource(R.string.backlog_label_what_to_do)) }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = description, onValueChange = { description = it },
                label = { Text(stringResource(R.string.label_note_optional)) }, modifier = Modifier.fillMaxWidth(), minLines = 2
            )
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.label_category), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            ScrollableChipRow(items = categories) { cat ->
                FilterChip(
                    selected = categoryId == cat.id,
                    onClick = { categoryId = if (categoryId == cat.id) null else cat.id },
                    label = { Text(cat.name) }
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.backlog_label_usual_duration), style = MaterialTheme.typography.titleMedium)
            WheelDurationPicker(initialMinutes = duration, onChange = { duration = it })
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { onConfirm(title, description, categoryId, duration) },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank()
            ) { Text(stringResource(R.string.backlog_action_add_to_list)) }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SendToScheduleSheet(
    task: BacklogTaskEntity,
    onDismiss: () -> Unit,
    onConfirm: (start: LocalDateTime, durationMinutes: Long) -> Unit
) {
    var date by remember { mutableStateOf(LocalDate.now()) }
    var time by remember { mutableStateOf(LocalTime.now().withSecond(0).withNano(0)) }
    var duration by remember { mutableStateOf(task.defaultDurationMinutes) }
    var showDatePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.padding(horizontal = 20.dp, vertical = 8.dp).navigationBarsPadding().verticalScroll(rememberScrollState())
        ) {
            Text(stringResource(R.string.backlog_plant_title), style = MaterialTheme.typography.titleLarge)
            Text(task.title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))

            Text(stringResource(R.string.label_date), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            OutlinedButton(onClick = { showDatePicker = true }) {
                Text(date.format(DateTimeFormatter.ofPattern("d MMMM yyyy")))
            }

            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.label_start_time), style = MaterialTheme.typography.titleMedium)
            WheelTimePicker(initial = time, onChange = { time = it })

            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.label_duration), style = MaterialTheme.typography.titleMedium)
            WheelDurationPicker(initialMinutes = duration, onChange = { duration = it })

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { onConfirm(LocalDateTime.of(date, time), duration) },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.backlog_action_plant_to_day)) }
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
