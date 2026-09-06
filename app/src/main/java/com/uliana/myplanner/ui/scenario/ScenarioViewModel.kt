package com.uliana.myplanner.ui.scenario

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uliana.myplanner.data.*
import com.uliana.myplanner.notifications.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class ScenarioViewModel(
    application: Application,
    private val repository: PlannerRepository
) : AndroidViewModel(application) {

    val scenarios = repository.scenarios.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categories = repository.categories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveScenario(scenario: ScenarioEntity, steps: List<ScenarioStepEntity>, isNew: Boolean, onDone: () -> Unit) =
        viewModelScope.launch {
            if (isNew) repository.createScenario(scenario, steps) else repository.updateScenario(scenario, steps)
            onDone()
        }

    fun deleteScenario(scenario: ScenarioEntity) = viewModelScope.launch {
        repository.deleteScenario(scenario)
    }

    /** Запускает сценарий: сразу расставляет все его шаги в планировщик и планирует напоминания. */
    fun runScenario(scenarioId: Long, startAt: LocalDateTime, onDone: () -> Unit) = viewModelScope.launch {
        val ids = repository.runScenario(scenarioId, startAt)
        ids.forEach { id ->
            val task = com.uliana.myplanner.data.AppDatabase.getInstance(getApplication<android.app.Application>()).taskDao().getById(id)
            if (task != null) ReminderScheduler.scheduleNextForTask(getApplication<android.app.Application>(), task)
        }
        onDone()
    }
}
