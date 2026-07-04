package com.example.data

import android.content.Context
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.Calendar

class FinanceRepository(
    private val context: Context,
    private val financeDao: FinanceDao
) {
    val allTransactions: Flow<List<Transaction>> = financeDao.getAllTransactions()
    val allBudgets: Flow<List<Budget>> = financeDao.getAllBudgets()
    val allBills: Flow<List<Bill>> = financeDao.getAllBills()
    val allInvestments: Flow<List<Investment>> = financeDao.getAllInvestments()
    val allSavingsGoals: Flow<List<SavingsGoal>> = financeDao.getAllSavingsGoals()
    val allPendingSmsTransactions: Flow<List<PendingSmsTransaction>> = financeDao.getAllPendingSmsTransactions()
    val allChitFunds: Flow<List<ChitFund>> = financeDao.getAllChitFunds()

    suspend fun insertTransaction(transaction: Transaction) {
        financeDao.insertTransaction(transaction)

        // Only check budget alert for expenses
        if (!transaction.isIncome) {
            checkAndTriggerBudgetAlert(transaction.category)
            checkAndTriggerBudgetAlert("Overall")
        }
    }

    suspend fun deleteTransaction(id: Int) {
        financeDao.deleteTransaction(id)
    }

    suspend fun insertBudget(budget: Budget) {
        financeDao.insertBudget(budget)
    }

    // Bills CRUD
    suspend fun insertBill(bill: Bill) {
        financeDao.insertBill(bill)
        checkAndTriggerBillReminders()
    }

    suspend fun deleteBill(id: Int) {
        financeDao.deleteBill(id)
    }

    suspend fun updateBillPaidStatus(id: Int, isPaid: Boolean) {
        financeDao.updateBillPaidStatus(id, isPaid)
    }

    suspend fun updateBillNotificationStatus(id: Int, notified: Boolean) {
        financeDao.updateBillNotificationStatus(id, notified)
    }

    suspend fun checkAndTriggerBillReminders() {
        val billsList = financeDao.getAllBills().firstOrNull() ?: return
        val currentCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        billsList.forEach { bill ->
            if (!bill.isPaid && !bill.notified) {
                val dueCal = Calendar.getInstance().apply {
                    timeInMillis = bill.dueDate
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val diffMillis = dueCal.timeInMillis - currentCal.timeInMillis
                val daysRemaining = (diffMillis / (1000 * 60 * 60 * 24)).toInt()

                // Notify if due within 3 days (due today, tomorrow, or next 2 days)
                if (daysRemaining in 0..3) {
                    NotificationHelper.sendBillReminderNotification(context, bill.name, bill.amount, daysRemaining)
                    financeDao.updateBillNotificationStatus(bill.id, true)
                }
            }
        }
    }

    // Investments CRUD
    suspend fun insertInvestment(investment: Investment) {
        financeDao.insertInvestment(investment)
    }

    suspend fun deleteInvestment(id: Int) {
        financeDao.deleteInvestment(id)
    }

    // Savings Goals CRUD
    suspend fun insertSavingsGoal(goal: SavingsGoal) {
        financeDao.insertSavingsGoal(goal)
    }

    suspend fun deleteSavingsGoal(id: Int) {
        financeDao.deleteSavingsGoal(id)
    }

    suspend fun updateSavingsGoalSavedAmount(id: Int, savedAmount: Double) {
        financeDao.updateSavingsGoalSavedAmount(id, savedAmount)
    }

    // Pending SMS Operations
    suspend fun insertPendingSms(sms: PendingSmsTransaction) {
        financeDao.insertPendingSms(sms)
    }

    suspend fun deletePendingSms(id: Int) {
        financeDao.deletePendingSms(id)
    }

    // Chit Funds Operations
    suspend fun insertChitFund(chitFund: ChitFund) {
        financeDao.insertChitFund(chitFund)
    }

    suspend fun deleteChitFund(id: Int) {
        financeDao.deleteChitFund(id)
    }

    private suspend fun checkAndTriggerBudgetAlert(category: String) {
        val budget = financeDao.getBudgetByCategory(category) ?: return
        val limit = budget.limitAmount
        if (limit <= 0) return

        // Calculate total expenses for current calendar month
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis

        val transactions = financeDao.getAllTransactions().firstOrNull() ?: return
        val spent = transactions.filter {
            it.date >= startOfMonth &&
            !it.isIncome &&
            (category == "Overall" || it.category.equals(category, ignoreCase = true))
        }.sumOf { it.amount }

        // Alert user if spent is 80% or more of the budget
        if (spent >= limit * 0.8) {
            NotificationHelper.sendBudgetWarningNotification(context, category, spent, limit)
        }
    }
}
