package com.uliana.myplanner.ui.finance

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uliana.myplanner.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class FinanceViewModel(
    application: Application,
    private val repository: FinanceRepository
) : AndroidViewModel(application) {

    val accounts = repository.accounts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val categories = repository.categories.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val transactions = repository.transactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val activeSavingsGoals = repository.activeSavingsGoals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val purchasedGoalsCount = repository.purchasedGoalsCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun addAccount(name: String, colorHex: String, icon: String) = viewModelScope.launch {
        repository.addAccount(FinanceAccountEntity(name = name, colorHex = colorHex, icon = icon))
    }

    fun deleteAccount(account: FinanceAccountEntity) = viewModelScope.launch {
        repository.deleteAccount(account)
    }

    fun addCategory(name: String, colorHex: String, icon: String) = viewModelScope.launch {
        repository.addCategory(FinanceCategoryEntity(name = name, colorHex = colorHex, icon = icon))
    }

    fun addTransaction(
        accountId: Long,
        categoryId: Long?,
        type: TransactionType,
        amount: Double,
        note: String,
        date: LocalDate
    ) = viewModelScope.launch {
        if (amount <= 0.0) return@launch
        repository.addTransaction(
            TransactionEntity(
                accountId = accountId,
                categoryId = categoryId,
                type = type,
                amount = amount,
                note = note,
                date = date
            )
        )
    }

    fun deleteTransaction(transaction: TransactionEntity) = viewModelScope.launch {
        repository.deleteTransaction(transaction)
    }

    fun addSavingsGoal(title: String, targetAmount: Double, colorHex: String, note: String) = viewModelScope.launch {
        if (title.isBlank() || targetAmount <= 0.0) return@launch
        repository.addSavingsGoal(SavingsGoalEntity(title = title, targetAmount = targetAmount, colorHex = colorHex, note = note))
    }

    fun deleteSavingsGoal(goal: SavingsGoalEntity) = viewModelScope.launch {
        repository.deleteSavingsGoal(goal)
    }

    /** Кладёт деньги в копилку (с анимацией монеток на стороне UI) и списывает их со счёта. */
    fun depositToGoal(goal: SavingsGoalEntity, amount: Double, fromAccountId: Long?, onDone: () -> Unit) =
        viewModelScope.launch {
            if (amount > 0.0) {
                repository.depositToGoal(goal, amount, fromAccountId)
            }
            onDone()
        }

    /** Отмечает копилку купленной — UI после этого запускает анимацию разбития. */
    fun markGoalPurchased(goal: SavingsGoalEntity) = viewModelScope.launch {
        repository.markGoalPurchased(goal)
    }
}
