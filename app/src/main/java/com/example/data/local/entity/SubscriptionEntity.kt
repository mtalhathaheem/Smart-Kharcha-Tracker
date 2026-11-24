package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AccountType

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double,
    val dueDayOfMonth: Int = 1,
    val category: String = "Bills & Utilities",
    val provider: String = "Utility / Service",
    val accountType: AccountType = AccountType.EASYPAISA,
    val isAutoPay: Boolean = false,
    val isPaidThisMonth: Boolean = false,
    val notes: String? = null,
    val lastPaidTimestamp: Long? = null
)
