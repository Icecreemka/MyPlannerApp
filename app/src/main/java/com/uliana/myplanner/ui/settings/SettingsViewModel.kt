package com.uliana.myplanner.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uliana.myplanner.data.PlannerRepository
import com.uliana.myplanner.data.SleepSchedule
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application,
    private val repository: PlannerRepository
) : AndroidViewModel(application) {

    val sleepSchedule = repository.settingsRepository.sleepSchedule
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SleepSchedule())

    val defaultReminderMinutes = repository.settingsRepository.defaultReminderMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 15)

    val categories = repository.categories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSleepSchedule(schedule: SleepSchedule) = viewModelScope.launch {
        repository.settingsRepository.setSleepSchedule(schedule)
    }

    fun updateDefaultReminder(minutes: Int) = viewModelScope.launch {
        repository.settingsRepository.setDefaultReminderMinutes(minutes)
    }

    fun addCategory(name: String, colorHex: String, icon: String) = viewModelScope.launch {
        repository.createCategory(com.uliana.myplanner.data.Category(name = name, colorHex = colorHex, icon = icon))
    }

    fun deleteCategory(category: com.uliana.myplanner.data.Category) = viewModelScope.launch {
        repository.deleteCategory(category)
    }
}
