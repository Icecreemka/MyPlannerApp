package com.uliana.myplanner.ui.taskedit

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uliana.myplanner.data.*
import com.uliana.myplanner.notifications.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class TaskEditState(
    val taskId: Long? = null,
    val title: String = "",
    val description: String = "",
    val categoryId: Long? = null,
    val date: LocalDate = LocalDate.now(),
    val startTime: LocalTime = LocalTime.now().withSecond(0).withNano(0),
    val endTime: LocalTime = LocalTime.now().plusHours(1).withSecond(0).withNano(0),
    val durationMinutes: Long = 60,
    val repeatRule: RepeatRule = RepeatRule(),
    val reminderMinutesBefore: Int? = 15,
    val isSaving: Boolean = false,
    val isNew: Boolean = true
)

/**
 * Редактор дела: начало/конец/длительность всегда синхронизированы — при изменении
 * любого одного значения два других пересчитываются сами, без явного выбора "режима".
 * Правило: меняешь начало — сдвигается конец (длительность не трогаем);
 * меняешь конец — пересчитывается длительность; меняешь длительность — сдвигается конец.
 */
class TaskEditViewModel(
    application: Application,
    private val repository: PlannerRepository,
    taskId: Long?,
    initialDate: LocalDate?
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(
        TaskEditState(date = initialDate ?: LocalDate.now())
    )
    val state: StateFlow<TaskEditState> = _state

    val categories = repository.categories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var loadedTask: TaskEntity? = null

    init {
        if (taskId != null && taskId > 0) {
            viewModelScope.launch {
                val task = AppDatabase.getInstance(getApplication<Application>()).taskDao().getById(taskId)
                if (task != null) {
                    loadedTask = task
                    _state.value = TaskEditState(
                        taskId = task.id,
                        title = task.title,
                        description = task.description,
                        categoryId = task.categoryId,
                        date = task.anchorStart.toLocalDate(),
                        startTime = task.anchorStart.toLocalTime(),
                        endTime = task.anchorEnd.toLocalTime(),
                        durationMinutes = task.durationMinutes,
                        repeatRule = task.repeatRule,
                        reminderMinutesBefore = task.reminderMinutesBefore,
                        isNew = false
                    )
                }
            }
        }
    }

    fun updateTitle(v: String) { _state.value = _state.value.copy(title = v) }
    fun updateDescription(v: String) { _state.value = _state.value.copy(description = v) }
    fun updateCategory(id: Long?) { _state.value = _state.value.copy(categoryId = id) }
    fun updateRepeatRule(r: RepeatRule) { _state.value = _state.value.copy(repeatRule = r) }
    fun updateReminder(minutes: Int?) { _state.value = _state.value.copy(reminderMinutesBefore = minutes) }

    fun updateDate(d: LocalDate) { _state.value = _state.value.copy(date = d) }

    /** Меняем начало — длительность остаётся прежней, конец пересчитывается. */
    fun updateStartTime(t: LocalTime) {
        val s = _state.value
        val newEnd = t.plusMinutes(s.durationMinutes)
        _state.value = s.copy(startTime = t, endTime = newEnd)
    }

    /** Меняем конец — начало остаётся прежним, длительность пересчитывается (минимум 5 минут). */
    fun updateEndTime(t: LocalTime) {
        val s = _state.value
        val startDt = LocalDateTime.of(s.date, s.startTime)
        var endDt = LocalDateTime.of(s.date, t)
        if (!endDt.isAfter(startDt)) endDt = endDt.plusDays(1)
        val duration = java.time.Duration.between(startDt, endDt).toMinutes().coerceAtLeast(5)
        _state.value = s.copy(endTime = t, durationMinutes = duration)
    }

    /** Меняем длительность — начало остаётся прежним, конец пересчитывается. */
    fun updateDuration(minutes: Long) {
        val s = _state.value
        val safeMinutes = minutes.coerceAtLeast(5)
        val newEnd = s.startTime.plusMinutes(safeMinutes)
        _state.value = s.copy(durationMinutes = safeMinutes, endTime = newEnd)
    }

    fun save(onDone: () -> Unit) {
        val s = _state.value
        if (s.title.isBlank()) return
        _state.value = s.copy(isSaving = true)
        viewModelScope.launch {
            val anchorStart = LocalDateTime.of(s.date, s.startTime)
            val anchorEnd = anchorStart.plusMinutes(s.durationMinutes)
            val entity = (loadedTask ?: TaskEntity(
                title = s.title, categoryId = s.categoryId, anchorStart = anchorStart,
                anchorEnd = anchorEnd, durationMinutes = s.durationMinutes
            )).copy(
                title = s.title,
                description = s.description,
                categoryId = s.categoryId,
                anchorStart = anchorStart,
                anchorEnd = anchorEnd,
                durationMinutes = s.durationMinutes,
                repeatRule = s.repeatRule,
                reminderMinutesBefore = s.reminderMinutesBefore
            )
            val id = if (loadedTask == null) repository.createTask(entity) else {
                repository.updateTaskTemplate(entity); entity.id
            }
            val saved = entity.copy(id = id)
            ReminderScheduler.scheduleNextForTask(getApplication<Application>(), saved)
            _state.value = s.copy(isSaving = false)
            onDone()
        }
    }

    fun delete(onDone: () -> Unit) {
        val task = loadedTask ?: return
        viewModelScope.launch {
            ReminderScheduler.cancelForTask(getApplication<Application>(), task.id)
            repository.deleteTaskSeries(task)
            onDone()
        }
    }
}
