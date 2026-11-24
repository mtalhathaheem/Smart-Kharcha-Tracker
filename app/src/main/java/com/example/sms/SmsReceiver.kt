package com.example.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.data.local.AppDatabase
import com.example.data.repository.SmartKharchaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val sender = messages[0].displayOriginatingAddress ?: return
        val fullBody = StringBuilder()
        var timestamp = System.currentTimeMillis()

        for (sms in messages) {
            fullBody.append(sms.displayMessageBody)
            timestamp = sms.timestampMillis
        }

        val bodyText = fullBody.toString()

        if (SmsParser.isFinancialSms(sender, bodyText)) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val parsed = SmsParser.parse(sender, bodyText, timestamp)
                    if (parsed != null) {
                        val database = AppDatabase.getDatabase(context)
                        val repository = SmartKharchaRepository(database)
                        if (!repository.isSmsDuplicate(parsed.smsHash)) {
                            repository.insertTransaction(parsed.toTransactionEntity())
                        }
                    }
                } catch (e: Exception) {
                    // Silently ignore broadcast processing failure
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
