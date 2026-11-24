package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.AddTransactionDialog
import com.example.ui.components.BudgetDialog
import com.example.ui.components.SmsTestSimulatorDialog
import com.example.ui.screens.AccountsScreen
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.LuminousLavender
import androidx.compose.material.icons.filled.Lock
import com.example.auth.AuthScreen
import com.example.auth.AuthViewModel
import com.example.ui.viewmodel.KharchaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: KharchaViewModel,
    authViewModel: AuthViewModel,
    onLockSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val showAddDialog by viewModel.showAddDialog.collectAsStateWithLifecycle()
    val showBudgetDialog by viewModel.showBudgetDialog.collectAsStateWithLifecycle()
    val showSmsSimulator by viewModel.showSmsSimulator.collectAsStateWithLifecycle()
    val editingTxn by viewModel.editingTransaction.collectAsStateWithLifecycle()
    val accounts by viewModel.activeAccounts.collectAsStateWithLifecycle()
    val isScanningSms by viewModel.isScanningSms.collectAsStateWithLifecycle()

    BackHandler(enabled = currentTab != 0) {
        viewModel.setTab(0)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(38.dp),
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_kharcha_logo_1793297856079),
                                contentDescription = "Smart Kharcha Logo",
                                modifier = Modifier.padding(3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = when (currentTab) {
                                    0 -> "Smart Kharcha"
                                    1 -> "Transactions"
                                    2 -> "Vaults & Accounts"
                                    3 -> "Monthly Budgets"
                                    4 -> "Financial Reports"
                                    5 -> "Settings & About"
                                    else -> "Smart Kharcha"
                                },
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium,
                                letterSpacing = (-0.3).sp
                            )
                            Text(
                                text = "Pakistan Expense Suite",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                actions = {
                    // Global Balance Privacy Toggle
                    val isMasked by viewModel.isBalanceMasked.collectAsStateWithLifecycle()
                    IconButton(
                        onClick = { viewModel.toggleBalanceMask() },
                        modifier = Modifier.testTag("toggle_global_balance_mask_topbar")
                    ) {
                        Icon(
                            imageVector = if (isMasked) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isMasked) "Show Balances" else "Mask Balances",
                            tint = if (isMasked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // SMS Simulator button
                    IconButton(
                        onClick = { viewModel.openSmsSimulator() },
                        modifier = Modifier.testTag("open_sms_simulator_topbar")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Science,
                            contentDescription = "Test SMS Parser",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Quick Sync SMS button
                    IconButton(
                        onClick = { viewModel.scanSmsMessages() },
                        enabled = !isScanningSms,
                        modifier = Modifier.testTag("quick_sync_sms_topbar")
                    ) {
                        if (isScanningSms) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync SMS Inbox"
                            )
                        }
                    }

                    // Settings & About Button
                    IconButton(
                        onClick = { viewModel.setTab(5) },
                        modifier = Modifier.testTag("open_settings_topbar")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings & About",
                            tint = if (currentTab == 5) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Quick Lock Button (when PIN enabled)
                    if (authUiState.isPinEnabled) {
                        IconButton(
                            onClick = onLockSession,
                            modifier = Modifier.testTag("quick_lock_app_topbar")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Lock App",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { viewModel.setTab(0) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == 0) Icons.Filled.Dashboard else Icons.Outlined.Dashboard,
                                contentDescription = "Dashboard"
                            )
                        },
                        label = { Text("Home", style = MaterialTheme.typography.labelSmall, fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("nav_tab_dashboard")
                    )

                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { viewModel.setTab(1) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == 1) Icons.Filled.Receipt else Icons.Outlined.Receipt,
                                contentDescription = "Transactions"
                            )
                        },
                        label = { Text("Kharcha", style = MaterialTheme.typography.labelSmall, fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("nav_tab_transactions")
                    )

                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { viewModel.setTab(2) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == 2) Icons.Filled.AccountBalance else Icons.Outlined.AccountBalance,
                                contentDescription = "Accounts"
                            )
                        },
                        label = { Text("Vaults", style = MaterialTheme.typography.labelSmall, fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("nav_tab_accounts")
                    )

                    NavigationBarItem(
                        selected = currentTab == 3,
                        onClick = { viewModel.setTab(3) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == 3) Icons.Filled.PieChart else Icons.Outlined.PieChart,
                                contentDescription = "Budgets"
                            )
                        },
                        label = { Text("Budgets", style = MaterialTheme.typography.labelSmall, fontWeight = if (currentTab == 3) FontWeight.Bold else FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("nav_tab_budgets")
                    )

                    NavigationBarItem(
                        selected = currentTab == 4,
                        onClick = { viewModel.setTab(4) },
                        icon = {
                            Icon(
                                imageVector = if (currentTab == 4) Icons.Filled.Assessment else Icons.Outlined.Assessment,
                                contentDescription = "Reports"
                            )
                        },
                        label = { Text("Reports", style = MaterialTheme.typography.labelSmall, fontWeight = if (currentTab == 4) FontWeight.Bold else FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("nav_tab_reports")
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.openAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier.testTag("fab_add_kharcha")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Kharcha", modifier = Modifier.size(26.dp))
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                0 -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToTransactions = { viewModel.setTab(1) }
                )
                1 -> TransactionsScreen(viewModel = viewModel)
                2 -> AccountsScreen(viewModel = viewModel)
                3 -> BudgetsScreen(viewModel = viewModel)
                4 -> ReportsScreen(viewModel = viewModel)
                5 -> SettingsScreen(
                    viewModel = viewModel,
                    authViewModel = authViewModel,
                    onLockNow = onLockSession,
                    onSetupPin = { authViewModel.navigateTo(AuthScreen.PIN_SETUP) }
                )
            }
        }
    }

    // Dialogs
    if (showAddDialog) {
        AddTransactionDialog(
            initialTransaction = editingTxn,
            availableAccounts = accounts,
            onDismiss = { viewModel.closeAddDialog() },
            onSave = { id, amount, type, category, accountType, merchant, timestamp, notes ->
                viewModel.saveManualTransaction(
                    id = id,
                    amount = amount,
                    type = type,
                    category = category,
                    accountType = accountType,
                    merchant = merchant,
                    timestamp = timestamp,
                    notes = notes
                )
            }
        )
    }

    if (showBudgetDialog) {
        BudgetDialog(
            onDismiss = { viewModel.closeBudgetDialog() },
            onSaveBudget = { category, limit ->
                viewModel.saveBudget(category, limit)
            }
        )
    }

    if (showSmsSimulator) {
        SmsTestSimulatorDialog(
            onDismiss = { viewModel.closeSmsSimulator() },
            onSimulate = { sender, body ->
                viewModel.processSimulatedSms(sender, body)
            }
        )
    }
}
