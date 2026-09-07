package com.uliana.myplanner.data

import kotlinx.coroutines.flow.Flow

class FinanceRepository(
    private val accountDao: FinanceAccountDao,
    private val categoryDao: FinanceCategoryDao,
    private val transactionDao: TransactionDao,
    private val savingsGoalDao: SavingsGoalDao
) {
    val accounts: Flow<List<FinanceAccountEntity>> = accountDao.observeAll()
    val categories: Flow<List<FinanceCategoryEntity>> = categoryDao.observeAll()
    val transactions: Flow<List<TransactionEntity>> = transactionDao.observeAll()
    val activeSavingsGoals: Flow<List<SavingsGoalEntity>> = savingsGoalDao.observeActive()
    val purchasedSavingsGoals: Flow<List<SavingsGoalEntity>> = savingsGoalDao.observePurchased()
    val purchasedGoalsCount: Flow<Int> = savingsGoalDao.observePurchasedCount()

    suspend fun addAccount(account: FinanceAccountEntity) = accountDao.insert(account)
    suspend fun deleteAccount(account: FinanceAccountEntity) = accountDao.delete(account)

    suspend fun addCategory(category: FinanceCategoryEntity) = categoryDao.insert(category)
    suspend fun deleteCategory(category: FinanceCategoryEntity) = categoryDao.delete(category)

    suspend fun addTransaction(transaction: TransactionEntity) {
        transactionDao.insert(transaction)
        val account = accountDao.getById(transaction.accountId) ?: return
        val delta = if (transaction.type == TransactionType.EXPENSE) -transaction.amount else transaction.amount
        accountDao.update(account.copy(balance = account.balance + delta))
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.delete(transaction)
        val account = accountDao.getById(transaction.accountId) ?: return

        val delta = if (transaction.type == TransactionType.EXPENSE) transaction.amount else -transaction.amount
        accountDao.update(account.copy(balance = account.balance + delta))
    }

    suspend fun addSavingsGoal(goal: SavingsGoalEntity) = savingsGoalDao.insert(goal)
    suspend fun deleteSavingsGoal(goal: SavingsGoalEntity) = savingsGoalDao.delete(goal)

    suspend fun depositToGoal(goal: SavingsGoalEntity, amount: Double, fromAccountId: Long?) {
        savingsGoalDao.update(goal.copy(savedAmount = goal.savedAmount + amount))
        if (fromAccountId != null) {
            val account = accountDao.getById(fromAccountId) ?: return
            accountDao.update(account.copy(balance = account.balance - amount))
        }
    }

    suspend fun markGoalPurchased(goal: SavingsGoalEntity) {
        savingsGoalDao.update(goal.copy(isPurchased = true, purchasedAt = System.currentTimeMillis()))
    }
}
