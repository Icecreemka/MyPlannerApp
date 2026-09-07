package com.uliana.myplanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "backlog_tasks")
data class BacklogTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val categoryId: Long? = null,
    val defaultDurationMinutes: Long = 30,
    val createdAt: Long = System.currentTimeMillis()
)
