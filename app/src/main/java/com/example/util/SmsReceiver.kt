package com.example.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsMessage
import android.util.Log
import com.example.data.FinanceDatabase
import com.example.data.PendingSmsTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    private val TAG = "SmsReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.provider.Telephony.SMS_RECEIVED") {
            val bundle = intent.extras
            if (bundle != null) {
                try {
                    val pdus = bundle.get("pdus") as Array<*>
                    val format = bundle.getString("format")
                    
                    var sender = ""
                    val bodyBuilder = java.lang.StringBuilder()
                    
                    for (pdu in pdus) {
                        val message = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            SmsMessage.createFromPdu(pdu as ByteArray, format)
                        } else {
                            @Suppress("DEPRECATION")
                            SmsMessage.createFromPdu(pdu as ByteArray)
                        }
                        
                        sender = message.originatingAddress ?: "Unknown"
                        bodyBuilder.append(message.messageBody)
                    }
                    
                    val body = bodyBuilder.toString()
                    Log.d(TAG, "Received SMS from $sender: $body")
                    
                    val parsed = SmsParser.parse(sender, body)
                    if (parsed != null) {
                        Log.d(TAG, "Parsed Transaction: Amount: ${parsed.amount}, Type: ${parsed.transactionType}, Bank: ${parsed.bankName}")
                        
                        val database = FinanceDatabase.getDatabase(context)
                        val dao = database.financeDao()
                        
                        val pendingSms = PendingSmsTransaction(
                            sender = sender,
                            body = body,
                            amount = parsed.amount,
                            date = System.currentTimeMillis(),
                            transactionType = parsed.transactionType,
                            probableCategory = parsed.probableCategory,
                            bankName = parsed.bankName
                        )
                        
                        val pendingResult = goAsync()
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                dao.insertPendingSms(pendingSms)
                                NotificationHelper.sendSmsDetectedNotification(
                                    context = context,
                                    amount = parsed.amount,
                                    bank = parsed.bankName
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "Error saving pending SMS", e)
                            } finally {
                                pendingResult.finish()
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing SMS bundle", e)
                }
            }
        }
    }
}

object SmsParser {
    fun parse(sender: String, body: String): ParsedSms? {
        val cleanBody = body.replace(",", "")
        
        // Find Amount using various patterns
        val amountRegexes = listOf(
            Regex("(?i)(?:Rs\\.?|INR|₹)\\s*([\\d\\.]+)"),
            Regex("(?i)debited\\s*(?:by|for)?\\s*(?:Rs\\.?|INR|₹)?\\s*([\\d\\.]+)"),
            Regex("(?i)spent\\s*(?:Rs\\.?|INR|₹)?\\s*([\\d\\.]+)"),
            Regex("(?i)sent\\s*(?:Rs\\.?|INR|₹)?\\s*([\\d\\.]+)")
        )
        
        var amount: Double? = null
        for (regex in amountRegexes) {
            val match = regex.find(cleanBody)
            if (match != null) {
                val amtStr = match.groupValues[1]
                // Make sure it looks like a valid number, and doesn't end with a dot unless it's decimal
                val cleanAmtStr = if (amtStr.endsWith(".")) amtStr.dropLast(1) else amtStr
                amount = cleanAmtStr.toDoubleOrNull()
                if (amount != null) break
            }
        }
        
        if (amount == null || amount <= 0.0) return null

        // Determine Debit/Credit type
        val isCredit = body.contains("credited", ignoreCase = true) || 
                       body.contains("received", ignoreCase = true) ||
                       body.contains("refunded", ignoreCase = true) ||
                       body.contains("added to", ignoreCase = true)
        val transactionType = if (isCredit) "CREDIT" else "DEBIT"

        // Determine probable category
        val probableCategory = when {
            body.contains("swiggy", ignoreCase = true) || 
            body.contains("zomato", ignoreCase = true) || 
            body.contains("restaurant", ignoreCase = true) ||
            body.contains("cafe", ignoreCase = true) ||
            body.contains("dominos", ignoreCase = true) ||
            body.contains("pizza", ignoreCase = true) ||
            body.contains("food", ignoreCase = true) -> "Food"
            
            body.contains("amazon", ignoreCase = true) || 
            body.contains("flipkart", ignoreCase = true) || 
            body.contains("myntra", ignoreCase = true) || 
            body.contains("nykaa", ignoreCase = true) || 
            body.contains("groceries", ignoreCase = true) ||
            body.contains("blinkit", ignoreCase = true) ||
            body.contains("zepto", ignoreCase = true) ||
            body.contains("bigbasket", ignoreCase = true) ||
            body.contains("shopping", ignoreCase = true) ||
            body.contains("reliance", ignoreCase = true) -> "Shopping"
            
            body.contains("airtel", ignoreCase = true) || 
            body.contains("jio", ignoreCase = true) || 
            body.contains("vi ", ignoreCase = true) || 
            body.contains("electric", ignoreCase = true) || 
            body.contains("bescom", ignoreCase = true) || 
            body.contains("bill", ignoreCase = true) || 
            body.contains("water", ignoreCase = true) || 
            body.contains("recharge", ignoreCase = true) -> "Utilities"
            
            body.contains("netflix", ignoreCase = true) || 
            body.contains("hotstar", ignoreCase = true) || 
            body.contains("pvr", ignoreCase = true) || 
            body.contains("bookmyshow", ignoreCase = true) || 
            body.contains("spotify", ignoreCase = true) || 
            body.contains("movie", ignoreCase = true) || 
            body.contains("cinema", ignoreCase = true) -> "Entertainment"
            
            body.contains("ola", ignoreCase = true) || 
            body.contains("uber", ignoreCase = true) || 
            body.contains("auto", ignoreCase = true) || 
            body.contains("metro", ignoreCase = true) || 
            body.contains("fuel", ignoreCase = true) || 
            body.contains("petrol", ignoreCase = true) || 
            body.contains("cng", ignoreCase = true) || 
            body.contains("diesel", ignoreCase = true) -> "Transport"
            
            else -> "Other"
        }

        // Determine bank name from sender or message content
        val bankName = when {
            sender.contains("HDFC", ignoreCase = true) || body.contains("HDFC", ignoreCase = true) -> "HDFC Bank"
            sender.contains("ICICI", ignoreCase = true) || body.contains("ICICI", ignoreCase = true) -> "ICICI Bank"
            sender.contains("SBI", ignoreCase = true) || body.contains("SBI", ignoreCase = true) -> "State Bank of India"
            sender.contains("AXIS", ignoreCase = true) || body.contains("AXIS", ignoreCase = true) -> "Axis Bank"
            sender.contains("PNB", ignoreCase = true) || body.contains("PNB", ignoreCase = true) -> "Punjab National Bank"
            sender.contains("PAYTM", ignoreCase = true) || body.contains("PAYTM", ignoreCase = true) -> "Paytm"
            sender.contains("KOTAK", ignoreCase = true) || body.contains("KOTAK", ignoreCase = true) -> "Kotak Bank"
            sender.contains("BOB", ignoreCase = true) || body.contains("BOB", ignoreCase = true) -> "Bank of Baroda"
            sender.contains("PHONEPE", ignoreCase = true) || body.contains("PHONEPE", ignoreCase = true) -> "PhonePe"
            sender.contains("UNIONB", ignoreCase = true) || body.contains("UNIONB", ignoreCase = true) -> "Union Bank"
            else -> {
                // Strip common carrier code prefixes like "AD-", "VM-", "DZ-", "JM-"
                val rawSender = sender.uppercase()
                val hyphenIndex = rawSender.indexOf("-")
                if (hyphenIndex != -1 && hyphenIndex < rawSender.length - 1) {
                    rawSender.substring(hyphenIndex + 1)
                } else {
                    rawSender
                }
            }
        }

        return ParsedSms(amount, transactionType, probableCategory, bankName)
    }
}

data class ParsedSms(
    val amount: Double,
    val transactionType: String,
    val probableCategory: String,
    val bankName: String
)
