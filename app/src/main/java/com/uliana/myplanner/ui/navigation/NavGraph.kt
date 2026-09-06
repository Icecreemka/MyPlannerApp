@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.uliana.myplanner.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.compose.runtime.collectAsState
import com.uliana.myplanner.R
import com.uliana.myplanner.ui.backlog.BacklogScreen
import com.uliana.myplanner.ui.components.GardenBackground
import com.uliana.myplanner.ui.components.SkyBackground
import com.uliana.myplanner.ui.finance.FinanceScreen
import com.uliana.myplanner.ui.onboarding.OnboardingOverlay
import com.uliana.myplanner.ui.planner.*
import com.uliana.myplanner.ui.rememberRepository
import com.uliana.myplanner.ui.scenario.*
import com.uliana.myplanner.ui.settings.SettingsScreen
import com.uliana.myplanner.ui.taskedit.TaskEditScreen
import kotlinx.coroutines.launch
import java.time.LocalDate

private data class BottomItem(val dest: Dest, val labelResId: Int, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val bottomItems = listOf(
    BottomItem(Dest.Day, R.string.day_nav_label, Icons.Filled.Today),
    BottomItem(Dest.Backlog, R.string.backlog_nav_label, Icons.Filled.Spa),
    BottomItem(Dest.Finance, R.string.finance_nav_label, Icons.Filled.Savings)
)

@Composable
fun MyPlannerNavGraph() {
    val navController = rememberNavController()
    val plannerViewModel = rememberPlannerViewModel()
    val repository = rememberRepository()
    val coroutineScope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination

    val isBottomLevel = bottomItems.any { it.dest.route == currentRoute?.route }
    val isDayRoute = currentRoute?.route == Dest.Day.route

    val hasSeenOnboarding by repository.settingsRepository.hasSeenOnboarding.collectAsState(initial = true)
    var showOnboarding by remember { mutableStateOf(false) }
    LaunchedEffect(hasSeenOnboarding) {
        if (!hasSeenOnboarding) showOnboarding = true
    }

    val scaffoldContent: @Composable () -> Unit = {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                if (isBottomLevel) {
                    TopAppBar(
                        title = { Text(stringResource(R.string.app_name)) },
                        actions = {
                            IconButton(onClick = { showOnboarding = true }) {
                                Icon(Icons.Filled.HelpOutline, contentDescription = stringResource(R.string.onboarding_help_desc))
                            }
                            IconButton(onClick = { navController.navigate(Dest.Scenarios.route) }) {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = stringResource(R.string.scenarios_nav_desc))
                            }
                            IconButton(onClick = { navController.navigate(Dest.Settings.route) }) {
                                Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.settings_nav_desc))
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f)
                        )
                    )
                }
            },
            bottomBar = {
                if (isBottomLevel) {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)) {
                        bottomItems.forEach { item ->
                            val label = stringResource(item.labelResId)
                            NavigationBarItem(
                                selected = currentRoute?.hierarchy?.any { it.route == item.dest.route } == true,
                                onClick = {
                                    navController.navigate(item.dest.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(item.icon, contentDescription = label) },
                                label = { Text(label) }
                            )
                        }
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Dest.Day.route,
                modifier = Modifier.padding(padding)
            ) {
                composable(Dest.Day.route) {
                    DayScreen(
                        viewModel = plannerViewModel,
                        onAddTask = { date -> navController.navigate(Dest.TaskEdit.build(date = date.toString())) },
                        onEditTask = { id -> navController.navigate(Dest.TaskEdit.build(taskId = id)) }
                    )
                }
                composable(Dest.Backlog.route) {
                    BacklogScreen()
                }
                composable(Dest.Finance.route) {
                    FinanceScreen()
                }
                composable(Dest.Scenarios.route) {
                    val scenarioViewModel = rememberScenarioViewModel()
                    val scenarios by scenarioViewModel.scenarios.collectAsState()
                    var runningId by remember { mutableStateOf<Long?>(null) }
                    ScenarioListScreen(
                        viewModel = scenarioViewModel,
                        onBack = { navController.popBackStack() },
                        onAddScenario = { navController.navigate(Dest.ScenarioEdit.build(null)) },
                        onEditScenario = { id -> navController.navigate(Dest.ScenarioEdit.build(id)) },
                        onRun = { id -> runningId = id }
                    )
                    runningId?.let { id ->
                        val scenarioName = scenarios.find { it.scenario.id == id }?.scenario?.name ?: ""
                        RunScenarioSheet(
                            scenarioName = scenarioName,
                            onDismiss = { runningId = null },
                            onConfirm = { startAt ->
                                scenarioViewModel.runScenario(id, startAt) {
                                    runningId = null
                                    navController.navigate(Dest.Day.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                    }
                                }
                            }
                        )
                    }
                }
                composable(
                    route = Dest.ScenarioEdit.route,
                    arguments = listOf(navArgument("scenarioId") { type = NavType.LongType })
                ) { backEntry ->
                    val scenarioViewModel = rememberScenarioViewModel()
                    val idArg = backEntry.arguments?.getLong("scenarioId") ?: 0L
                    ScenarioEditScreen(
                        scenarioId = if (idArg <= 0) null else idArg,
                        viewModel = scenarioViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Dest.Settings.route) {
                    SettingsScreen(onBack = { navController.popBackStack() })
                }
                composable(
                    route = Dest.TaskEdit.route,
                    arguments = listOf(
                        navArgument("taskId") { type = NavType.LongType; defaultValue = -1L },
                        navArgument("date") { type = NavType.StringType; defaultValue = "" }
                    )
                ) { backEntry ->
                    val taskId = backEntry.arguments?.getLong("taskId") ?: -1L
                    val dateStr = backEntry.arguments?.getString("date") ?: ""
                    TaskEditScreen(
                        taskId = if (taskId <= 0) null else taskId,
                        initialDate = dateStr.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it) },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }

    if (isDayRoute) {
        SkyBackground { scaffoldContent() }
    } else {
        GardenBackground { scaffoldContent() }
    }

    if (showOnboarding) {
        OnboardingOverlay(onFinish = {
            showOnboarding = false
            coroutineScope.launch { repository.settingsRepository.setOnboardingSeen(true) }
        })
    }
}
