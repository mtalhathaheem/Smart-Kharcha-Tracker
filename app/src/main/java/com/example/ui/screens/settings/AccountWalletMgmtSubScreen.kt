package com.example.ui.screens.settings

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.KharchaViewModel

data class SupportedWalletInfo(
    val id: String,
    val name: String,
    val type: String,
    val senderCode: String,
    val color: Color
)

@Composable
fun AccountWalletMgmtSubScreen(
    viewModel: KharchaViewModel,
    onNavigateToVaultsTab: () -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accounts by viewModel.activeAccounts.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()

    val walletProviders = remember {
        listOf(
            SupportedWalletInfo("easypaisa", "EasyPaisa", "Digital Wallet", "3737", Color(0xFF00B156)),
            SupportedWalletInfo("jazzcash", "JazzCash", "Digital Wallet", "8558", Color(0xFFE50914)),
            SupportedWalletInfo("nayapay", "NayaPay", "Fintech E-Wallet", "NayaPay", Color(0xFFFF6D00)),
            SupportedWalletInfo("sadapay", "SadaPay", "Fintech Mastercard", "SadaPay", Color(0xFF00C2FF)),
            SupportedWalletInfo("meezan", "Meezan Bank", "Islamic Bank", "8257", Color(0xFF2B2D42)),
            SupportedWalletInfo("hbl", "Habib Bank Limited (HBL)", "Commercial Bank", "4250", Color(0xFF00835D)),
            SupportedWalletInfo("alfalah", "Bank Alfalah", "Commercial Bank", "Alfalah", Color(0xFFD32F2F)),
            SupportedWalletInfo("cash", "Cash in Hand", "Physical Vault", "Manual Entry", Color(0xFFD4AF37))
        )
    }

    val enabledSmsMap = remember {
        mutableStateMapOf<String, Boolean>().apply {
            walletProviders.forEach { put(it.id, true) }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        SettingsSubPageHeader(
            title = "Account & Wallet Management",
            subtitle = "Connected banks, wallets & SMS tracking",
            icon = Icons.Default.AccountBalanceWallet,
            accentColor = Color(0xFFE50914),
            onNavigateBack = onNavigateBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Quick Stats & Tab Redirect Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Active Vaults",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${accounts.size} payment accounts configured",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = onNavigateToVaultsTab,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("manage_vaults_button")
                            ) {
                                Text("Open Vaults", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
            }

            // Supported Pakistani Wallets & SMS Tracking Toggles
            item {
                SettingsSectionHeader(title = "SMS Detection by Provider")
                Text(
                    text = "Toggle automatic expense capture for individual Pakistani financial institutions:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            items(walletProviders) { wallet ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = wallet.color.copy(alpha = 0.15f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = if (wallet.id == "cash") Icons.Default.Payments else Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = wallet.color,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = wallet.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    ) {
                                        Text(
                                            text = wallet.senderCode,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = wallet.type,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Switch(
                            checked = enabledSmsMap[wallet.id] ?: true,
                            onCheckedChange = { isChecked ->
                                enabledSmsMap[wallet.id] = isChecked
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = wallet.color,
                                checkedTrackColor = wallet.color.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}
