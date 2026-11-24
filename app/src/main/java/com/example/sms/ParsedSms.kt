package com.example.sms

import com.example.data.local.entity.TransactionEntity
import com.example.data.model.AccountType
import com.example.data.model.TransactionSource
import com.example.data.model.TransactionType

data class ParsedSms(
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val accountType: AccountType,
    val accountName: String,
    val merchantOrRecipient: String,
    val timestamp: Long,
    val rawSender: String,
    val rawBody: String,
    val balanceAfter: Double? = null,
    val referenceId: String? = null,
    val smsHash: String
) {
    fun toTransactionEntity(): TransactionEntity {
        return TransactionEntity(
            smsHash = smsHash,
            amount = amount,
            type = type,
            category = category,
            accountType = accountType,
            accountName = accountName,
            merchantOrRecipient = merchantOrRecipient,
            timestamp = timestamp,
            rawSmsBody = rawBody,
            smsSender = rawSender,
            source = TransactionSource.SMS_AUTO,
            accountBalanceAfter = balanceAfter,
            referenceId = referenceId
        )
    }
}
