package com.uliana.myplanner.ui.backlog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uliana.myplanner.data.AppDatabase
import com.uliana.myplanner.data.BacklogTaskEntity
import com.uliana.myplanner.data.PlannerRepository
import com.uliana.myplanner.notifications.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class BacklogViewModel(
    application: Application,
    private val repository: PlannerRepository
) : AndroidViewModel(application) {

    val backlogTasks = repository.backlogTasks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categories = repository.categories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTask(title: String, description: String, categoryId: Long?, defaultDurationMinutes: Long) =
        viewModelScope.launch {
            repository.addBacklogTask(
                BacklogTaskEntity(
                    title = title,
                    description = description,
                    categoryId = categoryId,
                    defaultDurationMinutes = defaultDurationMinutes
                )
            )
        }

    fun deleteTask(task: BacklogTaskEntity) = viewModelScope.launch {
        repository.deleteBacklogTask(task)
    }

    fun sendToSchedule(task: BacklogTaskEntity, start: LocalDateTime, durationMinutes: Long, onDone: () -> Unit) =
        viewModelScope.launch {
            val taskId = repository.sendBacklogTaskToSchedule(task, start, durationMinutes)
            val created = AppDatabase.getInstance(getApplication<Application>()).taskDao().getById(taskId)
            if (created != null) ReminderScheduler.scheduleNextForTask(getApplication<Application>(), created)
            onDone()
        }
}
