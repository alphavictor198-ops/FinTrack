package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.Budget
import com.example.data.Bill
import com.example.data.Investment
import com.example.data.SavingsGoal
import com.example.data.PendingSmsTransaction
import com.example.data.ChitFund
import com.example.data.FinanceDatabase
import com.example.data.FinanceRepository
import com.example.data.Transaction
import com.example.network.GeminiService
import com.example.network.ParsedTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FinanceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FinanceRepository

    init {
        val database = FinanceDatabase.getDatabase(application)
        repository = FinanceRepository(application, database.financeDao())

        // Seed default budgets in INR to make first-boot visuals pleasant in Indian context
        viewModelScope.launch {
            repository.allBudgets.collect { budgets ->
                if (budgets.isEmpty()) {
                    repository.insertBudget(Budget("Food", 10000.0))
                    repository.insertBudget(Budget("Shopping", 8000.0))
                    repository.insertBudget(Budget("Utilities", 5000.0))
                    repository.insertBudget(Budget("Entertainment", 4000.0))
                    repository.insertBudget(Budget("Transport", 3000.0))
                    repository.insertBudget(Budget("Other", 5000.0))
                    repository.insertBudget(Budget("Overall", 35000.0))
                }
            }
        }

        // Trigger bill reminders check on app startup
        viewModelScope.launch {
            repository.checkAndTriggerBillReminders()
        }
    }

    val transactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgets: StateFlow<List<Budget>> = repository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bills: StateFlow<List<Bill>> = repository.allBills
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val investments: StateFlow<List<Investment>> = repository.allInvestments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savingsGoals: StateFlow<List<SavingsGoal>> = repository.allSavingsGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingSmsTransactions: StateFlow<List<PendingSmsTransaction>> = repository.allPendingSmsTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chitFunds: StateFlow<List<ChitFund>> = repository.allChitFunds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // OCR / Smart Scanning State
    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanError = MutableStateFlow<String?>(null)
    val scanError: StateFlow<String?> = _scanError.asStateFlow()

    private val _parsedResult = MutableStateFlow<ParsedTransaction?>(null)
    val parsedResult: StateFlow<ParsedTransaction?> = _parsedResult.asStateFlow()

    fun addTransaction(title: String, amount: Double, category: String, isIncome: Boolean, notes: String) {
        viewModelScope.launch {
            val transaction = Transaction(
                title = title,
                amount = amount,
                category = category,
                isIncome = isIncome,
                notes = notes
            )
            repository.insertTransaction(transaction)
        }
    }

    fun addParsedTransaction(parsed: ParsedTransaction) {
        viewModelScope.launch {
            val transaction = Transaction(
                title = parsed.title,
                amount = parsed.amount,
                category = parsed.category,
                isIncome = parsed.isIncome,
                notes = parsed.notes
            )
            repository.insertTransaction(transaction)
            _parsedResult.value = null // Clear result after saving
        }
    }

    fun clearParsedResult() {
        _parsedResult.value = null
    }

    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            repository.deleteTransaction(id)
        }
    }

    fun updateBudget(category: String, limit: Double) {
        viewModelScope.launch {
            repository.insertBudget(Budget(category, limit))
        }
    }

    // Bills operations
    fun addBill(name: String, amount: Double, dueDate: Long) {
        viewModelScope.launch {
            repository.insertBill(Bill(name = name, amount = amount, dueDate = dueDate))
        }
    }

    fun deleteBill(id: Int) {
        viewModelScope.launch {
            repository.deleteBill(id)
        }
    }

    fun toggleBillPaid(bill: Bill) {
        viewModelScope.launch {
            repository.updateBillPaidStatus(bill.id, !bill.isPaid)
        }
    }

    fun checkBillReminders() {
        viewModelScope.launch {
            repository.checkAndTriggerBillReminders()
        }
    }

    // Investments operations
    fun addInvestment(name: String, assetType: String, purchasePrice: Double, currentValue: Double, quantity: Double) {
        viewModelScope.launch {
            repository.insertInvestment(
                Investment(
                    name = name,
                    assetType = assetType,
                    purchasePrice = purchasePrice,
                    currentValue = currentValue,
                    quantity = quantity
                )
            )
        }
    }

    fun deleteInvestment(id: Int) {
        viewModelScope.launch {
            repository.deleteInvestment(id)
        }
    }

    // Savings Goals operations
    fun addSavingsGoal(name: String, targetAmount: Double, targetDate: Long) {
        viewModelScope.launch {
            repository.insertSavingsGoal(
                SavingsGoal(
                    name = name,
                    targetAmount = targetAmount,
                    targetDate = targetDate,
                    savedAmount = 0.0
                )
            )
        }
    }

    fun deleteSavingsGoal(id: Int) {
        viewModelScope.launch {
            repository.deleteSavingsGoal(id)
        }
    }

    fun allocateSavings(id: Int, currentSaved: Double, amountToAllocate: Double) {
        viewModelScope.launch {
            repository.updateSavingsGoalSavedAmount(id, currentSaved + amountToAllocate)
        }
    }

    fun parseWithGemini(text: String?, bitmap: Bitmap?, userApiKey: String? = null) {
        viewModelScope.launch {
            _isScanning.value = true
            _scanError.value = null
            _parsedResult.value = null

            val result = GeminiService.parseTransaction(text, bitmap, userApiKey)
            if (result != null) {
                _parsedResult.value = result
            } else {
                _scanError.value = "Failed to parse. Please check that your API key is correct and you are connected to the internet."
            }
            _isScanning.value = false
        }
    }

    // Pending SMS Operations
    fun approvePendingSms(sms: PendingSmsTransaction, selectedCategory: String, customizedTitle: String) {
        viewModelScope.launch {
            val isIncome = sms.transactionType == "CREDIT"
            val transaction = Transaction(
                title = customizedTitle,
                amount = sms.amount,
                category = selectedCategory,
                isIncome = isIncome,
                date = sms.date,
                notes = "Auto-tracked from SMS (${sms.bankName})"
            )
            repository.insertTransaction(transaction)
            repository.deletePendingSms(sms.id)
        }
    }

    fun dismissPendingSms(id: Int) {
        viewModelScope.launch {
            repository.deletePendingSms(id)
        }
    }

    // Chit Funds Operations
    fun addChitFund(
        name: String,
        totalMembers: Int,
        monthlyContribution: Double,
        durationMonths: Int,
        currentMonth: Int,
        bidWinner: String,
        wonAmount: Double,
        isWinner: Boolean
    ) {
        viewModelScope.launch {
            repository.insertChitFund(
                ChitFund(
                    name = name,
                    totalMembers = totalMembers,
                    monthlyContribution = monthlyContribution,
                    durationMonths = durationMonths,
                    currentMonth = currentMonth,
                    bidWinner = bidWinner,
                    wonAmount = wonAmount,
                    isWinner = isWinner
                )
            )
        }
    }

    fun deleteChitFund(id: Int) {
        viewModelScope.launch {
            repository.deleteChitFund(id)
        }
    }

    fun scanSmsInbox(context: android.content.Context) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val uri = android.net.Uri.parse("content://sms/inbox")
            val projection = arrayOf("address", "body", "date")
            val cursor = context.contentResolver.query(uri, projection, null, null, "date DESC")
            
            if (cursor != null) {
                val addressIndex = cursor.getColumnIndexOrThrow("address")
                val bodyIndex = cursor.getColumnIndexOrThrow("body")
                val dateIndex = cursor.getColumnIndexOrThrow("date")
                
                var count = 0
                // Limit scanning to avoid taking too much time
                while (cursor.moveToNext() && count < 100) {
                    val address = cursor.getString(addressIndex) ?: "Unknown"
                    val body = cursor.getString(bodyIndex) ?: ""
                    val date = cursor.getLong(dateIndex)
                    
                    val parsed = com.example.util.SmsParser.parse(address, body)
                    if (parsed != null) {
                        val pendingSms = PendingSmsTransaction(
                            sender = address,
                            body = body,
                            amount = parsed.amount,
                            date = date,
                            transactionType = parsed.transactionType,
                            probableCategory = parsed.probableCategory,
                            bankName = parsed.bankName
                        )
                        repository.insertPendingSms(pendingSms)
                        count++
                    }
                }
                cursor.close()
            }
        }
    }
}
