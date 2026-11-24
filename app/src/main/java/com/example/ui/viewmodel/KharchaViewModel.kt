package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.AppThemeMode
import com.example.data.local.PreferenceManager
import com.example.data.local.dao.CategorySpending
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.SavingsGoalEntity
import com.example.data.local.entity.SubscriptionEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.AccountType
import com.example.data.model.ExpenseCategories
import com.example.data.model.TransactionSource
import com.example.data.model.TransactionType
import com.example.data.repository.SmartKharchaRepository
import com.example.sms.ParsedSms
import com.example.sms.SmsParser
import com.example.sms.SmsReader
import com.example.sms.SmsScanResult
import com.example.ui.util.CurrencyUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

enum class DateFilter(val title: String) {
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    ALL_TIME("All Time")
}

data class CategoryBudgetStatus(
    val category: String,
    val spentAmount: Double,
    val budgetLimit: Double,
    val percentageUsed: Float,
    val isOverBudget: Boolean,
    val isNearBudget: Boolean
)

data class DaySpendingPoint(
    val dayLabel: String,
    val dateTimestamp: Long,
    val amount: Double
)

private data class FilterCriteria(
    val date: DateFilter,
    val query: String,
    val cat: String?,
    val acc: String?,
    val type: TransactionType?
)

class KharchaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SmartKharchaRepository
    private val smsReader: SmsReader
    private val preferenceManager = PreferenceManager.getInstance(application)

    val themeMode: StateFlow<AppThemeMode> = preferenceManager.themeMode
    val currencySymbol: StateFlow<String> = preferenceManager.currencySymbol
    val isBalanceMasked: StateFlow<Boolean> = preferenceManager.isBalanceMasked
    val autoLockTimeoutSeconds: StateFlow<Int> = preferenceManager.autoLockTimeoutSeconds

    init {
        val database = AppDatabase.getDatabase(application)
        repository = SmartKharchaRepository(database)
        smsReader = SmsReader(application, repository)
    }

    fun setThemeMode(mode: AppThemeMode) {
        preferenceManager.setThemeMode(mode)
    }

    fun setCurrencySymbol(symbol: String) {
        preferenceManager.setCurrencySymbol(symbol)
    }

    fun toggleBalanceMask() {
        preferenceManager.setBalanceMasked(!isBalanceMasked.value)
    }

    fun setBalanceMasked(masked: Boolean) {
        preferenceManager.setBalanceMasked(masked)
    }

    fun setAutoLockTimeoutSeconds(seconds: Int) {
        preferenceManager.setAutoLockTimeoutSeconds(seconds)
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.resetWithDemoData()
            _scanBanner.value = "Sample Pakistani data restored."
        }
    }

    val activeAccounts: StateFlow<List<AccountEntity>> = repository.activeAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBudgets: StateFlow<List<BudgetEntity>> = repository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSavingsGoals: StateFlow<List<SavingsGoalEntity>> = repository.allSavingsGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSubscriptions: StateFlow<List<SubscriptionEntity>> = repository.allSubscriptions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentTab = MutableStateFlow(0)
    val currentTab = _currentTab.asStateFlow()

    private val _dateFilter = MutableStateFlow(DateFilter.THIS_MONTH)
    val dateFilter = _dateFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedAccount = MutableStateFlow<String?>(null)
    val selectedAccount = _selectedAccount.asStateFlow()

    private val _selectedType = MutableStateFlow<TransactionType?>(null)
    val selectedType = _selectedType.asStateFlow()

    private val _isScanningSms = MutableStateFlow(false)
    val isScanningSms = _isScanningSms.asStateFlow()

    private val _scanBanner = MutableStateFlow<String?>(null)
    val scanBanner = _scanBanner.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog = _showAddDialog.asStateFlow()

    private val _showBudgetDialog = MutableStateFlow(false)
    val showBudgetDialog = _showBudgetDialog.asStateFlow()

    private val _showSmsSimulator = MutableStateFlow(false)
    val showSmsSimulator = _showSmsSimulator.asStateFlow()

    private val _editingTransaction = MutableStateFlow<TransactionEntity?>(null)
    val editingTransaction = _editingTransaction.asStateFlow()

    val rawTransactions: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _filterCriteria = combine(
        _dateFilter,
        _searchQuery,
        _selectedCategory,
        _selectedAccount,
        _selectedType
    ) { date, query, cat, acc, type ->
        FilterCriteria(date, query, cat, acc, type)
    }

    // Filtered transactions
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        rawTransactions,
        _filterCriteria
    ) { txns, criteria ->
        val (startTime, endTime) = getTimeRange(criteria.date)
        txns.filter { txn ->
            val inTime = txn.timestamp in startTime..endTime
            val matchesQuery = criteria.query.isBlank() ||
                    txn.merchantOrRecipient.contains(criteria.query, ignoreCase = true) ||
                    txn.category.contains(criteria.query, ignoreCase = true) ||
                    txn.accountName.contains(criteria.query, ignoreCase = true) ||
                    (txn.notes?.contains(criteria.query, ignoreCase = true) == true) ||
                    (txn.referenceId?.contains(criteria.query, ignoreCase = true) == true)
            val matchesCat = criteria.cat == null || txn.category.equals(criteria.cat, ignoreCase = true)
            val matchesAcc = criteria.acc == null || txn.accountType.name.equals(criteria.acc, ignoreCase = true)
            val matchesType = criteria.type == null || txn.type == criteria.type

            inTime && matchesQuery && matchesCat && matchesAcc && matchesType
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Financial summaries for selected date range
    val summaryTotals = combine(
        rawTransactions,
        _dateFilter
    ) { txns, filter ->
        val (startTime, endTime) = getTimeRange(filter)
        val inRange = txns.filter { it.timestamp in startTime..endTime }
        val totalExpense = inRange.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val totalIncome = inRange.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val netSavings = totalIncome - totalExpense
        Triple(totalExpense, totalIncome, netSavings)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Triple(0.0, 0.0, 0.0))

    // Total net worth across all accounts
    val totalAccountsBalance = activeAccounts.combine(MutableStateFlow(Unit)) { accounts, _ ->
        accounts.sumOf { it.currentBalance }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Category breakdown
    val categoryBreakdown = combine(
        rawTransactions,
        _dateFilter
    ) { txns, filter ->
        val (startTime, endTime) = getTimeRange(filter)
        val expenses = txns.filter { it.type == TransactionType.EXPENSE && it.timestamp in startTime..endTime }
        val totalExp = expenses.sumOf { it.amount }

        expenses.groupBy { it.category }
            .map { (cat, list) ->
                val sum = list.sumOf { it.amount }
                val pct = if (totalExp > 0) ((sum / totalExp) * 100).toFloat() else 0f
                Triple(cat, sum, pct)
            }
            .sortedByDescending { it.second }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Daily spending points for chart (last 7 days of the selected period)
    val dailyChartPoints = rawTransactions.combine(_dateFilter) { txns, _ ->
        val cal = Calendar.getInstance()
        val days = mutableListOf<DaySpendingPoint>()
        val dayNames = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

        // Last 7 days
        for (i in 6 downTo 0) {
            val targetCal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -i)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = targetCal.timeInMillis
            val endOfDay = startOfDay + (24 * 3600 * 1000) - 1
            val dayLabel = dayNames[targetCal.get(Calendar.DAY_OF_WEEK) - 1]

            val spent = txns.filter {
                it.type == TransactionType.EXPENSE && it.timestamp in startOfDay..endOfDay
            }.sumOf { it.amount }

            days.add(DaySpendingPoint(dayLabel, startOfDay, spent))
        }
        days
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Budget vs Spending
    val budgetStatuses = combine(
        rawTransactions,
        allBudgets
    ) { txns, budgets ->
        val (startTime, endTime) = getTimeRange(DateFilter.THIS_MONTH)
        val monthlyExpenses = txns.filter { it.type == TransactionType.EXPENSE && it.timestamp in startTime..endTime }

        budgets.map { budget ->
            val spent = monthlyExpenses.filter { it.category.equals(budget.category, ignoreCase = true) }
                .sumOf { it.amount }
            val ratio = if (budget.monthlyLimit > 0) (spent / budget.monthlyLimit).toFloat() else 0f
            CategoryBudgetStatus(
                category = budget.category,
                spentAmount = spent,
                budgetLimit = budget.monthlyLimit,
                percentageUsed = ratio,
                isOverBudget = spent > budget.monthlyLimit,
                isNearBudget = spent >= (budget.monthlyLimit * 0.8) && spent <= budget.monthlyLimit
            )
        }.sortedByDescending { it.percentageUsed }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTab(index: Int) {
        _currentTab.value = index
    }

    fun setDateFilter(filter: DateFilter) {
        _dateFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategory.value = category
    }

    fun setAccountFilter(account: String?) {
        _selectedAccount.value = account
    }

    fun setTypeFilter(type: TransactionType?) {
        _selectedType.value = type
    }

    fun openAddDialog(txn: TransactionEntity? = null) {
        _editingTransaction.value = txn
        _showAddDialog.value = true
    }

    fun closeAddDialog() {
        _editingTransaction.value = null
        _showAddDialog.value = false
    }

    fun openBudgetDialog() {
        _showBudgetDialog.value = true
    }

    fun closeBudgetDialog() {
        _showBudgetDialog.value = false
    }

    fun openSmsSimulator() {
        _showSmsSimulator.value = true
    }

    fun closeSmsSimulator() {
        _showSmsSimulator.value = false
    }

    fun dismissBanner() {
        _scanBanner.value = null
    }

    fun scanSmsMessages() {
        viewModelScope.launch {
            _isScanningSms.value = true
            try {
                val result: SmsScanResult = smsReader.scanInbox(maxMessages = 400)
                _scanBanner.value = when {
                    result.newTransactionsImported > 0 ->
                        "Imported ${result.newTransactionsImported} new transactions from SMS (${result.duplicatesSkipped} duplicates skipped)."
                    result.financialSmsFound > 0 ->
                        "All ${result.financialSmsFound} financial SMS are already synced. No new transactions."
                    result.totalSmsExamined > 0 ->
                        "Examined ${result.totalSmsExamined} messages. No transaction SMS found from supported banks or wallets."
                    else ->
                        "Inbox scanned. Grant SMS permission or test SMS detection using the SMS Simulator."
                }
            } catch (e: Exception) {
                _scanBanner.value = "SMS scan failed: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                _isScanningSms.value = false
            }
        }
    }

    fun processSimulatedSms(sender: String, body: String) {
        viewModelScope.launch {
            val parsed = SmsParser.parse(sender, body, System.currentTimeMillis())
            if (parsed == null) {
                _scanBanner.value = "Could not parse SMS. Ensure it includes an amount (e.g. Rs. 1500) and bank/wallet sender."
                return@launch
            }

            val isDuplicate = repository.isSmsDuplicate(parsed.smsHash)
            if (isDuplicate) {
                _scanBanner.value = "Duplicate prevented: Transaction ${parsed.referenceId ?: "with this timestamp"} already exists!"
            } else {
                val id = repository.insertTransaction(parsed.toTransactionEntity())
                if (id > 0) {
                    _scanBanner.value = "Detected ${parsed.type.name}: ${CurrencyUtils.formatPkr(parsed.amount)} via ${parsed.accountName} (${parsed.category})!"
                } else {
                    _scanBanner.value = "Could not save transaction."
                }
            }
        }
    }

    fun saveManualTransaction(
        id: Long = 0,
        amount: Double,
        type: TransactionType,
        category: String,
        accountType: AccountType,
        merchant: String,
        timestamp: Long,
        notes: String?
    ) {
        viewModelScope.launch {
            val hash = if (id > 0) {
                _editingTransaction.value?.smsHash ?: "manual_${UUID.randomUUID()}"
            } else {
                "manual_${UUID.randomUUID()}"
            }

            val entity = TransactionEntity(
                id = id,
                smsHash = hash,
                amount = amount,
                type = type,
                category = category,
                accountType = accountType,
                accountName = accountType.title,
                merchantOrRecipient = merchant.ifBlank { accountType.title },
                timestamp = timestamp,
                rawSmsBody = null,
                smsSender = null,
                source = TransactionSource.MANUAL,
                notes = notes,
                accountBalanceAfter = null,
                referenceId = "MAN-${System.currentTimeMillis() % 100000}"
            )

            if (id > 0) {
                repository.updateTransaction(entity)
                _scanBanner.value = "Transaction updated successfully."
            } else {
                repository.insertTransaction(entity)
                _scanBanner.value = "Manual ${type.name.lowercase()} of ${CurrencyUtils.formatPkr(amount)} added."
            }
            closeAddDialog()
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            _scanBanner.value = "Transaction deleted."
        }
    }

    fun updateAccountBalance(accountId: String, newBalance: Double) {
        viewModelScope.launch {
            repository.updateAccountBalance(accountId, newBalance)
            _scanBanner.value = "Account balance updated."
        }
    }

    fun saveBudget(category: String, limit: Double) {
        viewModelScope.launch {
            val monthYear = CurrencyUtils.getMonthYearKey()
            repository.upsertBudget(category, limit, monthYear)
            _scanBanner.value = "Budget for $category set to ${CurrencyUtils.formatPkr(limit)}."
            closeBudgetDialog()
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch {
            repository.deleteBudget(id)
            _scanBanner.value = "Budget removed."
        }
    }

    // Savings Goals Operations
    fun saveSavingsGoal(
        id: Long = 0,
        title: String,
        targetAmount: Double,
        savedAmount: Double,
        category: String,
        note: String? = null
    ) {
        viewModelScope.launch {
            val goal = SavingsGoalEntity(
                id = id,
                title = title,
                targetAmount = targetAmount,
                savedAmount = savedAmount,
                category = category,
                note = note
            )
            repository.upsertSavingsGoal(goal)
            _scanBanner.value = if (id > 0) "Goal '$title' updated." else "New savings goal '$title' created!"
        }
    }

    fun depositToGoal(goal: SavingsGoalEntity, amount: Double) {
        viewModelScope.launch {
            val newSaved = goal.savedAmount + amount
            repository.adjustGoalSavedAmount(goal.id, newSaved)
            _scanBanner.value = "Deposited ${CurrencyUtils.formatPkr(amount)} into '${goal.title}'."
        }
    }

    fun withdrawFromGoal(goal: SavingsGoalEntity, amount: Double) {
        viewModelScope.launch {
            val newSaved = (goal.savedAmount - amount).coerceAtLeast(0.0)
            repository.adjustGoalSavedAmount(goal.id, newSaved)
            _scanBanner.value = "Withdrew ${CurrencyUtils.formatPkr(amount)} from '${goal.title}'."
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(goal)
            _scanBanner.value = "Goal '${goal.title}' removed."
        }
    }

    // Subscriptions Operations
    fun saveSubscription(
        id: Long = 0,
        title: String,
        amount: Double,
        dueDay: Int,
        category: String,
        provider: String,
        accountType: AccountType,
        isAutoPay: Boolean = false
    ) {
        viewModelScope.launch {
            val sub = SubscriptionEntity(
                id = id,
                title = title,
                amount = amount,
                dueDayOfMonth = dueDay,
                category = category,
                provider = provider,
                accountType = accountType,
                isAutoPay = isAutoPay,
                isPaidThisMonth = false
            )
            repository.upsertSubscription(sub)
            _scanBanner.value = if (id > 0) "Subscription '$title' updated." else "Recurring bill '$title' added."
        }
    }

    fun toggleSubscriptionPaid(subscription: SubscriptionEntity) {
        viewModelScope.launch {
            val newStatus = !subscription.isPaidThisMonth
            repository.toggleSubscriptionPaid(subscription.id, newStatus)
            _scanBanner.value = if (newStatus) "'${subscription.title}' marked as PAID for this month." else "'${subscription.title}' marked UNPAID."
        }
    }

    fun deleteSubscription(subscription: SubscriptionEntity) {
        viewModelScope.launch {
            repository.deleteSubscription(subscription)
            _scanBanner.value = "Subscription '${subscription.title}' removed."
        }
    }

    // Real Excel CSV Export (RFC-4180 with UTF-8 BOM for Microsoft Excel)
    fun exportExcelCsv(): String {
        val txns = rawTransactions.value.sortedByDescending { it.timestamp }
        val sb = StringBuilder()
        // UTF-8 BOM so Excel opens Urdu/Pakistani chars cleanly
        sb.append("\uFEFF")
        sb.append("\"Transaction ID\",\"Date\",\"Time\",\"Type\",\"Amount (PKR)\",\"Category\",\"Account\",\"Merchant / Recipient\",\"Reference ID\",\"Notes\",\"Source\"\n")
        txns.forEach { t ->
            val dateStr = CurrencyUtils.formatDate(t.timestamp)
            val timeStr = CurrencyUtils.formatTime(t.timestamp)
            val cleanNotes = (t.notes ?: "").replace("\"", "\"\"")
            val cleanMerchant = t.merchantOrRecipient.replace("\"", "\"\"")
            val cleanRef = (t.referenceId ?: "").replace("\"", "\"\"")
            sb.append("\"${t.id}\",\"$dateStr\",\"$timeStr\",\"${t.type.name}\",\"${t.amount}\",\"${t.category}\",\"${t.accountName}\",\"$cleanMerchant\",\"$cleanRef\",\"$cleanNotes\",\"${t.source.name}\"\n")
        }
        return sb.toString()
    }

    fun exportExpenseReportText(): String {
        val (exp, inc, net) = summaryTotals.value
        val txns = filteredTransactions.value
        val sb = StringBuilder()
        sb.append("====================================================\n")
        sb.append("         SMART KHARCHA TRACKER - PAKISTAN           \n")
        sb.append("         Executive Financial Audit Report           \n")
        sb.append("====================================================\n")
        sb.append("Period: ${_dateFilter.value.title}\n")
        sb.append("Generated: ${CurrencyUtils.formatDateTime(System.currentTimeMillis())}\n")
        sb.append("Developer: Muhammad Talha Thaheem\n\n")

        sb.append("--- EXECUTIVE CASH FLOW SUMMARY ---\n")
        sb.append("Total Inflow  (Income):   ${CurrencyUtils.formatPkr(inc)}\n")
        sb.append("Total Outflow (Expense):  ${CurrencyUtils.formatPkr(exp)}\n")
        val status = if (net >= 0) "SURPLUS" else "DEFICIT"
        sb.append("Net Savings:              ${CurrencyUtils.formatPkr(net)} ($status)\n")
        if (inc > 0) {
            val rate = ((net / inc) * 100).toInt()
            sb.append("Net Savings Rate:         $rate%\n")
        }
        sb.append("\n--- SPENDING BY EXPENSE CATEGORY ---\n")
        if (categoryBreakdown.value.isEmpty()) {
            sb.append("No expenses logged for this period.\n")
        } else {
            categoryBreakdown.value.forEach { (cat, sum, pct) ->
                sb.append(String.format("- %-22s : %12s  (%4.1f%%)\n", cat, CurrencyUtils.formatPkr(sum), pct))
            }
        }

        // Subscriptions
        val subs = allSubscriptions.value
        if (subs.isNotEmpty()) {
            val totalSubs = subs.sumOf { it.amount }
            sb.append("\n--- MONTHLY RECURRING COMMITMENTS (${subs.size}) ---\n")
            sb.append("Total Monthly Bills: ${CurrencyUtils.formatPkr(totalSubs)}\n")
            subs.forEach { s ->
                val paidStr = if (s.isPaidThisMonth) "[PAID]" else "[PENDING (Due ${s.dueDayOfMonth}th)]"
                sb.append("- ${s.title}: ${CurrencyUtils.formatPkr(s.amount)} $paidStr via ${s.accountType.title}\n")
            }
        }

        // Savings Goals
        val goals = allSavingsGoals.value
        if (goals.isNotEmpty()) {
            sb.append("\n--- ACTIVE SAVINGS GOALS (${goals.size}) ---\n")
            goals.forEach { g ->
                val pct = if (g.targetAmount > 0) ((g.savedAmount / g.targetAmount) * 100).toInt() else 0
                sb.append("- ${g.title}: Saved ${CurrencyUtils.formatPkr(g.savedAmount)} of ${CurrencyUtils.formatPkr(g.targetAmount)} ($pct% achieved)\n")
            }
        }

        sb.append("\n--- RECENT TRANSACTIONS (Top ${txns.take(20).size}) ---\n")
        txns.take(20).forEach { t ->
            val sign = if (t.type == TransactionType.EXPENSE) "-" else "+"
            sb.append("${CurrencyUtils.formatDate(t.timestamp)} | ${t.merchantOrRecipient} | $sign${CurrencyUtils.formatPkr(t.amount)} | ${t.accountName} | ${t.category}\n")
        }

        sb.append("\n====================================================\n")
        sb.append("Smart Kharcha Tracker • Developed by Muhammad Talha Thaheem\n")
        sb.append("====================================================\n")
        return sb.toString()
    }

    private fun getTimeRange(filter: DateFilter): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val now = cal.timeInMillis

        return when (filter) {
            DateFilter.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                Pair(cal.timeInMillis, now)
            }
            DateFilter.THIS_WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                Pair(cal.timeInMillis, now)
            }
            DateFilter.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                Pair(cal.timeInMillis, now)
            }
            DateFilter.ALL_TIME -> {
                Pair(0L, Long.MAX_VALUE)
            }
        }
    }
}
