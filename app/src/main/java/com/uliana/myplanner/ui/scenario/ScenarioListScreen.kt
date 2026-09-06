@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.uliana.myplanner.ui.scenario

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uliana.myplanner.R
import com.uliana.myplanner.ui.components.iconFor
import com.uliana.myplanner.ui.rememberRepository
import java.time.LocalDateTime

@Composable
fun rememberScenarioViewModel(): ScenarioViewModel {
    val repo = rememberRepository()
    val app = LocalContext.current.applicationContext as android.app.Application
    val factory = remember { viewModelFactory { initializer { ScenarioViewModel(app, repo) } } }
    return viewModel(factory = factory)
}

@Composable
fun ScenarioListScreen(
    viewModel: ScenarioViewModel,
    onBack: () -> Unit,
    onAddScenario: () -> Unit,
    onEditScenario: (Long) -> Unit,
    onRun: (Long) -> Unit
) {
    val scenarios by viewModel.scenarios.collectAsState()
    val categories by viewModel.categories.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.scenarios_title)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.scenarios_back_desc)) } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddScenario) { Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.scenarios_new_desc)) }
        }
    ) { padding ->
        if (scenarios.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.scenarios_empty),
                    modifier = Modifier.padding(horizontal = 32.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
                items(scenarios, key = { it.scenario.id }) { withSteps ->
                    val cat = categories.find { it.id == withSteps.scenario.categoryId }
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        onClick = { onEditScenario(withSteps.scenario.id) }
                    ) {
                        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                iconFor(withSteps.scenario.icon), contentDescription = null,
                                tint = cat?.let { Color(android.graphics.Color.parseColor(it.colorHex)) } ?: MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(withSteps.scenario.name, style = MaterialTheme.typography.titleMedium)
                                val stepsCount = withSteps.steps.size
                                val context = LocalContext.current
                                Text(
                                    context.resources.getQuantityString(R.plurals.steps_count, stepsCount, stepsCount),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            IconButton(onClick = { onRun(withSteps.scenario.id) }) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = stringResource(R.string.scenarios_run_desc))
                            }
                        }
                    }
                }
            }
        }
    }
}
