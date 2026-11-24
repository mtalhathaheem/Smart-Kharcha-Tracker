package com.example.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class FaqItem(val question: String, val answer: String)

@Composable
fun HelpSupportSubScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val faqs = remember {
        listOf(
            FaqItem(
                "Does Smart Kharcha send my bank SMS to any cloud server?",
                "No! Your banking SMS messages and financial details are parsed 100% on-device and stored in an encrypted local SQLite database. Nothing is ever sent to external third parties."
            ),
            FaqItem(
                "How does Smart Kharcha prevent duplicate transactions?",
                "Every captured SMS transaction computes a deterministic cryptographic hash based on the sender (e.g. 3737), transaction amount, and timestamp. Duplicate SMS alerts are automatically filtered out."
            ),
            FaqItem(
                "Which Pakistani banks and digital wallets are supported?",
                "EasyPaisa (3737), JazzCash (8558), NayaPay, SadaPay, Meezan Bank, Habib Bank (HBL), Bank Alfalah, UBL, Standard Chartered, and manual Cash in Hand."
            ),
            FaqItem(
                "What if an incoming SMS was not captured?",
                "You can tap 'Rescan Inbox' in Advanced Configuration or Settings to re-read recent messages, or log it in seconds using the '+' Floating Action Button."
            )
        )
    }

    val expandedStates = remember { mutableStateMapOf<Int, Boolean>() }

    Column(modifier = modifier.fillMaxSize()) {
        SettingsSubPageHeader(
            title = "Help & Support",
            subtitle = "FAQs, SMS guide & developer assistance",
            icon = Icons.Default.HelpOutline,
            accentColor = Color(0xFFF472B6),
            onNavigateBack = onNavigateBack
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // SMS Guide Banner
            item {
                SettingsCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Sms,
                            contentDescription = null,
                            tint = Color(0xFFF472B6),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "How SMS Expense Parsing Works",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Automated transaction capture in Pakistan",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "When you make a payment or receive funds via EasyPaisa (3737), JazzCash (8558), NayaPay, or your bank, Android notifies Smart Kharcha's broadcast receiver. The regex parser identifies amount, merchant, and timestamp, instantly categorizing it into your budget.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }

            // FAQs
            item {
                SettingsSectionHeader(title = "Frequently Asked Questions")
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    faqs.forEachIndexed { index, faq ->
                        val isExpanded = expandedStates[index] ?: false
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { expandedStates[index] = !isExpanded },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = faq.question,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                AnimatedVisibility(visible = isExpanded) {
                                    Column {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = faq.answer,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Developer Contact
            item {
                SettingsSectionHeader(title = "Developer Support")
                SettingsCard {
                    Text(
                        text = "Have questions, feedback, or need a custom bank regex added? Reach out directly to the developer:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:mtsindhithahee@gmail.com")
                                putExtra(Intent.EXTRA_SUBJECT, "Smart Kharcha Tracker - Support & Feedback")
                            }
                            try {
                                context.startActivity(emailIntent)
                            } catch (e: Exception) {
                                // Fallback
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("contact_developer_support_button")
                    ) {
                        Icon(Icons.Default.Mail, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Email Muhammad Talha Thaheem", fontWeight = FontWeight.Bold)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}
