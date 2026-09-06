package com.uliana.myplanner.ui.navigation

sealed class Dest(val route: String) {
    data object Day : Dest("day")
    data object Backlog : Dest("backlog")
    data object Finance : Dest("finance")
    data object Scenarios : Dest("scenarios")
    data object ScenarioEdit : Dest("scenario_edit/{scenarioId}") {
        fun build(scenarioId: Long?) = "scenario_edit/${scenarioId ?: 0}"
    }
    data object Settings : Dest("settings")
    data object Categories : Dest("categories")
    data object TaskEdit : Dest("task_edit?taskId={taskId}&date={date}") {
        fun build(taskId: Long? = null, date: String? = null) =
            "task_edit?taskId=${taskId ?: -1}&date=${date ?: ""}"
    }

    companion object {
        val bottomBarItems = listOf(Day, Backlog, Finance)
    }
}
