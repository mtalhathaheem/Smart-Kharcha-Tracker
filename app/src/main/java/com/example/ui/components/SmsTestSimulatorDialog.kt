package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SmsPreset(
    val title: String,
    val sender: String,
    val body: String
)

@Composable
fun SmsTestSimulatorDialog(
    onDismiss: () -> Unit,
    onSimulate: (sender: String, body: String) -> Unit
) {
    val presets = remember {
        listOf(
            SmsPreset(
                title = "EasyPaisa",
                sender = "3737",
                body = "Trx ID: 7891234. You have paid Rs. 2,150 at Cheezious via EasyPaisa on 29-10-2026. Remaining balance: Rs. 12,100."
            ),
            SmsPreset(
                title = "JazzCash",
                sender = "8558",
                body = "Rs. 3,500.00 transferred to Farhan Ali (03211234567) from your JazzCash Mobile Account on 29-Oct-2026. TID: 55443322. Remaining Balance: Rs. 4,950.00."
            ),
            SmsPreset(
                title = "NayaPay",
                sender = "NayaPay",
                body = "You spent Rs. 4,200.00 at Carrefour using your NayaPay Visa Card (...4521). Available balance: Rs. 18,950.00."
            ),
            SmsPreset(
                title = "Meezan Bank",
                sender = "8257",
                body = "Dear Customer, PKR 6,500.00 debited from A/C ending 1234 on 29-OCT-26 at Shell Petrol. Available Bal: PKR 58,900.00."
            ),
            SmsPreset(
                title = "SadaPay",
                sender = "SadaPay",
                body = "You just spent Rs. 1,650.00 at KFC. Your updated SadaPay balance is Rs. 8,930.00."
            ),
            SmsPreset(
                title = "Salary Credit",
                sender = "8257",
                body = "PKR 45,000.00 credited to your Account ending 1234 via Raast/IBFT. Available Bal: PKR 103,900.00."
            )
        )
    }

    var selectedPresetIndex by remember { mutableStateOf(0) }
    var senderText by remember { mutableStateOf(presets[0].sender) }
    var bodyText by remember { mutableStateOf(presets[0].body) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("SMS Parser & Detection Tester", fontWeight = FontWeight.Bold)
                Text(
                    "Test Pakistani bank & wallet SMS extraction and duplicate prevention directly on device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Pick a realistic Pakistani SMS preset:", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEachIndexed { index, preset ->
                        FilterChip(
                            selected = selectedPresetIndex == index,
                            onClick = {
                                selectedPresetIndex = index
                                senderText = preset.sender
                                bodyText = preset.body
                            },
                            label = { Text(preset.title) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = senderText,
                    onValueChange = { senderText = it },
                    label = { Text("Sender ID / Shortcode") },
                    placeholder = { Text("e.g. 3737, 8558, NayaPay, Meezan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("sms_sender_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = bodyText,
                    onValueChange = { bodyText = it },
                    label = { Text("SMS Message Body") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth().testTag("sms_body_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Smart Parser will extract: Amount, Expense/Income type, Merchant, Category, and Account Balance, while preventing duplicate entries.",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (senderText.isNotBlank() && bodyText.isNotBlank()) {
                        onSimulate(senderText.trim(), bodyText.trim())
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("simulate_sms_btn")
            ) {
                Text("Process SMS")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
