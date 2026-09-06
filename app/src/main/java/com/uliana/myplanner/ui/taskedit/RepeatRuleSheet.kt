@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.uliana.myplanner.ui.taskedit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.uliana.myplanner.R
import com.uliana.myplanner.data.IntervalUnit
import com.uliana.myplanner.data.RepeatEndType
import com.uliana.myplanner.data.RepeatRule
import com.uliana.myplanner.data.RepeatType
import com.uliana.myplanner.ui.components.ScrollableChipRow
import java.time.DayOfWeek

@Composable
fun RepeatRuleSheet(initial: RepeatRule, onDismiss: () -> Unit, onConfirm: (RepeatRule) -> Unit) {
    var rule by remember { mutableStateOf(initial) }
    val dayNames = stringArrayResource(R.array.day_of_week_short)
    val context = LocalContext.current

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(20.dp).navigationBarsPadding()) {
            Text(stringResource(R.string.repeat_sheet_title), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))

            val repeatTypeOptions = listOf(
                RepeatType.NONE to stringResource(R.string.repeat_type_none),
                RepeatType.INTERVAL to stringResource(R.string.repeat_type_interval),
                RepeatType.WEEKLY to stringResource(R.string.repeat_type_weekly),
                RepeatType.MONTHLY to stringResource(R.string.repeat_type_monthly),
                RepeatType.YEARLY to stringResource(R.string.repeat_type_yearly)
            )
            ScrollableChipRow(items = repeatTypeOptions) { (type, label) ->
                FilterChip(selected = rule.type == type, onClick = { rule = rule.copy(type = type) }, label = { Text(label) })
            }

            if (rule.type == RepeatType.INTERVAL) {
                Spacer(Modifier.height(14.dp))
                Text(stringResource(R.string.repeat_every_label), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { rule = rule.copy(intervalAmount = (rule.intervalAmount - 1).coerceAtLeast(1)) }) { Text("–") }
                    Text("${rule.intervalAmount}", style = MaterialTheme.typography.titleMedium)
                    OutlinedButton(onClick = { rule = rule.copy(intervalAmount = rule.intervalAmount + 1) }) { Text("+") }
                }
                Spacer(Modifier.height(8.dp))
                val unitOptions = listOf(
                    IntervalUnit.MINUTES to stringResource(R.string.unit_minutes),
                    IntervalUnit.HOURS to stringResource(R.string.unit_hours),
                    IntervalUnit.DAYS to stringResource(R.string.unit_days),
                    IntervalUnit.WEEKS to stringResource(R.string.unit_weeks)
                )
                ScrollableChipRow(items = unitOptions) { (u, l) ->
                    FilterChip(selected = rule.intervalUnit == u, onClick = { rule = rule.copy(intervalUnit = u) }, label = { Text(l) })
                }
            }

            if (rule.type == RepeatType.WEEKLY) {
                Spacer(Modifier.height(14.dp))
                Text(stringResource(R.string.repeat_days_of_week_label), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                ScrollableChipRow(items = DayOfWeek.entries.toList()) { day ->
                    FilterChip(
                        selected = day in rule.daysOfWeek,
                        onClick = {
                            rule = rule.copy(
                                daysOfWeek = if (day in rule.daysOfWeek) rule.daysOfWeek - day else rule.daysOfWeek + day
                            )
                        },
                        label = { Text(dayNames[day.ordinal]) }
                    )
                }
            }

            if (rule.type != RepeatType.NONE) {
                Spacer(Modifier.height(14.dp))
                Text(stringResource(R.string.repeat_end_label), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                val endOptions = listOf(
                    RepeatEndType.NEVER to stringResource(R.string.repeat_end_never),
                    RepeatEndType.UNTIL_DATE to stringResource(R.string.repeat_end_until_date),
                    RepeatEndType.COUNT to stringResource(R.string.repeat_end_count)
                )
                ScrollableChipRow(items = endOptions) { (t, l) ->
                    FilterChip(selected = rule.endType == t, onClick = { rule = rule.copy(endType = t) }, label = { Text(l) })
                }
                if (rule.endType == RepeatEndType.COUNT) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { rule = rule.copy(endCount = ((rule.endCount ?: 1) - 1).coerceAtLeast(1)) }) { Text("–") }
                        val count = rule.endCount ?: 1
                        Text(context.resources.getQuantityString(R.plurals.times_count, count, count))
                        OutlinedButton(onClick = { rule = rule.copy(endCount = (rule.endCount ?: 1) + 1) }) { Text("+") }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            Button(onClick = { onConfirm(rule) }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.common_done)) }
            Spacer(Modifier.height(8.dp))
        }
    }
}
