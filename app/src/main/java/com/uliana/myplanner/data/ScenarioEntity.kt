package com.uliana.myplanner.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "scenarios")
data class ScenarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val categoryId: Long? = null,
    val icon: String = "scenario"
)

@Entity(
    tableName = "scenario_steps",
    foreignKeys = [
        ForeignKey(
            entity = ScenarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["scenarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ScenarioStepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scenarioId: Long,
    val title: String,
    val offsetMinutesFromStart: Int,
    val durationMinutes: Int,
    val stepOrder: Int,
    val reminderMinutesBefore: Int? = 5
)
