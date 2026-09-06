package com.uliana.myplanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Дело без точного времени выполнения — просто список того, что нужно сделать
 * "когда-нибудь", без привязки к плану на день. Потом любое такое дело можно
 * "посадить" в расписание, указав дату и время — тогда оно станет обычным TaskEntity.
 */
@Entity(tableName = "backlog_tasks")
data class BacklogTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val categoryId: Long? = null,
    val defaultDurationMinutes: Long = 30,
    val createdAt: Long = System.currentTimeMillis()
)
