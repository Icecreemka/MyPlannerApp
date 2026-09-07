package com.uliana.myplanner.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime

private val Context.dataStore by preferencesDataStore(name = "my_planner_settings")

data class SleepSchedule(
    val enabled: Boolean = false,
    val sleepStart: LocalTime = LocalTime.of(23, 0),
    val sleepEnd: LocalTime = LocalTime.of(7, 0)
) {

    val durationMinutes: Long
        get() {
            val startMin = sleepStart.hour * 60 + sleepStart.minute
            val endMin = sleepEnd.hour * 60 + sleepEnd.minute
            return if (endMin > startMin) (endMin - startMin).toLong()
            else (24 * 60 - startMin + endMin).toLong()
        }
}

class SettingsRepository(private val context: Context) {

    private object Keys {
        val SLEEP_ENABLED = booleanPreferencesKey("sleep_enabled")
        val SLEEP_START = stringPreferencesKey("sleep_start")
        val SLEEP_END = stringPreferencesKey("sleep_end")
        val DEFAULT_REMINDER = intPreferencesKey("default_reminder_minutes")
        val ONBOARDING_SEEN = booleanPreferencesKey("onboarding_seen")
        val TREES_GROWN = intPreferencesKey("trees_grown")
    }

    val sleepSchedule: Flow<SleepSchedule> = context.dataStore.data.map { prefs ->
        SleepSchedule(
            enabled = prefs[Keys.SLEEP_ENABLED] ?: false,
            sleepStart = prefs[Keys.SLEEP_START]?.let { LocalTime.parse(it) } ?: LocalTime.of(23, 0),
            sleepEnd = prefs[Keys.SLEEP_END]?.let { LocalTime.parse(it) } ?: LocalTime.of(7, 0)
        )
    }

    val defaultReminderMinutes: Flow<Int> = context.dataStore.data.map { it[Keys.DEFAULT_REMINDER] ?: 15 }

    val hasSeenOnboarding: Flow<Boolean> = context.dataStore.data.map { it[Keys.ONBOARDING_SEEN] ?: false }

    val treesGrown: Flow<Int> = context.dataStore.data.map { it[Keys.TREES_GROWN] ?: 0 }

    suspend fun setOnboardingSeen(seen: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_SEEN] = seen }
    }

    suspend fun incrementTreesGrown() {
        context.dataStore.edit { it[Keys.TREES_GROWN] = (it[Keys.TREES_GROWN] ?: 0) + 1 }
    }

    suspend fun decrementTreesGrown() {
        context.dataStore.edit { it[Keys.TREES_GROWN] = ((it[Keys.TREES_GROWN] ?: 0) - 1).coerceAtLeast(0) }
    }

    suspend fun setSleepSchedule(schedule: SleepSchedule) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SLEEP_ENABLED] = schedule.enabled
            prefs[Keys.SLEEP_START] = schedule.sleepStart.toString()
            prefs[Keys.SLEEP_END] = schedule.sleepEnd.toString()
        }
    }

    suspend fun setDefaultReminderMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.DEFAULT_REMINDER] = minutes }
    }
}
