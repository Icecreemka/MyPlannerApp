package com.uliana.myplanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Дело в планировщике.
 *
 * Если repeatRule.type == NONE, это разовое дело: anchorStart/anchorEnd — его точное время.
 * Если дело повторяющееся, anchorStart/anchorEnd задают время ПЕРВОГО вхождения и служат
 * шаблоном для расчёта времени всех последующих (RepeatEngine).
 * Индивидуальные изменения конкретных вхождений (перенос одного дня, отметка выполнения,
 * пропуск) хранятся отдельно в TaskOccurrenceOverride, не трогая сам шаблон.
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val categoryId: Long?,
    val anchorStart: LocalDateTime,
    val anchorEnd: LocalDateTime,
    val durationMinutes: Long,
    val repeatRule: RepeatRule = RepeatRule(),
    val reminderMinutesBefore: Int? = 15,
    val scenarioRunId: String? = null,
    val scenarioStepOrder: Int? = null,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    /** true, только если дело "посажено" из Списка дел — только у таких показывается росток/дерево. */
    val fromBacklog: Boolean = false
)
