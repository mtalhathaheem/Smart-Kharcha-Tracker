package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auth.AuthViewModel
import com.example.ui.screens.settings.AboutAppSubScreen
import com.example.ui.screens.settings.AccountWalletMgmtSubScreen
import com.example.ui.screens.settings.AdvancedConfigSubScreen
import com.example.ui.screens.settings.AppearanceThemesSubScreen
import com.example.ui.screens.settings.DataBackupSubScreen
import com.example.ui.screens.settings.FinancialPreferencesSubScreen
import com.example.ui.screens.settings.HelpSupportSubScreen
import com.example.ui.screens.settings.LanguageAccessibilitySubScreen
import com.example.ui.screens.settings.MyAccountSubScreen
import com.example.ui.screens.settings.NotificationsSubScreen
import com.example.ui.screens.settings.PrivacySecuritySubScreen
import com.example.ui.screens.settings.SettingsCategoryGroup
import com.example.ui.screens.settings.SettingsSubPage
import com.example.ui.theme.ElectricLavender
import com.example.ui.theme.NeonEmerald
import com.example.ui.viewmodel.KharchaViewModel

@Composable
fun SettingsScreen(
    viewModel: KharchaViewModel,
    authViewModel: AuthViewModel,
    onLockNow: () -> Unit,
    onSetupPin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentSubPage by rememberSaveable { mutableStateOf<SettingsSubPage?>(null) }

    // Intercept back presses when on a sub-page to return to main settings hub
    BackHandler(enabled = currentSubPage != null) {
        currentSubPage = null
    }

    AnimatedContent(
        targetState = currentSubPage,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "SettingsSubPageTransition",
        modifier = modifier.fillMaxSize()
    ) { subPage ->
        if (subPage == null) {
            // Main Settings Hub Dashboard
            SettingsMainHub(
                authViewModel = authViewModel,
                onSelectSubPage = { currentSubPage = it }
            )
        } else {
            // Dedicated Sub-Page Routing
            when (subPage) {
                SettingsSubPage.MY_ACCOUNT -> {
                    MyAccountSubScreen(
                        authViewModel = authViewModel,
                        onNavigateBack = { currentSubPage = null }
                    )
                }
                SettingsSubPage.APPEARANCE_THEMES -> {
                    AppearanceThemesSubScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentSubPage = null }
                    )
                }
                SettingsSubPage.PRIVACY_SECURITY -> {
                    PrivacySecuritySubScreen(
                        viewModel = viewModel,
                        authViewModel = authViewModel,
                        onLockNow = onLockNow,
                        onSetupPin = onSetupPin,
                        onNavigateBack = { currentSubPage = null }
                    )
                }
                SettingsSubPage.NOTIFICATIONS -> {
                    NotificationsSubScreen(
                        onNavigateBack = { currentSubPage = null }
                    )
                }
                SettingsSubPage.FINANCIAL_PREFERENCES -> {
                    FinancialPreferencesSubScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentSubPage = null }
                    )
                }
                SettingsSubPage.ACCOUNT_WALLET_MGMT -> {
                    AccountWalletMgmtSubScreen(
                        viewModel = viewModel,
                        onNavigateToVaultsTab = {
                            currentSubPage = null
                            viewModel.setTab(2)
                        },
                        onNavigateBack = { currentSubPage = null }
                    )
                }
                SettingsSubPage.DATA_BACKUP -> {
                    DataBackupSubScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentSubPage = null }
                    )
                }
                SettingsSubPage.LANGUAGE_ACCESSIBILITY -> {
                    LanguageAccessibilitySubScreen(
                        onNavigateBack = { currentSubPage = null }
                    )
                }
                SettingsSubPage.HELP_SUPPORT -> {
                    HelpSupportSubScreen(
                        onNavigateBack = { currentSubPage = null }
                    )
                }
                SettingsSubPage.ABOUT_APPLICATION -> {
                    AboutAppSubScreen(
                        onNavigateBack = { currentSubPage = null }
                    )
                }
                SettingsSubPage.ADVANCED_CONFIG -> {
                    AdvancedConfigSubScreen(
                        viewModel = viewModel,
                        onNavigateBack = { currentSubPage = null }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsMainHub(
    authViewModel: AuthViewModel,
    onSelectSubPage: (SettingsSubPage) -> Unit,
    modifier: Modifier = Modifier
) {
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val user = authUiState.currentUser

    val userDisplayName = user?.displayName?.ifBlank { null }
        ?: authUiState.savedEmail.substringBefore("@").ifBlank { "Smart Kharcha User" }
    val userEmail = user?.email?.ifBlank { null }
        ?: authUiState.savedEmail.ifBlank { "kharcha.user@pakistan.pk" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Top Header
        item {
            Text(
                text = "Settings & Preferences",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Manage your Pakistani finance vault, themes & security",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Profile Quick Card (Tapping opens My Account)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectSubPage(SettingsSubPage.MY_ACCOUNT) }
                    .testTag("settings_hub_profile_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(ElectricLavender, MaterialTheme.colorScheme.primary)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userDisplayName.take(1).uppercase(),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userDisplayName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = userEmail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (user?.isEmailVerified == true) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = NeonEmerald.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Verified", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = NeonEmerald)
                                    }
                                }
                            }

                            if (authUiState.isPinEnabled) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("PIN Protected", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // 11 Settings Categories Grouped Logically
        SettingsCategoryGroup.entries.forEach { group ->
            val pagesInGroup = SettingsSubPage.entries.filter { it.group == group }
            if (pagesInGroup.isNotEmpty()) {
                item {
                    Text(
                        text = group.groupTitle.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.1.sp,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                    ) {
                        Column {
                            pagesInGroup.forEachIndexed { index, page ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onSelectSubPage(page) }
                                        .padding(horizontal = 16.dp, vertical = 14.dp)
                                        .testTag("settings_nav_${page.name.lowercase()}"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = page.accentColor.copy(alpha = 0.14f),
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(
                                                imageVector = page.icon,
                                                contentDescription = null,
                                                tint = page.accentColor,
                                                modifier = Modifier.padding(9.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column {
                                            Text(
                                                text = page.title,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = page.subtitle,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                if (index < pagesInGroup.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = 70.dp, end = 16.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Developer Credit Footer Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("developer_credits_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Smart Kharcha Tracker v2.4.0",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Developed by Muhammad Talha Thaheem",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Crafted for Pakistan 🇵🇰 • mtsindhithahee@gmail.com",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(60.dp)) }
    }
}
