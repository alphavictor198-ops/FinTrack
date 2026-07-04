package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val category: String,
    val date: Long = System.currentTimeMillis(),
    val isIncome: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey val category: String, // e.g., "Food", "Shopping", "Utilities", "Entertainment", "Transport", "Overall"
    val limitAmount: Double
)

@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val amount: Double,
    val dueDate: Long,
    val isPaid: Boolean = false,
    val notified: Boolean = false
)

@Entity(tableName = "investments")
data class Investment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val assetType: String, // "Stocks", "Crypto", "Real Estate", "Mutual Funds", "Other"
    val purchasePrice: Double, // Price per unit
    val currentValue: Double,  // Current price per unit
    val quantity: Double
)

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val targetAmount: Double,
    val targetDate: Long,
    val savedAmount: Double = 0.0
)

@Entity(tableName = "pending_sms_transactions")
data class PendingSmsTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String,
    val body: String,
    val amount: Double,
    val date: Long,
    val transactionType: String, // "DEBIT" or "CREDIT"
    val probableCategory: String = "Uncategorized",
    val bankName: String
)

@Entity(tableName = "chit_funds")
data class ChitFund(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val totalMembers: Int,
    val monthlyContribution: Double,
    val durationMonths: Int,
    val currentMonth: Int = 1,
    val bidWinner: String = "",
    val wonAmount: Double = 0.0,
    val isWinner: Boolean = false
)

@Dao
interface FinanceDao {
    // Transactions
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Int)

    // Budgets
    @Query("SELECT * FROM budgets")
    fun getAllBudgets(): Flow<List<Budget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)

    @Query("SELECT * FROM budgets WHERE category = :category LIMIT 1")
    suspend fun getBudgetByCategory(category: String): Budget?

    // Bills
    @Query("SELECT * FROM bills ORDER BY dueDate ASC")
    fun getAllBills(): Flow<List<Bill>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: Bill)

    @Query("DELETE FROM bills WHERE id = :id")
    suspend fun deleteBill(id: Int)

    @Query("UPDATE bills SET isPaid = :isPaid WHERE id = :id")
    suspend fun updateBillPaidStatus(id: Int, isPaid: Boolean)

    @Query("UPDATE bills SET notified = :notified WHERE id = :id")
    suspend fun updateBillNotificationStatus(id: Int, notified: Boolean)

    // Investments
    @Query("SELECT * FROM investments ORDER BY id DESC")
    fun getAllInvestments(): Flow<List<Investment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvestment(investment: Investment)

    @Query("DELETE FROM investments WHERE id = :id")
    suspend fun deleteInvestment(id: Int)

    // Savings Goals
    @Query("SELECT * FROM savings_goals ORDER BY targetDate ASC")
    fun getAllSavingsGoals(): Flow<List<SavingsGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsGoal(goal: SavingsGoal)

    @Query("DELETE FROM savings_goals WHERE id = :id")
    suspend fun deleteSavingsGoal(id: Int)

    @Query("UPDATE savings_goals SET savedAmount = :savedAmount WHERE id = :id")
    suspend fun updateSavingsGoalSavedAmount(id: Int, savedAmount: Double)

    // Pending SMS Transactions
    @Query("SELECT * FROM pending_sms_transactions ORDER BY date DESC")
    fun getAllPendingSmsTransactions(): Flow<List<PendingSmsTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingSms(sms: PendingSmsTransaction)

    @Query("DELETE FROM pending_sms_transactions WHERE id = :id")
    suspend fun deletePendingSms(id: Int)

    // Chit Funds
    @Query("SELECT * FROM chit_funds ORDER BY id DESC")
    fun getAllChitFunds(): Flow<List<ChitFund>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChitFund(chitFund: ChitFund)

    @Query("DELETE FROM chit_funds WHERE id = :id")
    suspend fun deleteChitFund(id: Int)
}

@Database(
    entities = [
        Transaction::class,
        Budget::class,
        Bill::class,
        Investment::class,
        SavingsGoal::class,
        PendingSmsTransaction::class,
        ChitFund::class
    ],
    version = 3,
    exportSchema = false
)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun financeDao(): FinanceDao

    companion object {
        @Volatile
        private var INSTANCE: FinanceDatabase? = null

        fun getDatabase(context: Context): FinanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    "finance_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
