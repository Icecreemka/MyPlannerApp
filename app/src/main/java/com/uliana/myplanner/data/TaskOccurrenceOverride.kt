package com.uliana.myplanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "task_overrides")
data class TaskOccurrenceOverride(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val originalStart: LocalDateTime,
    val newStart: LocalDateTime? = null,
    val newEnd: LocalDateTime? = null,
    val isSkipped: Boolean = false,
    val isCompleted: Boolean = false
)
