package com.uliana.myplanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Индивидуальное изменение одного вхождения повторяющегося дела:
 * перенос по времени, пропуск, отметка о выполнении — без изменения всей серии.
 *
 * originalStart — изначально рассчитанное (до переноса) время начала вхождения; служит
 * уникальным ключом, так как у дела может быть несколько вхождений в один день.
 */
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
