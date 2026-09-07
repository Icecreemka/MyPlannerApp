@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.uliana.myplanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uliana.myplanner.R
import com.uliana.myplanner.domain.TaskOccurrence
import java.time.format.DateTimeFormatter

private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

private fun completionIcon(occurrence: TaskOccurrence): androidx.compose.ui.graphics.vector.ImageVector =
    if (occurrence.task.fromBacklog) {
        if (occurrence.isCompleted) Icons.Filled.Park else Icons.Filled.Grass
    } else {
        if (occurrence.isCompleted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked
    }

@Composable
fun FreeTimeBadge(freeMinutes: Long, modifier: Modifier = Modifier) {
    val hours = freeMinutes / 60
    val minutes = freeMinutes % 60
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            Icons.Filled.Eco, contentDescription = null,
            modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = stringResource(R.string.free_time_badge, hours, minutes),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun TreesGrownBadge(count: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            Icons.Filled.Park, contentDescription = stringResource(R.string.trees_grown_badge_desc),
            modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun TimelineTaskBlock(
    occurrence: TaskOccurrence,
    categoryColor: Color,
    categoryName: String?,
    compact: Boolean,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    onToggleComplete: () -> Unit,
    dragHandleModifier: Modifier = Modifier,
    modifier: Modifier = Modifier
) {
    val completedDesc = stringResource(R.string.task_block_completed_desc)
    val markCompleteDesc = stringResource(R.string.task_block_mark_complete_desc)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = androidx.compose.ui.graphics.lerp(MaterialTheme.colorScheme.surface, categoryColor, 0.22f),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, categoryColor.copy(alpha = 0.7f))
    ) {
        Row(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(22.dp)
                    .then(dragHandleModifier),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.DragIndicator,
                    contentDescription = stringResource(R.string.task_block_drag_desc),
                    modifier = Modifier.size(16.dp),
                    tint = categoryColor.copy(alpha = 0.8f)
                )
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .combinedClickable(onClick = onClick, onLongClick = onLongPress)
                    .padding(end = 8.dp, top = if (compact) 2.dp else 6.dp, bottom = if (compact) 2.dp else 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(categoryColor)
                )
                Spacer(Modifier.width(6.dp))

                if (compact) {

                    Text(
                        text = occurrence.task.title,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (occurrence.isCompleted) TextDecoration.LineThrough else null,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = occurrence.start.format(timeFmt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Clip
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = completionIcon(occurrence),
                        contentDescription = if (occurrence.isCompleted) completedDesc else markCompleteDesc,
                        tint = if (occurrence.isCompleted) categoryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        modifier = Modifier
                            .size(14.dp)
                            .clickable(onClick = onToggleComplete)
                    )
                } else {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = occurrence.task.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textDecoration = if (occurrence.isCompleted) TextDecoration.LineThrough else null
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${occurrence.start.format(timeFmt)}–${occurrence.end.format(timeFmt)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (occurrence.task.repeatRule.isRepeating) {
                                Icon(
                                    Icons.Filled.Repeat,
                                    contentDescription = stringResource(R.string.task_block_repeating_desc),
                                    modifier = Modifier.size(11.dp),
                                    tint = categoryColor
                                )
                            }
                            if (categoryName != null) {
                                Text(categoryName, style = MaterialTheme.typography.labelSmall, color = categoryColor)
                            }
                        }
                    }
                    Icon(
                        imageVector = completionIcon(occurrence),
                        contentDescription = if (occurrence.isCompleted) completedDesc else markCompleteDesc,
                        tint = if (occurrence.isCompleted) categoryColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(onClick = onToggleComplete)
                    )
                }
            }
        }
    }
}
