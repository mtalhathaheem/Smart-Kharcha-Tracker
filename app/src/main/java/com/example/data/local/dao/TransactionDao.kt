package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

data class CategorySpending(
    val category: String,
    val totalAmount: Double,
    val count: Int
)

data class DailySpending(
    val dayTimestamp: Long,
    val totalAmount: Double
)

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM transactions WHERE smsHash = :hash)")
    suspend fun existsBySmsHash(hash: String): Boolean

    @Query("SELECT * FROM transactions WHERE smsHash = :hash LIMIT 1")
    suspend fun getBySmsHash(hash: String): TransactionEntity?

    @Query("SELECT category, SUM(amount) as totalAmount, COUNT(id) as count FROM transactions WHERE type = 'EXPENSE' AND timestamp >= :startTime AND timestamp <= :endTime GROUP BY category ORDER BY totalAmount DESC")
    fun getExpenseBreakdownByCategory(startTime: Long, endTime: Long): Flow<List<CategorySpending>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(transactions: List<TransactionEntity>): List<Long>

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM transactions")
    fun getTransactionCount(): Flow<Int>

    @Query("DELETE FROM transactions")
    suspend fun clearAll()
}
