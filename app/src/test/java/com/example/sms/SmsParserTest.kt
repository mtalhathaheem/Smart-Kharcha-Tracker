package com.example.sms

import com.example.data.model.AccountType
import com.example.data.model.ExpenseCategories
import com.example.data.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SmsParserTest {

    @Test
    fun testEasyPaisaExpenseParsing() {
        val sender = "3737"
        val body = "Trx ID: 8871234. You have paid Rs. 1450 at Foodpanda via EasyPaisa on 29-10-2026. Remaining balance: Rs. 14250."
        val timestamp = 1730190000000L

        val result = SmsParser.parse(sender, body, timestamp)
        assertNotNull(result)
        result?.let {
            assertEquals(1450.0, it.amount, 0.01)
            assertEquals(TransactionType.EXPENSE, it.type)
            assertEquals(AccountType.EASYPAISA, it.accountType)
            assertEquals(ExpenseCategories.FOOD, it.category)
            assertEquals(14250.0, it.balanceAfter ?: 0.0, 0.01)
            assertEquals("8871234", it.referenceId)
            assertTrue(it.smsHash.isNotEmpty())
        }
    }

    @Test
    fun testJazzCashTransferParsing() {
        val sender = "8558"
        val body = "Rs. 3,500.00 transferred to Farhan Ali (03211234567) from your JazzCash Mobile Account on 29-Oct-2026. TID: 55443322. Remaining Balance: Rs. 4,950.00."
        val timestamp = 1730190000000L

        val result = SmsParser.parse(sender, body, timestamp)
        assertNotNull(result)
        result?.let {
            assertEquals(3500.0, it.amount, 0.01)
            assertEquals(TransactionType.EXPENSE, it.type)
            assertEquals(AccountType.JAZZCASH, it.accountType)
            assertEquals(4950.0, it.balanceAfter ?: 0.0, 0.01)
            assertEquals("55443322", it.referenceId)
        }
    }

    @Test
    fun testNayaPayGroceryParsing() {
        val sender = "NayaPay"
        val body = "You spent Rs. 4,200.00 at Naheed Supermarket using your NayaPay Visa Card (...4521). Available balance: Rs. 18,950.00."
        val timestamp = 1730190000000L

        val result = SmsParser.parse(sender, body, timestamp)
        assertNotNull(result)
        result?.let {
            assertEquals(4200.0, it.amount, 0.01)
            assertEquals(TransactionType.EXPENSE, it.type)
            assertEquals(AccountType.NAYAPAY, it.accountType)
            assertEquals(ExpenseCategories.GROCERIES, it.category)
            assertEquals(18950.0, it.balanceAfter ?: 0.0, 0.01)
        }
    }

    @Test
    fun testMeezanBankDebitParsing() {
        val sender = "8257"
        val body = "Dear Customer, PKR 6,500.00 debited from A/C ending 1234 on 29-OCT-26 at Shell Petrol. Available Bal: PKR 58,900.00."
        val timestamp = 1730190000000L

        val result = SmsParser.parse(sender, body, timestamp)
        assertNotNull(result)
        result?.let {
            assertEquals(6500.0, it.amount, 0.01)
            assertEquals(TransactionType.EXPENSE, it.type)
            assertEquals(AccountType.MEEZAN_BANK, it.accountType)
            assertEquals(ExpenseCategories.FUEL_TRANSPORT, it.category)
            assertEquals(58900.0, it.balanceAfter ?: 0.0, 0.01)
        }
    }

    @Test
    fun testSalaryCreditParsing() {
        val sender = "8257"
        val body = "PKR 45,000.00 credited to your Account ending 1234 via Raast from Monthly Salary. Available Bal: PKR 103,900.00."
        val timestamp = 1730190000000L

        val result = SmsParser.parse(sender, body, timestamp)
        assertNotNull(result)
        result?.let {
            assertEquals(45000.0, it.amount, 0.01)
            assertEquals(TransactionType.INCOME, it.type)
            assertEquals(ExpenseCategories.SALARY, it.category)
            assertEquals(103900.0, it.balanceAfter ?: 0.0, 0.01)
        }
    }

    @Test
    fun testDuplicateHashConsistency() {
        val sender = "3737"
        val body = "Trx ID: 11223344. You have paid Rs. 500 at Careem on 29-10-2026. Remaining balance: Rs. 10000."
        val timestamp = 1730190000000L

        val result1 = SmsParser.parse(sender, body, timestamp)
        val result2 = SmsParser.parse(sender, body, timestamp)

        assertNotNull(result1)
        assertNotNull(result2)
        assertEquals(result1?.smsHash, result2?.smsHash)
    }
}
