package com.example

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.auth.AuthScreen
import com.example.auth.AuthViewModel
import com.example.auth.BiometricHelper
import com.example.ui.MainScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.auth.ForgotPasswordScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.PinLockScreen
import com.example.ui.screens.auth.PinSetupScreen
import com.example.ui.screens.auth.SignUpScreen
import com.example.ui.screens.auth.WelcomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.KharchaViewModel

class MainActivity : FragmentActivity() {

    private val viewModel: KharchaViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private var backgroundTimestamp: Long = 0L

    override fun onStop() {
        super.onStop()
        backgroundTimestamp = System.currentTimeMillis()
    }

    override fun onStart() {
        super.onStart()
        if (backgroundTimestamp > 0L) {
            val elapsedSeconds = (System.currentTimeMillis() - backgroundTimestamp) / 1000
            val timeout = viewModel.autoLockTimeoutSeconds.value
            val authState = authViewModel.uiState.value
            if (authState.isPinEnabled && authState.currentUser != null && authState.isSessionUnlocked) {
                if (timeout == 0 || (timeout > 0 && elapsedSeconds >= timeout)) {
                    authViewModel.lockSession()
                }
            }
        }
        backgroundTimestamp = 0L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val currentTheme by viewModel.themeMode.collectAsStateWithLifecycle()
            val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
            var showSplash by remember { mutableStateOf(true) }

            // Handle back presses during auth sub-screens
            if (!showSplash && authUiState.currentUser == null) {
                BackHandler(enabled = authUiState.currentScreen != AuthScreen.WELCOME) {
                    authViewModel.navigateTo(AuthScreen.WELCOME)
                }
            } else if (!showSplash && authUiState.currentScreen == AuthScreen.PIN_SETUP) {
                BackHandler {
                    authViewModel.clearPinInput()
                    authViewModel.navigateTo(AuthScreen.WELCOME)
                }
            }

            MyApplicationTheme(themeMode = currentTheme) {
                Crossfade(
                    targetState = showSplash,
                    animationSpec = tween(durationMillis = 400),
                    label = "splash_crossfade"
                ) { isSplash ->
                    if (isSplash) {
                        SplashScreen(
                            onSplashFinished = { showSplash = false }
                        )
                    } else {
                        // Routing based on authentication and security status
                        val user = authUiState.currentUser
                        if (user == null) {
                            // Unauthenticated user
                            when (authUiState.currentScreen) {
                                AuthScreen.WELCOME -> {
                                    WelcomeScreen(
                                        isLoading = authUiState.isLoading,
                                        onNavigateToLogin = { authViewModel.navigateTo(AuthScreen.LOGIN) },
                                        onNavigateToSignUp = { authViewModel.navigateTo(AuthScreen.SIGN_UP) },
                                        onGoogleSignInClick = {
                                            authViewModel.loginWithGoogle(this@MainActivity) {}
                                        }
                                    )
                                }
                                AuthScreen.LOGIN -> {
                                    LoginScreen(
                                        initialEmail = authUiState.savedEmail,
                                        initialRememberMe = authUiState.rememberMe,
                                        isLoading = authUiState.isLoading,
                                        errorMessage = authUiState.errorMessage,
                                        onLoginClick = { email, pass ->
                                            authViewModel.loginWithEmail(email, pass) {}
                                        },
                                        onGoogleSignInClick = {
                                            authViewModel.loginWithGoogle(this@MainActivity) {}
                                        },
                                        onNavigateToSignUp = { authViewModel.navigateTo(AuthScreen.SIGN_UP) },
                                        onNavigateToForgotPassword = { authViewModel.navigateTo(AuthScreen.FORGOT_PASSWORD) },
                                        onNavigateBack = { authViewModel.navigateTo(AuthScreen.WELCOME) },
                                        onRememberMeChanged = { authViewModel.setRememberMe(it) }
                                    )
                                }
                                AuthScreen.SIGN_UP -> {
                                    SignUpScreen(
                                        isLoading = authUiState.isLoading,
                                        errorMessage = authUiState.errorMessage,
                                        onSignUpClick = { name, email, pass, confirmPass ->
                                            authViewModel.signUpWithEmail(name, email, pass, confirmPass) {}
                                        },
                                        onGoogleSignInClick = {
                                            authViewModel.loginWithGoogle(this@MainActivity) {}
                                        },
                                        onNavigateToLogin = { authViewModel.navigateTo(AuthScreen.LOGIN) },
                                        onNavigateBack = { authViewModel.navigateTo(AuthScreen.WELCOME) }
                                    )
                                }
                                AuthScreen.FORGOT_PASSWORD -> {
                                    ForgotPasswordScreen(
                                        isLoading = authUiState.isLoading,
                                        errorMessage = authUiState.errorMessage,
                                        successMessage = authUiState.successMessage,
                                        onSendResetLink = { email ->
                                            authViewModel.sendPasswordReset(email)
                                        },
                                        onNavigateBack = { authViewModel.navigateTo(AuthScreen.LOGIN) }
                                    )
                                }
                                else -> {
                                    WelcomeScreen(
                                        isLoading = authUiState.isLoading,
                                        onNavigateToLogin = { authViewModel.navigateTo(AuthScreen.LOGIN) },
                                        onNavigateToSignUp = { authViewModel.navigateTo(AuthScreen.SIGN_UP) },
                                        onGoogleSignInClick = {
                                            authViewModel.loginWithGoogle(this@MainActivity) {}
                                        }
                                    )
                                }
                            }
                        } else {
                            // User is authenticated
                            if (authUiState.isPinEnabled && !authUiState.isSessionUnlocked) {
                                // Locked state requiring PIN or Biometric
                                PinLockScreen(
                                    userName = user.displayName ?: authUiState.savedEmail.substringBefore("@"),
                                    userEmail = user.email ?: "",
                                    pinInput = authUiState.pinInput,
                                    errorMessage = authUiState.errorMessage,
                                    isBiometricAvailable = BiometricHelper.isBiometricAvailable(this@MainActivity),
                                    isBiometricEnabled = authUiState.isBiometricEnabled,
                                    onDigitClick = { digit ->
                                        authViewModel.appendPinDigit(digit) {
                                            authViewModel.verifyEnteredPin {}
                                        }
                                    },
                                    onBackspaceClick = { authViewModel.deletePinDigit() },
                                    onBiometricClick = {
                                        BiometricHelper.showBiometricPrompt(
                                            activity = this@MainActivity,
                                            onSuccess = { authViewModel.unlockViaBiometric {} },
                                            onError = { /* fallback */ },
                                            onCancel = { /* fallback */ }
                                        )
                                    },
                                    onSignOutClick = {
                                        authViewModel.signOut {}
                                    }
                                )
                            } else if (authUiState.currentScreen == AuthScreen.PIN_SETUP) {
                                // Explicit PIN setup requested from Settings
                                PinSetupScreen(
                                    step = authUiState.pinSetupStep,
                                    pinInput = authUiState.pinInput,
                                    errorMessage = authUiState.errorMessage,
                                    onDigitClick = { digit ->
                                        authViewModel.appendPinDigit(digit) {
                                            authViewModel.setupPinStep {}
                                        }
                                    },
                                    onBackspaceClick = { authViewModel.deletePinDigit() },
                                    onNavigateBack = {
                                        authViewModel.clearPinInput()
                                        authViewModel.navigateTo(AuthScreen.WELCOME)
                                    }
                                )
                            } else {
                                // Fully unlocked app dashboard
                                MainScreen(
                                    viewModel = viewModel,
                                    authViewModel = authViewModel,
                                    onLockSession = { authViewModel.lockSession() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
