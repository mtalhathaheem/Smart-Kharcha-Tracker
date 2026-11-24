package com.example.sms

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import com.example.data.repository.SmartKharchaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class SmsScanResult(
    val totalSmsExamined: Int,
    val financialSmsFound: Int,
    val newTransactionsImported: Int,
    val duplicatesSkipped: Int,
    val errors: Int
)

class SmsReader(
    private val context: Context,
    private val repository: SmartKharchaRepository
) {

    suspend fun scanInbox(maxMessages: Int = 500): SmsScanResult = withContext(Dispatchers.IO) {
        var totalExamined = 0
        var financialFound = 0
        var newImported = 0
        var duplicates = 0
        var errors = 0

        val uri: Uri = Telephony.Sms.Inbox.CONTENT_URI
        val projection = arrayOf(
            Telephony.Sms._ID,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )

        try {
            val cursor = context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                "${Telephony.Sms.DATE} DESC"
            )

            cursor?.use {
                val addressIdx = it.getColumnIndex(Telephony.Sms.ADDRESS)
                val bodyIdx = it.getColumnIndex(Telephony.Sms.BODY)
                val dateIdx = it.getColumnIndex(Telephony.Sms.DATE)

                while (it.moveToNext() && totalExamined < maxMessages) {
                    totalExamined++
                    val address = if (addressIdx != -1) it.getString(addressIdx) ?: "" else ""
                    val body = if (bodyIdx != -1) it.getString(bodyIdx) ?: "" else ""
                    val date = if (dateIdx != -1) it.getLong(dateIdx) else System.currentTimeMillis()

                    if (address.isNotBlank() && body.isNotBlank()) {
                        if (SmsParser.isFinancialSms(address, body)) {
                            financialFound++
                            val parsed = SmsParser.parse(address, body, date)
                            if (parsed != null) {
                                val isDuplicate = repository.isSmsDuplicate(parsed.smsHash)
                                if (isDuplicate) {
                                    duplicates++
                                } else {
                                    val id = repository.insertTransaction(parsed.toTransactionEntity())
                                    if (id > 0) {
                                        newImported++
                                    } else {
                                        duplicates++
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            errors++
        }

        SmsScanResult(
            totalSmsExamined = totalExamined,
            financialSmsFound = financialFound,
            newTransactionsImported = newImported,
            duplicatesSkipped = duplicates,
            errors = errors
        )
    }
}
