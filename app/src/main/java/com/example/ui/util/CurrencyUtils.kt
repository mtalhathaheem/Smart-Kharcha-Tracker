package com.example.ui.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CurrencyUtils {

    private val pkrFormatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("en-PK")).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }

    fun formatPkr(amount: Double, prefix: String = "Rs."): String {
        val formatted = pkrFormatter.format(amount)
        return if (prefix.isNotBlank()) "$prefix $formatted" else formatted
    }

    fun formatPkrOrMasked(amount: Double, isMasked: Boolean, prefix: String = "Rs."): String {
        if (isMasked) return "$prefix ••••••"
        return formatPkr(amount, prefix)
    }

    fun formatCompactPkr(amount: Double, prefix: String = "Rs."): String {
        return when {
            amount >= 1_000_000 -> String.format(Locale.ROOT, "%s %.1fM", prefix, amount / 1_000_000.0)
            amount >= 1_000 -> String.format(Locale.ROOT, "%s %.1fK", prefix, amount / 1_000.0)
            else -> formatPkr(amount, prefix)
        }
    }

    fun formatCompactPkrOrMasked(amount: Double, isMasked: Boolean, prefix: String = "Rs."): String {
        if (isMasked) return "$prefix ••••"
        return formatCompactPkr(amount, prefix)
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM, yyyy - hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun getMonthYearKey(timestamp: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.ROOT)
        return sdf.format(Date(timestamp))
    }

    fun getMonthYearDisplay(monthYear: String): String {
        return try {
            val sdfInput = SimpleDateFormat("yyyy-MM", Locale.ROOT)
            val date = sdfInput.parse(monthYear) ?: return monthYear
            val sdfOutput = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            sdfOutput.format(date)
        } catch (e: Exception) {
            monthYear
        }
    }
}
