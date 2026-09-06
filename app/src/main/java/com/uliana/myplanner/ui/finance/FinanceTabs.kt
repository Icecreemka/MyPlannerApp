@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.uliana.myplanner.ui.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.uliana.myplanner.R
import com.uliana.myplanner.data.FinanceAccountEntity
import com.uliana.myplanner.data.FinanceCategoryEntity
import com.uliana.myplanner.data.SavingsGoalEntity
import com.uliana.myplanner.data.TransactionEntity
import com.uliana.myplanner.data.TransactionType
import java.time.format.DateTimeFormatter

private val dateFmt = DateTimeFormatter.ofPattern("d MMMM")

@Composable
fun AccountsTab(
    accounts: List<FinanceAccountEntity>,
    transactions: List<TransactionEntity>,
    categories: List<FinanceCategoryEntity>,
    onDeleteAccount: (FinanceAccountEntity) -> Unit
) {
    val total = accounts.sumOf { it.balance }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(stringResource(R.string.finance_total_label), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(
                        money(total),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.finance_accounts_label), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
        }
        items(accounts, key = { it.id }) { account ->
            val accentColor = Color(android.graphics.Color.parseColor(account.colorHex))
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(32.dp).clip(CircleShape).background(accentColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.AccountBalanceWallet, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(account.name, style = MaterialTheme.typography.bodyLarge)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(money(account.balance), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    IconButton(onClick = { onDeleteAccount(account) }) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = stringResource(R.string.finance_delete_account_desc), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
        item {
            Spacer(Modifier.height(20.dp))
            Text(stringResource(R.string.finance_recent_transactions), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
        }
        items(transactions.take(8), key = { it.id }) { tx ->
            TransactionRow(tx, accounts, categories, onDelete = null)
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
fun ExpensesTab(
    transactions: List<TransactionEntity>,
    accounts: List<FinanceAccountEntity>,
    categories: List<FinanceCategoryEntity>,
    onDelete: (TransactionEntity) -> Unit
) {
    val thisMonth = java.time.LocalDate.now().month
    val spentThisMonth = transactions
        .filter { it.type == TransactionType.EXPENSE && it.date.month == thisMonth }
        .sumOf { it.amount }
    val earnedThisMonth = transactions
        .filter { it.type == TransactionType.INCOME && it.date.month == thisMonth }
        .sumOf { it.amount }

    if (transactions.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.finance_empty_transactions), style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                SummaryCard(stringResource(R.string.finance_spent_this_month), spentThisMonth, MaterialTheme.colorScheme.errorContainer, Modifier.weight(1f))
                SummaryCard(stringResource(R.string.finance_earned_this_month), earnedThisMonth, MaterialTheme.colorScheme.primaryContainer, Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
        }
        items(transactions, key = { it.id }) { tx ->
            TransactionRow(tx, accounts, categories, onDelete = onDelete)
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun SummaryCard(label: String, amount: Double, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = color) {
        Column(Modifier.padding(14.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(money(amount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun TransactionRow(
    tx: TransactionEntity,
    accounts: List<FinanceAccountEntity>,
    categories: List<FinanceCategoryEntity>,
    onDelete: ((TransactionEntity) -> Unit)?
) {
    val category = categories.find { it.id == tx.categoryId }
    val account = accounts.find { it.id == tx.accountId }
    val isExpense = tx.type == TransactionType.EXPENSE
    val amountColor = if (isExpense) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val expenseLabel = stringResource(R.string.finance_expense_label)
    val incomeLabel = stringResource(R.string.finance_income_label)

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                category?.name ?: if (isExpense) expenseLabel else incomeLabel,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                listOfNotNull(account?.name, tx.date.format(dateFmt), tx.note.takeIf { it.isNotBlank() }).joinToString(" · "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            (if (isExpense) "-" else "+") + money(tx.amount),
            style = MaterialTheme.typography.titleMedium,
            color = amountColor,
            fontWeight = FontWeight.SemiBold
        )
        if (onDelete != null) {
            IconButton(onClick = { onDelete(tx) }) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.finance_delete_transaction_desc), modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun SavingsTab(
    goals: List<SavingsGoalEntity>,
    purchasedCount: Int,
    accounts: List<FinanceAccountEntity>,
    onDeposit: (SavingsGoalEntity, Double, Long?, () -> Unit) -> Unit,
    onPurchased: (SavingsGoalEntity) -> Unit,
    onDelete: (SavingsGoalEntity) -> Unit
) {
    var depositTarget by remember { mutableStateOf<SavingsGoalEntity?>(null) }

    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Celebration, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                val context = LocalContext.current
                Text(
                    context.resources.getQuantityString(R.plurals.savings_goals_purchased_count, purchasedCount, purchasedCount),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        if (goals.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    stringResource(R.string.finance_empty_goals),
                    modifier = Modifier.padding(horizontal = 40.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)) {
                items(goals, key = { it.id }) { goal ->
                    GoalCard(
                        goal = goal,
                        onDeposit = { depositTarget = goal },
                        onPurchased = { onPurchased(goal) },
                        onDelete = { onDelete(goal) }
                    )
                    Spacer(Modifier.height(12.dp))
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    depositTarget?.let { goal ->
        DepositSheet(
            goal = goal,
            accounts = accounts,
            onDismiss = { depositTarget = null },
            onConfirm = { amount, accountId -> onDeposit(goal, amount, accountId) { depositTarget = null } }
        )
    }
}

@Composable
private fun GoalCard(
    goal: SavingsGoalEntity,
    onDeposit: () -> Unit,
    onPurchased: () -> Unit,
    onDelete: () -> Unit
) {
    val color = Color(android.graphics.Color.parseColor(goal.colorHex))
    var coinDropTrigger by remember { mutableIntStateOf(0) }
    var previousSaved by remember { mutableStateOf(goal.savedAmount) }
    var breaking by remember { mutableStateOf(false) }
    var removed by remember { mutableStateOf(false) }

    // Монетки "падают" именно тогда, когда накопленная сумма реально увеличилась
    // (после подтверждения пополнения), а не в момент нажатия кнопки.
    LaunchedEffect(goal.savedAmount) {
        if (goal.savedAmount > previousSaved) {
            coinDropTrigger++
        }
        previousSaved = goal.savedAmount
    }

    if (removed) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = androidx.compose.ui.graphics.lerp(MaterialTheme.colorScheme.surface, color, 0.08f),
        tonalElevation = 2.dp
    ) {
        Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            PiggyBank(
                progress = goal.progress,
                color = color,
                coinDropTrigger = coinDropTrigger,
                breakTrigger = breaking,
                onBreakFinished = { removed = true; onPurchased() }
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(goal.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    stringResource(R.string.finance_goal_progress, money(goal.savedAmount), money(goal.targetAmount)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = onDeposit, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) {
                        Text(stringResource(R.string.finance_deposit_action), style = MaterialTheme.typography.labelSmall)
                    }
                    if (goal.progress >= 1f) {
                        Button(
                            onClick = { breaking = true },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) { Text(stringResource(R.string.finance_purchased_action), style = MaterialTheme.typography.labelSmall) }
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = stringResource(R.string.finance_delete_goal_desc), modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
