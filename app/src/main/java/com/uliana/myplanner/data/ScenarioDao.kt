package com.uliana.myplanner.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class ScenarioWithSteps(
    @Embedded val scenario: ScenarioEntity,
    @Relation(parentColumn = "id", entityColumn = "scenarioId")
    val steps: List<ScenarioStepEntity>
)

@Dao
interface ScenarioDao {
    @Query("SELECT * FROM scenarios ORDER BY name ASC")
    fun observeAllScenarios(): Flow<List<ScenarioEntity>>

    @Transaction
    @Query("SELECT * FROM scenarios WHERE id = :id")
    suspend fun getScenarioWithSteps(id: Long): ScenarioWithSteps?

    @Transaction
    @Query("SELECT * FROM scenarios ORDER BY name ASC")
    fun observeAllWithSteps(): Flow<List<ScenarioWithSteps>>

    @Insert
    suspend fun insertScenario(scenario: ScenarioEntity): Long

    @Insert
    suspend fun insertSteps(steps: List<ScenarioStepEntity>)

    @Update
    suspend fun updateScenario(scenario: ScenarioEntity)

    @Delete
    suspend fun deleteScenario(scenario: ScenarioEntity)

    @Query("DELETE FROM scenario_steps WHERE scenarioId = :scenarioId")
    suspend fun deleteStepsForScenario(scenarioId: Long)
}
