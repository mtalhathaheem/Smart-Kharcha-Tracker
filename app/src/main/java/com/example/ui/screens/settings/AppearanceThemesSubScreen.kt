package com.example.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.data.local.AppThemeMode
import com.example.ui.theme.ElectricLavender
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonCrimson
import com.example.ui.viewmodel.KharchaViewModel

@Composable
fun AppearanceThemesSubScreen(
    viewModel: KharchaViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTheme by viewModel.themeMode.collectAsStateWithLifecycle()
    var isAmoledBlack by remember { mutableStateOf(false) }
    var isHighContrast by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        SettingsSubPageHeader(
            title = "Appearance & Themes",
            subtitle = "Palette, dark mode & visual aesthetics",
            icon = Icons.Default.ColorLens,
            accentColor = Color(0xFFC084FC),
            onNavigateBack = onNavigateBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Theme Live Preview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LIVE THEME PREVIEW",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = currentTheme.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Mini Dashboard Mockup Preview
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Total Monthly Spending",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Rs. 42,500",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = NeonEmerald.copy(alpha = 0.15f),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "+Rs. 85,000 In",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonEmerald,
                                            modifier = Modifier.padding(6.dp)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = NeonCrimson.copy(alpha = 0.15f),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = "-Rs. 42,500 Out",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonCrimson,
                                            modifier = Modifier.padding(6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Theme Options Selector
            item {
                SettingsSectionHeader(title = "Fintech Color Schemes")
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppThemeMode.entries.forEach { mode ->
                        val isSelected = currentTheme == mode
                        val (primaryColor, surfaceColor, desc, icon) = when (mode) {
                            AppThemeMode.PREMIUM_PURPLE -> listOf(
                                Color(0xFFC084FC),
                                Color(0xFF130424),
                                "Revolut-inspired signature purple & electric lavender",
                                Icons.Default.Palette
                            )
                            AppThemeMode.DARK -> listOf(
                                Color(0xFF8B5CF6),
                                Color(0xFF090410),
                                "High-contrast dark mode for nighttime battery saving",
                                Icons.Default.DarkMode
                            )
                            AppThemeMode.LIGHT -> listOf(
                                Color(0xFF6B21A8),
                                Color(0xFFFAF7F2),
                                "Soft cream & warm ivory light theme with high legibility",
                                Icons.Default.LightMode
                            )
                            AppThemeMode.EMERALD_MINT -> listOf(
                                Color(0xFF05D580),
                                Color(0xFF032219),
                                "Islamic banking deep emerald green with fresh mint accents",
                                Icons.Default.ColorLens
                            )
                            AppThemeMode.OCEAN_SAPPHIRE -> listOf(
                                Color(0xFF00B4D8),
                                Color(0xFF06101E),
                                "Fintech oceanic navy with electric cyan highlights",
                                Icons.Default.Contrast
                            )
                            AppThemeMode.SUNSET_AMBER -> listOf(
                                Color(0xFFF59E0B),
                                Color(0xFF0F0B06),
                                "Royal obsidian black with warm amber & gold accents",
                                Icons.Default.Palette
                            )
                            AppThemeMode.SYSTEM -> listOf(
                                Color(0xFF8B5CF6),
                                Color(0xFF2B2D42),
                                "Automatically switches with Android OS day/night mode",
                                Icons.Default.PhoneAndroid
                            )
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setThemeMode(mode) }
                                .testTag("theme_mode_${mode.name.lowercase()}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Color swatches indicator
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(surfaceColor as Color)
                                        .border(2.dp, primaryColor as Color, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon as androidx.compose.ui.graphics.vector.ImageVector,
                                        contentDescription = null,
                                        tint = primaryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = mode.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = desc as String,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Display Enhancements
            item {
                SettingsSectionHeader(title = "Display Adjustments")
                SettingsCard {
                    SettingsToggleRow(
                        title = "AMOLED Pitch Black",
                        subtitle = "Uses pure #000000 background pixels on OLED screens",
                        checked = isAmoledBlack,
                        onCheckedChange = { isAmoledBlack = it },
                        icon = Icons.Default.DarkMode,
                        iconTint = MaterialTheme.colorScheme.primary
                    )

                    SettingsToggleRow(
                        title = "High Contrast Amounts",
                        subtitle = "Bolds transaction figures and enhances green/red contrasts",
                        checked = isHighContrast,
                        onCheckedChange = { isHighContrast = it },
                        icon = Icons.Default.Contrast,
                        iconTint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}
