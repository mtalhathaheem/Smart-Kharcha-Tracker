package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class TransactionType {
    EXPENSE,
    INCOME
}

enum class TransactionSource {
    SMS_AUTO,
    MANUAL
}

enum class AccountType(
    val title: String,
    val defaultColor: Long,
    val defaultPrefix: String
) {
    EASYPAISA("EasyPaisa", 0xFF00A651, "03"),
    JAZZCASH("JazzCash", 0xFFD32F2F, "03"),
    NAYAPAY("NayaPay", 0xFFFF6D00, "NP"),
    SADAPAY("SadaPay", 0xFF00B4D8, "SP"),
    MEEZAN_BANK("Meezan Bank", 0xFF1A365D, "PK"),
    HBL("HBL Bank", 0xFF00695C, "PK"),
    BANK_ALFALAH("Bank Alfalah", 0xFFB71C1C, "PK"),
    OTHER_BANK("Other Bank", 0xFF3F51B5, "PK"),
    CASH("Cash in Hand", 0xFFF59E0B, "Cash");

    companion object {
        fun fromString(value: String): AccountType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: OTHER_BANK
        }
    }
}

object ExpenseCategories {
    const val FOOD = "Food & Dining"
    const val GROCERIES = "Groceries"
    const val FUEL_TRANSPORT = "Fuel & Transport"
    const val BILLS_UTILITIES = "Bills & Utilities"
    const val SHOPPING = "Shopping"
    const val MOBILE_INTERNET = "Mobile & Internet"
    const val MONEY_TRANSFER = "Money Transfer"
    const val HEALTH = "Health & Medical"
    const val EDUCATION = "Education"
    const val ENTERTAINMENT = "Entertainment"
    const val SALARY = "Salary & Income"
    const val INVESTMENT = "Investment & Savings"
    const val OTHERS = "Other"

    val allExpenseCategories = listOf(
        FOOD,
        GROCERIES,
        FUEL_TRANSPORT,
        BILLS_UTILITIES,
        SHOPPING,
        MOBILE_INTERNET,
        MONEY_TRANSFER,
        HEALTH,
        EDUCATION,
        ENTERTAINMENT,
        OTHERS
    )

    val allIncomeCategories = listOf(
        SALARY,
        MONEY_TRANSFER,
        INVESTMENT,
        OTHERS
    )

    fun getCategoryIcon(category: String): ImageVector {
        return when (category) {
            FOOD -> Icons.Default.Fastfood
            GROCERIES -> Icons.Default.ShoppingCart
            FUEL_TRANSPORT -> Icons.Default.DirectionsCar
            BILLS_UTILITIES -> Icons.Default.ElectricBolt
            SHOPPING -> Icons.Default.ShoppingCart
            MOBILE_INTERNET -> Icons.Default.PhoneAndroid
            MONEY_TRANSFER -> Icons.Default.Send
            HEALTH -> Icons.Default.LocalHospital
            EDUCATION -> Icons.Default.School
            ENTERTAINMENT -> Icons.Default.Movie
            SALARY -> Icons.Default.AttachMoney
            INVESTMENT -> Icons.Default.AccountBalance
            else -> Icons.Default.Category
        }
    }

    fun getCategoryColor(category: String): Color {
        return when (category) {
            FOOD -> Color(0xFFEF4444)
            GROCERIES -> Color(0xFF10B981)
            FUEL_TRANSPORT -> Color(0xFF3B82F6)
            BILLS_UTILITIES -> Color(0xFFF59E0B)
            SHOPPING -> Color(0xFF8B5CF6)
            MOBILE_INTERNET -> Color(0xFF06B6D4)
            MONEY_TRANSFER -> Color(0xFF6366F1)
            HEALTH -> Color(0xFFEC4899)
            EDUCATION -> Color(0xFF14B8A6)
            ENTERTAINMENT -> Color(0xFFF97316)
            SALARY -> Color(0xFF10B981)
            INVESTMENT -> Color(0xFF0D9488)
            else -> Color(0xFF6B7280)
        }
    }
}
