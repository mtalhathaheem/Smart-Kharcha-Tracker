package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.local.AppThemeMode

// 1. Light Mode: Warm Ivory & Soft Lavender with Deep Violet Accents
private val LightColorScheme = lightColorScheme(
    primary = RichPurplePrimary,
    onPrimary = Color.White,
    primaryContainer = SoftLavenderLight,
    onPrimaryContainer = DeepPurpleDark,
    secondary = BrightVioletPrimary,
    onSecondary = Color.White,
    secondaryContainer = SoftLavender,
    onSecondaryContainer = DeepPurpleDark,
    tertiary = WarningAmber,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFEF3C7),
    onTertiaryContainer = Color(0xFF78350F),
    background = WarmIvoryBackground,
    onBackground = Color(0xFF160B24),
    surface = WarmIvorySurface,
    onSurface = Color(0xFF160B24),
    surfaceVariant = WarmIvoryCard,
    onSurfaceVariant = Color(0xFF4C3E59),
    outline = WarmIvoryBorder,
    outlineVariant = Color(0xFFE9E2D5)
)

// 2. Dark Mode: Midnight Violet & Electric Lavender
private val DarkColorScheme = darkColorScheme(
    primary = ElectricLavender,
    onPrimary = DeepPurpleDark,
    primaryContainer = Color(0xFF381466),
    onPrimaryContainer = SoftLavenderLight,
    secondary = LuminousLavender,
    onSecondary = Color(0xFF1F073B),
    secondaryContainer = Color(0xFF280B4A),
    onSecondaryContainer = SoftLavender,
    tertiary = WarningAmber,
    onTertiary = Color(0xFF451A03),
    tertiaryContainer = WarningAmberContainer,
    onTertiaryContainer = Color(0xFFFEF3C7),
    background = MidnightDarkBackground,
    onBackground = Color(0xFFFAF5FF),
    surface = MidnightDarkSurface,
    onSurface = Color(0xFFFAF5FF),
    surfaceVariant = MidnightDarkCard,
    onSurfaceVariant = Color(0xFFD6C7E8),
    outline = MidnightDarkBorder,
    outlineVariant = Color(0x1AD8B4FE)
)

// 3. Premium Purple Mode: Royal Velvet Night, Deep Amethyst & Glowing Lavender
private val PremiumPurpleColorScheme = darkColorScheme(
    primary = LuminousLavender,
    onPrimary = DeepPurpleDark,
    primaryContainer = RoyalPurpleVelvet,
    onPrimaryContainer = SoftLavenderLight,
    secondary = ElectricLavender,
    onSecondary = Color(0xFF1B0333),
    secondaryContainer = Color(0xFF320857),
    onSecondaryContainer = Color(0xFFEEDCFF),
    tertiary = WarningAmber,
    onTertiary = Color(0xFF3D2000),
    tertiaryContainer = Color(0xFF6B3E0A),
    onTertiaryContainer = Color(0xFFFEF3C7),
    background = VelvetNightBackground,
    onBackground = Color(0xFFFBF7FF),
    surface = VelvetNightSurface,
    onSurface = Color(0xFFFBF7FF),
    surfaceVariant = VelvetNightCard,
    onSurfaceVariant = Color(0xFFE2D0F8),
    outline = VelvetNightBorder,
    outlineVariant = Color(0x24D8B4FE)
)

// 4. Islamic Banking Emerald & Mint Scheme
private val EmeraldMintColorScheme = darkColorScheme(
    primary = EmeraldMintPrimary,
    onPrimary = EmeraldForestDark,
    primaryContainer = EmeraldMintContainer,
    onPrimaryContainer = EmeraldMintAccent,
    secondary = EmeraldMintAccent,
    onSecondary = EmeraldForestDark,
    secondaryContainer = Color(0xFF074032),
    onSecondaryContainer = Color(0xFFD1FAE5),
    tertiary = AmberGoldPrimary,
    onTertiary = Color(0xFF451A03),
    tertiaryContainer = AmberGoldContainer,
    onTertiaryContainer = AmberGoldAccent,
    background = EmeraldForestDark,
    onBackground = Color(0xFFECFDF5),
    surface = EmeraldForestSurface,
    onSurface = Color(0xFFECFDF5),
    surfaceVariant = EmeraldForestCard,
    onSurfaceVariant = Color(0xFFA7F3D0),
    outline = Color(0x3305D580),
    outlineVariant = Color(0x1A05D580)
)

// 5. Fintech Ocean Sapphire Scheme
private val OceanSapphireColorScheme = darkColorScheme(
    primary = SapphirePrimary,
    onPrimary = SapphireNavyDark,
    primaryContainer = SapphireContainer,
    onPrimaryContainer = SapphireCyanAccent,
    secondary = SapphireCyanAccent,
    onSecondary = SapphireNavyDark,
    secondaryContainer = Color(0xFF0F2642),
    onSecondaryContainer = Color(0xFFCAF0F8),
    tertiary = WarningAmber,
    onTertiary = Color(0xFF451A03),
    tertiaryContainer = WarningAmberContainer,
    onTertiaryContainer = Color(0xFFFEF3C7),
    background = SapphireNavyDark,
    onBackground = Color(0xFFF0F9FF),
    surface = SapphireNavySurface,
    onSurface = Color(0xFFF0F9FF),
    surfaceVariant = SapphireNavyCard,
    onSurfaceVariant = Color(0xFFBAE6FD),
    outline = Color(0x3300B4D8),
    outlineVariant = Color(0x1A00B4D8)
)

// 6. Royal Sunset Amber & Gold Scheme
private val SunsetAmberColorScheme = darkColorScheme(
    primary = AmberGoldPrimary,
    onPrimary = ObsidianAmberDark,
    primaryContainer = AmberGoldContainer,
    onPrimaryContainer = AmberGoldAccent,
    secondary = AmberGoldAccent,
    onSecondary = ObsidianAmberDark,
    secondaryContainer = Color(0xFF33200B),
    onSecondaryContainer = Color(0xFFFEF3C7),
    tertiary = IncomeGreen,
    onTertiary = Color(0xFF022C22),
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = Color(0xFFA7F3D0),
    background = ObsidianAmberDark,
    onBackground = Color(0xFFFFFBEB),
    surface = ObsidianAmberSurface,
    onSurface = Color(0xFFFFFBEB),
    surfaceVariant = ObsidianAmberCard,
    onSurfaceVariant = Color(0xFFFDE68A),
    outline = Color(0x33F59E0B),
    outlineVariant = Color(0x1AF59E0B)
)

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.PREMIUM_PURPLE,
    content: @Composable () -> Unit,
) {
    val isSystemDark = isSystemInDarkTheme()

    val colorScheme = when (themeMode) {
        AppThemeMode.LIGHT -> LightColorScheme
        AppThemeMode.DARK -> DarkColorScheme
        AppThemeMode.PREMIUM_PURPLE -> PremiumPurpleColorScheme
        AppThemeMode.EMERALD_MINT -> EmeraldMintColorScheme
        AppThemeMode.OCEAN_SAPPHIRE -> OceanSapphireColorScheme
        AppThemeMode.SUNSET_AMBER -> SunsetAmberColorScheme
        AppThemeMode.SYSTEM -> if (isSystemDark) DarkColorScheme else LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
