package com.example.auth

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthScreen {
    WELCOME,
    LOGIN,
    SIGN_UP,
    FORGOT_PASSWORD,
    PIN_LOCK,
    PIN_SETUP
}

data class AuthUiState(
    val currentScreen: AuthScreen = AuthScreen.WELCOME,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val currentUser: FirebaseUser? = null,
    val isEmailVerified: Boolean = false,
    val rememberMe: Boolean = false,
    val savedEmail: String = "",
    val isPinEnabled: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val isSessionUnlocked: Boolean = false,
    val pinInput: String = "",
    val pinSetupStep: Int = 1,
    val temporaryPin: String = ""
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository(application)
    private val prefs = AuthPreferences(application)

    private val _uiState = MutableStateFlow(
        AuthUiState(
            currentUser = repository.currentUser,
            isEmailVerified = repository.currentUser?.isEmailVerified == true,
            rememberMe = prefs.rememberMe,
            savedEmail = if (prefs.rememberMe) prefs.savedEmail else "",
            isPinEnabled = prefs.isPinEnabled,
            isBiometricEnabled = prefs.isBiometricEnabled,
            isSessionUnlocked = !prefs.isPinEnabled, // unlocked if no PIN is set
            currentScreen = determineInitialScreen()
        )
    )
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.authStateFlow.collect { user ->
                _uiState.update { current ->
                    current.copy(
                        currentUser = user,
                        isEmailVerified = user?.isEmailVerified == true,
                        currentScreen = if (user != null) {
                            if (prefs.isPinEnabled && !current.isSessionUnlocked) {
                                AuthScreen.PIN_LOCK
                            } else {
                                current.currentScreen
                            }
                        } else {
                            if (current.currentScreen == AuthScreen.PIN_LOCK || current.currentScreen == AuthScreen.PIN_SETUP) {
                                AuthScreen.WELCOME
                            } else {
                                current.currentScreen
                            }
                        }
                    )
                }
            }
        }
    }

    private fun determineInitialScreen(): AuthScreen {
        val user = repository.currentUser
        return if (user != null) {
            if (prefs.isPinEnabled) AuthScreen.PIN_LOCK else AuthScreen.WELCOME
        } else {
            AuthScreen.WELCOME
        }
    }

    fun navigateTo(screen: AuthScreen) {
        _uiState.update { it.copy(currentScreen = screen, errorMessage = null, successMessage = null, pinInput = "") }
    }

    fun setRememberMe(enabled: Boolean) {
        prefs.rememberMe = enabled
        _uiState.update { it.copy(rememberMe = enabled) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun loginWithEmail(email: String, pass: String, onSuccess: () -> Unit) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter both email and password.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.signInWithEmail(email, pass)
            result.onSuccess { user ->
                if (prefs.rememberMe) {
                    prefs.savedEmail = email.trim()
                } else {
                    prefs.savedEmail = ""
                }
                prefs.displayName = user.displayName ?: ""
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentUser = user,
                        isEmailVerified = user.isEmailVerified,
                        isSessionUnlocked = !prefs.isPinEnabled,
                        currentScreen = if (prefs.isPinEnabled) AuthScreen.PIN_LOCK else AuthScreen.WELCOME
                    )
                }
                onSuccess()
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err.localizedMessage ?: "Sign in failed. Check your credentials."
                    )
                }
            }
        }
    }

    fun signUpWithEmail(fullName: String, email: String, pass: String, confirmPass: String, onSuccess: () -> Unit) {
        if (fullName.isBlank() || email.isBlank() || pass.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please fill in all required fields.") }
            return
        }
        if (pass.length < 6) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters long.") }
            return
        }
        if (pass != confirmPass) {
            _uiState.update { it.copy(errorMessage = "Passwords do not match.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.signUpWithEmail(email, pass, fullName)
            result.onSuccess { user ->
                if (prefs.rememberMe) {
                    prefs.savedEmail = email.trim()
                }
                prefs.displayName = fullName.trim()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentUser = user,
                        isEmailVerified = user.isEmailVerified,
                        isSessionUnlocked = true,
                        successMessage = "Account created successfully! Verification email sent."
                    )
                }
                onSuccess()
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err.localizedMessage ?: "Failed to create account. Please try again."
                    )
                }
            }
        }
    }

    fun loginWithGoogle(activityContext: Context, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.signInWithGoogle(activityContext)
            result.onSuccess { user ->
                if (prefs.rememberMe) {
                    prefs.savedEmail = user.email ?: ""
                }
                prefs.displayName = user.displayName ?: ""
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentUser = user,
                        isEmailVerified = user.isEmailVerified,
                        isSessionUnlocked = !prefs.isPinEnabled,
                        currentScreen = if (prefs.isPinEnabled) AuthScreen.PIN_LOCK else AuthScreen.WELCOME
                    )
                }
                onSuccess()
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err.localizedMessage ?: "Google sign in cancelled or failed."
                    )
                }
            }
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter your email address.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            val result = repository.sendPasswordReset(email)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "Password reset instructions sent to $email"
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err.localizedMessage ?: "Failed to send reset email."
                    )
                }
            }
        }
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.sendEmailVerification()
            result.onSuccess {
                _uiState.update {
                    it.copy(isLoading = false, successMessage = "Verification email sent. Please check your inbox.")
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = err.localizedMessage ?: "Failed to send verification email.")
                }
            }
        }
    }

    fun refreshEmailVerification() {
        viewModelScope.launch {
            val result = repository.reloadUser()
            result.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        currentUser = user,
                        isEmailVerified = user.isEmailVerified,
                        successMessage = if (user.isEmailVerified) "Email verified successfully!" else "Email not verified yet."
                    )
                }
            }
        }
    }

    // PIN Pad inputs
    fun appendPinDigit(digit: String, onComplete: ((String) -> Unit)? = null) {
        val currentInput = _uiState.value.pinInput
        if (currentInput.length < 4) {
            val newInput = currentInput + digit
            _uiState.update { it.copy(pinInput = newInput, errorMessage = null) }
            if (newInput.length == 4) {
                onComplete?.invoke(newInput)
            }
        }
    }

    fun deletePinDigit() {
        val currentInput = _uiState.value.pinInput
        if (currentInput.isNotEmpty()) {
            _uiState.update { it.copy(pinInput = currentInput.dropLast(1), errorMessage = null) }
        }
    }

    fun clearPinInput() {
        _uiState.update { it.copy(pinInput = "", errorMessage = null) }
    }

    fun verifyEnteredPin(onSuccess: () -> Unit) {
        val pin = _uiState.value.pinInput
        if (pin.length != 4) return

        if (prefs.verifyPin(pin)) {
            _uiState.update {
                it.copy(
                    isSessionUnlocked = true,
                    pinInput = "",
                    errorMessage = null
                )
            }
            onSuccess()
        } else {
            _uiState.update {
                it.copy(
                    pinInput = "",
                    errorMessage = "Incorrect PIN. Please try again."
                )
            }
        }
    }

    fun setupPinStep(onFinished: () -> Unit) {
        val pin = _uiState.value.pinInput
        if (pin.length != 4) return

        if (_uiState.value.pinSetupStep == 1) {
            _uiState.update {
                it.copy(
                    temporaryPin = pin,
                    pinInput = "",
                    pinSetupStep = 2,
                    errorMessage = null
                )
            }
        } else {
            if (pin == _uiState.value.temporaryPin) {
                prefs.setPin(pin)
                _uiState.update {
                    it.copy(
                        isPinEnabled = true,
                        isSessionUnlocked = true,
                        pinInput = "",
                        pinSetupStep = 1,
                        temporaryPin = "",
                        successMessage = "Security PIN set successfully!",
                        currentScreen = AuthScreen.WELCOME
                    )
                }
                onFinished()
            } else {
                _uiState.update {
                    it.copy(
                        pinInput = "",
                        pinSetupStep = 1,
                        temporaryPin = "",
                        errorMessage = "PINs did not match. Please start again."
                    )
                }
            }
        }
    }

    fun unlockViaBiometric(onSuccess: () -> Unit) {
        _uiState.update {
            it.copy(
                isSessionUnlocked = true,
                pinInput = "",
                errorMessage = null
            )
        }
        onSuccess()
    }

    fun toggleBiometric(enabled: Boolean) {
        prefs.isBiometricEnabled = enabled
        _uiState.update { it.copy(isBiometricEnabled = enabled) }
    }

    fun removePin() {
        prefs.clearPin()
        _uiState.update {
            it.copy(
                isPinEnabled = false,
                isBiometricEnabled = false,
                isSessionUnlocked = true,
                pinInput = "",
                pinSetupStep = 1,
                temporaryPin = "",
                successMessage = "PIN lock disabled."
            )
        }
    }

    fun lockSession() {
        if (prefs.isPinEnabled) {
            _uiState.update {
                it.copy(
                    isSessionUnlocked = false,
                    currentScreen = AuthScreen.PIN_LOCK,
                    pinInput = "",
                    errorMessage = null
                )
            }
        }
    }

    fun signOut(onSignedOut: () -> Unit) {
        repository.signOut()
        prefs.clearSession()
        _uiState.update {
            it.copy(
                currentUser = null,
                isEmailVerified = false,
                isSessionUnlocked = false,
                currentScreen = AuthScreen.WELCOME,
                pinInput = "",
                errorMessage = null,
                successMessage = null
            )
        }
        onSignedOut()
    }

    fun deleteAccount(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.deleteAccount()
            result.onSuccess {
                prefs.clearPin()
                prefs.clearSession()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentUser = null,
                        isEmailVerified = false,
                        isSessionUnlocked = false,
                        currentScreen = AuthScreen.WELCOME,
                        successMessage = "Account deleted successfully."
                    )
                }
                onSuccess()
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err.localizedMessage ?: "Failed to delete account. You may need to re-login."
                    )
                }
            }
        }
    }
}
