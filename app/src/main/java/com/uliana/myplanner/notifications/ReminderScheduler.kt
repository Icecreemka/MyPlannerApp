package com.uliana.myplanner.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.uliana.myplanner.data.AppDatabase
import com.uliana.myplanner.data.TaskEntity
import com.uliana.myplanner.domain.RepeatEngine
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

object ReminderScheduler {

    const val EXTRA_TASK_ID = "extra_task_id"
    const val EXTRA_TITLE = "extra_title"
    const val EXTRA_OCCURRENCE_START = "extra_occurrence_start"

    private const val SEARCH_HORIZON_DAYS = 400L

    fun scheduleNextForTask(context: Context, task: TaskEntity) {
        cancelForTask(context, task.id)

        val reminderMinutes = task.reminderMinutesBefore ?: return
        if (task.repeatRule.isStoppedForever && task.repeatRule.stopAfterDate?.let { !LocalDate.now().isBefore(it) } == true) {
            return
        }

        val db = AppDatabase.getInstance(context)
        val overrides = runCatching {
            kotlinx.coroutines.runBlocking { db.taskDao().getOverridesForTask(task.id) }
        }.getOrDefault(emptyList())

        val now = LocalDateTime.now()
        val occurrences = RepeatEngine.occurrencesInRange(
            task, overrides, LocalDate.now(), LocalDate.now().plusDays(SEARCH_HORIZON_DAYS)
        )

        val next = occurrences
            .filter { !it.isCompleted && it.start.minusMinutes(reminderMinutes.toLong()).isAfter(now) }
            .minByOrNull { it.start } ?: return

        val triggerAt = next.start.minusMinutes(reminderMinutes.toLong())
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, task.id)
            putExtra(EXTRA_TITLE, task.title)
            putExtra(EXTRA_OCCURRENCE_START, next.start.toString())
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode(task.id), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        if (canExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    fun cancelForTask(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode(taskId), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun rescheduleAll(context: Context) {
        val db = AppDatabase.getInstance(context)
        kotlinx.coroutines.runBlocking {

            val snapshot = db.taskDao().observeAll().first()
            snapshot.forEach { scheduleNextForTask(context, it) }
        }
    }

    private fun requestCode(taskId: Long): Int = ("task_$taskId").hashCode()
}
