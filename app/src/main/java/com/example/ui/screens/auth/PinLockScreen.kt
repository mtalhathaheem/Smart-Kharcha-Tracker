package com.example.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricLavender
import com.example.ui.theme.LuxuryPurpleDark
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.SurfaceGlassLight
import com.example.ui.theme.WarmIvory

@Composable
fun PinLockScreen(
    userName: String,
    userEmail: String,
    pinInput: String,
    errorMessage: String?,
    isBiometricAvailable: Boolean,
    isBiometricEnabled: Boolean,
    onDigitClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onBiometricClick: () -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Attempt biometric prompt on initial launch if enabled
    LaunchedEffect(isBiometricEnabled) {
        if (isBiometricEnabled && isBiometricAvailable) {
            onBiometricClick()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        LuxuryPurpleDark,
                        Color(0xFF140A28),
                        Color(0xFF090314)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                // User Avatar Circle
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(ElectricLavender, Color(0xFF6B46C1))
                            )
                        )
                        .border(2.dp, WarmIvory.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = if (userName.isNotBlank()) {
                        userName.take(1).uppercase()
                    } else if (userEmail.isNotBlank()) {
                        userEmail.take(1).uppercase()
                    } else {
                        "K"
                    }
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = LuxuryPurpleDark
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (userName.isNotBlank()) userName else "Smart Kharcha Vault",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = WarmIvory
                )

                if (userEmail.isNotBlank()) {
                    Text(
                        text = userEmail,
                        style = MaterialTheme.typography.bodySmall,
                        color = ElectricLavender.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Enter 4-Digit Security PIN",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = WarmIvory.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // PIN Dots Indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < pinInput.length
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFilled) ElectricLavender else Color.Transparent
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (isFilled) ElectricLavender else WarmIvory.copy(alpha = 0.4f),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                // Error Text
                AnimatedVisibility(
                    visible = !errorMessage.isNullOrBlank(),
                    enter = fadeIn() + scaleIn()
                ) {
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = NeonCrimson,
                        modifier = Modifier.padding(top = 14.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Keypad
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val row1 = listOf("1", "2", "3")
                val row2 = listOf("4", "5", "6")
                val row3 = listOf("7", "8", "9")

                KeypadRow(digits = row1, onDigitClick = onDigitClick)
                KeypadRow(digits = row2, onDigitClick = onDigitClick)
                KeypadRow(digits = row3, onDigitClick = onDigitClick)

                // Bottom row: Biometric / empty, 0, Backspace
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Biometric button
                    if (isBiometricAvailable && isBiometricEnabled) {
                        IconButton(
                            onClick = onBiometricClick,
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(SurfaceGlassLight)
                                .testTag("pin_biometric_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = "Biometric Unlock",
                                tint = ElectricLavender,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(70.dp))
                    }

                    // Digit '0'
                    KeypadButton(digit = "0", onClick = { onDigitClick("0") })

                    // Backspace
                    IconButton(
                        onClick = onBackspaceClick,
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(SurfaceGlassLight)
                            .testTag("pin_backspace_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = "Backspace",
                            tint = WarmIvory,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            // Footer Actions
            TextButton(
                onClick = onSignOutClick,
                modifier = Modifier.testTag("pin_sign_out_button")
            ) {
                Text(
                    text = "Forgot PIN? Sign Out",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = ElectricLavender.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun KeypadRow(
    digits: List<String>,
    onDigitClick: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        digits.forEach { digit ->
            KeypadButton(digit = digit, onClick = { onDigitClick(digit) })
        }
    }
}

@Composable
private fun KeypadButton(
    digit: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(70.dp)
            .clip(CircleShape)
            .background(SurfaceGlassLight)
            .border(1.dp, WarmIvory.copy(alpha = 0.12f), CircleShape)
            .clickable(onClick = onClick)
            .testTag("pin_key_$digit"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = digit,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = WarmIvory
        )
    }
}
