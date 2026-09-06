@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.uliana.myplanner.ui.planner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.uliana.myplanner.R
import com.uliana.myplanner.domain.TaskOccurrence
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")
private fun timeRange(occ: TaskOccurrence) = "${occ.start.format(timeFmt)}–${occ.end.format(timeFmt)}"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OccurrenceActionSheet(
    occurrence: TaskOccurrence,
    otherOccurrencesToday: List<TaskOccurrence>,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onSkipOnce: () -> Unit,
    onPause: (from: LocalDate, until: LocalDate?) -> Unit,
    onResume: () -> Unit,
    onStopForever: () -> Unit,
    onDeleteSeries: () -> Unit,
    onSwapWith: (TaskOccurrence) -> Unit
) {
    var showPauseOptions by remember { mutableStateOf(false) }
    var showSwapOptions by remember { mutableStateOf(false) }
    val isRepeating = occurrence.task.repeatRule.isRepeating
    val isPaused = occurrence.task.repeatRule.pausedFrom != null
    val swapCandidates = otherOccurrencesToday.filter {
        it.task.id != occurrence.task.id || it.originalStart != occurrence.originalStart
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp).navigationBarsPadding()) {
            Text(occurrence.task.title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            ActionRow(Icons.Filled.Edit, stringResource(R.string.action_edit_task), onEdit)

            if (swapCandidates.isNotEmpty()) {
                ActionRow(Icons.Filled.SwapHoriz, stringResource(R.string.action_swap_with), { showSwapOptions = !showSwapOptions })
                if (showSwapOptions) {
                    Column(Modifier.padding(start = 8.dp, bottom = 8.dp)) {
                        swapCandidates.forEach { other ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSwapWith(other) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Filled.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text(
                                    stringResource(R.string.action_swap_candidate, other.task.title, timeRange(other)),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            if (isRepeating) {
                ActionRow(Icons.Filled.EventBusy, stringResource(R.string.action_skip_once), onSkipOnce)
                if (!showPauseOptions && !isPaused) {
                    ActionRow(Icons.Filled.PauseCircle, stringResource(R.string.action_pause_repeat), { showPauseOptions = true })
                }
                if (showPauseOptions) {
                    Text(stringResource(R.string.action_pause_question), style = MaterialTheme.typography.labelSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                        listOf(1, 3, 7, 30).forEach { days ->
                            AssistChip(
                                onClick = {
                                    onPause(occurrence.start.toLocalDate(), occurrence.start.toLocalDate().plusDays(days.toLong()))
                                },
                                label = { Text(stringResource(R.string.action_pause_days, days)) }
                            )
                        }
                        AssistChip(
                            onClick = { onPause(occurrence.start.toLocalDate(), null) },
                            label = { Text(stringResource(R.string.action_pause_until_resumed)) }
                        )
                    }
                }
                if (isPaused) {
                    ActionRow(Icons.Filled.PlayCircle, stringResource(R.string.action_resume_repeat), onResume)
                }
                ActionRow(Icons.Filled.Block, stringResource(R.string.action_stop_repeat_forever), onStopForever)
            }

            ActionRow(Icons.Filled.Delete, stringResource(R.string.action_delete_series), onDeleteSeries, tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun ActionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit, tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(Modifier)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint)
        Text(label, color = tint)
    }
}
