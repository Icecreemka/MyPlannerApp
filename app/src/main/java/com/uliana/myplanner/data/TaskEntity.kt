package com.uliana.myplanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

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

    val fromBacklog: Boolean = false
)
