package com.uliana.myplanner.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceAccountDao {
    @Query("SELECT * FROM finance_accounts ORDER BY createdAt ASC")
    fun observeAll(): Flow<List<FinanceAccountEntity>>

    @Insert
    suspend fun insert(account: FinanceAccountEntity): Long

    @Insert
    suspend fun insertAll(accounts: List<FinanceAccountEntity>)

    @Update
    suspend fun update(account: FinanceAccountEntity)

    @Delete
    suspend fun delete(account: FinanceAccountEntity)

    @Query("SELECT COUNT(*) FROM finance_accounts")
    suspend fun count(): Int

    @Query("SELECT * FROM finance_accounts WHERE id = :id")
    suspend fun getById(id: Long): FinanceAccountEntity?
}

@Dao
interface FinanceCategoryDao {
    @Query("SELECT * FROM finance_categories ORDER BY name ASC")
    fun observeAll(): Flow<List<FinanceCategoryEntity>>

    @Insert
    suspend fun insertAll(categories: List<FinanceCategoryEntity>)

    @Insert
    suspend fun insert(category: FinanceCategoryEntity): Long

    @Delete
    suspend fun delete(category: FinanceCategoryEntity)

    @Query("SELECT COUNT(*) FROM finance_categories")
    suspend fun count(): Int
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM finance_transactions ORDER BY date DESC, createdAt DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Delete
    suspend fun delete(transaction: TransactionEntity)
}

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals WHERE isPurchased = 0 ORDER BY createdAt ASC")
    fun observeActive(): Flow<List<SavingsGoalEntity>>

    @Query("SELECT * FROM savings_goals WHERE isPurchased = 1 ORDER BY purchasedAt DESC")
    fun observePurchased(): Flow<List<SavingsGoalEntity>>

    @Query("SELECT COUNT(*) FROM savings_goals WHERE isPurchased = 1")
    fun observePurchasedCount(): Flow<Int>

    @Insert
    suspend fun insert(goal: SavingsGoalEntity): Long

    @Update
    suspend fun update(goal: SavingsGoalEntity)

    @Delete
    suspend fun delete(goal: SavingsGoalEntity)
}
