@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.uliana.myplanner.ui.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.uliana.myplanner.R
import com.uliana.myplanner.data.FinanceAccountEntity
import com.uliana.myplanner.data.FinanceCategoryEntity
import com.uliana.myplanner.data.SavingsGoalEntity
import com.uliana.myplanner.data.TransactionType
import com.uliana.myplanner.ui.components.ScrollableChipRow
import com.uliana.myplanner.ui.theme.CategoryPalette
import java.time.LocalDate

private fun parseAmount(text: String): Double = text.replace(',', '.').toDoubleOrNull() ?: 0.0

@Composable
private fun AmountField(value: String, onChange: (String) -> Unit, label: String = stringResource(R.string.finance_amount_label)) {
    OutlinedTextField(
        value = value,
        onValueChange = { text -> onChange(text.filter { it.isDigit() || it == '.' || it == ',' }) },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
}

@Composable
fun AddAccountSheet(onDismiss: () -> Unit, onConfirm: (name: String, colorHex: String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var color by remember { mutableStateOf(CategoryPalette.first()) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp).navigationBarsPadding()) {
            Text(stringResource(R.string.finance_new_account_title), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text(stringResource(R.string.finance_account_name_label)) }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    val hex = String.format("#%06X", 0xFFFFFF and color.toArgb())
                    onConfirm(name, hex)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) { Text(stringResource(R.string.finance_add_account_action)) }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun AddTransactionSheet(
    accounts: List<FinanceAccountEntity>,
    categories: List<FinanceCategoryEntity>,
    onDismiss: () -> Unit,
    onAddCategory: (name: String, colorHex: String) -> Unit,
    onConfirm: (accountId: Long, categoryId: Long?, type: TransactionType, amount: Double, note: String, date: LocalDate) -> Unit
) {
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var accountId by remember { mutableStateOf(accounts.firstOrNull()?.id) }
    var categoryId by remember { mutableStateOf<Long?>(null) }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var showNewCategoryForm by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryColor by remember { mutableStateOf(CategoryPalette.first()) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier.padding(horizontal = 20.dp, vertical = 8.dp).navigationBarsPadding().verticalScroll(rememberScrollState())
        ) {
            Text(stringResource(R.string.finance_new_transaction_title), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = type == TransactionType.EXPENSE, onClick = { type = TransactionType.EXPENSE }, label = { Text(stringResource(R.string.finance_expense_label)) })
                FilterChip(selected = type == TransactionType.INCOME, onClick = { type = TransactionType.INCOME }, label = { Text(stringResource(R.string.finance_income_label)) })
            }
            Spacer(Modifier.height(16.dp))

            AmountField(amountText, { amountText = it })

            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.finance_account_label), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            ScrollableChipRow(items = accounts) { acc ->
                FilterChip(selected = accountId == acc.id, onClick = { accountId = acc.id }, label = { Text(acc.name) })
            }

            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.label_category), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                ScrollableChipRow(items = categories, modifier = Modifier.weight(1f, fill = false)) { cat ->
                    FilterChip(
                        selected = categoryId == cat.id,
                        onClick = { categoryId = if (categoryId == cat.id) null else cat.id },
                        label = { Text(cat.name) }
                    )
                }
                AssistChip(onClick = { showNewCategoryForm = !showNewCategoryForm }, label = { Text(stringResource(R.string.finance_add_new_category)) })
            }

            if (showNewCategoryForm) {
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = newCategoryName, onValueChange = { newCategoryName = it },
                    label = { Text(stringResource(R.string.finance_new_category_name_label)) }, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoryPalette.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (color == newCategoryColor)
                                        Modifier.border(2.dp, Color.Black, CircleShape)
                                    else Modifier
                                )
                                .clickable { newCategoryColor = color }
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            val hex = String.format("#%06X", 0xFFFFFF and newCategoryColor.toArgb())
                            onAddCategory(newCategoryName, hex)
                            newCategoryName = ""
                            showNewCategoryForm = false
                        }
                    },
                    enabled = newCategoryName.isNotBlank()
                ) { Text(stringResource(R.string.finance_add_category_action)) }
            }

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = note, onValueChange = { note = it },
                label = { Text(stringResource(R.string.label_note_optional)) }, modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    accountId?.let { accId ->
                        onConfirm(accId, categoryId, type, parseAmount(amountText), note, LocalDate.now())
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = accountId != null && parseAmount(amountText) > 0.0
            ) { Text(stringResource(R.string.common_add)) }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun AddGoalSheet(onDismiss: () -> Unit, onConfirm: (title: String, targetAmount: Double, note: String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp).navigationBarsPadding()) {
            Text(stringResource(R.string.finance_new_goal_title), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text(stringResource(R.string.finance_goal_name_label)) }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            AmountField(targetText, { targetText = it }, label = stringResource(R.string.finance_goal_target_label))
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = note, onValueChange = { note = it },
                label = { Text(stringResource(R.string.label_note_optional)) }, modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { onConfirm(title, parseAmount(targetText), note) },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotBlank() && parseAmount(targetText) > 0.0
            ) { Text(stringResource(R.string.finance_create_goal_action)) }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun DepositSheet(
    goal: SavingsGoalEntity,
    accounts: List<FinanceAccountEntity>,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, fromAccountId: Long?) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var accountId by remember { mutableStateOf(accounts.firstOrNull()?.id) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 20.dp, vertical = 8.dp).navigationBarsPadding()) {
            Text(stringResource(R.string.finance_deposit_title, goal.title), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))
            AmountField(amountText, { amountText = it })

            if (accounts.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.finance_deposit_from_account), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                ScrollableChipRow(items = accounts) { acc ->
                    FilterChip(selected = accountId == acc.id, onClick = { accountId = acc.id }, label = { Text(acc.name) })
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { onConfirm(parseAmount(amountText), accountId) },
                modifier = Modifier.fillMaxWidth(),
                enabled = parseAmount(amountText) > 0.0
            ) { Text(stringResource(R.string.finance_deposit_action_confirm)) }
            Spacer(Modifier.height(8.dp))
        }
    }
}
