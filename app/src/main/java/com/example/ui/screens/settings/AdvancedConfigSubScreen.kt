package com.example.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.KharchaViewModel

@Composable
fun AdvancedConfigSubScreen(
    viewModel: KharchaViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isScanningSms by viewModel.isScanningSms.collectAsStateWithLifecycle()
    var strictDuplicateHashing by remember { mutableStateOf(true) }
    var loggingVerbosity by remember { mutableStateOf("Info") }

    Column(modifier = modifier.fillMaxSize()) {
        SettingsSubPageHeader(
            title = "Advanced Configuration",
            subtitle = "SMS regex engine, diagnostics & simulation",
            icon = Icons.Default.Tune,
            accentColor = Color(0xFFF97316),
            onNavigateBack = onNavigateBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Developer Diagnostics & Testing Tools
            item {
                SettingsSectionHeader(title = "Testing & Inbox Rescan")
                SettingsCard {
                    Text(
                        text = "Trigger real-time regex parsing simulation or scan existing SMS messages in device inbox:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.openSmsSimulator() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("subpage_open_sms_simulator"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Test Simulator", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.scanSmsMessages() },
                            enabled = !isScanningSms,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("subpage_rescan_sms_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isScanningSms) "Scanning..." else "Rescan Inbox", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Regex Engine Patterns Status
            item {
                SettingsSectionHeader(title = "Active Regex Patterns")
                SettingsCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        val patterns = listOf(
                            "EasyPaisa (3737)" to "Rs\\.?\\s*([\\d,]+\\.?\\d*).*paid to\\s+(.+)",
                            "JazzCash (8558)" to "Trans\\. ID\\s*\\w+.*Rs\\.?\\s*([\\d,]+)",
                            "NayaPay" to "paid\\s+Rs\\.?\\s*([\\d,]+)\\s+at\\s+(.+)",
                            "SadaPay" to "spent\\s+Rs\\.?\\s*([\\d,]+)\\s+at\\s+(.+)",
                            "Meezan Bank" to "debit of PKR\\s*([\\d,]+).*at\\s+(.+)"
                        )

                        patterns.forEach { (provider, regex) ->
                            Column {
                                Text(
                                    text = provider,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = regex,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontFamily = FontFamily.Monospace,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Duplicate Prevention Engine
            item {
                SettingsSectionHeader(title = "Deduplication & Logs")
                SettingsCard {
                    SettingsToggleRow(
                        title = "Deterministic Hash Deduplication",
                        subtitle = "Generates unique SHA-256 fingerprint from amount, sender, and minute window to stop double counting",
                        checked = strictDuplicateHashing,
                        onCheckedChange = { strictDuplicateHashing = it },
                        icon = Icons.Default.Code,
                        iconTint = MaterialTheme.colorScheme.primary
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Parser Logging Level",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Logcat output for SMS receiver debugging",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = loggingVerbosity,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}
