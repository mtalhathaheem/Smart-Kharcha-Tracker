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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.model.AccountType
import com.example.data.model.ExpenseCategories
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.util.CurrencyUtils

@Composable
fun AddSavingsGoalDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, targetAmount: Double, initialSaved: Double, category: String, note: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetStr by remember { mutableStateOf("") }
    var initialSavedStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Emergency Fund") }
    var note by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Emergency Fund", "Hajj & Umrah", "Eid Qurbani", "Vehicle / Bike", "Home & Furniture", "Tech & Laptop", "Wedding / Kameti", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Savings, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Savings Goal", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title (e.g. Eid-ul-Adha Fund)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("goal_title_input")
                )

                OutlinedTextField(
                    value = targetStr,
                    onValueChange = { targetStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Target Amount (PKR)") },
                    prefix = { Text("Rs. ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("goal_target_input")
                )

                OutlinedTextField(
                    value = initialSavedStr,
                    onValueChange = { initialSavedStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Already Saved (PKR, optional)") },
                    prefix = { Text("Rs. ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("goal_saved_input")
                )

                Text("Category", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 12.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Purpose / Bank Notes") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetStr.toDoubleOrNull() ?: 0.0
                    val initialSaved = initialSavedStr.toDoubleOrNull() ?: 0.0
                    if (title.isBlank()) {
                        error = "Please enter a goal title."
                    } else if (target <= 0.0) {
                        error = "Please enter a valid target amount in PKR."
                    } else {
                        onSave(title.trim(), target, initialSaved, category, note.ifBlank { null })
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("save_goal_confirm_button")
            ) {
                Text("Save Goal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun DepositWithdrawGoalDialog(
    goal: SavingsGoalEntity,
    isDeposit: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double) -> Unit
) {
    var amountStr by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isDeposit) Icons.Default.Add else Icons.Default.Remove,
                    contentDescription = null,
                    tint = if (isDeposit) IncomeGreen else ExpenseRed,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isDeposit) "Deposit to '${goal.title}'" else "Withdraw from '${goal.title}'",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Current saved: ${CurrencyUtils.formatPkr(goal.savedAmount)} of ${CurrencyUtils.formatPkr(goal.targetAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text(if (isDeposit) "Deposit Amount (PKR)" else "Withdrawal Amount (PKR)") },
                    prefix = { Text("Rs. ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("goal_adjust_amount_input")
                )

                // Quick increment chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(5000, 10000, 25000).forEach { amt ->
                        OutlinedButton(
                            onClick = { amountStr = amt.toString() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+${amt / 1000}k", fontSize = 11.sp)
                        }
                    }
                }

                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (amount <= 0.0) {
                        error = "Enter a valid PKR amount."
                    } else if (!isDeposit && amount > goal.savedAmount) {
                        error = "Cannot withdraw more than saved balance."
                    } else {
                        onConfirm(amount)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDeposit) IncomeGreen else ExpenseRed
                ),
                modifier = Modifier.testTag("confirm_goal_adjust_button")
            ) {
                Text(if (isDeposit) "Add Deposit" else "Confirm Withdrawal", color = androidx.compose.ui.graphics.Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddSubscriptionDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, dueDay: Int, category: String, provider: String, accountType: AccountType) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var dueDayStr by remember { mutableStateOf("10") }
    var provider by remember { mutableStateOf("K-Electric") }
    var selectedAccount by remember { mutableStateOf(AccountType.EASYPAISA) }
    var category by remember { mutableStateOf(ExpenseCategories.BILLS_UTILITIES) }
    var error by remember { mutableStateOf<String?>(null) }

    val presetBills = listOf(
        "K-Electric Bill" to ("K-Electric" to ExpenseCategories.BILLS_UTILITIES),
        "Nayatel Internet" to ("Nayatel" to ExpenseCategories.MOBILE_INTERNET),
        "SNGPL Sui Gas" to ("SNGPL" to ExpenseCategories.BILLS_UTILITIES),
        "PTCL Broadband" to ("PTCL" to ExpenseCategories.BILLS_UTILITIES),
        "Netflix Pakistan" to ("Netflix" to ExpenseCategories.ENTERTAINMENT),
        "Jazz Monthly Super" to ("Jazz" to ExpenseCategories.MOBILE_INTERNET)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Subscriptions, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Recurring Bill / Subscription", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Quick Presets (Pakistan)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presetBills.forEach { (name, pair) ->
                        FilterChip(
                            selected = title == name,
                            onClick = {
                                title = name
                                provider = pair.first
                                category = pair.second
                            },
                            label = { Text(name, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Bill / Service Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("sub_title_input")
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Monthly Amount (PKR)") },
                    prefix = { Text("Rs. ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("sub_amount_input")
                )

                OutlinedTextField(
                    value = dueDayStr,
                    onValueChange = { dueDayStr = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Due Day of Month (1 - 31)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("sub_dueday_input")
                )

                Text("Payment Wallet / Bank", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AccountType.entries.forEach { acc ->
                        FilterChip(
                            selected = selectedAccount == acc,
                            onClick = { selectedAccount = acc },
                            label = { Text(acc.title, fontSize = 11.sp) }
                        )
                    }
                }

                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    val day = dueDayStr.toIntOrNull() ?: 1
                    if (title.isBlank()) {
                        error = "Please enter a bill or subscription name."
                    } else if (amount <= 0.0) {
                        error = "Enter a valid amount in PKR."
                    } else if (day !in 1..31) {
                        error = "Due day must be between 1 and 31."
                    } else {
                        onSave(title.trim(), amount, day, category, provider, selectedAccount)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("save_subscription_confirm_button")
            ) {
                Text("Add Bill")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
