package com.example.sms

import com.example.data.model.AccountType
import com.example.data.model.ExpenseCategories
import com.example.data.model.TransactionType
import java.security.MessageDigest
import java.util.Locale
import java.util.regex.Pattern

object SmsParser {

    private val AMOUNT_PATTERN = Pattern.compile(
        """(?:Rs\.?|PKR|₨)\s*([0-9]+(?:,[0-9]+)*(?:\.[0-9]+)?)""",
        Pattern.CASE_INSENSITIVE
    )

    private val AMOUNT_AFTER_PATTERN = Pattern.compile(
        """([0-9]+(?:,[0-9]+)*(?:\.[0-9]+)?)\s*(?:PKR|Rs\.?|₨)""",
        Pattern.CASE_INSENSITIVE
    )

    private val BALANCE_PATTERN = Pattern.compile(
        """(?:(?:Available|Remaining|New|Updated)?\s*(?:Balance|Bal))\s*(?:is|:)?\s*(?:Rs\.?|PKR|₨)?\s*([0-9]+(?:,[0-9]+)*(?:\.[0-9]+)?)""",
        Pattern.CASE_INSENSITIVE
    )

    private val REF_PATTERN = Pattern.compile(
        """(?:Trx\s*ID|TID|Txn\s*ID|Ref(?:erence)?|Tran\s*ID)[:\s#]*([A-Za-z0-9_-]+)""",
        Pattern.CASE_INSENSITIVE
    )

    // Common Pakistan merchant and recipient patterns
    private val RECIPIENT_PATTERNS = listOf(
        Pattern.compile("""(?:sent|transferred)\s+Rs\.?\s*[0-9,.]+\s+to\s+([^(\n.,]+)""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""(?:paid|spent)\s+(?:Rs\.?|PKR|₨)?\s*[0-9,.]+\s+(?:at|to)\s+([^(\n.,]+)""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""used\s+(?:for|at)\s+(?:PKR|Rs\.?)\s*[0-9,.]+\s+at\s+([^(\n.,]+)""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""payment\s+of\s+(?:Rs\.?|PKR)\s*[0-9,.]+\s+to\s+([^(\n.,]+)""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""received\s+(?:Rs\.?|PKR)\s*[0-9,.]+\s+from\s+([^(\n.,]+)""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""credited.*?from\s+([^(\n.,]+)""", Pattern.CASE_INSENSITIVE)
    )

    fun parse(sender: String, body: String, timestamp: Long): ParsedSms? {
        val cleanSender = sender.trim()
        val cleanBody = body.trim()

        val accountType = detectAccountType(cleanSender, cleanBody) ?: return null

        val amount = extractAmount(cleanBody) ?: return null
        if (amount <= 0.0) return null

        val type = detectTransactionType(cleanBody)
        val balanceAfter = extractBalance(cleanBody)
        val refId = extractRefId(cleanBody)
        val merchant = extractMerchantOrRecipient(cleanBody, type, accountType)
        val category = categorize(cleanBody, merchant, type)

        val hash = generateHash(cleanSender, timestamp, amount, type, refId, balanceAfter, cleanBody)

        return ParsedSms(
            amount = amount,
            type = type,
            category = category,
            accountType = accountType,
            accountName = accountType.title,
            merchantOrRecipient = merchant,
            timestamp = timestamp,
            rawSender = cleanSender,
            rawBody = cleanBody,
            balanceAfter = balanceAfter,
            referenceId = refId,
            smsHash = hash
        )
    }

    fun isFinancialSms(sender: String, body: String): Boolean {
        val lowerSender = sender.lowercase(Locale.ROOT)
        val lowerBody = body.lowercase(Locale.ROOT)

        val isSenderMatch = lowerSender.contains("3737") ||
                lowerSender.contains("easypaisa") ||
                lowerSender.contains("8558") ||
                lowerSender.contains("jazzcash") ||
                lowerSender.contains("nayapay") ||
                lowerSender.contains("sadapay") ||
                lowerSender.contains("8257") ||
                lowerSender.contains("meezan") ||
                lowerSender.contains("4250") ||
                lowerSender.contains("hbl") ||
                lowerSender.contains("alfalah") ||
                lowerSender.contains("822") ||
                lowerSender.contains("ubl") ||
                lowerSender.contains("mcb") ||
                lowerSender.contains("faysal") ||
                lowerSender.contains("bank")

        val hasCurrency = lowerBody.contains("pkr") ||
                lowerBody.contains("rs.") ||
                lowerBody.contains("rs ") ||
                lowerBody.contains("₨")

        val hasAction = lowerBody.contains("debited") ||
                lowerBody.contains("credited") ||
                lowerBody.contains("sent") ||
                lowerBody.contains("spent") ||
                lowerBody.contains("paid") ||
                lowerBody.contains("transferred") ||
                lowerBody.contains("received") ||
                lowerBody.contains("balance")

        return (isSenderMatch && hasCurrency) || (hasCurrency && hasAction)
    }

    private fun detectAccountType(sender: String, body: String): AccountType? {
        val s = sender.lowercase(Locale.ROOT)
        val b = body.lowercase(Locale.ROOT)

        return when {
            s.contains("3737") || s.contains("easypaisa") || b.contains("easypaisa") -> AccountType.EASYPAISA
            s.contains("8558") || s.contains("jazzcash") || b.contains("jazzcash") -> AccountType.JAZZCASH
            s.contains("nayapay") || b.contains("nayapay") -> AccountType.NAYAPAY
            s.contains("sadapay") || b.contains("sadapay") -> AccountType.SADAPAY
            s.contains("8257") || s.contains("meezan") || b.contains("meezan") -> AccountType.MEEZAN_BANK
            s.contains("4250") || s.contains("hbl") || b.contains("hbl") -> AccountType.HBL
            s.contains("822") || s.contains("alfalah") || b.contains("alfalah") -> AccountType.BANK_ALFALAH
            isFinancialSms(sender, body) -> AccountType.OTHER_BANK
            else -> null
        }
    }

    private fun extractAmount(body: String): Double? {
        val m1 = AMOUNT_PATTERN.matcher(body)
        if (m1.find()) {
            val amountStr = m1.group(1)?.replace(",", "")?.trim()
            return amountStr?.toDoubleOrNull()
        }

        val m2 = AMOUNT_AFTER_PATTERN.matcher(body)
        if (m2.find()) {
            val amountStr = m2.group(1)?.replace(",", "")?.trim()
            return amountStr?.toDoubleOrNull()
        }

        return null
    }

    private fun detectTransactionType(body: String): TransactionType {
        val lower = body.lowercase(Locale.ROOT)

        val isIncome = lower.contains("credited") ||
                lower.contains("received") ||
                lower.contains("money added") ||
                lower.contains("cashback") ||
                lower.contains("refund") ||
                lower.contains("deposit") ||
                lower.contains("salary credited")

        val isExpense = lower.contains("debited") ||
                lower.contains("sent") ||
                lower.contains("spent") ||
                lower.contains("paid") ||
                lower.contains("transferred") ||
                lower.contains("withdrawn") ||
                lower.contains("fee:")

        return if (isIncome && !isExpense) {
            TransactionType.INCOME
        } else {
            TransactionType.EXPENSE
        }
    }

    private fun extractBalance(body: String): Double? {
        val matcher = BALANCE_PATTERN.matcher(body)
        if (matcher.find()) {
            val balStr = matcher.group(1)?.replace(",", "")?.trim()
            return balStr?.toDoubleOrNull()
        }
        return null
    }

    private fun extractRefId(body: String): String? {
        val matcher = REF_PATTERN.matcher(body)
        if (matcher.find()) {
            return matcher.group(1)?.trim()
        }
        return null
    }

    private fun extractMerchantOrRecipient(body: String, type: TransactionType, accountType: AccountType): String {
        for (pattern in RECIPIENT_PATTERNS) {
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                val candidate = matcher.group(1)?.trim()
                if (!candidate.isNullOrBlank() && candidate.length > 2) {
                    val clean = candidate
                        .replace(Regex("""\s+on\s+.*""", RegexOption.IGNORE_CASE), "")
                        .replace(Regex("""\s+via\s+.*""", RegexOption.IGNORE_CASE), "")
                        .replace(Regex("""\s+from\s+.*""", RegexOption.IGNORE_CASE), "")
                        .replace(Regex("""\s+using\s+.*""", RegexOption.IGNORE_CASE), "")
                        .trim()
                    if (clean.isNotBlank() && clean.length <= 40) {
                        return clean
                    }
                }
            }
        }

        // Specific fallbacks for Pakistani merchants
        val lower = body.lowercase(Locale.ROOT)
        return when {
            lower.contains("foodpanda") -> "Foodpanda"
            lower.contains("careem") -> "Careem"
            lower.contains("indrive") -> "InDrive"
            lower.contains("bykea") -> "Bykea"
            lower.contains("daraz") -> "Daraz.pk"
            lower.contains("k-electric") || lower.contains("kelectric") -> "K-Electric"
            lower.contains("lesco") -> "LESCO Bill"
            lower.contains("sngpl") -> "SNGPL Gas"
            lower.contains("ssgc") -> "SSGC Gas"
            lower.contains("ptcl") -> "PTCL Bill"
            lower.contains("stormfiber") -> "StormFiber"
            lower.contains("nayatel") -> "Nayatel"
            lower.contains("naheed") -> "Naheed Supermarket"
            lower.contains("imtiaz") -> "Imtiaz Super Market"
            lower.contains("carrefour") -> "Carrefour"
            lower.contains("metro") -> "Metro Cash & Carry"
            lower.contains("pso") -> "PSO Petrol Pump"
            lower.contains("shell") -> "Shell Station"
            lower.contains("total") -> "Total Parco"
            lower.contains("mcdonald") -> "McDonald's"
            lower.contains("kfc") -> "KFC Pakistan"
            lower.contains("mobile load") || lower.contains("easyload") -> "Mobile Load Recharge"
            lower.contains("raast") -> "Raast Instant Transfer"
            lower.contains("ibft") -> "IBFT Bank Transfer"
            lower.contains("atm") -> "ATM Cash Withdrawal"
            type == TransactionType.INCOME -> "Direct Credit / Transfer"
            else -> "${accountType.title} Payment"
        }
    }

    fun categorize(body: String, merchant: String, type: TransactionType): String {
        val text = "${body.lowercase(Locale.ROOT)} ${merchant.lowercase(Locale.ROOT)}"

        if (type == TransactionType.INCOME) {
            return if (text.contains("salary")) {
                ExpenseCategories.SALARY
            } else if (text.contains("dividend") || text.contains("profit") || text.contains("saving")) {
                ExpenseCategories.INVESTMENT
            } else {
                ExpenseCategories.MONEY_TRANSFER
            }
        }

        return when {
            text.contains("foodpanda") || text.contains("mcdonald") || text.contains("kfc") ||
                    text.contains("subway") || text.contains("burger") || text.contains("pizza") ||
                    text.contains("cafe") || text.contains("restaurant") || text.contains("dining") ||
                    text.contains("bakery") || text.contains("chai") || text.contains("cheezious") ||
                    text.contains("kababjees") || text.contains("broadway") || text.contains("hardees") -> {
                ExpenseCategories.FOOD
            }

            text.contains("naheed") || text.contains("imtiaz") || text.contains("metro") ||
                    text.contains("carrefour") || text.contains("chase up") || text.contains("al-fatah") ||
                    text.contains("grocery") || text.contains("mart") || text.contains("supermarket") ||
                    text.contains("store") || text.contains("bazaar") || text.contains("spar") -> {
                ExpenseCategories.GROCERIES
            }

            text.contains("shell") || text.contains("total") || text.contains("pso") ||
                    text.contains("attock") || text.contains("hascol") || text.contains("petrol") ||
                    text.contains("fuel") || text.contains("cng") || text.contains("careem") ||
                    text.contains("indrive") || text.contains("bykea") || text.contains("uber") ||
                    text.contains("yango") || text.contains("toll") -> {
                ExpenseCategories.FUEL_TRANSPORT
            }

            text.contains("k-electric") || text.contains("kelectric") || text.contains("lesco") ||
                    text.contains("iesco") || text.contains("gepco") || text.contains("fesco") ||
                    text.contains("mepco") || text.contains("pesco") || text.contains("sngpl") ||
                    text.contains("ssgc") || text.contains("ptcl") || text.contains("stormfiber") ||
                    text.contains("nayatel") || text.contains("bill") || text.contains("electricity") ||
                    text.contains("gas bill") || text.contains("water board") -> {
                ExpenseCategories.BILLS_UTILITIES
            }

            text.contains("mobile load") || text.contains("easyload") || text.contains("topup") ||
                    text.contains("jazz topup") || text.contains("telenor load") || text.contains("zong") ||
                    text.contains("ufone") || text.contains("super card") || text.contains("bundle") ||
                    text.contains("package") || text.contains("recharge") -> {
                ExpenseCategories.MOBILE_INTERNET
            }

            text.contains("daraz") || text.contains("outfitters") || text.contains("khaadi") ||
                    text.contains("sapphire") || text.contains("gul ahmed") || text.contains("junaid jamshed") ||
                    text.contains("j.") || text.contains("stylo") || text.contains("elo") ||
                    text.contains("aliexpress") || text.contains("mall") || text.contains("clothing") ||
                    text.contains("shoes") || text.contains("fashion") -> {
                ExpenseCategories.SHOPPING
            }

            text.contains("pharmacy") || text.contains("servaid") || text.contains("d.watson") ||
                    text.contains("fazal din") || text.contains("clinic") || text.contains("hospital") ||
                    text.contains("doctor") || text.contains("medical") || text.contains("lab") ||
                    text.contains("chughtai") || text.contains("shaukat khanum") -> {
                ExpenseCategories.HEALTH
            }

            text.contains("school") || text.contains("college") || text.contains("university") ||
                    text.contains("academy") || text.contains("tuition") || text.contains("fee") ||
                    text.contains("lums") || text.contains("nust") || text.contains("iba") -> {
                ExpenseCategories.EDUCATION
            }

            text.contains("netflix") || text.contains("cinepax") || text.contains("nueplex") ||
                    text.contains("cinema") || text.contains("movie") || text.contains("steam") ||
                    text.contains("playstation") || text.contains("spotify") || text.contains("game") -> {
                ExpenseCategories.ENTERTAINMENT
            }

            text.contains("transferred") || text.contains("sent to") || text.contains("raast") ||
                    text.contains("ibft") || text.contains("atm withdrawal") || text.contains("transfer") -> {
                ExpenseCategories.MONEY_TRANSFER
            }

            else -> ExpenseCategories.OTHERS
        }
    }

    private fun generateHash(
        sender: String,
        timestamp: Long,
        amount: Double,
        type: TransactionType,
        refId: String?,
        balanceAfter: Double?,
        body: String
    ): String {
        // Robust deduplication signature
        val rawSignature = if (!refId.isNullOrBlank()) {
            "${sender.lowercase(Locale.ROOT)}_$refId"
        } else {
            // Use 1-minute window tolerance on timestamp if from same sender & amount & balance
            val timeWindow = timestamp / (60 * 1000)
            "${sender.lowercase(Locale.ROOT)}_${timeWindow}_${amount}_${type}_${balanceAfter ?: body.take(25)}"
        }

        return try {
            val md = MessageDigest.getInstance("SHA-256")
            val bytes = md.digest(rawSignature.toByteArray(Charsets.UTF_8))
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            rawSignature.hashCode().toString()
        }
    }
}
