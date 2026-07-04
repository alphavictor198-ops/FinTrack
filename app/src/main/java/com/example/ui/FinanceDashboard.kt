package com.example.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalActivity
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.ShoppingBag
import com.example.network.ParsedTransaction
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.data.Budget
import com.example.data.Transaction
import com.example.ui.theme.HighDensityPrimary
import com.example.ui.theme.HighDensityBackground
import com.example.ui.theme.HighDensitySecondary
import com.example.ui.theme.HighDensitySecondaryContainer
import com.example.ui.theme.HighDensitySurface
import com.example.ui.theme.HighDensityText
import com.example.ui.theme.HighDensityTextMuted
import com.example.ui.theme.HighDensityWarningBg
import com.example.ui.theme.HighDensityWarningText
import com.example.ui.theme.HighDensityWarningBorder
import com.example.ui.theme.BadgeSafeBg
import com.example.ui.theme.BadgeSafeText
import com.example.ui.theme.BadgeNearLimitBg
import com.example.ui.theme.BadgeNearLimitText
import com.example.ui.theme.BadgeCriticalBg
import com.example.ui.theme.BadgeCriticalText
import com.example.ui.theme.BadgeFixedBg
import com.example.ui.theme.BadgeFixedText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceDashboard(viewModel: FinanceViewModel) {
    val context = LocalContext.current
    val transactions by viewModel.transactions.collectAsState()
    val budgets by viewModel.budgets.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val scanError by viewModel.scanError.collectAsState()
    val parsedResult by viewModel.parsedResult.collectAsState()

    val bills by viewModel.bills.collectAsState()
    val investments by viewModel.investments.collectAsState()
    val savingsGoals by viewModel.savingsGoals.collectAsState()
    val pendingSmsList by viewModel.pendingSmsTransactions.collectAsState()
    val chitFundsList by viewModel.chitFunds.collectAsState()

    var selectedTab by remember { mutableStateOf("Dashboard") } // "Dashboard", "Bills", "Investments", "Savings"
    var showAddDialog by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(false) }
    var showAddBillDialog by remember { mutableStateOf(false) }
    var showAddInvestmentDialog by remember { mutableStateOf(false) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAllocateDialogGoalId by remember { mutableStateOf<Int?>(null) }
    var showReviewSmsDialog by remember { mutableStateOf(false) }
    var showAddChitFundDialog by remember { mutableStateOf(false) }

    var userApiKey by remember { mutableStateOf("") }

    // Request notifications permission on Android 13+
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Spending alert notifications enabled!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Notifications disabled. Warning alerts will show in-app only.", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Dynamic stats computation for the current calendar month
    val currentMonthTransactions = remember(transactions) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis
        transactions.filter { it.date >= startOfMonth }
    }

    val totalIncome = remember(currentMonthTransactions) {
        currentMonthTransactions.filter { it.isIncome }.sumOf { it.amount }
    }

    val totalExpenses = remember(currentMonthTransactions) {
        currentMonthTransactions.filter { !it.isIncome }.sumOf { it.amount }
    }

    val netBalance = totalIncome - totalExpenses

    val categories = listOf("Food", "Shopping", "Utilities", "Entertainment", "Transport", "Other")

    val spentByCategory = remember(currentMonthTransactions) {
        categories.associateWith { cat ->
            currentMonthTransactions.filter { !it.isIncome && it.category.equals(cat, ignoreCase = true) }.sumOf { it.amount }
        }
    }

    val budgetLimits = remember(budgets) {
        val limitMap = budgets.associate { it.category to it.limitAmount }.toMutableMap()
        // Ensure every category has some budget value mapped
        categories.forEach { cat ->
            if (!limitMap.containsKey(cat)) limitMap[cat] = 0.0
        }
        if (!limitMap.containsKey("Overall")) limitMap["Overall"] = 0.0
        limitMap
    }

    val spentOverall = totalExpenses
    val limitOverall = budgetLimits["Overall"] ?: 0.0

    val warningCategory = remember(spentByCategory, budgetLimits) {
        categories
            .filter { (budgetLimits[it] ?: 0.0) > 0.0 }
            .map { cat ->
                val spent = spentByCategory[cat] ?: 0.0
                val limit = budgetLimits[cat] ?: 1.0
                cat to (spent / limit)
            }
            .filter { it.second >= 0.8 } // Show warning if spent is >= 80%
            .maxByOrNull { it.second }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // User Avatar
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(HighDensitySecondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "A", // Alex
                                color = Color(0xFF21005D),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        
                        Column {
                            Text(
                                text = "Morning, Alex",
                                color = HighDensityTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Finance Central",
                                color = HighDensityText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                        }
                    }
                },
                actions = {
                    // AI Settings button: styled as a white circle card with a subtle shadow
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(36.dp)
                            .clickable { showApiKeyDialog = true }
                            .testTag("api_settings_button")
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Settings",
                                tint = if (userApiKey.isNotBlank()) HighDensityPrimary else HighDensityTextMuted.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Notification button: styled as a white circle card with a subtle shadow
                    Card(
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .size(36.dp)
                            .clickable {
                                if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    Toast.makeText(context, "Notifications alerts are active!", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .testTag("notification_status_button")
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Notification Status",
                                tint = if (hasNotificationPermission) HighDensityPrimary else HighDensityWarningBorder,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HighDensityBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (selectedTab) {
                        "Dashboard" -> showAddDialog = true
                        "Bills" -> showAddBillDialog = true
                        "Investments" -> showAddInvestmentDialog = true
                        "Savings" -> showAddGoalDialog = true
                    }
                },
                containerColor = HighDensityPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_transaction_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item")
            }
        },
        containerColor = HighDensityBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Critical Overspending Warning Banner
            if (warningCategory != null) {
                item {
                    val catName = warningCategory.first
                    val pct = (warningCategory.second * 100).toInt()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(HighDensityWarningBg)
                            .border(1.dp, HighDensityWarningBorder, RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(HighDensityWarningBorder),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Warning,
                                contentDescription = "Warning",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "OVERSPENDING WARNING",
                                color = HighDensityWarningText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "$catName is at $pct% of monthly limit.",
                                color = HighDensityWarningText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            // 1. Financial Health Banner
            item {
                FinanceOverviewCard(
                    netBalance = netBalance,
                    income = totalIncome,
                    expense = totalExpenses,
                    spentOverall = spentOverall,
                    limitOverall = limitOverall
                )
            }

            // Tab Selection Row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color.Black.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val tabs = listOf("Dashboard", "Bills", "Investments", "Savings")
                    tabs.forEach { tab ->
                        val isSelected = selectedTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) HighDensityPrimary else Color.Transparent)
                                .clickable { selectedTab = tab }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab,
                                color = if (isSelected) Color.White else HighDensityTextMuted,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            if (selectedTab == "Dashboard") {
                // Pending SMS Alert Banner or Sync Inbox Card
                if (pendingSmsList.isNotEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFFB5E2FA), RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(HighDensityPrimary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.NotificationsActive,
                                            contentDescription = "SMS Detected",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "SMS TRANSACTION ALERTS",
                                            color = HighDensityPrimary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = "We detected ${pendingSmsList.size} transaction alerts from your SMS.",
                                            color = HighDensityText,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { showReviewSmsDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = HighDensityPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1.2f)
                                    ) {
                                        Text("Review & Categorize", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    
                                    val smsLauncher = rememberLauncherForActivityResult(
                                        contract = ActivityResultContracts.RequestMultiplePermissions()
                                    ) { perms ->
                                        if (perms[Manifest.permission.READ_SMS] == true) {
                                            viewModel.scanSmsInbox(context)
                                            Toast.makeText(context, "Scanning SMS Inbox...", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    
                                    OutlinedButton(
                                        onClick = {
                                            val hasRead = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
                                            if (hasRead) {
                                                viewModel.scanSmsInbox(context)
                                                Toast.makeText(context, "Scanning SMS Inbox...", Toast.LENGTH_SHORT).show()
                                            } else {
                                                smsLauncher.launch(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS))
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, HighDensityPrimary.copy(alpha = 0.4f)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HighDensityPrimary),
                                        modifier = Modifier.weight(0.8f)
                                    ) {
                                        Text("Sync Inbox", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.Black.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = "SMS Sync",
                                        tint = HighDensityTextMuted.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "SMS Auto-Tracker",
                                            color = HighDensityText,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Sync device SMS to auto-track bank transactions",
                                            color = HighDensityTextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                
                                val smsLauncher = rememberLauncherForActivityResult(
                                    contract = ActivityResultContracts.RequestMultiplePermissions()
                                ) { perms ->
                                    if (perms[Manifest.permission.READ_SMS] == true) {
                                        viewModel.scanSmsInbox(context)
                                        Toast.makeText(context, "Scanning SMS Inbox...", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                
                                Button(
                                    onClick = {
                                        val hasRead = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
                                        if (hasRead) {
                                            viewModel.scanSmsInbox(context)
                                            Toast.makeText(context, "Scanning SMS Inbox...", Toast.LENGTH_SHORT).show()
                                        } else {
                                            smsLauncher.launch(arrayOf(Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS))
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = HighDensitySecondary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("sms_sync_button")
                                ) {
                                    Text("Sync", color = Color(0xFF21005D), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // 2. Budget Visualizations Block
                item {
                    BudgetVisualizerCard(
                        categories = categories,
                        spentByCategory = spentByCategory,
                        budgetLimits = budgetLimits,
                        onSetBudgetClick = { showBudgetDialog = true }
                    )
                }

                // 3. Smart Automated Tracker (Gemini Parsing Area)
                item {
                    SmartScannerSection(
                        isScanning = isScanning,
                        scanError = scanError,
                        parsedResult = parsedResult,
                        userApiKey = userApiKey,
                        onParseText = { text -> viewModel.parseWithGemini(text, null, userApiKey) },
                        onSaveParsed = { parsed -> viewModel.addParsedTransaction(parsed) },
                        onDiscardParsed = { viewModel.clearParsedResult() }
                    )
                }

                // 4. Transaction Log Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Transactions",
                            color = HighDensityText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "This Month (${currentMonthTransactions.size})",
                            color = HighDensityTextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 5. Transaction List Rows
                if (currentMonthTransactions.isEmpty()) {
                    item {
                        EmptyTransactionsState()
                    }
                } else {
                    items(currentMonthTransactions) { tx ->
                        TransactionItemRow(
                            transaction = tx,
                            onDelete = { viewModel.deleteTransaction(tx.id) }
                        )
                    }
                }
            } else if (selectedTab == "Bills") {
                // Bills Summary Card
                item {
                    val unpaidAmount = bills.filter { !it.isPaid }.sumOf { it.amount }
                    val upcomingCount = bills.filter { !it.isPaid }.size
                    val paidCount = bills.filter { it.isPaid }.size

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.Black.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Unpaid Bills",
                                    color = HighDensityTextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "₹${String.format("%.2f", unpaidAmount)}",
                                    color = HighDensityText,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BadgeCriticalBg)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "$upcomingCount Pending",
                                        color = BadgeCriticalText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BadgeSafeBg)
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "$paidCount Paid",
                                        color = BadgeSafeText,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Add Bill Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Upcoming Bills",
                            color = HighDensityText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap + to add bills",
                            color = HighDensityTextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Bills List
                if (bills.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "No Bills",
                                tint = HighDensityTextMuted.copy(alpha = 0.3f),
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Bills Logged Yet",
                                color = HighDensityText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Add bills with due dates to receive timely notification reminders.",
                                color = HighDensityTextMuted,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                } else {
                    items(bills) { bill ->
                        val df = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        val dateStr = df.format(Date(bill.dueDate))

                        val currentCal = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val dueCal = Calendar.getInstance().apply {
                            timeInMillis = bill.dueDate
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val diffMillis = dueCal.timeInMillis - currentCal.timeInMillis
                        val daysRemaining = (diffMillis / (1000 * 60 * 60 * 24)).toInt()

                        val daysStr = when {
                            bill.isPaid -> "Paid"
                            daysRemaining < 0 -> "Overdue by ${-daysRemaining} days"
                            daysRemaining == 0 -> "Due today! 🚨"
                            daysRemaining == 1 -> "Due tomorrow"
                            else -> "In $daysRemaining days"
                        }

                        val cardAlpha = if (bill.isPaid) 0.6f else 1f

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.Black.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                                .testTag("bill_item_${bill.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Checkbox(
                                        checked = bill.isPaid,
                                        onCheckedChange = { viewModel.toggleBillPaid(bill) },
                                        modifier = Modifier.testTag("bill_checkbox_${bill.id}")
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column {
                                        Text(
                                            text = bill.name,
                                            color = HighDensityText.copy(alpha = cardAlpha),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Due $dateStr • $daysStr",
                                            color = if (daysRemaining <= 0 && !bill.isPaid) BadgeCriticalText else HighDensityTextMuted,
                                            fontSize = 11.sp,
                                            fontWeight = if (daysRemaining <= 0 && !bill.isPaid) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "₹${String.format("%.2f", bill.amount)}",
                                        color = if (bill.isPaid) HighDensityTextMuted else HighDensityText,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { viewModel.deleteBill(bill.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete Bill",
                                            tint = HighDensityTextMuted.copy(alpha = 0.4f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (selectedTab == "Investments") {
                // Portfolio Summary Card
                item {
                    val totalCost = investments.sumOf { it.quantity * it.purchasePrice }
                    val totalValue = investments.sumOf { it.quantity * it.currentValue }
                    val netProfit = totalValue - totalCost
                    val profitPct = if (totalCost > 0) (netProfit / totalCost) * 100 else 0.0

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.Black.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Total Portfolio Value",
                                color = HighDensityTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "₹${String.format("%.2f", totalValue)}",
                                color = HighDensityText,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Total Invested",
                                        color = HighDensityTextMuted,
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = "₹${String.format("%.2f", totalCost)}",
                                        color = HighDensityText,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Total Return",
                                        color = HighDensityTextMuted,
                                        fontSize = 10.sp
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (netProfit >= 0) "+₹${String.format("%.2f", netProfit)}" else "-₹${String.format("%.2f", -netProfit)}",
                                            color = if (netProfit >= 0) BadgeSafeText else BadgeCriticalText,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (netProfit >= 0) BadgeSafeBg else BadgeCriticalBg)
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "${if (netProfit >= 0) "+" else ""}${String.format("%.1f", profitPct)}%",
                                                color = if (netProfit >= 0) BadgeSafeText else BadgeCriticalText,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Physical Gold Live Price Estimator Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF5)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFFFFD700).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    ) {
                        var goldWeight by remember { mutableStateOf("") }
                        var selectedKarat by remember { mutableStateOf("22K") }
                        var showKaratDropdown by remember { mutableStateOf(false) }
                        
                        val goldRate = if (selectedKarat == "24K") 7600.0 else 6960.0
                        val estimatedGoldValue = (goldWeight.toDoubleOrNull() ?: 0.0) * goldRate
                        
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "🪙", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Physical Gold Estimator",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF856404)
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFFF3CD))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Live: ₹$goldRate/g",
                                        color = Color(0xFF856404),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = goldWeight,
                                    onValueChange = { goldWeight = it },
                                    label = { Text("Weight (g)", color = Color(0xFF856404)) },
                                    textStyle = androidx.compose.ui.text.TextStyle(color = Color(0xFF856404)),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFFFFD700),
                                        unfocusedBorderColor = Color(0xFFFFD700).copy(alpha = 0.3f),
                                        focusedLabelColor = Color(0xFF856404),
                                        unfocusedLabelColor = Color(0xFF856404).copy(alpha = 0.6f)
                                    )
                                )
                                
                                Box(modifier = Modifier.weight(0.8f)) {
                                    OutlinedButton(
                                        onClick = { showKaratDropdown = true },
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD700)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF856404)),
                                        modifier = Modifier.fillMaxWidth().height(56.dp)
                                    ) {
                                        Text(selectedKarat, fontWeight = FontWeight.Bold)
                                    }
                                    DropdownMenu(
                                        expanded = showKaratDropdown,
                                        onDismissRequest = { showKaratDropdown = false },
                                        modifier = Modifier.background(Color.White)
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("24K Gold", color = Color(0xFF856404)) },
                                            onClick = {
                                                selectedKarat = "24K"
                                                showKaratDropdown = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("22K Gold", color = Color(0xFF856404)) },
                                            onClick = {
                                                selectedKarat = "22K"
                                                showKaratDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                            
                            if (estimatedGoldValue > 0) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Estimated Market Value:", fontSize = 11.sp, color = Color(0xFF856404).copy(alpha = 0.8f))
                                        Text(
                                            text = "₹${String.format("%,.2f", estimatedGoldValue)}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 18.sp,
                                            color = Color(0xFF856404)
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.addInvestment(
                                                name = "$selectedKarat Physical Gold",
                                                assetType = "Gold / SGB",
                                                purchasePrice = goldRate,
                                                currentValue = goldRate,
                                                quantity = goldWeight.toDoubleOrNull() ?: 0.0
                                            )
                                            goldWeight = ""
                                            Toast.makeText(context, "Added Gold to Portfolio!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Add to Portfolio", color = Color(0xFF21005D), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "My Assets",
                            color = HighDensityText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${investments.size} Assets Logged",
                            color = HighDensityTextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Assets list
                if (investments.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Paid,
                                contentDescription = "No Investments",
                                tint = HighDensityTextMuted.copy(alpha = 0.3f),
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Investments Logged",
                                color = HighDensityText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Keep track of your stocks, crypto, mutual funds and more in one visual portfolio dashboard.",
                                color = HighDensityTextMuted,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                } else {
                    items(investments) { asset ->
                        val assetCost = asset.quantity * asset.purchasePrice
                        val assetValue = asset.quantity * asset.currentValue
                        val assetProfit = assetValue - assetCost
                        val assetProfitPct = if (assetCost > 0) (assetProfit / assetCost) * 100 else 0.0

                        val assetBadgeText = when (asset.assetType) {
                            "Stocks" -> BadgeFixedText
                            "Crypto" -> BadgeNearLimitText
                            "Real Estate" -> BadgeSafeText
                            "Mutual Funds" -> BadgeFixedText
                            else -> HighDensityTextMuted
                        }
                        val assetBadgeBg = when (asset.assetType) {
                            "Stocks" -> BadgeFixedBg
                            "Crypto" -> BadgeNearLimitBg
                            "Real Estate" -> BadgeSafeBg
                            "Mutual Funds" -> BadgeFixedBg
                            else -> HighDensityBackground
                        }

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.Black.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                                .testTag("asset_item_${asset.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Asset Symbol Box
                                    val symbol = if (asset.name.isNotEmpty()) asset.name.take(2).uppercase() else "AS"
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(assetBadgeBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = symbol,
                                            color = assetBadgeText,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = asset.name,
                                                color = HighDensityText,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(assetBadgeBg)
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = asset.assetType,
                                                    color = assetBadgeText,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Qty: ${asset.quantity} • Buy: ₹${String.format("%.2f", asset.purchasePrice)} • Now: ₹${String.format("%.2f", asset.currentValue)}",
                                            color = HighDensityTextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "₹${String.format("%.2f", assetValue)}",
                                            color = HighDensityText,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${if (assetProfit >= 0) "+" else ""}${String.format("%.1f", assetProfitPct)}%",
                                            color = if (assetProfit >= 0) BadgeSafeText else BadgeCriticalText,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { viewModel.deleteInvestment(asset.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete Holding",
                                            tint = HighDensityTextMuted.copy(alpha = 0.4f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (selectedTab == "Savings") {
                // Savings Summary Card
                item {
                    val totalSaved = savingsGoals.sumOf { it.savedAmount }
                    val totalTarget = savingsGoals.sumOf { it.targetAmount }
                    val overallProgress = if (totalTarget > 0) (totalSaved / totalTarget) else 0.0

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.Black.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Total Savings Accumulated",
                                color = HighDensityTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "₹${String.format("%.2f", totalSaved)}",
                                color = HighDensityText,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Progress towards targets",
                                    color = HighDensityTextMuted,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "${(overallProgress * 100).toInt()}%",
                                    color = HighDensityPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { overallProgress.toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = HighDensityPrimary,
                                trackColor = HighDensityBackground,
                                strokeCap = StrokeCap.Round
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "₹${String.format("%.2f", totalSaved)} of ₹${String.format("%.2f", totalTarget)} target",
                                color = HighDensityTextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                // Header
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Savings Goals",
                            color = HighDensityText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${savingsGoals.size} Goals",
                            color = HighDensityTextMuted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Goals list
                if (savingsGoals.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = "No Goals",
                                tint = HighDensityTextMuted.copy(alpha = 0.3f),
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Savings Goals Defined",
                                color = HighDensityText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Define milestone targets, track progress, and allocate funds directly to achieve your dreams.",
                                color = HighDensityTextMuted,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
                } else {
                    items(savingsGoals) { goal ->
                        val df = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        val targetDateStr = df.format(Date(goal.targetDate))
                        val goalProgress = if (goal.targetAmount > 0) (goal.savedAmount / goal.targetAmount) else 0.0

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.Black.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                                .testTag("goal_item_${goal.id}")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = goal.name,
                                            color = HighDensityText,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Target Date: $targetDateStr",
                                            color = HighDensityTextMuted,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${(goalProgress * 100).toInt()}%",
                                            color = HighDensityPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = { viewModel.deleteSavingsGoal(goal.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Delete Goal",
                                                tint = HighDensityTextMuted.copy(alpha = 0.4f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                LinearProgressIndicator(
                                    progress = { goalProgress.toFloat().coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = HighDensityPrimary,
                                    trackColor = HighDensityBackground,
                                    strokeCap = StrokeCap.Round
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "₹${String.format("%.2f", goal.savedAmount)} of ₹${String.format("%.2f", goal.targetAmount)} saved",
                                        color = HighDensityTextMuted,
                                        fontSize = 11.sp
                                    )
                                    Button(
                                        onClick = { showAllocateDialogGoalId = goal.id },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = HighDensitySecondaryContainer,
                                            contentColor = HighDensityPrimary
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .height(30.dp)
                                            .testTag("allocate_funds_button_${goal.id}")
                                    ) {
                                        Text("Save", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Chit Funds & Committees Section
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Chit Funds / Committees 🤝",
                            color = HighDensityText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = { showAddChitFundDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = HighDensitySecondary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("+ New Fund", color = Color(0xFF21005D), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (chitFundsList.isEmpty()) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.Black.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Track Local Chit Funds (Committees)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = HighDensityText
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Widely popular in India. Keep track of your monthly chitty contributions, duration, current bid winners, and payouts seamlessly.",
                                    color = HighDensityTextMuted,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(chitFundsList) { fund ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color.Black.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(BadgeSafeBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🤝", fontSize = 12.sp)
                                        }
                                        Column {
                                            Text(
                                                fund.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = HighDensityText
                                            )
                                            Text(
                                                "Month ${fund.currentMonth} of ${fund.durationMonths}",
                                                fontSize = 11.sp,
                                                color = HighDensityTextMuted
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { viewModel.deleteChitFund(fund.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Delete Fund",
                                            tint = HighDensityTextMuted.copy(alpha = 0.4f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Contribution / Mo", fontSize = 10.sp, color = HighDensityTextMuted)
                                        Text(
                                            "₹${String.format("%,.2f", fund.monthlyContribution)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = HighDensityText
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Total Pool Value", fontSize = 10.sp, color = HighDensityTextMuted)
                                        Text(
                                            "₹${String.format("%,.2f", fund.monthlyContribution * fund.totalMembers)}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = HighDensityPrimary
                                        )
                                    }
                                }

                                if (fund.isWinner) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(BadgeSafeBg)
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                "Winner: ${fund.bidWinner}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BadgeSafeText
                                            )
                                            Text(
                                                "Received Payout",
                                                fontSize = 9.sp,
                                                color = BadgeSafeText.copy(alpha = 0.8f)
                                            )
                                        }
                                        Text(
                                            text = "₹${String.format("%,.2f", fund.wonAmount)}",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp,
                                            color = BadgeSafeText
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(HighDensityBackground)
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Payout pending. Bid winner not decided yet.",
                                            fontSize = 10.sp,
                                            color = HighDensityTextMuted,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Extra bottom spacing so standard FAB doesn't overlay bottom elements awkwardly
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Dialogs
    if (showAddDialog) {
        AddTransactionDialog(
            categories = categories,
            onDismiss = { showAddDialog = false },
            onSave = { title, amount, category, isIncome, notes ->
                viewModel.addTransaction(title, amount, category, isIncome, notes)
                showAddDialog = false
            }
        )
    }

    if (showBudgetDialog) {
        SetBudgetsDialog(
            categories = categories,
            currentLimits = budgetLimits,
            onDismiss = { showBudgetDialog = false },
            onSave = { categoryLimits ->
                categoryLimits.forEach { (cat, limit) ->
                    viewModel.updateBudget(cat, limit)
                }
                showBudgetDialog = false
            }
        )
    }

    if (showApiKeyDialog) {
        ApiKeyDialog(
            currentKey = userApiKey,
            onDismiss = { showApiKeyDialog = false },
            onSave = { key ->
                userApiKey = key
                showApiKeyDialog = false
            }
        )
    }

    if (showAddBillDialog) {
        AddBillDialog(
            onDismiss = { showAddBillDialog = false },
            onSave = { name, amount, dueDate ->
                viewModel.addBill(name, amount, dueDate)
                showAddBillDialog = false
            }
        )
    }

    if (showAddInvestmentDialog) {
        AddInvestmentDialog(
            onDismiss = { showAddInvestmentDialog = false },
            onSave = { name, type, buyPrice, currentPrice, qty ->
                viewModel.addInvestment(name, type, buyPrice, currentPrice, qty)
                showAddInvestmentDialog = false
            }
        )
    }

    if (showAddGoalDialog) {
        AddSavingsGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onSave = { name, target, targetDate ->
                viewModel.addSavingsGoal(name, target, targetDate)
                showAddGoalDialog = false
            }
        )
    }

    if (showAllocateDialogGoalId != null) {
        val goalId = showAllocateDialogGoalId
        if (goalId != null) {
            val goal = savingsGoals.find { it.id == goalId }
            if (goal != null) {
                AllocateSavingsDialog(
                    onDismiss = { showAllocateDialogGoalId = null },
                    onSave = { amount ->
                        viewModel.allocateSavings(goal.id, goal.savedAmount, amount)
                        showAllocateDialogGoalId = null
                    }
                )
            }
        }
    }

    if (showReviewSmsDialog && pendingSmsList.isNotEmpty()) {
        ReviewSmsDialog(
            pendingSmsList = pendingSmsList,
            categories = categories,
            onDismiss = { showReviewSmsDialog = false },
            onApprove = { sms, selectedCategory, customizedTitle ->
                viewModel.approvePendingSms(sms, selectedCategory, customizedTitle)
                if (pendingSmsList.size <= 1) {
                    showReviewSmsDialog = false
                }
            },
            onDismissSms = { id ->
                viewModel.dismissPendingSms(id)
                if (pendingSmsList.size <= 1) {
                    showReviewSmsDialog = false
                }
            }
        )
    }

    if (showAddChitFundDialog) {
        AddChitFundDialog(
            onDismiss = { showAddChitFundDialog = false },
            onSave = { name, members, contrib, duration, curM, bidWinner, wonAmt, isWinner ->
                viewModel.addChitFund(name, members, contrib, duration, curM, bidWinner, wonAmt, isWinner)
                showAddChitFundDialog = false
            }
        )
    }
}

// Subcomponent: Beautiful High Density Main Budget Card
@Composable
fun FinanceOverviewCard(
    netBalance: Double,
    income: Double,
    expense: Double,
    spentOverall: Double,
    limitOverall: Double
) {
    val hasLimit = limitOverall > 0.0
    val remainingBudget = if (hasLimit) limitOverall - spentOverall else netBalance
    val balanceLabel = if (hasLimit) "Remaining Budget" else "Net Balance"
    
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = HighDensityPrimary),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(28.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = balanceLabel,
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "₹${String.format("%,.2f", remainingBudget)}",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(100.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    val currentMonth = remember {
                        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
                    }
                    Text(
                        text = currentMonth,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Progress bar and labels
            if (hasLimit) {
                val ratio = (spentOverall / limitOverall).toFloat().coerceIn(0f, 1f)
                val spentStr = String.format("%,.2f", spentOverall)
                val limitStr = String.format("%,.2f", limitOverall)
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "₹$spentStr spent",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "₹$limitStr total",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    LinearProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = HighDensitySecondary, // Light Purple
                        trackColor = Color.White.copy(alpha = 0.2f)
                    )
                }
            } else {
                // If there's no overall limit, display income and expense summaries side-by-side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Total Income", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        Text(
                            text = "+₹${String.format("%,.2f", income)}",
                            color = Color(0xFFC8E6C9), // Light pastel green
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Total Expenses", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                        Text(
                            text = "-₹${String.format("%,.2f", expense)}",
                            color = Color(0xFFFFCDD2), // Light pastel red
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// Subcomponent: Interactive Budget Visualization Block
@Composable
fun BudgetVisualizerCard(
    categories: List<String>,
    spentByCategory: Map<String, Double>,
    budgetLimits: Map<String, Double>,
    onSetBudgetClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CATEGORY TRACKING",
                color = HighDensityTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            IconButton(
                onClick = onSetBudgetClick,
                modifier = Modifier
                    .size(28.dp)
                    .testTag("set_budgets_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Budgets",
                    tint = HighDensityPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid layout representation using rows of 2 columns
        val chunkedCategories = categories.chunked(2)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            chunkedCategories.forEach { rowCategories ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowCategories.forEach { cat ->
                        val spent = spentByCategory[cat] ?: 0.0
                        val limit = budgetLimits[cat] ?: 0.0
                        val ratio = if (limit > 0.0) spent / limit else 0.0
                        
                        val (badgeText, badgeBg, badgeTextColor) = when {
                            limit <= 0.0 -> Triple("FIXED", BadgeFixedBg, BadgeFixedText)
                            ratio >= 1.0 -> Triple("CRITICAL", BadgeCriticalBg, BadgeCriticalText)
                            ratio >= 0.8 -> Triple("NEAR LIMIT", BadgeNearLimitBg, BadgeNearLimitText)
                            else -> Triple("SAFE", BadgeSafeBg, BadgeSafeText)
                        }

                        val icon = when (cat.lowercase()) {
                            "food" -> Icons.Default.Fastfood
                            "shopping" -> Icons.Default.ShoppingBag
                            "utilities" -> Icons.Default.Power
                            "entertainment" -> Icons.Default.LocalActivity
                            "transport" -> Icons.Default.LocalGasStation
                            else -> Icons.Default.Category
                        }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, Color.Black.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(HighDensitySecondaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = cat,
                                            tint = HighDensityText,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    
                                    // Status Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(badgeBg)
                                            .padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Text(
                                            text = badgeText,
                                            color = badgeTextColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Column {
                                    Text(
                                        text = cat,
                                        color = HighDensityTextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "₹${String.format("%,.2f", spent)}",
                                        color = HighDensityText,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (limit > 0.0) {
                                        Text(
                                            text = "Limit: ₹$limit",
                                            color = HighDensityTextMuted.copy(alpha = 0.5f),
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (rowCategories.size < 2) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

// Subcomponent: Smart Automated Tracker Section
@Composable
fun SmartScannerSection(
    isScanning: Boolean,
    scanError: String?,
    parsedResult: ParsedTransaction?,
    userApiKey: String,
    onParseText: (String) -> Unit,
    onSaveParsed: (ParsedTransaction) -> Unit,
    onDiscardParsed: () -> Unit
) {
    var rawText by remember { mutableStateOf("") }

    // Preset receipts to demonstrate Gemini AI capabilities
    val presets = listOf(
        "Walmart Grocery\nDate: 04-Jul-2026\nApples: \$4.20\nChicken: \$12.50\nMilk: \$3.80\nTOTAL: \$20.50",
        "Utility Bill\nElectricity Bill Month June\nAccount: 4851\nDue Amount: \$142.10\nPowerCo LLC",
        "Starbucks Cafe\nMerchant #5821\n1x Iced Latte: \$5.50\n1x Croissant: \$3.25\nTOTAL: \$8.75\nThanks!",
        "Credit Alert Deposit\nDirect deposit from ACME CORP\nType: Direct Salary\nCREDIT: \$2500.00\nFreq: Monthly"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Black.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Smart AI Tracker",
                    tint = HighDensityPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AI Receipt & SMS Auto-Tracker",
                    color = HighDensityText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Paste text, an SMS debit notification, or tap a preset receipt below to let Gemini AI extract details automatically.",
                color = HighDensityTextMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Text input Area
            OutlinedTextField(
                value = rawText,
                onValueChange = { rawText = it },
                placeholder = {
                    Text(
                        "Paste receipt details or bank SMS alert here...",
                        color = HighDensityTextMuted.copy(alpha = 0.5f),
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .testTag("ai_scanner_input"),
                textStyle = androidx.compose.ui.text.TextStyle(color = HighDensityText, fontSize = 13.sp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HighDensityPrimary,
                    unfocusedBorderColor = Color.Black.copy(alpha = 0.08f),
                    focusedContainerColor = HighDensityBackground.copy(alpha = 0.3f),
                    unfocusedContainerColor = HighDensityBackground.copy(alpha = 0.3f)
                ),
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Presets row
            Text(
                text = "TRY TEMPLATES:",
                color = HighDensityTextMuted,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val labels = listOf("🛒 Grocery", "💡 Electric", "☕ Coffee", "💼 Salary")
                labels.forEachIndexed { idx, label ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(HighDensitySecondaryContainer)
                            .clickable { rawText = presets[idx] }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = HighDensityText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = {
                    if (rawText.isBlank()) {
                        return@Button
                    }
                    onParseText(rawText)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("parse_receipt_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HighDensityPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isScanning && rawText.isNotBlank()
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gemini AI Processing...", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Scan",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Run AI Auto-Parser", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // Error display
            if (scanError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(HighDensityWarningBg)
                        .border(1.dp, HighDensityWarningBorder, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = "Error",
                        tint = HighDensityWarningBorder,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = scanError,
                        color = HighDensityWarningText,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Parsed Result Card
            AnimatedVisibility(
                visible = parsedResult != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                val result = parsedResult
                if (result != null) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(HighDensityBackground.copy(alpha = 0.5f))
                            .border(1.dp, HighDensityPrimary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Parsed Result ✅",
                                color = HighDensityPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            val isResultIncome = result.isIncome
                            val parsedBadgeBg = if (isResultIncome) BadgeSafeBg else BadgeCriticalBg
                            val parsedBadgeText = if (isResultIncome) BadgeSafeText else BadgeCriticalText
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(parsedBadgeBg)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                             ) {
                                Text(
                                    text = result.category.uppercase(),
                                    color = parsedBadgeText,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = result.title,
                            color = HighDensityText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = if (result.isIncome) "+₹${String.format("%.2f", result.amount)}"
                            else "-₹${String.format("%.2f", result.amount)}",
                            color = if (result.isIncome) BadgeSafeText else HighDensityText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )

                        if (result.notes.isNotBlank()) {
                            Text(
                                text = result.notes,
                                color = HighDensityTextMuted,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDiscardParsed,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = HighDensityTextMuted
                                )
                            ) {
                                Text("Discard", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { onSaveParsed(result) },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(36.dp)
                                    .testTag("save_parsed_button"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HighDensityPrimary,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Approve & Save", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Subcomponent: Transaction list item row
@Composable
fun TransactionItemRow(
    transaction: Transaction,
    onDelete: () -> Unit
) {
    val df = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dateStr = df.format(Date(transaction.date))

    val icon = when (transaction.category.lowercase()) {
        "food" -> Icons.Default.Fastfood
        "shopping" -> Icons.Default.ShoppingBag
        "utilities" -> Icons.Default.Power
        "entertainment" -> Icons.Default.LocalActivity
        "transport" -> Icons.Default.LocalGasStation
        else -> Icons.Default.Category
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Black.copy(alpha = 0.04f), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                val iconBg = if (transaction.isIncome) BadgeSafeBg else HighDensityWarningBg
                val iconColor = if (transaction.isIncome) BadgeSafeText else HighDensityWarningBorder
                
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = transaction.category,
                        tint = iconColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = transaction.title,
                        color = HighDensityText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$dateStr • ${transaction.category}",
                        color = HighDensityTextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (transaction.isIncome) "+₹${String.format("%.2f", transaction.amount)}"
                        else "-₹${String.format("%.2f", transaction.amount)}",
                        color = if (transaction.isIncome) BadgeSafeText else HighDensityText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (transaction.notes.isNotBlank()) {
                        Text(
                            text = transaction.notes,
                            color = HighDensityTextMuted,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 100.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = HighDensityTextMuted.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// Empty state placeholder
@Composable
fun EmptyTransactionsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = "No Transactions",
            tint = HighDensityTextMuted.copy(alpha = 0.3f),
            modifier = Modifier.size(44.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No Transactions Recorded Yet",
            color = HighDensityText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Your transactions for this month will appear here.",
            color = HighDensityTextMuted,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

// Dialog: Add Manual Transaction
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (String, Double, String, Boolean, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var isIncome by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var menuExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Add Transaction",
                    color = HighDensityText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Income / Expense selector tab
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(HighDensityBackground)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (!isIncome) BadgeCriticalBg else Color.Transparent)
                            .clickable { isIncome = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Expense",
                            color = if (!isIncome) BadgeCriticalText else HighDensityTextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isIncome) BadgeSafeBg else Color.Transparent)
                            .clickable { isIncome = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Income",
                            color = if (isIncome) BadgeSafeText else HighDensityTextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Merchant / Source") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = HighDensityText),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_tx_title"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HighDensityPrimary,
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.12f),
                        focusedLabelColor = HighDensityPrimary,
                        unfocusedLabelColor = HighDensityTextMuted
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (₹)") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = HighDensityText),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_tx_amount"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HighDensityPrimary,
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.12f),
                        focusedLabelColor = HighDensityPrimary,
                        unfocusedLabelColor = HighDensityTextMuted
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category dropdown selector
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        label = { Text("Category") },
                        readOnly = true,
                        textStyle = androidx.compose.ui.text.TextStyle(color = HighDensityText),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Category,
                                contentDescription = "Dropdown",
                                tint = HighDensityPrimary,
                                modifier = Modifier.clickable { menuExpanded = true }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { menuExpanded = true },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HighDensityPrimary,
                            unfocusedBorderColor = Color.Black.copy(alpha = 0.12f),
                            focusedLabelColor = HighDensityPrimary,
                            unfocusedLabelColor = HighDensityTextMuted
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, color = HighDensityText) },
                                onClick = {
                                    selectedCategory = cat
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Comments (Optional)") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = HighDensityText),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HighDensityPrimary,
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.12f),
                        focusedLabelColor = HighDensityPrimary,
                        unfocusedLabelColor = HighDensityTextMuted
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = HighDensityTextMuted
                        )
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val parsedAmount = amount.toDoubleOrNull() ?: 0.0
                            if (title.isNotBlank() && parsedAmount > 0.0) {
                                onSave(title, parsedAmount, selectedCategory, isIncome, notes)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_manual_tx_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HighDensityPrimary,
                            contentColor = Color.White
                        ),
                        enabled = title.isNotBlank() && amount.isNotBlank()
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

// Dialog: Set Budgets Dialog
@Composable
fun SetBudgetsDialog(
    categories: List<String>,
    currentLimits: Map<String, Double>,
    onDismiss: () -> Unit,
    onSave: (Map<String, Double>) -> Unit
) {
    val limitsState = remember {
        mutableStateOf(categories.associateWith { (currentLimits[it] ?: 0.0).toString() }.toMutableMap())
    }
    var overallLimitState by remember { mutableStateOf((currentLimits["Overall"] ?: 0.0).toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Set Monthly Budgets",
                        color = HighDensityText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = overallLimitState,
                        onValueChange = { overallLimitState = it },
                        label = { Text("Overall Budget Limit (₹)") },
                        textStyle = androidx.compose.ui.text.TextStyle(color = HighDensityText),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("overall_budget_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HighDensityPrimary,
                            unfocusedBorderColor = Color.Black.copy(alpha = 0.12f),
                            focusedLabelColor = HighDensityPrimary,
                            unfocusedLabelColor = HighDensityTextMuted
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "CATEGORY LIMITS",
                        color = HighDensityTextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                items(categories) { cat ->
                    val value = limitsState.value[cat] ?: ""
                    OutlinedTextField(
                        value = value,
                        onValueChange = { newVal ->
                            val updatedMap = limitsState.value.toMutableMap()
                            updatedMap[cat] = newVal
                            limitsState.value = updatedMap
                        },
                        label = { Text("$cat Limit (₹)") },
                        textStyle = androidx.compose.ui.text.TextStyle(color = HighDensityText),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HighDensityPrimary,
                            unfocusedBorderColor = Color.Black.copy(alpha = 0.12f),
                            focusedLabelColor = HighDensityPrimary,
                            unfocusedLabelColor = HighDensityTextMuted
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = HighDensityTextMuted
                            )
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val resultMap = mutableMapOf<String, Double>()
                                categories.forEach { cat ->
                                    resultMap[cat] = limitsState.value[cat]?.toDoubleOrNull() ?: 0.0
                                }
                                resultMap["Overall"] = overallLimitState.toDoubleOrNull() ?: 0.0
                                onSave(resultMap)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("save_budgets_confirm_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HighDensityPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Save All")
                        }
                    }
                }
            }
        }
    }
}

// Dialog: Setup API Key (Optional Override)
@Composable
fun ApiKeyDialog(
    currentKey: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var key by remember { mutableStateOf(currentKey) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Gemini AI API Key Configuration",
                    color = HighDensityText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Text(
                    text = "By default, the app uses the API Key stored securely in the system secrets. If you haven't set it yet, you can temporarily override and paste your Gemini API Key directly below to test receipt parsing.",
                    color = HighDensityTextMuted,
                    fontSize = 11.sp,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("Gemini API Key") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = HighDensityText),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HighDensityPrimary,
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.12f),
                        focusedLabelColor = HighDensityPrimary,
                        unfocusedLabelColor = HighDensityTextMuted
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = HighDensityTextMuted
                        )
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = { onSave(key) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_api_key_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HighDensityPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Save Override")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBillDialog(
    onDismiss: () -> Unit,
    onSave: (String, Double, Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    val calendar = Calendar.getInstance()
    val context = LocalContext.current

    val df = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Add Bill Reminder",
                    color = HighDensityText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Bill Name") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = HighDensityText),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bill_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HighDensityPrimary,
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.12f),
                        focusedLabelColor = HighDensityPrimary,
                        unfocusedLabelColor = HighDensityTextMuted
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (₹)") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = HighDensityText),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bill_amount_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HighDensityPrimary,
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.12f),
                        focusedLabelColor = HighDensityPrimary,
                        unfocusedLabelColor = HighDensityTextMuted
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Date Picker trigger Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(HighDensityBackground)
                        .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                        .clickable {
                            val picker = android.app.DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    val cal = Calendar.getInstance()
                                    cal.set(year, month, day)
                                    selectedDate = cal.timeInMillis
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            )
                            picker.show()
                        }
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Due Date",
                        color = HighDensityTextMuted,
                        fontSize = 13.sp
                    )
                    Text(
                        text = df.format(Date(selectedDate)),
                        color = HighDensityText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HighDensityTextMuted)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull() ?: 0.0
                            if (name.isNotBlank() && amt > 0.0) {
                                onSave(name, amt, selectedDate)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_bill_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HighDensityPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Add Bill")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInvestmentDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Double, Double, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var assetType by remember { mutableStateOf("Stocks") }
    var purchasePrice by remember { mutableStateOf("") }
    var currentValue by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val assetTypes = listOf("Stocks", "Crypto", "Real Estate", "Mutual Funds", "Other")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Add Investment Holding",
                    color = HighDensityText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Asset Name (e.g. BTC, AAPL)") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = HighDensityText),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("asset_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HighDensityPrimary,
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.12f),
                        focusedLabelColor = HighDensityPrimary,
                        unfocusedLabelColor = HighDensityTextMuted
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Asset Type Selector dropdown field style
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = assetType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Asset Type") },
                        textStyle = androidx.compose.ui.text.TextStyle(color = HighDensityText),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { dropdownExpanded = true }
                            .testTag("asset_type_dropdown"),
                        enabled = false, // disable editing directly, click works on container Box
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledBorderColor = Color.Black.copy(alpha = 0.12f),
                            disabledTextColor = HighDensityText,
                            disabledLabelColor = HighDensityTextMuted
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                    // Transparent clickable layer on top to handle dropdown expand
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { dropdownExpanded = true }
                    )
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        assetTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type, color = HighDensityText) },
                                onClick = {
                                    assetType = type
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = HighDensityText),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("asset_quantity_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HighDensityPrimary,
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.12f),
                        focusedLabelColor = HighDensityPrimary,
                        unfocusedLabelColor = HighDensityTextMuted
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = purchasePrice,
                        onValueChange = { purchasePrice = it },
                        label = { Text("Buy Price (₹)") },
                        textStyle = androidx.compose.ui.text.TextStyle(color = HighDensityText),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("asset_buy_price_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HighDensityPrimary,
                            unfocusedBorderColor = Color.Black.copy(alpha = 0.12f),
                            focusedLabelColor = HighDensityPrimary,
                            unfocusedLabelColor = HighDensityTextMuted
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = currentValue,
                        onValueChange = { currentValue = it },
                        label = { Text("Now Price (₹)") },
                        textStyle = androidx.compose.ui.text.TextStyle(color = HighDensityText),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("asset_current_price_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HighDensityPrimary,
                            unfocusedBorderColor = Color.Black.copy(alpha = 0.12f),
                            focusedLabelColor = HighDensityPrimary,
                            unfocusedLabelColor = HighDensityTextMuted
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HighDensityTextMuted)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val qty = quantity.toDoubleOrNull() ?: 0.0
                            val buy = purchasePrice.toDoubleOrNull() ?: 0.0
                            val now = currentValue.toDoubleOrNull() ?: 0.0
                            if (name.isNotBlank() && qty > 0.0 && buy >= 0.0 && now >= 0.0) {
                                onSave(name, assetType, buy, now, qty)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_asset_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HighDensityPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Save Asset")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSavingsGoalDialog(
    onDismiss: () -> Unit,
    onSave: (String, Double, Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    val calendar = Calendar.getInstance()
    val context = LocalContext.current

    val df = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Create Savings Goal",
                    color = HighDensityText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal Name (e.g. Emergency Fund)") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = HighDensityText),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("goal_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HighDensityPrimary,
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.12f),
                        focusedLabelColor = HighDensityPrimary,
                        unfocusedLabelColor = HighDensityTextMuted
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("Target Amount (₹)") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = HighDensityText),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("goal_target_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HighDensityPrimary,
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.12f),
                        focusedLabelColor = HighDensityPrimary,
                        unfocusedLabelColor = HighDensityTextMuted
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(HighDensityBackground)
                        .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                        .clickable {
                            val picker = android.app.DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    val cal = Calendar.getInstance()
                                    cal.set(year, month, day)
                                    selectedDate = cal.timeInMillis
                                },
                                calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH)
                            )
                            picker.show()
                        }
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Target Date",
                        color = HighDensityTextMuted,
                        fontSize = 13.sp
                    )
                    Text(
                        text = df.format(Date(selectedDate)),
                        color = HighDensityText,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HighDensityTextMuted)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val tgt = target.toDoubleOrNull() ?: 0.0
                            if (name.isNotBlank() && tgt > 0.0) {
                                onSave(name, tgt, selectedDate)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_goal_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HighDensityPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Create Goal")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllocateSavingsDialog(
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var amount by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Allocate Funds",
                    color = HighDensityText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Add money saved to your goal target balance.",
                    color = HighDensityTextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (₹)") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = HighDensityText),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("allocate_amount_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HighDensityPrimary,
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.12f),
                        focusedLabelColor = HighDensityPrimary,
                        unfocusedLabelColor = HighDensityTextMuted
                    ),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HighDensityTextMuted)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull() ?: 0.0
                            if (amt > 0.0) {
                                onSave(amt)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_allocation_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HighDensityPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Save Funds")
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewSmsDialog(
    pendingSmsList: List<com.example.data.PendingSmsTransaction>,
    categories: List<String>,
    onDismiss: () -> Unit,
    onApprove: (com.example.data.PendingSmsTransaction, selectedCategory: String, customizedTitle: String) -> Unit,
    onDismissSms: (Int) -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }
    if (currentIndex >= pendingSmsList.size) {
        onDismiss()
        return
    }
    
    val sms = pendingSmsList[currentIndex]
    
    var selectedCategory by remember(sms.id) { mutableStateOf(if (sms.probableCategory in categories) sms.probableCategory else categories.firstOrNull() ?: "Uncategorized") }
    
    val initialTitle = remember(sms.id) {
        val typeLabel = if (sms.transactionType == "CREDIT") "Received" else "Spent"
        "${sms.bankName} $typeLabel"
    }
    var titleInput by remember(sms.id) { mutableStateOf(initialTitle) }
    var showCategoryDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = HighDensitySurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Review Detected SMS",
                        color = HighDensityText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${currentIndex + 1}/${pendingSmsList.size}",
                        color = HighDensityTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = HighDensityBackground),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "From: ${sms.sender}",
                                color = HighDensityTextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            val formattedDate = remember(sms.date) {
                                SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(sms.date))
                            }
                            Text(
                                text = formattedDate,
                                color = HighDensityTextMuted,
                                fontSize = 9.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = sms.body,
                            color = HighDensityText,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val typeColor = if (sms.transactionType == "CREDIT") BadgeSafeText else BadgeCriticalText
                            val typeBg = if (sms.transactionType == "CREDIT") BadgeSafeBg else BadgeCriticalBg
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(typeBg)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = sms.transactionType,
                                    color = typeColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "₹${String.format("%.2f", sms.amount)}",
                                color = HighDensityText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(14.dp))
                
                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Transaction Title") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = HighDensityText),
                    modifier = Modifier.fillMaxWidth().testTag("sms_review_title"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HighDensityPrimary,
                        unfocusedBorderColor = Color.Black.copy(alpha = 0.12f),
                        focusedLabelColor = HighDensityPrimary,
                        unfocusedLabelColor = HighDensityTextMuted
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = "Select Category",
                    color = HighDensityTextMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { showCategoryDropdown = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("sms_review_category_select"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = HighDensityText),
                        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.12f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(selectedCategory, color = HighDensityText, fontSize = 13.sp)
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = HighDensityTextMuted
                            )
                        }
                    }
                    
                    DropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false },
                        modifier = Modifier.fillMaxWidth(0.8f).background(HighDensitySurface)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, color = HighDensityText) },
                                onClick = {
                                    selectedCategory = cat
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            onDismissSms(sms.id)
                            if (currentIndex >= pendingSmsList.size - 1) {
                                onDismiss()
                            } else {
                                currentIndex++
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("sms_review_ignore_btn"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BadgeCriticalText),
                        border = BorderStroke(1.dp, BadgeCriticalBg)
                    ) {
                        Text("Ignore")
                    }
                    
                    Button(
                        onClick = {
                            onApprove(sms, selectedCategory, titleInput)
                            if (currentIndex >= pendingSmsList.size - 1) {
                                onDismiss()
                            } else {
                                currentIndex++
                            }
                        },
                        modifier = Modifier.weight(1.2f).testTag("sms_review_approve_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HighDensityPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Track It")
                    }
                }
            }
        }
    }
}

@Composable
fun AddChitFundDialog(
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        totalMembers: Int,
        monthlyContribution: Double,
        durationMonths: Int,
        currentMonth: Int,
        bidWinnerName: String,
        wonBidAmount: Double,
        userIsWinner: Boolean
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var totalMembers by remember { mutableStateOf("10") }
    var monthlyContribution by remember { mutableStateOf("") }
    var durationMonths by remember { mutableStateOf("10") }
    var currentMonth by remember { mutableStateOf("1") }
    var bidWinner by remember { mutableStateOf("") }
    var wonAmount by remember { mutableStateOf("") }
    var isWinner by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = HighDensitySurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Black.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "New Chit Fund / Committee",
                        color = HighDensityText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = "Chit funds are highly popular local savings and borrowing instruments in India. Track your committees accurately.",
                        color = HighDensityTextMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Chit Fund / Committee Name") },
                        textStyle = androidx.compose.ui.text.TextStyle(color = HighDensityText),
                        modifier = Modifier.fillMaxWidth().testTag("chit_fund_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = HighDensityPrimary,
                            unfocusedBorderColor = Color.Black.copy(alpha = 0.12f),
                            focusedLabelColor = HighDensityPrimary,
                            unfocusedLabelColor = HighDensityTextMuted
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = totalMembers,
                            onValueChange = { totalMembers = it },
                            label = { Text("Total Members") },
                            textStyle = androidx.compose.ui.text.TextStyle(color = HighDensityText),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("chit_fund_members_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HighDensityPrimary,
                                unfocusedBorderColor = Color.Black.copy(alpha = 0.12f),
                                focusedLabelColor = HighDensityPrimary,
                                unfocusedLabelColor = HighDensityTextMuted
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = durationMonths,
                            onValueChange = { durationMonths = it },
                            label = { Text("Duration (Months)") },
                            textStyle = androidx.compose.ui.text.TextStyle(color = HighDensityText),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("chit_fund_duration_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HighDensityPrimary,
                                unfocusedBorderColor = Color.Black.copy(alpha = 0.12f),
                                focusedLabelColor = HighDensityPrimary,
                                unfocusedLabelColor = HighDensityTextMuted
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = monthlyContribution,
                            onValueChange = { monthlyContribution = it },
                            label = { Text("Contribution (₹)") },
                            textStyle = androidx.compose.ui.text.TextStyle(color = HighDensityText),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1.2f).testTag("chit_fund_contrib_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HighDensityPrimary,
                                unfocusedBorderColor = Color.Black.copy(alpha = 0.12f),
                                focusedLabelColor = HighDensityPrimary,
                                unfocusedLabelColor = HighDensityTextMuted
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = currentMonth,
                            onValueChange = { currentMonth = it },
                            label = { Text("Current Month") },
                            textStyle = androidx.compose.ui.text.TextStyle(color = HighDensityText),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(0.8f).testTag("chit_fund_current_month_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HighDensityPrimary,
                                unfocusedBorderColor = Color.Black.copy(alpha = 0.12f),
                                focusedLabelColor = HighDensityPrimary,
                                unfocusedLabelColor = HighDensityTextMuted
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isWinner,
                            onCheckedChange = { isWinner = it },
                            modifier = Modifier.testTag("chit_fund_is_winner_chk")
                        )
                        Text(
                            text = "Have you won the bid this month?",
                            color = HighDensityText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { isWinner = !isWinner }
                        )
                    }
                }

                if (isWinner) {
                    item {
                        OutlinedTextField(
                            value = wonAmount,
                            onValueChange = { wonAmount = it },
                            label = { Text("Bid Amount Won (₹)") },
                            textStyle = androidx.compose.ui.text.TextStyle(color = HighDensityText),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("chit_fund_won_amount_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HighDensityPrimary,
                                unfocusedBorderColor = Color.Black.copy(alpha = 0.12f),
                                focusedLabelColor = HighDensityPrimary,
                                unfocusedLabelColor = HighDensityTextMuted
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                } else {
                    item {
                        OutlinedTextField(
                            value = bidWinner,
                            onValueChange = { bidWinner = it },
                            label = { Text("Current Bid Winner Name") },
                            textStyle = androidx.compose.ui.text.TextStyle(color = HighDensityText),
                            modifier = Modifier.fillMaxWidth().testTag("chit_fund_winner_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = HighDensityPrimary,
                                unfocusedBorderColor = Color.Black.copy(alpha = 0.12f),
                                focusedLabelColor = HighDensityPrimary,
                                unfocusedLabelColor = HighDensityTextMuted
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = HighDensityTextMuted)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val members = totalMembers.toIntOrNull() ?: 10
                                val contrib = monthlyContribution.toDoubleOrNull() ?: 0.0
                                val dur = durationMonths.toIntOrNull() ?: 10
                                val curM = currentMonth.toIntOrNull() ?: 1
                                val wonBid = wonAmount.toDoubleOrNull() ?: 0.0
                                
                                if (name.isNotBlank() && contrib > 0.0) {
                                    onSave(
                                        name,
                                        members,
                                        contrib,
                                        dur,
                                        curM,
                                        if (isWinner) "You" else bidWinner,
                                        wonBid,
                                        isWinner
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f).testTag("save_chit_fund_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HighDensityPrimary,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Save Fund")
                        }
                    }
                }
            }
        }
    }
}
