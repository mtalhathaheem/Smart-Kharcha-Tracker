package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE isActive = 1 ORDER BY isDefault DESC, displayName ASC")
    fun getAllActiveAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getAccountById(id: String): AccountEntity?

    @Query("SELECT * FROM accounts")
    suspend fun getAllAccountsSnapshot(): List<AccountEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(account: AccountEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIfAbsent(accounts: List<AccountEntity>)

    @Update
    suspend fun update(account: AccountEntity)

    @Query("UPDATE accounts SET currentBalance = :newBalance, lastUpdated = :lastUpdated WHERE id = :id")
    suspend fun updateBalance(id: String, newBalance: Double, lastUpdated: Long)

    @Query("UPDATE accounts SET currentBalance = currentBalance + :delta, lastUpdated = :lastUpdated WHERE id = :id")
    suspend fun adjustBalance(id: String, delta: Double, lastUpdated: Long)

    @Delete
    suspend fun delete(account: AccountEntity)
}
