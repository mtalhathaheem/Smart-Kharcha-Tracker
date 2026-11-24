package com.example.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.KharchaViewModel

@Composable
fun FinancialPreferencesSubScreen(
    viewModel: KharchaViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    var selectedMonthCycle by remember { mutableStateOf("1st of Month (Calendar)") }
    var defaultPaymentAccount by remember { mutableStateOf("EasyPaisa") }
    var autoCategorizeMerchants by remember { mutableStateOf(true) }
    var roundUpSavings by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        SettingsSubPageHeader(
            title = "Financial Preferences",
            subtitle = "Currency, fiscal cycle & smart automation",
            icon = Icons.Default.CurrencyExchange,
            accentColor = Color(0xFF38BDF8),
            onNavigateBack = onNavigateBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Currency Notation
            item {
                SettingsSectionHeader(title = "Pakistani Rupee Currency Notation")
                SettingsCard {
                    Text(
                        text = "Preferred currency prefix/postfix for balances, kharcha cards, and reports:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        listOf("Rs.", "PKR", "₨", "$").forEach { sym ->
                            FilterChip(
                                selected = currencySymbol == sym,
                                onClick = { viewModel.setCurrencySymbol(sym) },
                                label = {
                                    Text(
                                        text = sym,
                                        fontWeight = if (currencySymbol == sym) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                modifier = Modifier.testTag("currency_chip_$sym")
                            )
                        }
                    }
                }
            }

            // Month Start Date (Fiscal Cycle)
            item {
                SettingsSectionHeader(title = "Monthly Budget Cycle (Salary Day)")
                SettingsCard {
                    Text(
                        text = "Calculate monthly expenses and budget limits from your monthly salary deposit date:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    listOf(
                        "1st of Month (Calendar Default)",
                        "5th of Month (Pakistani Corporate Salary)",
                        "10th of Month (Mid-month cycle)",
                        "25th of Month (Advance salary)"
                    ).forEach { cycle ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedMonthCycle = cycle }
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedMonthCycle == cycle,
                                onClick = { selectedMonthCycle = cycle }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = cycle,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (selectedMonthCycle == cycle) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Default Payment Method for Manual Entries
            item {
                SettingsSectionHeader(title = "Default Payment Method")
                SettingsCard {
                    Text(
                        text = "Pre-selected account when tapping the quick '+' Add Kharcha button:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    listOf("EasyPaisa", "JazzCash", "NayaPay", "Meezan Bank", "Cash in Hand").forEach { acc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { defaultPaymentAccount = acc }
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = defaultPaymentAccount == acc,
                                onClick = { defaultPaymentAccount = acc }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = acc,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (defaultPaymentAccount == acc) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Smart Categorization & Savings Rules
            item {
                SettingsSectionHeader(title = "Smart Automation")
                SettingsCard {
                    SettingsToggleRow(
                        title = "Auto-Categorize Merchants",
                        subtitle = "Automatically tag Fuel for Shell/PSO/Total, Dining for KFC/FoodPanda, Utilities for K-Electric/SNGPL",
                        checked = autoCategorizeMerchants,
                        onCheckedChange = { autoCategorizeMerchants = it },
                        icon = Icons.Default.AutoAwesome,
                        iconTint = MaterialTheme.colorScheme.primary
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )

                    SettingsToggleRow(
                        title = "Spare Change Round-Up Vault",
                        subtitle = "Round up debit transactions to nearest 50/100 PKR into virtual emergency savings",
                        checked = roundUpSavings,
                        onCheckedChange = { roundUpSavings = it },
                        icon = Icons.Default.Savings,
                        iconTint = Color(0xFF34D399)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}
