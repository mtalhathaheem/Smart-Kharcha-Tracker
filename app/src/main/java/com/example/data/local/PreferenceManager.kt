package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode(val title: String) {
    SYSTEM("System Default"),
    PREMIUM_PURPLE("Royal Velvet Purple (Signature)"),
    DARK("Midnight Dark (AMOLED)"),
    LIGHT("Warm Ivory & Lavender (Light)"),
    EMERALD_MINT("Islamic Banking Emerald & Mint"),
    OCEAN_SAPPHIRE("Fintech Sapphire & Cyan"),
    SUNSET_AMBER("Royal Amber & Gold")
}

class PreferenceManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("smart_kharcha_preferences", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _currencySymbol = MutableStateFlow(loadCurrencySymbol())
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private val _isBalanceMasked = MutableStateFlow(loadBalanceMasked())
    val isBalanceMasked: StateFlow<Boolean> = _isBalanceMasked.asStateFlow()

    private val _autoLockTimeoutSeconds = MutableStateFlow(loadAutoLockTimeout())
    val autoLockTimeoutSeconds: StateFlow<Int> = _autoLockTimeoutSeconds.asStateFlow()

    private fun loadThemeMode(): AppThemeMode {
        val saved = prefs.getString(KEY_THEME_MODE, AppThemeMode.PREMIUM_PURPLE.name)
        return try {
            AppThemeMode.valueOf(saved ?: AppThemeMode.PREMIUM_PURPLE.name)
        } catch (e: Exception) {
            AppThemeMode.PREMIUM_PURPLE
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeMode.value = mode
    }

    private fun loadCurrencySymbol(): String {
        return prefs.getString(KEY_CURRENCY_SYMBOL, "Rs.") ?: "Rs."
    }

    fun setCurrencySymbol(symbol: String) {
        prefs.edit().putString(KEY_CURRENCY_SYMBOL, symbol).apply()
        _currencySymbol.value = symbol
    }

    private fun loadBalanceMasked(): Boolean {
        return prefs.getBoolean(KEY_BALANCE_MASKED, false)
    }

    fun setBalanceMasked(masked: Boolean) {
        prefs.edit().putBoolean(KEY_BALANCE_MASKED, masked).apply()
        _isBalanceMasked.value = masked
    }

    private fun loadAutoLockTimeout(): Int {
        return prefs.getInt(KEY_AUTOLOCK_TIMEOUT_SECONDS, 0) // default: 0 = immediately
    }

    fun setAutoLockTimeoutSeconds(seconds: Int) {
        prefs.edit().putInt(KEY_AUTOLOCK_TIMEOUT_SECONDS, seconds).apply()
        _autoLockTimeoutSeconds.value = seconds
    }

    companion object {
        private const val KEY_THEME_MODE = "key_app_theme_mode"
        private const val KEY_CURRENCY_SYMBOL = "key_currency_symbol"
        private const val KEY_BALANCE_MASKED = "key_balance_masked"
        private const val KEY_AUTOLOCK_TIMEOUT_SECONDS = "key_autolock_timeout_seconds"

        @Volatile
        private var INSTANCE: PreferenceManager? = null

        fun getInstance(context: Context): PreferenceManager {
            return INSTANCE ?: synchronized(this) {
                val instance = PreferenceManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
