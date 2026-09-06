package com.uliana.myplanner.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY anchorStart ASC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE scenarioRunId = :runId ORDER BY scenarioStepOrder ASC")
    suspend fun getByScenarioRun(runId: String): List<TaskEntity>

    @Insert
    suspend fun insert(task: TaskEntity): Long

    @Insert
    suspend fun insertAll(tasks: List<TaskEntity>): List<Long>

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM tasks WHERE scenarioRunId = :runId")
    suspend fun deleteByScenarioRun(runId: String)

    // Overrides

    @Query("SELECT * FROM task_overrides")
    fun observeAllOverrides(): Flow<List<TaskOccurrenceOverride>>

    @Query("SELECT * FROM task_overrides WHERE taskId = :taskId")
    suspend fun getOverridesForTask(taskId: Long): List<TaskOccurrenceOverride>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOverride(override: TaskOccurrenceOverride)

    @Query("DELETE FROM task_overrides WHERE taskId = :taskId")
    suspend fun deleteOverridesForTask(taskId: Long)
}
