package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.dao.CategorySpending
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.AccountType
import com.example.data.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID

import com.example.data.local.dao.SavingsGoalDao
import com.example.data.local.dao.SubscriptionDao
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.SubscriptionEntity

class SmartKharchaRepository(private val database: AppDatabase) {

    private val transactionDao = database.transactionDao()
    private val accountDao = database.accountDao()
    private val budgetDao = database.budgetDao()
    private val savingsGoalDao = database.savingsGoalDao()
    private val subscriptionDao = database.subscriptionDao()

    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val activeAccounts: Flow<List<AccountEntity>> = accountDao.getAllActiveAccounts()
    val allBudgets: Flow<List<BudgetEntity>> = budgetDao.getAllBudgets()
    val allSavingsGoals: Flow<List<SavingsGoalEntity>> = savingsGoalDao.getAllGoals()
    val allSubscriptions: Flow<List<SubscriptionEntity>> = subscriptionDao.getAllSubscriptions()

    fun getTransactionsBetween(start: Long, end: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsBetween(start, end)
    }

    fun getCategorySpending(start: Long, end: Long): Flow<List<CategorySpending>> {
        return transactionDao.getExpenseBreakdownByCategory(start, end)
    }

    suspend fun isSmsDuplicate(hash: String): Boolean = withContext(Dispatchers.IO) {
        transactionDao.existsBySmsHash(hash)
    }

    suspend fun insertTransaction(transaction: TransactionEntity): Long = withContext(Dispatchers.IO) {
        val insertedId = transactionDao.insert(transaction)
        if (insertedId > 0) {
            // Update account balance
            val accountId = transaction.accountType.name
            val existingAccount = accountDao.getAccountById(accountId)
            if (existingAccount != null) {
                if (transaction.accountBalanceAfter != null && transaction.accountBalanceAfter > 0) {
                    accountDao.updateBalance(accountId, transaction.accountBalanceAfter, transaction.timestamp)
                } else {
                    val delta = if (transaction.type == TransactionType.EXPENSE) -transaction.amount else transaction.amount
                    accountDao.adjustBalance(accountId, delta, transaction.timestamp)
                }
            } else {
                // Create account if not present
                val newBal = transaction.accountBalanceAfter ?: if (transaction.type == TransactionType.INCOME) transaction.amount else 0.0
                accountDao.insertOrUpdate(
                    AccountEntity(
                        id = accountId,
                        displayName = transaction.accountName,
                        accountType = transaction.accountType,
                        currentBalance = newBal,
                        lastUpdated = transaction.timestamp
                    )
                )
            }
        }
        insertedId
    }

    suspend fun updateTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.update(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.delete(transaction)
        // Reverse account balance adjustment
        val delta = if (transaction.type == TransactionType.EXPENSE) transaction.amount else -transaction.amount
        accountDao.adjustBalance(transaction.accountType.name, delta, System.currentTimeMillis())
    }

    suspend fun updateAccountBalance(accountId: String, newBalance: Double) = withContext(Dispatchers.IO) {
        accountDao.updateBalance(accountId, newBalance, System.currentTimeMillis())
    }

    suspend fun upsertAccount(account: AccountEntity) = withContext(Dispatchers.IO) {
        accountDao.insertOrUpdate(account)
    }

    suspend fun upsertBudget(category: String, limit: Double, monthYear: String) = withContext(Dispatchers.IO) {
        budgetDao.insertOrUpdate(
            BudgetEntity(
                category = category,
                monthlyLimit = limit,
                monthYear = monthYear
            )
        )
    }

    suspend fun deleteBudget(budgetId: Long) = withContext(Dispatchers.IO) {
        budgetDao.deleteById(budgetId)
    }

    // Savings Goals
    suspend fun upsertSavingsGoal(goal: SavingsGoalEntity) = withContext(Dispatchers.IO) {
        savingsGoalDao.insert(goal)
    }

    suspend fun deleteSavingsGoal(goal: SavingsGoalEntity) = withContext(Dispatchers.IO) {
        savingsGoalDao.delete(goal)
    }

    suspend fun adjustGoalSavedAmount(id: Long, newAmount: Double) = withContext(Dispatchers.IO) {
        savingsGoalDao.updateSavedAmount(id, newAmount)
    }

    // Subscriptions
    suspend fun upsertSubscription(subscription: SubscriptionEntity) = withContext(Dispatchers.IO) {
        subscriptionDao.insert(subscription)
    }

    suspend fun deleteSubscription(subscription: SubscriptionEntity) = withContext(Dispatchers.IO) {
        subscriptionDao.delete(subscription)
    }

    suspend fun toggleSubscriptionPaid(id: Long, isPaid: Boolean) = withContext(Dispatchers.IO) {
        val timestamp = if (isPaid) System.currentTimeMillis() else null
        subscriptionDao.updatePaidStatus(id, isPaid, timestamp)
    }

    suspend fun resetWithDemoData() = withContext(Dispatchers.IO) {
        AppDatabase.seedInitialData(database)
    }
}
