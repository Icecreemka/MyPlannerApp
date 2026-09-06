@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.uliana.myplanner.ui.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.uliana.myplanner.R
import com.uliana.myplanner.data.*
import com.uliana.myplanner.ui.rememberFinanceRepository
import java.util.Locale

fun money(amount: Double): String = "%,.0f \u20BD".format(Locale("ru"), amount).replace(",", " ")

@Composable
private fun rememberFinanceViewModel(): FinanceViewModel {
    val repo = rememberFinanceRepository()
    val app = LocalContext.current.applicationContext as android.app.Application
    val factory = remember { viewModelFactory { initializer { FinanceViewModel(app, repo) } } }
    return viewModel(factory = factory)
}

private enum class FinanceTab(val labelResId: Int) {
    ACCOUNTS(R.string.finance_tab_accounts),
    EXPENSES(R.string.finance_tab_expenses),
    SAVINGS(R.string.finance_tab_savings)
}

@Composable
fun FinanceScreen() {
    val viewModel = rememberFinanceViewModel()
    var tab by remember { mutableStateOf(FinanceTab.ACCOUNTS) }
    var showAddTransaction by remember { mutableStateOf(false) }
    var showAddAccount by remember { mutableStateOf(false) }
    var showAddGoal by remember { mutableStateOf(false) }

    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val goals by viewModel.activeSavingsGoals.collectAsState()
    val purchasedCount by viewModel.purchasedGoalsCount.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                when (tab) {
                    FinanceTab.ACCOUNTS -> showAddAccount = true
                    FinanceTab.EXPENSES -> showAddTransaction = true
                    FinanceTab.SAVINGS -> showAddGoal = true
                }
            }) { Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.finance_fab_add_desc)) }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                Text(stringResource(R.string.finance_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            }
            TabRow(selectedTabIndex = tab.ordinal) {
                FinanceTab.entries.forEach { t ->
                    Tab(selected = tab == t, onClick = { tab = t }, text = { Text(stringResource(t.labelResId)) })
                }
            }
            Spacer(Modifier.height(8.dp))

            when (tab) {
                FinanceTab.ACCOUNTS -> AccountsTab(
                    accounts = accounts,
                    transactions = transactions,
                    categories = categories,
                    onDeleteAccount = { viewModel.deleteAccount(it) }
                )
                FinanceTab.EXPENSES -> ExpensesTab(
                    transactions = transactions,
                    accounts = accounts,
                    categories = categories,
                    onDelete = { viewModel.deleteTransaction(it) }
                )
                FinanceTab.SAVINGS -> SavingsTab(
                    goals = goals,
                    purchasedCount = purchasedCount,
                    accounts = accounts,
                    onDeposit = { goal, amount, accountId, onDone -> viewModel.depositToGoal(goal, amount, accountId, onDone) },
                    onPurchased = { viewModel.markGoalPurchased(it) },
                    onDelete = { viewModel.deleteSavingsGoal(it) }
                )
            }
        }
    }

    if (showAddAccount) {
        AddAccountSheet(
            onDismiss = { showAddAccount = false },
            onConfirm = { name, color -> viewModel.addAccount(name, color, "wallet"); showAddAccount = false }
        )
    }
    if (showAddTransaction) {
        AddTransactionSheet(
            accounts = accounts,
            categories = categories,
            onDismiss = { showAddTransaction = false },
            onAddCategory = { name, color -> viewModel.addCategory(name, color, "cart") },
            onConfirm = { accountId, categoryId, type, amount, note, date ->
                viewModel.addTransaction(accountId, categoryId, type, amount, note, date)
                showAddTransaction = false
            }
        )
    }
    if (showAddGoal) {
        AddGoalSheet(
            onDismiss = { showAddGoal = false },
            onConfirm = { title, target, note -> viewModel.addSavingsGoal(title, target, "#D4A76A", note); showAddGoal = false }
        )
    }
}
