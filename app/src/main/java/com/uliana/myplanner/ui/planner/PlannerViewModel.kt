package com.uliana.myplanner.ui.planner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uliana.myplanner.data.PlannerRepository
import com.uliana.myplanner.data.SleepSchedule
import com.uliana.myplanner.domain.FreeTimeCalculator
import com.uliana.myplanner.domain.TaskOccurrence
import com.uliana.myplanner.notifications.ReminderScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

class PlannerViewModel(
    application: Application,
    private val repository: PlannerRepository
) : AndroidViewModel(application) {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    val categories = repository.categories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val sleepSchedule = repository.settingsRepository.sleepSchedule
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SleepSchedule())

    val dayOccurrences: StateFlow<List<TaskOccurrence>> = _selectedDate
        .flatMapLatest { date -> repository.occurrencesInRange(date, date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val freeMinutesToday: StateFlow<Long> = combine(dayOccurrences, sleepSchedule, _selectedDate) { occ, sleep, date ->
        FreeTimeCalculator.freeMinutesForDay(date, occ, sleep)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 24 * 60L)

    val treesGrown = repository.settingsRepository.treesGrown
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun selectDate(date: LocalDate) { _selectedDate.value = date }
    fun shiftDate(days: Long) { _selectedDate.value = _selectedDate.value.plusDays(days) }

    fun toggleComplete(occurrence: TaskOccurrence) = viewModelScope.launch {
        val nowCompleted = !occurrence.isCompleted
        repository.markOccurrenceCompleted(occurrence, nowCompleted)

        if (occurrence.task.fromBacklog) {
            if (nowCompleted) {
                repository.settingsRepository.incrementTreesGrown()
            } else {
                repository.settingsRepository.decrementTreesGrown()
            }
        }
    }

    fun skipOccurrence(occurrence: TaskOccurrence) = viewModelScope.launch {
        repository.skipOccurrence(occurrence)
    }

    fun moveOccurrence(occurrence: TaskOccurrence, newStart: LocalDateTime) = viewModelScope.launch {
        repository.moveOccurrence(occurrence, newStart)
        rescheduleReminder(occurrence.task.id)
    }

    fun swapOccurrences(occurrence: TaskOccurrence, other: TaskOccurrence) = viewModelScope.launch {
        val (slotA, slotB) = if (!other.start.isBefore(occurrence.start)) occurrence to other else other to occurrence
        val gapMinutes = java.time.Duration.between(slotA.end, slotB.start).toMinutes()

        val newStartForSlotBTask = slotA.start
        val newStartForSlotATask = newStartForSlotBTask.plusMinutes(slotB.durationMinutes).plusMinutes(gapMinutes)

        repository.moveOccurrence(slotB, newStartForSlotBTask)
        repository.moveOccurrence(slotA, newStartForSlotATask)
        rescheduleReminder(slotA.task.id)
        rescheduleReminder(slotB.task.id)
    }

    fun pauseRepeatTemporarily(occurrence: TaskOccurrence, from: LocalDate, until: LocalDate?) = viewModelScope.launch {
        repository.pauseRepeat(occurrence.task, from, until)
        rescheduleReminder(occurrence.task.id)
    }

    fun resumeRepeat(occurrence: TaskOccurrence) = viewModelScope.launch {
        repository.resumeRepeat(occurrence.task)
        rescheduleReminder(occurrence.task.id)
    }

    fun stopRepeatForever(occurrence: TaskOccurrence, fromDate: LocalDate) = viewModelScope.launch {
        repository.stopRepeatForever(occurrence.task, fromDate)
        rescheduleReminder(occurrence.task.id)
    }

    fun deleteSeries(occurrence: TaskOccurrence) = viewModelScope.launch {
        ReminderScheduler.cancelForTask(getApplication<android.app.Application>(), occurrence.task.id)
        repository.deleteTaskSeries(occurrence.task)
    }

    private suspend fun rescheduleReminder(taskId: Long) {
        val task = getApplication<Application>().let {

            com.uliana.myplanner.data.AppDatabase.getInstance(it).taskDao().getById(taskId)
        }
        if (task != null) ReminderScheduler.scheduleNextForTask(getApplication<android.app.Application>(), task)
    }
}
