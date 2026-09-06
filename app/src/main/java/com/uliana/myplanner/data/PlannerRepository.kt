package com.uliana.myplanner.data

import com.uliana.myplanner.domain.RepeatEngine
import com.uliana.myplanner.domain.ScenarioEngine
import com.uliana.myplanner.domain.TaskOccurrence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.LocalDateTime

class PlannerRepository(
    private val taskDao: TaskDao,
    private val categoryDao: CategoryDao,
    private val scenarioDao: ScenarioDao,
    private val backlogDao: BacklogDao,
    val settingsRepository: SettingsRepository
) {
    val categories: Flow<List<Category>> = categoryDao.observeAll()
    val scenarios: Flow<List<ScenarioWithSteps>> = scenarioDao.observeAllWithSteps()
    val backlogTasks: Flow<List<BacklogTaskEntity>> = backlogDao.observeAll()

    /** Все вхождения дел в диапазоне дат — то, что реально показывается в планировщике. */
    fun occurrencesInRange(rangeStart: LocalDate, rangeEnd: LocalDate): Flow<List<TaskOccurrence>> =
        combine(taskDao.observeAll(), taskDao.observeAllOverrides()) { tasks, overrides ->
            val overridesByTask = overrides.groupBy { it.taskId }
            tasks.flatMap { task ->
                RepeatEngine.occurrencesInRange(
                    task = task,
                    overrides = overridesByTask[task.id].orEmpty(),
                    rangeStart = rangeStart,
                    rangeEnd = rangeEnd
                )
            }
        }

    // ---------- Дела ----------

    suspend fun createTask(task: TaskEntity): Long = taskDao.insert(task)

    suspend fun updateTaskTemplate(task: TaskEntity) = taskDao.update(task)

    suspend fun deleteTaskSeries(task: TaskEntity) {
        taskDao.deleteOverridesForTask(task.id)
        taskDao.delete(task)
    }

    /** Перенос одного конкретного вхождения (для NONE — правит сам шаблон, для повторов — override). */
    suspend fun moveOccurrence(occurrence: TaskOccurrence, newStart: LocalDateTime) {
        val newEnd = newStart.plusMinutes(occurrence.durationMinutes)
        if (occurrence.task.repeatRule.type == RepeatType.NONE) {
            taskDao.update(occurrence.task.copy(anchorStart = newStart, anchorEnd = newEnd))
        } else {
            val existing = taskDao.getOverridesForTask(occurrence.task.id)
                .find { it.originalStart == occurrence.originalStart }
            taskDao.upsertOverride(
                (existing ?: TaskOccurrenceOverride(taskId = occurrence.task.id, originalStart = occurrence.originalStart))
                    .copy(newStart = newStart, newEnd = newEnd)
            )
        }
    }

    suspend fun markOccurrenceCompleted(occurrence: TaskOccurrence, completed: Boolean) {
        if (occurrence.task.repeatRule.type == RepeatType.NONE) {
            taskDao.update(occurrence.task.copy(isCompleted = completed))
        } else {
            val existing = taskDao.getOverridesForTask(occurrence.task.id)
                .find { it.originalStart == occurrence.originalStart }
            taskDao.upsertOverride(
                (existing ?: TaskOccurrenceOverride(taskId = occurrence.task.id, originalStart = occurrence.originalStart))
                    .copy(isCompleted = completed)
            )
        }
    }

    suspend fun skipOccurrence(occurrence: TaskOccurrence) {
        val existing = taskDao.getOverridesForTask(occurrence.task.id)
            .find { it.originalStart == occurrence.originalStart }
        taskDao.upsertOverride(
            (existing ?: TaskOccurrenceOverride(taskId = occurrence.task.id, originalStart = occurrence.originalStart))
                .copy(isSkipped = true)
        )
    }

    /** Приостановить повторения на промежуток [from, until] (until = null -> пока не возобновят). */
    suspend fun pauseRepeat(task: TaskEntity, from: LocalDate, until: LocalDate?) {
        taskDao.update(task.copy(repeatRule = task.repeatRule.copy(pausedFrom = from, pausedUntil = until)))
    }

    suspend fun resumeRepeat(task: TaskEntity) {
        taskDao.update(task.copy(repeatRule = task.repeatRule.copy(pausedFrom = null, pausedUntil = null)))
    }

    /** Полностью прекратить повторения начиная с указанной даты. */
    suspend fun stopRepeatForever(task: TaskEntity, fromDate: LocalDate) {
        taskDao.update(task.copy(repeatRule = task.repeatRule.copy(isStoppedForever = true, stopAfterDate = fromDate)))
    }

    // ---------- Категории ----------

    suspend fun createCategory(category: Category) = categoryDao.insert(category)
    suspend fun updateCategory(category: Category) = categoryDao.update(category)
    suspend fun deleteCategory(category: Category) = categoryDao.delete(category)

    // ---------- Сценарии ----------

    suspend fun createScenario(scenario: ScenarioEntity, steps: List<ScenarioStepEntity>) {
        val id = scenarioDao.insertScenario(scenario)
        scenarioDao.insertSteps(steps.map { it.copy(scenarioId = id) })
    }

    suspend fun updateScenario(scenario: ScenarioEntity, steps: List<ScenarioStepEntity>) {
        scenarioDao.updateScenario(scenario)
        scenarioDao.deleteStepsForScenario(scenario.id)
        scenarioDao.insertSteps(steps.map { it.copy(scenarioId = scenario.id) })
    }

    suspend fun deleteScenario(scenario: ScenarioEntity) = scenarioDao.deleteScenario(scenario)

    /** Запускает сценарий: создаёт все дела сценария в планировщике начиная с указанного момента. */
    suspend fun runScenario(scenarioId: Long, startAt: LocalDateTime): List<Long> {
        val withSteps = scenarioDao.getScenarioWithSteps(scenarioId) ?: return emptyList()
        val tasks = ScenarioEngine.instantiate(withSteps, startAt)
        return taskDao.insertAll(tasks)
    }

    // ---------- Список дел без времени ----------

    suspend fun addBacklogTask(task: BacklogTaskEntity): Long = backlogDao.insert(task)
    suspend fun updateBacklogTask(task: BacklogTaskEntity) = backlogDao.update(task)
    suspend fun deleteBacklogTask(task: BacklogTaskEntity) = backlogDao.delete(task)

    /** "Сажает" дело из списка в расписание на конкретные дату/время и убирает его из списка. */
    suspend fun sendBacklogTaskToSchedule(
        backlogTask: BacklogTaskEntity,
        start: LocalDateTime,
        durationMinutes: Long
    ): Long {
        val taskId = taskDao.insert(
            TaskEntity(
                title = backlogTask.title,
                description = backlogTask.description,
                categoryId = backlogTask.categoryId,
                anchorStart = start,
                anchorEnd = start.plusMinutes(durationMinutes),
                durationMinutes = durationMinutes,
                fromBacklog = true
            )
        )
        backlogDao.delete(backlogTask)
        return taskId
    }
}
