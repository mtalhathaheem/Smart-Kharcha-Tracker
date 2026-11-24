package com.example.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun NotificationsSubScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var masterNotifications by remember { mutableStateOf(true) }
    var smsDetectionAlerts by remember { mutableStateOf(true) }
    var budgetWarning80 by remember { mutableStateOf(true) }
    var budgetExceeded100 by remember { mutableStateOf(true) }
    var dailyReminder by remember { mutableStateOf(true) }
    var weeklyDigest by remember { mutableStateOf(false) }
    var bankAdFilter by remember { mutableStateOf(true) }

    Column(modifier = modifier.fillMaxSize()) {
        SettingsSubPageHeader(
            title = "Notifications",
            subtitle = "Alerts, reminders & spending warnings",
            icon = Icons.Default.Notifications,
            accentColor = Color(0xFFFFB703),
            onNavigateBack = onNavigateBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Master Switch
            item {
                SettingsCard {
                    SettingsToggleRow(
                        title = "Enable Notifications",
                        subtitle = "Receive financial reminders and transaction alerts",
                        checked = masterNotifications,
                        onCheckedChange = { masterNotifications = it },
                        icon = Icons.Default.NotificationsActive,
                        iconTint = Color(0xFFFFB703)
                    )
                }
            }

            // Real-time SMS Detection Alerts
            item {
                SettingsSectionHeader(title = "Real-Time Transaction Alerts")
                SettingsCard {
                    SettingsToggleRow(
                        title = "SMS Detected Banner",
                        subtitle = "Alert when EasyPaisa, JazzCash, or bank SMS is parsed into kharcha",
                        checked = smsDetectionAlerts,
                        enabled = masterNotifications,
                        onCheckedChange = { smsDetectionAlerts = it },
                        icon = Icons.Default.Sms,
                        iconTint = MaterialTheme.colorScheme.primary
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )

                    SettingsToggleRow(
                        title = "Filter Promotional Bank SMS",
                        subtitle = "Ignore marketing messages, personal loans, and discount ads from 3737/8558",
                        checked = bankAdFilter,
                        enabled = masterNotifications,
                        onCheckedChange = { bankAdFilter = it },
                        icon = Icons.Default.FilterAlt,
                        iconTint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Budget Alerts
            item {
                SettingsSectionHeader(title = "Budget Spending Alerts")
                SettingsCard {
                    SettingsToggleRow(
                        title = "80% Budget Warning",
                        subtitle = "Notify when a category reaches 80% of its monthly allowance",
                        checked = budgetWarning80,
                        enabled = masterNotifications,
                        onCheckedChange = { budgetWarning80 = it },
                        icon = Icons.Default.Warning,
                        iconTint = Color(0xFFFFB74D)
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )

                    SettingsToggleRow(
                        title = "100% Budget Exceeded Alert",
                        subtitle = "Immediate critical warning when you exceed a monthly budget limit",
                        checked = budgetExceeded100,
                        enabled = masterNotifications,
                        onCheckedChange = { budgetExceeded100 = it },
                        icon = Icons.Default.Warning,
                        iconTint = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Routine Reminders
            item {
                SettingsSectionHeader(title = "Routine Reminders")
                SettingsCard {
                    SettingsToggleRow(
                        title = "Daily Evening Review (9:00 PM)",
                        subtitle = "Gentle reminder to log cash expenses and check today's kharcha",
                        checked = dailyReminder,
                        enabled = masterNotifications,
                        onCheckedChange = { dailyReminder = it },
                        icon = Icons.Default.Alarm,
                        iconTint = MaterialTheme.colorScheme.primary
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )

                    SettingsToggleRow(
                        title = "Weekly Financial Digest",
                        subtitle = "Sunday morning summary of weekly expenses and savings rate",
                        checked = weeklyDigest,
                        enabled = masterNotifications,
                        onCheckedChange = { weeklyDigest = it },
                        icon = Icons.Default.CalendarMonth,
                        iconTint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}
