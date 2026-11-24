package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AccountDao
import com.example.data.local.dao.BudgetDao
import com.example.data.local.dao.SavingsGoalDao
import com.example.data.local.dao.SubscriptionDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.SubscriptionEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.AccountType
import com.example.data.model.ExpenseCategories
import com.example.data.model.TransactionSource
import com.example.data.model.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

@Database(
    entities = [
        TransactionEntity::class,
        AccountEntity::class,
        BudgetEntity::class,
        SavingsGoalEntity::class,
        SubscriptionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun accountDao(): AccountDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun subscriptionDao(): SubscriptionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smart_kharcha.db"
                )
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val database = getDatabase(context)
                    seedInitialData(database)
                }
            }
        }

        suspend fun seedInitialData(database: AppDatabase) {
            val now = System.currentTimeMillis()
            val cal = Calendar.getInstance()
            val monthYear = String.format("%04d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)

            // Seed default Pakistani accounts
            val initialAccounts = listOf(
                AccountEntity(
                    id = "EASYPAISA",
                    displayName = "EasyPaisa Wallet",
                    accountType = AccountType.EASYPAISA,
                    currentBalance = 14250.0,
                    accountNumberLastDigits = "3737",
                    isDefault = true
                ),
                AccountEntity(
                    id = "JAZZCASH",
                    displayName = "JazzCash Wallet",
                    accountType = AccountType.JAZZCASH,
                    currentBalance = 8450.0,
                    accountNumberLastDigits = "8558"
                ),
                AccountEntity(
                    id = "NAYAPAY",
                    displayName = "NayaPay Visa",
                    accountType = AccountType.NAYAPAY,
                    currentBalance = 23150.0,
                    accountNumberLastDigits = "4521"
                ),
                AccountEntity(
                    id = "SADAPAY",
                    displayName = "SadaPay Mastercard",
                    accountType = AccountType.SADAPAY,
                    currentBalance = 10580.0,
                    accountNumberLastDigits = "8812"
                ),
                AccountEntity(
                    id = "MEEZAN_BANK",
                    displayName = "Meezan Bank Account",
                    accountType = AccountType.MEEZAN_BANK,
                    currentBalance = 65400.0,
                    accountNumberLastDigits = "1234"
                ),
                AccountEntity(
                    id = "CASH",
                    displayName = "Cash in Hand",
                    accountType = AccountType.CASH,
                    currentBalance = 5000.0
                )
            )
            database.accountDao().insertAllIfAbsent(initialAccounts)

            // Seed sensible Pakistani monthly budgets
            val initialBudgets = listOf(
                BudgetEntity(category = ExpenseCategories.FOOD, monthlyLimit = 25000.0, monthYear = monthYear),
                BudgetEntity(category = ExpenseCategories.GROCERIES, monthlyLimit = 35000.0, monthYear = monthYear),
                BudgetEntity(category = ExpenseCategories.FUEL_TRANSPORT, monthlyLimit = 15000.0, monthYear = monthYear),
                BudgetEntity(category = ExpenseCategories.BILLS_UTILITIES, monthlyLimit = 20000.0, monthYear = monthYear),
                BudgetEntity(category = ExpenseCategories.SHOPPING, monthlyLimit = 12000.0, monthYear = monthYear),
                BudgetEntity(category = ExpenseCategories.MOBILE_INTERNET, monthlyLimit = 4000.0, monthYear = monthYear)
            )
            initialBudgets.forEach { database.budgetDao().insertOrUpdate(it) }

            // Seed a few initial realistic transactions so the dashboard has rich visualization immediately
            val initialTransactions = listOf(
                TransactionEntity(
                    smsHash = "seed_easypaisa_foodpanda_01",
                    amount = 1450.0,
                    type = TransactionType.EXPENSE,
                    category = ExpenseCategories.FOOD,
                    accountType = AccountType.EASYPAISA,
                    accountName = "EasyPaisa Wallet",
                    merchantOrRecipient = "Foodpanda",
                    timestamp = now - (3 * 3600 * 1000), // 3 hours ago
                    rawSmsBody = "Trx ID: 8871234. You have paid Rs. 1450 at Foodpanda via EasyPaisa on 29-10-2026. Remaining balance: Rs. 14250.",
                    smsSender = "3737",
                    source = TransactionSource.SMS_AUTO,
                    accountBalanceAfter = 14250.0,
                    referenceId = "8871234"
                ),
                TransactionEntity(
                    smsHash = "seed_nayapay_naheed_02",
                    amount = 3850.0,
                    type = TransactionType.EXPENSE,
                    category = ExpenseCategories.GROCERIES,
                    accountType = AccountType.NAYAPAY,
                    accountName = "NayaPay Visa",
                    merchantOrRecipient = "Naheed Supermarket",
                    timestamp = now - (24 * 3600 * 1000), // yesterday
                    rawSmsBody = "You spent Rs. 3850.00 at Naheed Supermarket using your NayaPay Visa Card (...4521). Available balance: Rs. 23150.00.",
                    smsSender = "NayaPay",
                    source = TransactionSource.SMS_AUTO,
                    accountBalanceAfter = 23150.0,
                    referenceId = "NP99214"
                ),
                TransactionEntity(
                    smsHash = "seed_meezan_pso_03",
                    amount = 4000.0,
                    type = TransactionType.EXPENSE,
                    category = ExpenseCategories.FUEL_TRANSPORT,
                    accountType = AccountType.MEEZAN_BANK,
                    accountName = "Meezan Bank Account",
                    merchantOrRecipient = "PSO Petrol Pump",
                    timestamp = now - (48 * 3600 * 1000), // 2 days ago
                    rawSmsBody = "Dear Customer, PKR 4,000.00 debited from A/C ending 1234 on 27-OCT-26 at PSO Fuel Station. Available Bal: PKR 65,400.00.",
                    smsSender = "8257",
                    source = TransactionSource.SMS_AUTO,
                    accountBalanceAfter = 65400.0,
                    referenceId = "MZN3341"
                ),
                TransactionEntity(
                    smsHash = "seed_jazzcash_ke_04",
                    amount = 5200.0,
                    type = TransactionType.EXPENSE,
                    category = ExpenseCategories.BILLS_UTILITIES,
                    accountType = AccountType.JAZZCASH,
                    accountName = "JazzCash Wallet",
                    merchantOrRecipient = "K-Electric Bill",
                    timestamp = now - (72 * 3600 * 1000), // 3 days ago
                    rawSmsBody = "Rs. 5,200 paid to K-Electric bill via JazzCash Mobile Account on 26-Oct-2026. TID 449912. Remaining Balance: Rs. 8,450.",
                    smsSender = "8558",
                    source = TransactionSource.SMS_AUTO,
                    accountBalanceAfter = 8450.0,
                    referenceId = "449912"
                ),
                TransactionEntity(
                    smsHash = "seed_meezan_salary_05",
                    amount = 125000.0,
                    type = TransactionType.INCOME,
                    category = ExpenseCategories.SALARY,
                    accountType = AccountType.MEEZAN_BANK,
                    accountName = "Meezan Bank Account",
                    merchantOrRecipient = "Monthly Salary (Raast/IBFT)",
                    timestamp = now - (6 * 24 * 3600 * 1000), // 6 days ago
                    rawSmsBody = "PKR 125,000.00 credited to your Account ending 1234 via Raast/IBFT from Tech Solutions Ltd. Available Bal: PKR 140,000.00.",
                    smsSender = "8257",
                    source = TransactionSource.SMS_AUTO,
                    accountBalanceAfter = 140000.0,
                    referenceId = "RAAST-901"
                )
            )
            database.transactionDao().insertAll(initialTransactions)

            // Seed initial Pakistani Savings Goals
            val initialGoals = listOf(
                SavingsGoalEntity(
                    title = "Emergency Fund (3 Months)",
                    targetAmount = 150000.0,
                    savedAmount = 65000.0,
                    category = "Financial Safety",
                    note = "Buffer fund in Meezan Bank for unforeseen medical or household needs"
                ),
                SavingsGoalEntity(
                    title = "Eid-ul-Adha Qurbani",
                    targetAmount = 80000.0,
                    savedAmount = 45000.0,
                    category = "Family & Religious",
                    note = "Annual savings for Eid Qurbani"
                ),
                SavingsGoalEntity(
                    title = "New Laptop / Tech Upgrade",
                    targetAmount = 180000.0,
                    savedAmount = 72000.0,
                    category = "Career & Tech",
                    note = "Development machine upgrade"
                )
            )
            database.savingsGoalDao().insertAllIfAbsent(initialGoals)

            // Seed initial Pakistani Subscriptions & Utility Commitments
            val initialSubscriptions = listOf(
                SubscriptionEntity(
                    title = "K-Electric Electricity",
                    amount = 6500.0,
                    dueDayOfMonth = 10,
                    category = ExpenseCategories.BILLS_UTILITIES,
                    provider = "K-Electric",
                    accountType = AccountType.EASYPAISA,
                    isAutoPay = true,
                    isPaidThisMonth = true
                ),
                SubscriptionEntity(
                    title = "Nayatel Fiber Broadband",
                    amount = 3850.0,
                    dueDayOfMonth = 5,
                    category = ExpenseCategories.MOBILE_INTERNET,
                    provider = "Nayatel",
                    accountType = AccountType.NAYAPAY,
                    isAutoPay = false,
                    isPaidThisMonth = false
                ),
                SubscriptionEntity(
                    title = "SNGPL Gas Supply",
                    amount = 1450.0,
                    dueDayOfMonth = 18,
                    category = ExpenseCategories.BILLS_UTILITIES,
                    provider = "SNGPL",
                    accountType = AccountType.JAZZCASH,
                    isAutoPay = false,
                    isPaidThisMonth = false
                ),
                SubscriptionEntity(
                    title = "PTCL Landline & Flash Fiber",
                    amount = 2900.0,
                    dueDayOfMonth = 22,
                    category = ExpenseCategories.BILLS_UTILITIES,
                    provider = "PTCL",
                    accountType = AccountType.MEEZAN_BANK,
                    isAutoPay = false,
                    isPaidThisMonth = false
                ),
                SubscriptionEntity(
                    title = "Netflix Standard HD",
                    amount = 1100.0,
                    dueDayOfMonth = 1,
                    category = ExpenseCategories.ENTERTAINMENT,
                    provider = "Netflix Pakistan",
                    accountType = AccountType.SADAPAY,
                    isAutoPay = true,
                    isPaidThisMonth = true
                )
            )
            database.subscriptionDao().insertAllIfAbsent(initialSubscriptions)
        }
    }
}
