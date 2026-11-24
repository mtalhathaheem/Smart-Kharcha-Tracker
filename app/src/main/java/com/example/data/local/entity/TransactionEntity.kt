package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.AccountType
import com.example.data.model.TransactionSource
import com.example.data.model.TransactionType

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["smsHash"], unique = true),
        Index(value = ["timestamp"]),
        Index(value = ["category"]),
        Index(value = ["accountType"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val smsHash: String,
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val accountType: AccountType,
    val accountName: String,
    val merchantOrRecipient: String,
    val timestamp: Long,
    val rawSmsBody: String? = null,
    val smsSender: String? = null,
    val source: TransactionSource = TransactionSource.SMS_AUTO,
    val notes: String? = null,
    val accountBalanceAfter: Double? = null,
    val referenceId: String? = null
)
