package com.example.ui.screens.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class SettingsCategoryGroup(val groupTitle: String) {
    ACCOUNT_SECURITY("Account & Security"),
    FINANCES_WALLETS("Finances & Wallets"),
    LOOK_FEEL("Appearance & Experience"),
    SYSTEM_DATA("Data & System"),
    SUPPORT_ABOUT("Support & Credits")
}

enum class SettingsSubPage(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val group: SettingsCategoryGroup
) {
    MY_ACCOUNT(
        title = "My Account",
        subtitle = "Profile info, cloud session & email verification",
        icon = Icons.Default.Person,
        accentColor = Color(0xFF8B5CF6),
        group = SettingsCategoryGroup.ACCOUNT_SECURITY
    ),
    APPEARANCE_THEMES(
        title = "Appearance & Themes",
        subtitle = "Velvet Purple, Midnight Dark & Warm Ivory themes",
        icon = Icons.Default.ColorLens,
        accentColor = Color(0xFFC084FC),
        group = SettingsCategoryGroup.LOOK_FEEL
    ),
    PRIVACY_SECURITY(
        title = "Privacy & Security",
        subtitle = "4-digit PIN lock, biometrics & offline vault protection",
        icon = Icons.Default.Security,
        accentColor = Color(0xFF05D580),
        group = SettingsCategoryGroup.ACCOUNT_SECURITY
    ),
    NOTIFICATIONS(
        title = "Notifications",
        subtitle = "Budget thresholds, daily kharcha & SMS detection alerts",
        icon = Icons.Default.Notifications,
        accentColor = Color(0xFFFFB703),
        group = SettingsCategoryGroup.FINANCES_WALLETS
    ),
    FINANCIAL_PREFERENCES(
        title = "Financial Preferences",
        subtitle = "PKR currency notation, salary cycle & categorization",
        icon = Icons.Default.CurrencyExchange,
        accentColor = Color(0xFF38BDF8),
        group = SettingsCategoryGroup.FINANCES_WALLETS
    ),
    ACCOUNT_WALLET_MGMT(
        title = "Account & Wallet Management",
        subtitle = "EasyPaisa, JazzCash, NayaPay & bank accounts",
        icon = Icons.Default.AccountBalanceWallet,
        accentColor = Color(0xFFE50914),
        group = SettingsCategoryGroup.FINANCES_WALLETS
    ),
    DATA_BACKUP(
        title = "Data Management & Backup",
        subtitle = "Export CSV/JSON, SQLite storage & demo reset",
        icon = Icons.Default.Storage,
        accentColor = Color(0xFF34D399),
        group = SettingsCategoryGroup.SYSTEM_DATA
    ),
    LANGUAGE_ACCESSIBILITY(
        title = "Language & Accessibility",
        subtitle = "Language selection, haptics & high contrast",
        icon = Icons.Default.Language,
        accentColor = Color(0xFFA78BFA),
        group = SettingsCategoryGroup.LOOK_FEEL
    ),
    HELP_SUPPORT(
        title = "Help & Support",
        subtitle = "SMS reading FAQs, permissions & developer contact",
        icon = Icons.Default.HelpOutline,
        accentColor = Color(0xFFF472B6),
        group = SettingsCategoryGroup.SUPPORT_ABOUT
    ),
    ABOUT_APPLICATION(
        title = "About Application",
        subtitle = "Version 2.4.0 • Developed by Muhammad Talha Thaheem",
        icon = Icons.Default.Info,
        accentColor = Color(0xFFFDE68A),
        group = SettingsCategoryGroup.SUPPORT_ABOUT
    ),
    ADVANCED_CONFIG(
        title = "Advanced Configuration",
        subtitle = "SMS regex engine, diagnostic logs & SMS simulator",
        icon = Icons.Default.Tune,
        accentColor = Color(0xFFF97316),
        group = SettingsCategoryGroup.SYSTEM_DATA
    )
}
