package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object NotificationHelper {
    private const val CHANNEL_ID = "finance_budget_alerts"
    private const val CHANNEL_NAME = "Budget and Spending Alerts"
    private const val CHANNEL_DESC = "Notifications when you reach or exceed category spending limits"
    private const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendBudgetWarningNotification(context: Context, category: String, spent: Double, limit: Double) {
        createNotificationChannel(context)

        // Check permission for POST_NOTIFICATIONS on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted, we can't send notification
                return
            }
        }

        val percentage = ((spent / limit) * 100).toInt()
        val title = if (spent > limit) {
            "Budget Exceeded! 🚨"
        } else if (percentage >= 100) {
            "Budget Exceeded! 🚨"
        } else {
            "Budget Limit Approaching! ⚠️"
        }
        
        val message = if (spent > limit || percentage >= 100) {
            "You have exceeded your $category budget! Spent ₹${String.format("%.2f", spent)} of ₹${String.format("%.2f", limit)}."
        } else {
            "You have spent $percentage% of your $category budget (₹${String.format("%.2f", spent)} / ₹${String.format("%.2f", limit)})."
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_chat) // standard system chat/alert icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(NOTIFICATION_ID + category.hashCode(), builder.build())
            } catch (e: SecurityException) {
                // Handle missing permission gracefully
            }
        }
    }

    fun sendBillReminderNotification(context: Context, billName: String, amount: Double, daysRemaining: Int) {
        createNotificationChannel(context)

        // Check permission for POST_NOTIFICATIONS on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted, we can't send notification
                return
            }
        }

        val title = "Upcoming Bill Reminder 📅"
        val message = if (daysRemaining <= 0) {
            "Your bill \"$billName\" for ₹${String.format("%.2f", amount)} is due TODAY!"
        } else if (daysRemaining == 1) {
            "Your bill \"$billName\" for ₹${String.format("%.2f", amount)} is due TOMORROW!"
        } else {
            "Your bill \"$billName\" for ₹${String.format("%.2f", amount)} is due in $daysRemaining days!"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(NOTIFICATION_ID + billName.hashCode(), builder.build())
            } catch (e: SecurityException) {
                // Handle missing permission gracefully
            }
        }
    }

    fun sendSmsDetectedNotification(context: Context, amount: Double, bank: String) {
        createNotificationChannel(context)

        // Check permission for POST_NOTIFICATIONS on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val title = "SMS Transaction Detected 💳"
        val message = "Detected a spend of ₹${String.format("%.2f", amount)} via $bank. Tap to review and categorize."

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(NOTIFICATION_ID + amount.toInt(), builder.build())
            } catch (e: SecurityException) {
                // Handle missing permission gracefully
            }
        }
    }
}
