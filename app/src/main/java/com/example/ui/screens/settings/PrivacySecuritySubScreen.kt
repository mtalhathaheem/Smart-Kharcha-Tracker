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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auth.AuthViewModel
import com.example.auth.BiometricHelper
import com.example.ui.theme.ElectricLavender
import com.example.ui.theme.NeonEmerald
import com.example.ui.viewmodel.KharchaViewModel

@Composable
fun PrivacySecuritySubScreen(
    viewModel: KharchaViewModel,
    authViewModel: AuthViewModel,
    onLockNow: () -> Unit,
    onSetupPin: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val isBalanceMasked by viewModel.isBalanceMasked.collectAsStateWithLifecycle()
    val autoLockSeconds by viewModel.autoLockTimeoutSeconds.collectAsStateWithLifecycle()
    var showRemovePinDialog by remember { mutableStateOf(false) }

    val isBiometricHardwareAvailable = remember(context) {
        BiometricHelper.isBiometricAvailable(context)
    }

    Column(modifier = modifier.fillMaxSize()) {
        SettingsSubPageHeader(
            title = "Privacy & Security",
            subtitle = "Vault lock, biometrics & app security",
            icon = Icons.Default.Security,
            accentColor = Color(0xFF05D580),
            onNavigateBack = onNavigateBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Security Banner
            item {
                SettingsInfoBanner(
                    title = "Zero-Knowledge Local Storage",
                    description = "Smart Kharcha processes Pakistani banking SMS entirely on your device. Bank balances, OTPs, and transactions never leave your phone.",
                    icon = Icons.Default.Shield,
                    accentColor = NeonEmerald
                )
            }

            // PIN Lock Section
            item {
                SettingsSectionHeader(title = "App Access & Lock")
                SettingsCard {
                    SettingsToggleRow(
                        title = "4-Digit PIN Lock",
                        subtitle = if (authUiState.isPinEnabled) "Vault protection active" else "Require secure PIN on app launch",
                        checked = authUiState.isPinEnabled,
                        onCheckedChange = { enable ->
                            if (enable) {
                                onSetupPin()
                            } else {
                                showRemovePinDialog = true
                            }
                        },
                        icon = Icons.Default.Pin,
                        iconTint = if (authUiState.isPinEnabled) NeonEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                        testTag = "subpage_pin_switch"
                    )

                    if (authUiState.isPinEnabled) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 50.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onSetupPin,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("subpage_change_pin_button"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Change PIN", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Button(
                                onClick = onLockNow,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("subpage_lock_now_button"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Lock Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )

                    // Biometrics Row
                    SettingsToggleRow(
                        title = "Biometric Fingerprint / Face Unlock",
                        subtitle = if (!isBiometricHardwareAvailable) {
                            "Biometric sensor not available on this device"
                        } else if (!authUiState.isPinEnabled) {
                            "Enable 4-Digit PIN first to activate biometric unlock"
                        } else {
                            "Instant access using your device's biometric sensor"
                        },
                        checked = authUiState.isBiometricEnabled,
                        enabled = isBiometricHardwareAvailable && authUiState.isPinEnabled,
                        onCheckedChange = { authViewModel.toggleBiometric(it) },
                        icon = Icons.Default.Fingerprint,
                        iconTint = ElectricLavender,
                        testTag = "subpage_biometric_switch"
                    )
                }
            }

            // Auto-lock Timing
            item {
                SettingsSectionHeader(title = "Auto-Lock Timeout")
                SettingsCard {
                    val timeoutOptions = listOf(
                        "Immediately (on app switch)" to 0,
                        "After 1 minute" to 60,
                        "After 5 minutes" to 300,
                        "After 15 minutes" to 900,
                        "Never (lock manually only)" to -1
                    )
                    timeoutOptions.forEach { (label, seconds) ->
                        val isSelected = autoLockSeconds == seconds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = authUiState.isPinEnabled) {
                                    viewModel.setAutoLockTimeoutSeconds(seconds)
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.setAutoLockTimeoutSeconds(seconds) },
                                enabled = authUiState.isPinEnabled
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (authUiState.isPinEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            // Display Privacy
            item {
                SettingsSectionHeader(title = "Screen Privacy")
                SettingsCard {
                    SettingsToggleRow(
                        title = "Global Hide / Mask Balances",
                        subtitle = if (isBalanceMasked) "All balances and amounts are obscured (Rs. ••••••)" else "Show actual financial figures across cards and lists",
                        checked = isBalanceMasked,
                        onCheckedChange = { viewModel.setBalanceMasked(it) },
                        icon = Icons.Default.VisibilityOff,
                        iconTint = MaterialTheme.colorScheme.primary,
                        testTag = "subpage_mask_balance_switch"
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }

    if (showRemovePinDialog) {
        AlertDialog(
            onDismissRequest = { showRemovePinDialog = false },
            title = { Text("Disable 4-Digit PIN?") },
            text = { Text("Disabling the PIN will also turn off biometric fingerprint and face unlock protection.") },
            confirmButton = {
                Button(
                    onClick = {
                        showRemovePinDialog = false
                        authViewModel.removePin()
                    }
                ) {
                    Text("Disable PIN")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemovePinDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
