package com.uliana.myplanner.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BacklogDao {
    @Query("SELECT * FROM backlog_tasks ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<BacklogTaskEntity>>

    @Insert
    suspend fun insert(task: BacklogTaskEntity): Long

    @Update
    suspend fun update(task: BacklogTaskEntity)

    @Delete
    suspend fun delete(task: BacklogTaskEntity)

    @Query("DELETE FROM backlog_tasks WHERE id = :id")
    suspend fun deleteById(id: Long)
}
