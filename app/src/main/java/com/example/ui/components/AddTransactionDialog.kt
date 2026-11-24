package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.AccountType
import com.example.data.model.ExpenseCategories
import com.example.data.model.TransactionType
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen

@Composable
fun AddTransactionDialog(
    initialTransaction: TransactionEntity? = null,
    availableAccounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        amount: Double,
        type: TransactionType,
        category: String,
        accountType: AccountType,
        merchant: String,
        timestamp: Long,
        notes: String?
    ) -> Unit
) {
    var amountText by remember {
        mutableStateOf(initialTransaction?.amount?.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() } ?: "")
    }
    var transactionType by remember {
        mutableStateOf(initialTransaction?.type ?: TransactionType.EXPENSE)
    }
    var selectedCategory by remember {
        mutableStateOf(initialTransaction?.category ?: ExpenseCategories.FOOD)
    }
    var selectedAccountType by remember {
        mutableStateOf(initialTransaction?.accountType ?: AccountType.EASYPAISA)
    }
    var merchantText by remember {
        mutableStateOf(initialTransaction?.merchantOrRecipient ?: "")
    }
    var notesText by remember {
        mutableStateOf(initialTransaction?.notes ?: "")
    }

    val categories = if (transactionType == TransactionType.EXPENSE)
        ExpenseCategories.allExpenseCategories
    else
        ExpenseCategories.allIncomeCategories

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier.padding(end = 10.dp)
                ) {
                    Text(
                        text = "PKR",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Text(
                    text = if (initialTransaction != null) "Edit Transaction" else "Add Manual Kharcha",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Expense vs Income Segmented Toggle
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = transactionType == TransactionType.EXPENSE,
                        onClick = {
                            transactionType = TransactionType.EXPENSE
                            if (!ExpenseCategories.allExpenseCategories.contains(selectedCategory)) {
                                selectedCategory = ExpenseCategories.FOOD
                            }
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = ExpenseRed.copy(alpha = 0.15f),
                            activeContentColor = ExpenseRed
                        ),
                        modifier = Modifier.testTag("type_expense_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Expense")
                        }
                    }
                    SegmentedButton(
                        selected = transactionType == TransactionType.INCOME,
                        onClick = {
                            transactionType = TransactionType.INCOME
                            if (!ExpenseCategories.allIncomeCategories.contains(selectedCategory)) {
                                selectedCategory = ExpenseCategories.SALARY
                            }
                        },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = IncomeGreen.copy(alpha = 0.15f),
                            activeContentColor = IncomeGreen
                        ),
                        modifier = Modifier.testTag("type_income_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Income")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Amount Field with PKR Prefix
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    prefix = { Text("Rs. ", fontWeight = FontWeight.Bold) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_amount_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Merchant / Counterparty
                OutlinedTextField(
                    value = merchantText,
                    onValueChange = { merchantText = it },
                    label = {
                        Text(if (transactionType == TransactionType.EXPENSE) "Merchant / Spent At" else "Payer / Source")
                    },
                    placeholder = { Text("e.g. Foodpanda, Shell, Careem, Raast") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transaction_merchant_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Account Selection
                Text(
                    text = "Account / Wallet",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AccountType.entries.forEach { accType ->
                        val isSelected = selectedAccountType == accType
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedAccountType = accType },
                            label = { Text(accType.title) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category Selection
                Text(
                    text = "Expense Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notes Field
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notes (Optional)") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    if (amt != null && amt > 0.0) {
                        onSave(
                            initialTransaction?.id ?: 0L,
                            amt,
                            transactionType,
                            selectedCategory,
                            selectedAccountType,
                            merchantText.trim().ifEmpty { "Manual Entry" },
                            initialTransaction?.timestamp ?: System.currentTimeMillis(),
                            notesText.trim().ifEmpty { null }
                        )
                    }
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("save_transaction_btn")
            ) {
                Text(if (initialTransaction != null) "Update" else "Save Transaction")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}
