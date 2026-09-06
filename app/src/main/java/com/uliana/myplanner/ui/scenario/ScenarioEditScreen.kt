package com.uliana.myplanner.ui.scenario

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.uliana.myplanner.R
import com.uliana.myplanner.data.Category
import com.uliana.myplanner.data.ScenarioEntity
import com.uliana.myplanner.data.ScenarioStepEntity
import com.uliana.myplanner.ui.components.ScrollableChipRow

data class EditableStep(
    val id: Long = 0,
    val title: String,
    val offsetMinutes: Int,
    val durationMinutes: Int,
    val reminderMinutesBefore: Int? = 5
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScenarioEditScreen(
    scenarioId: Long?,
    viewModel: ScenarioViewModel,
    onBack: () -> Unit
) {
    val scenarios by viewModel.scenarios.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val existing = scenarios.find { it.scenario.id == scenarioId }

    var name by remember(existing) { mutableStateOf(existing?.scenario?.name ?: "") }
    var categoryId by remember(existing) { mutableStateOf(existing?.scenario?.categoryId) }
    var steps by remember(existing) {
        mutableStateOf(
            existing?.steps?.sortedBy { it.stepOrder }?.map {
                EditableStep(it.id, it.title, it.offsetMinutesFromStart, it.durationMinutes, it.reminderMinutesBefore)
            } ?: listOf(EditableStep(title = "", offsetMinutes = 0, durationMinutes = 15))
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) stringResource(R.string.scenario_edit_new_title) else stringResource(R.string.scenario_edit_title)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back)) } },
                actions = {
                    if (existing != null) {
                        IconButton(onClick = { viewModel.deleteScenario(existing.scenario); onBack() }) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.common_delete))
                        }
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Button(
                    onClick = {
                        val scenario = ScenarioEntity(id = existing?.scenario?.id ?: 0, name = name, categoryId = categoryId)
                        val entities = steps.mapIndexed { index, s ->
                            ScenarioStepEntity(
                                id = s.id, scenarioId = scenario.id, title = s.title,
                                offsetMinutesFromStart = s.offsetMinutes, durationMinutes = s.durationMinutes,
                                stepOrder = index, reminderMinutesBefore = s.reminderMinutesBefore
                            )
                        }
                        viewModel.saveScenario(scenario, entities, isNew = existing == null, onDone = onBack)
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    enabled = name.isNotBlank() && steps.all { it.title.isNotBlank() }
                ) { Text(stringResource(R.string.scenario_save_action)) }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(horizontal = 20.dp).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text(stringResource(R.string.scenario_name_label)) }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.label_category), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            ScrollableChipRow(items = categories) { cat: Category ->
                FilterChip(
                    selected = categoryId == cat.id,
                    onClick = { categoryId = if (categoryId == cat.id) null else cat.id },
                    label = { Text(cat.name) }
                )
            }

            Spacer(Modifier.height(20.dp))
            Text(stringResource(R.string.scenario_steps_label), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.scenario_steps_hint),
                style = MaterialTheme.typography.labelSmall
            )
            Spacer(Modifier.height(8.dp))

            steps.forEachIndexed { index, step ->
                StepEditorCard(
                    step = step,
                    stepNumber = index + 1,
                    onChange = { updated -> steps = steps.toMutableList().also { it[index] = updated } },
                    onRemove = { steps = steps.toMutableList().also { it.removeAt(index) } },
                    canRemove = steps.size > 1
                )
                Spacer(Modifier.height(10.dp))
            }

            OutlinedButton(
                onClick = {
                    val lastEnd = steps.lastOrNull()?.let { it.offsetMinutes + it.durationMinutes } ?: 0
                    steps = steps + EditableStep(title = "", offsetMinutes = lastEnd, durationMinutes = 15)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(R.string.scenario_add_step_action)) }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun StepEditorCard(
    step: EditableStep,
    stepNumber: Int,
    onChange: (EditableStep) -> Unit,
    onRemove: () -> Unit,
    canRemove: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.scenario_step_number, stepNumber), style = MaterialTheme.typography.titleMedium)
                if (canRemove) {
                    IconButton(onClick = onRemove) { Icon(Icons.Filled.DeleteOutline, contentDescription = stringResource(R.string.scenario_delete_step_desc)) }
                }
            }
            OutlinedTextField(
                value = step.title, onValueChange = { onChange(step.copy(title = it)) },
                label = { Text(stringResource(R.string.scenario_step_what_label)) }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.scenario_step_offset_label), style = MaterialTheme.typography.labelSmall)
                OutlinedButton(onClick = { onChange(step.copy(offsetMinutes = (step.offsetMinutes - 5).coerceAtLeast(0))) }) { Text("–") }
                Text("${step.offsetMinutes}")
                OutlinedButton(onClick = { onChange(step.copy(offsetMinutes = step.offsetMinutes + 5)) }) { Text("+") }
            }
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.scenario_step_duration_label), style = MaterialTheme.typography.labelSmall)
                OutlinedButton(onClick = { onChange(step.copy(durationMinutes = (step.durationMinutes - 5).coerceAtLeast(5))) }) { Text("–") }
                Text("${step.durationMinutes}")
                OutlinedButton(onClick = { onChange(step.copy(durationMinutes = step.durationMinutes + 5)) }) { Text("+") }
            }
        }
    }
}
