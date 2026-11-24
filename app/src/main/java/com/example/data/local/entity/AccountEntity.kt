package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AccountType

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey
    val id: String,
    val displayName: String,
    val accountType: AccountType,
    val currentBalance: Double,
    val lastUpdated: Long = System.currentTimeMillis(),
    val colorHex: Long = accountType.defaultColor,
    val accountNumberLastDigits: String? = null,
    val isDefault: Boolean = false,
    val isActive: Boolean = true
)
