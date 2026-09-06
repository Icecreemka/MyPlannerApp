@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.uliana.myplanner.ui.planner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uliana.myplanner.R
import com.uliana.myplanner.domain.TaskOccurrence
import com.uliana.myplanner.ui.components.FreeTimeBadge
import com.uliana.myplanner.ui.components.TimelineTaskBlock
import com.uliana.myplanner.ui.components.TreesGrownBadge
import com.uliana.myplanner.ui.rememberRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val dateHeaderFmt = DateTimeFormatter.ofPattern("d MMMM, EEEE")
private val HOUR_HEIGHT = 72.dp
private val LABEL_COLUMN_WIDTH = 52.dp
private val TIMELINE_LEFT_GAP = 8.dp
private val COLUMN_GAP = 4.dp

@Composable
fun rememberPlannerViewModel(): PlannerViewModel {
    val repo = rememberRepository()
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as android.app.Application
    val factory = remember {
        viewModelFactory { initializer { PlannerViewModel(app, repo) } }
    }
    return viewModel(factory = factory)
}

@Composable
fun DayScreen(
    viewModel: PlannerViewModel,
    onAddTask: (LocalDate) -> Unit,
    onEditTask: (Long) -> Unit
) {
    val date by viewModel.selectedDate.collectAsState()
    val occurrences by viewModel.dayOccurrences.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val freeMinutes by viewModel.freeMinutesToday.collectAsState()
    val treesGrown by viewModel.treesGrown.collectAsState()
    var actionMenuFor by remember { mutableStateOf<TaskOccurrence?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddTask(date) }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.day_fab_add_task_desc))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 10.dp, vertical = 6.dp)) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                tonalElevation = 3.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = { viewModel.shiftDate(-1) }) {
                            Icon(Icons.Filled.ChevronLeft, contentDescription = stringResource(R.string.day_prev_desc))
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { showDatePicker = true }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    date.format(dateHeaderFmt).replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(
                                    Icons.Filled.CalendarMonth,
                                    contentDescription = stringResource(R.string.day_open_calendar_desc),
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            TextButton(onClick = { viewModel.selectDate(LocalDate.now()) }) { Text(stringResource(R.string.common_today)) }
                        }
                        IconButton(onClick = { viewModel.shiftDate(1) }) {
                            Icon(Icons.Filled.ChevronRight, contentDescription = stringResource(R.string.day_next_desc))
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FreeTimeBadge(freeMinutes)
                        Spacer(Modifier.width(8.dp))
                        TreesGrownBadge(treesGrown)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Само небо остаётся видимым в этой области — читаемость обеспечивают
            // подписи часов и блоки дел, а не сплошная подложка на весь экран.
            // Выполненные дела прошедших дней уже "выросли" и посчитаны в общий счётчик —
            // на самом таймлайне прошлого дня они больше не показываются.
            val visibleOccurrences = remember(occurrences, date) {
                if (date.isBefore(LocalDate.now())) occurrences.filterNot { it.isCompleted && it.task.fromBacklog } else occurrences
            }
            DayTimeline(
                date = date,
                occurrences = visibleOccurrences,
                categoryColor = { catId -> categories.find { it.id == catId }?.colorHex },
                categoryName = { catId -> categories.find { it.id == catId }?.name },
                onEditTask = onEditTask,
                onLongPress = { actionMenuFor = it },
                onToggleComplete = { viewModel.toggleComplete(it) },
                onMoveOccurrence = { occurrence, newStart -> viewModel.moveOccurrence(occurrence, newStart) }
            )
        }
    }

    actionMenuFor?.let { occ ->
        OccurrenceActionSheet(
            occurrence = occ,
            otherOccurrencesToday = occurrences,
            onDismiss = { actionMenuFor = null },
            onEdit = { onEditTask(occ.task.id); actionMenuFor = null },
            onSkipOnce = { viewModel.skipOccurrence(occ); actionMenuFor = null },
            onPause = { from, until -> viewModel.pauseRepeatTemporarily(occ, from, until); actionMenuFor = null },
            onResume = { viewModel.resumeRepeat(occ); actionMenuFor = null },
            onStopForever = { viewModel.stopRepeatForever(occ, occ.start.toLocalDate()); actionMenuFor = null },
            onDeleteSeries = { viewModel.deleteSeries(occ); actionMenuFor = null },
            onSwapWith = { other -> viewModel.swapOccurrences(occ, other); actionMenuFor = null }
        )
    }

    if (showDatePicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        val newDate = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        viewModel.selectDate(newDate)
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.day_go_action)) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.common_cancel)) } }
        ) { DatePicker(state = pickerState) }
    }
}

/** Дело с рассчитанной колонкой: если дела накладываются по времени, они делят ширину пополам (или на N частей). */
private data class LayoutedOccurrence(val occurrence: TaskOccurrence, val column: Int, val columnCount: Int)

/**
 * Раскладывает дела по колонкам: пересекающиеся по времени дела автоматически становятся
 * в соседние колонки (половина ширины на двоих, треть на троих и т.д.), не пересекающиеся
 * по времени — занимают всю ширину. Благодаря этому перетаскивание одного дела поверх
 * времени другого сразу показывает оба рядом, без ручной настройки места.
 */
private fun layoutOccurrences(occurrences: List<TaskOccurrence>): List<LayoutedOccurrence> {
    val sorted = occurrences.sortedBy { it.start }
    val result = mutableListOf<LayoutedOccurrence>()

    var clusterItems = mutableListOf<TaskOccurrence>()
    var clusterColumnEnds = mutableListOf<LocalDateTime>()
    var clusterColumnOf = mutableMapOf<TaskOccurrence, Int>()
    var clusterMaxEnd: LocalDateTime? = null

    fun flushCluster() {
        val columnCount = clusterColumnEnds.size.coerceAtLeast(1)
        clusterItems.forEach { occ -> result.add(LayoutedOccurrence(occ, clusterColumnOf[occ] ?: 0, columnCount)) }
        clusterItems = mutableListOf()
        clusterColumnEnds = mutableListOf()
        clusterColumnOf = mutableMapOf()
        clusterMaxEnd = null
    }

    for (occ in sorted) {
        val currentClusterEnd = clusterMaxEnd
        if (currentClusterEnd != null && !occ.start.isBefore(currentClusterEnd)) {
            flushCluster()
        }
        var placedColumn = -1
        for (i in clusterColumnEnds.indices) {
            if (!occ.start.isBefore(clusterColumnEnds[i])) {
                placedColumn = i
                clusterColumnEnds[i] = occ.end
                break
            }
        }
        if (placedColumn == -1) {
            placedColumn = clusterColumnEnds.size
            clusterColumnEnds.add(occ.end)
        }
        clusterColumnOf[occ] = placedColumn
        clusterItems.add(occ)
        val previousMax = clusterMaxEnd
        clusterMaxEnd = if (previousMax == null) occ.end else maxOf(previousMax, occ.end)
    }
    flushCluster()
    return result
}

@Composable
private fun DayTimeline(
    date: LocalDate,
    occurrences: List<TaskOccurrence>,
    categoryColor: (Long?) -> String?,
    categoryName: (Long?) -> String?,
    onEditTask: (Long) -> Unit,
    onLongPress: (TaskOccurrence) -> Unit,
    onToggleComplete: (TaskOccurrence) -> Unit,
    onMoveOccurrence: (TaskOccurrence, LocalDateTime) -> Unit
) {
    val density = LocalDensity.current
    val hourHeightPx = with(density) { HOUR_HEIGHT.toPx() }
    val pxPerMinute = hourHeightPx / 60f
    val scrollState = rememberScrollState()
    val totalHeight = HOUR_HEIGHT * 24
    val layouted = remember(occurrences) { layoutOccurrences(occurrences) }
    val scope = rememberCoroutineScope()
    val edgeZonePx = with(density) { 56.dp.toPx() }
    val maxAutoScrollStepPx = with(density) { 14.dp.toPx() }

    LaunchedEffect(date) {
        val targetHour = if (date == LocalDate.now()) LocalDateTime.now().hour else 8
        scrollState.scrollTo((targetHour * hourHeightPx - hourHeightPx * 1.5f).toInt().coerceAtLeast(0))
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val fullWidth = maxWidth
        val viewportHeightPx = with(density) { maxHeight.toPx() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(totalHeight)) {
                // Сетка часов
                Column(Modifier.fillMaxWidth()) {
                    for (hour in 0..23) {
                        Row(Modifier.fillMaxWidth().height(HOUR_HEIGHT)) {
                            Box(
                                modifier = Modifier.width(LABEL_COLUMN_WIDTH).fillMaxHeight(),
                                contentAlignment = Alignment.TopEnd
                            ) {
                                Surface(
                                    modifier = Modifier.padding(top = 2.dp, end = 6.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                                ) {
                                    Text(
                                        text = String.format("%02d:00", hour),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Box(Modifier.weight(1f).fillMaxHeight()) {
                                HorizontalDivider(
                                    modifier = Modifier.align(Alignment.TopStart).fillMaxWidth(),
                                    thickness = 1.dp,
                                    color = Color.Gray.copy(alpha = 0.28f)
                                )
                            }
                        }
                    }
                }

                // Линия текущего времени
                if (date == LocalDate.now()) {
                    val now = LocalDateTime.now()
                    val nowMinutes = now.hour * 60 + now.minute
                    val nowOffsetDp = with(density) { (nowMinutes * pxPerMinute).toDp() }
                    HorizontalDivider(
                        modifier = Modifier
                            .padding(start = LABEL_COLUMN_WIDTH)
                            .offset(y = nowOffsetDp)
                            .fillMaxWidth(),
                        thickness = 2.dp,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // Дела поверх сетки: каждое в своей колонке (если накладывается по времени — рядом с другими)
                val taskAreaWidth = fullWidth - LABEL_COLUMN_WIDTH - TIMELINE_LEFT_GAP
                layouted.forEach { item ->
                    val occ = item.occurrence
                    var dragDeltaMinutes by remember(occ.task.id, occ.originalStart) { mutableStateOf(0f) }
                    val startMinutes = occ.start.hour * 60 + occ.start.minute
                    val durationMinutes = occ.durationMinutes.coerceAtLeast(15)
                    val blockHeight = with(density) { (durationMinutes * pxPerMinute).toDp() }.coerceAtLeast(30.dp)
                    val baseOffsetY = with(density) { (startMinutes * pxPerMinute).toDp() }
                    val dragOffsetY = with(density) { (dragDeltaMinutes * pxPerMinute).toDp() }
                    val safeColumnCount = item.columnCount.coerceAtLeast(1)
                    val blockWidth = ((taskAreaWidth - COLUMN_GAP * (safeColumnCount - 1)) / safeColumnCount)
                        .coerceAtLeast(48.dp)
                    val startX = (LABEL_COLUMN_WIDTH + TIMELINE_LEFT_GAP + (blockWidth + COLUMN_GAP) * item.column)
                        .coerceAtMost(fullWidth - blockWidth).coerceAtLeast(LABEL_COLUMN_WIDTH + TIMELINE_LEFT_GAP)

                    TimelineTaskBlock(
                        occurrence = occ,
                        categoryColor = categoryColor(occ.task.categoryId)?.let {
                            Color(android.graphics.Color.parseColor(it))
                        } ?: MaterialTheme.colorScheme.primary,
                        categoryName = categoryName(occ.task.categoryId),
                        compact = blockHeight < 46.dp,
                        onClick = { onEditTask(occ.task.id) },
                        onLongPress = { onLongPress(occ) },
                        onToggleComplete = { onToggleComplete(occ) },
                        dragHandleModifier = Modifier.pointerInput(occ.task.id, occ.originalStart) {
                            detectDragGestures(
                                onDragEnd = {
                                    if (dragDeltaMinutes != 0f) {
                                        val snapped = (dragDeltaMinutes / 5f).let { kotlin.math.round(it) * 5 }
                                        if (snapped != 0f) {
                                            val newStart = occ.start.plusMinutes(snapped.toLong())
                                            onMoveOccurrence(occ, newStart)
                                        }
                                    }
                                    dragDeltaMinutes = 0f
                                },
                                onDragCancel = { dragDeltaMinutes = 0f }
                            ) { change, dragAmount ->
                                change.consume()
                                dragDeltaMinutes += dragAmount.y / pxPerMinute

                                // Автопрокрутка: если тянем дело к верхнему или нижнему краю
                                // видимой области, сама область прокручивается, а дело
                                // продолжает двигаться дальше по времени вместе с ней.
                                val itemTopPx = (startMinutes + dragDeltaMinutes) * pxPerMinute
                                val itemBottomPx = itemTopPx + durationMinutes * pxPerMinute
                                val viewportTopPx = scrollState.value.toFloat()
                                val viewportBottomPx = viewportTopPx + viewportHeightPx

                                val distanceFromTop = itemTopPx - viewportTopPx
                                val distanceFromBottom = viewportBottomPx - itemBottomPx

                                val scrollDelta = when {
                                    distanceFromTop < edgeZonePx && scrollState.value > 0 -> {
                                        val strength = ((edgeZonePx - distanceFromTop) / edgeZonePx).coerceIn(0f, 1f)
                                        -maxAutoScrollStepPx * strength
                                    }
                                    distanceFromBottom < edgeZonePx && scrollState.value < scrollState.maxValue -> {
                                        val strength = ((edgeZonePx - distanceFromBottom) / edgeZonePx).coerceIn(0f, 1f)
                                        maxAutoScrollStepPx * strength
                                    }
                                    else -> 0f
                                }
                                if (scrollDelta != 0f) {
                                    dragDeltaMinutes += scrollDelta / pxPerMinute
                                    scope.launch { scrollState.scrollBy(scrollDelta) }
                                }
                            }
                        },
                        modifier = Modifier
                            .padding(start = startX)
                            .offset(y = baseOffsetY + dragOffsetY)
                            .width(blockWidth)
                            .height(blockHeight)
                    )
                }
            }
        }
    }
}
