package com.example.auth

import android.content.Context
import android.content.SharedPreferences
import java.security.MessageDigest

class AuthPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("smart_kharcha_auth_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_REMEMBER_ME = "key_remember_me"
        private const val KEY_SAVED_EMAIL = "key_saved_email"
        private const val KEY_PIN_HASH = "key_pin_hash"
        private const val KEY_PIN_ENABLED = "key_pin_enabled"
        private const val KEY_BIOMETRIC_ENABLED = "key_biometric_enabled"
        private const val KEY_DISPLAY_NAME = "key_display_name"
        private const val PIN_SALT = "SmartKharchaSecureSaltPK2026"
    }

    var rememberMe: Boolean
        get() = prefs.getBoolean(KEY_REMEMBER_ME, false)
        set(value) = prefs.edit().putBoolean(KEY_REMEMBER_ME, value).apply()

    var savedEmail: String
        get() = prefs.getString(KEY_SAVED_EMAIL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_SAVED_EMAIL, value).apply()

    var displayName: String
        get() = prefs.getString(KEY_DISPLAY_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_DISPLAY_NAME, value).apply()

    var isPinEnabled: Boolean
        get() = prefs.getBoolean(KEY_PIN_ENABLED, false) && pinHash.isNotEmpty()
        set(value) = prefs.edit().putBoolean(KEY_PIN_ENABLED, value).apply()

    var isBiometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, value).apply()

    var pinHash: String
        get() = prefs.getString(KEY_PIN_HASH, "") ?: ""
        private set(value) = prefs.edit().putString(KEY_PIN_HASH, value).apply()

    fun setPin(pin: String) {
        val hash = hashPin(pin)
        pinHash = hash
        isPinEnabled = true
    }

    fun verifyPin(pin: String): Boolean {
        if (!isPinEnabled || pinHash.isEmpty()) return true
        val candidateHash = hashPin(pin)
        return candidateHash == pinHash
    }

    fun clearPin() {
        prefs.edit()
            .remove(KEY_PIN_HASH)
            .putBoolean(KEY_PIN_ENABLED, false)
            .putBoolean(KEY_BIOMETRIC_ENABLED, false)
            .apply()
    }

    fun clearSession() {
        if (!rememberMe) {
            prefs.edit().remove(KEY_SAVED_EMAIL).apply()
        }
    }

    private fun hashPin(pin: String): String {
        val input = "$PIN_SALT:$pin"
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
