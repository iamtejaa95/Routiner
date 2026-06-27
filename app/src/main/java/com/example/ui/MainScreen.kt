package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.HourglassEmpty
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.CloudQueue
import androidx.compose.material.icons.rounded.CompassCalibration
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Key
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import java.util.Calendar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import android.os.Build
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatMessage
import com.example.data.DailyNote
import com.example.data.HistoryLog
import com.example.data.Reminder
import com.example.data.Routine
import com.example.ui.theme.CafeCream
import com.example.ui.theme.CocoaWarm
import com.example.ui.theme.ElegantBronze
import com.example.ui.theme.EspressoBrown
import com.example.ui.theme.ObsidianBlack
import com.example.ui.theme.VelvetCharcoal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ScreenTab {
    DASHBOARD, ROUTINES, NOTES, CHAT
}

@Composable
fun MainScreen(
    viewModel: AuraViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(ScreenTab.DASHBOARD) }

    val notes by viewModel.notes.collectAsState()
    val routines by viewModel.routines.collectAsState()
    val reminders by viewModel.reminders.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isAuraLoading by viewModel.isAuraLoading.collectAsState()
    val historyLogs by viewModel.historyLogs.collectAsState()
    val customApiKey by viewModel.customApiKey.collectAsState()
    val isSuitableOnline by viewModel.isSuitableOnline.collectAsState()

    // Focus state
    val isFocusModeActive by viewModel.isFocusModeActive.collectAsState()
    val focusTimeRemaining by viewModel.focusTimeRemaining.collectAsState()
    val focusType by viewModel.focusType.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        // Core background layer
        LiquidGlassBackground()

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent, // Let liquid glass background draw underneath
            bottomBar = {
                // Hide tab bar when screen is strictly locked
                if (!isFocusModeActive) {
                    val tabIndex = when (activeTab) {
                        ScreenTab.DASHBOARD -> 0
                        ScreenTab.ROUTINES -> 1
                        ScreenTab.NOTES -> 2
                        ScreenTab.CHAT -> 3
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 28.dp)
                    ) {
                        GlassTabBar(selectedIndex = tabIndex) {
                            GlassTabItem(
                                selected = activeTab == ScreenTab.DASHBOARD,
                                onClick = { activeTab = ScreenTab.DASHBOARD },
                                icon = Icons.Rounded.Dashboard,
                                text = "well-being"
                            )
                            GlassTabItem(
                                selected = activeTab == ScreenTab.ROUTINES,
                                onClick = { activeTab = ScreenTab.ROUTINES },
                                icon = Icons.Rounded.Star,
                                text = "routines"
                            )
                            GlassTabItem(
                                selected = activeTab == ScreenTab.NOTES,
                                onClick = { activeTab = ScreenTab.NOTES },
                                icon = Icons.Rounded.Description,
                                text = "notes"
                            )
                            GlassTabItem(
                                selected = activeTab == ScreenTab.CHAT,
                                onClick = { activeTab = ScreenTab.CHAT },
                                icon = Icons.Rounded.Chat,
                                text = "ai aura"
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (activeTab) {
                    ScreenTab.DASHBOARD -> DashboardTab(viewModel, routines, reminders, historyLogs)
                    ScreenTab.ROUTINES -> RoutinesTab(viewModel, routines)
                    ScreenTab.NOTES -> NotesTab(viewModel, notes)
                    ScreenTab.CHAT -> ChatTab(viewModel, chatMessages, isAuraLoading)
                }
            }
        }

        // 5. IMMERSIVE FULL-SCREEN LIQUID GLASS LOCK LAYER
        AnimatedVisibility(
            visible = isFocusModeActive,
            enter = fadeIn(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(500))
        ) {
            FocusOverlayLockScreen(
                focusTimeRemaining = focusTimeRemaining,
                focusType = focusType,
                onCancel = { viewModel.cancelFocusMode() }
            )
        }
    }
}

// ==========================================
// 1. DASHBOARD & WELL-BEING COCKPIT
// ==========================================

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardTab(
    viewModel: AuraViewModel,
    routines: List<Routine>,
    reminders: List<Reminder>,
    historyLogs: List<HistoryLog>
) {
    val screenTimeMinutes by viewModel.screenTimeMinutes.collectAsState()
    val workApps by viewModel.workApps.collectAsState()
    val entertainmentApps by viewModel.entertainmentApps.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val customApiKey by viewModel.customApiKey.collectAsState()
    val isSuitableOnline by viewModel.isSuitableOnline.collectAsState()

    var showReminderCreator by remember { mutableStateOf(false) }

    val lazyListState = rememberLazyListState()

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // App Title Banner
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "routiner",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = "mindful productivity • obsidian glass",
                        fontSize = 12.sp,
                        color = ElegantBronze,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Momentum and Habit Streaks Analytics Dashboard
        item {
            StreakAndAnalyticsCard(viewModel, historyLogs, routines)
        }

        // Personalized Wisdom Reflection Guide
        item {
            DailyReflectionSuitableCard(viewModel, routines, notes)
        }

        // Well-Being and Live Screen Time Monitor Card
        item {
            LiquidGlassCard {
                Text(
                    text = "WELL-BEING DEVICE LOG",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeCream,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${screenTimeMinutes / 60}h ${screenTimeMinutes % 60}m",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "Today's Active Screen Time",
                            fontSize = 13.sp,
                            color = Color.LightGray
                        )
                    }

                    // Rounded visual well-being score ring
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .background(Color(0x1F8A6E56), CircleShape)
                            .border(2.dp, ElegantBronze, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val productivityScore = if (screenTimeMinutes > 180) "Muted" else "Optimal"
                            Text(
                                text = productivityScore,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElegantBronze
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Distracting notifications are blocked dynamically. Keep limits healthy to protect concentration.",
                    fontSize = 12.sp,
                    color = CafeCream.copy(alpha = 0.8f)
                )
            }
        }

        // Strict Focus Timer Controller
        item {
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = ElegantBronze,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "STRICT LOCK WORK TIME",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(0xFF2C1D11), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "STRICT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = ElegantBronze
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Initiate full-screen locking. All distracting apps (Instagram, Snapchat) are locked out entirely during work. Full overlay prevents scrolling social media.",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )

                Spacer(modifier = Modifier.height(18.dp))

                var chosenMinutes by remember { mutableStateOf(25) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(10, 25, 45, 60).forEach { mins ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (chosenMinutes == mins) ElegantBronze else Color(0x1AFFFFFF)
                                )
                                .clickable { chosenMinutes = mins }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${mins}m",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (chosenMinutes == mins) Color.White else CafeCream
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                GlassButton(
                    onClick = { viewModel.startFocusMode(chosenMinutes, "Work focus") },
                    text = "Engage Lock Screen Mode",
                    icon = Icons.Rounded.Lock,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "lock_work_button"
                )
            }
        }

        // Aura Settings & API Deployment Cockpit (Offline-first companion configuration)
        item {
            GlassCard {
                val context = LocalContext.current

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = null,
                            tint = ElegantBronze,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AURA SETTINGS & API DEPLOYMENT",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Configure system overlays and grant needed permissions to enable the full lock screen and offline sound controls.",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Connection indicator status
                Text(
                    text = "SYSTEM ARCHITECTURE STATUS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElegantBronze
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x0AFFFFFF))
                        .border(width = 1.dp, color = Color(0x11FFFFFF), shape = RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Psychology,
                            contentDescription = null,
                            tint = CafeCream,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AURA SECURE SYSTEM INTENT",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Aura secure hybrid-cognitive services are fully synchronized. Offline reflection engines and streak diagnostics remain continuously active.",
                                fontSize = 11.sp,
                                color = CafeCream,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // System Control Permissions Section
                Text(
                    text = "SYSTEM SECURITY & CONTROL PERMISSIONS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElegantBronze
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Unlock high-productivity system triggers. This app runs fully locally and never collects or transmits any private data.",
                    fontSize = 10.sp,
                    color = Color.LightGray
                )
                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // System Overlay Button
                    GlassButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            }
                        },
                        text = "Enable Lock Screen Overlay",
                        icon = Icons.Rounded.Lock,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "overlay_permission_btn"
                    )

                    // Alarm scheduler button
                    GlassButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                val intent = Intent(
                                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            }
                        },
                        text = "Enable Offline Alarm Timers",
                        icon = Icons.Rounded.Notifications,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "alarm_permission_btn"
                    )
                }
            }
        }

        // Active Reminders, Custom Tones & Smart Snooze Panel
        item {
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = null,
                            tint = ElegantBronze,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SMART REMINDERS & TONES",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    IconButton(onClick = { showReminderCreator = !showReminderCreator }) {
                        Icon(
                            imageVector = if (showReminderCreator) Icons.Rounded.Close else Icons.Rounded.Add,
                            contentDescription = "Create Reminder",
                            tint = Color.White
                        )
                    }
                }

                AnimatedVisibility(visible = showReminderCreator) {
                    ReminderCreatorPanel(viewModel) { showReminderCreator = false }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (reminders.isEmpty()) {
                    Text(
                        text = "No scheduled reminders. Tap + to add premium notifications with iOS chime selectors.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        reminders.forEach { r ->
                            ReminderItemRow(r, viewModel)
                        }
                    }
                }
            }
        }

        // Mind Reflection Studio (Mistakes Ledger & Worry Stoic Reframing Box)
        item {
            ReflectionStudioPanel(viewModel, notes)
        }

        // History logs with Time, Date, and Delete actions
        item {
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Timeline,
                            contentDescription = null,
                            tint = ElegantBronze,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "HISTORY TIMELINE",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    if (historyLogs.isNotEmpty()) {
                        Text(
                            text = "Clear All",
                            color = Color(0xFFC59B7F),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { viewModel.clearAllHistory() }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                if (historyLogs.isEmpty()) {
                    Text(
                        text = "No logs yet. Completed routines and notes are saved dynamically with exact date and time.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        historyLogs.take(15).forEach { log ->
                            HistoryLogItemRow(log, viewModel)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp)) // Padding to float over navigation bar
        }
    }
}

// ==========================================
// REMINDER CREATOR PANEL
// ==========================================

@Composable
fun ReminderCreatorPanel(viewModel: AuraViewModel, onComplete: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf("") }
    var hourInput by remember { mutableStateOf("08") }
    var minInput by remember { mutableStateOf("30") }
    var isAm by remember { mutableStateOf(true) }
    var urgency by remember { mutableStateOf("Medium") }
    var selectedTone by remember { mutableStateOf("Zen Chime") }

    val tonesList = listOf("Zen Chime", "Amber Echo", "Espresso Tone", "Silent")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x1F000000), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GlassTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = "Task / Note Title",
            testTag = "reminder_title_field"
        )

        GlassTextField(
            value = msg,
            onValueChange = { msg = it },
            placeholder = "Alert Message content",
            testTag = "reminder_message_field"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Time (12h format)", fontSize = 12.sp, color = CafeCream)
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Hour Input Box
                BasicTextField(
                    value = hourInput,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() }
                        if (filtered.length <= 2) {
                            hourInput = filtered
                        }
                    },
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    cursorBrush = SolidColor(ElegantBronze),
                    modifier = Modifier
                        .width(42.dp)
                        .background(Color(0x1AFFFFFF), RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.Center) {
                            if (hourInput.isEmpty()) {
                                Text("12", color = Color.Gray, fontSize = 14.sp)
                            }
                            innerTextField()
                        }
                    }
                )

                Spacer(modifier = Modifier.width(6.dp))
                Text(text = ":", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))

                // Minute Input Box
                BasicTextField(
                    value = minInput,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() }
                        if (filtered.length <= 2) {
                            minInput = filtered
                        }
                    },
                    textStyle = TextStyle(color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    cursorBrush = SolidColor(ElegantBronze),
                    modifier = Modifier
                        .width(42.dp)
                        .background(Color(0x1AFFFFFF), RoundedCornerShape(8.dp))
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    decorationBox = { innerTextField ->
                        Box(contentAlignment = Alignment.Center) {
                            if (minInput.isEmpty()) {
                                Text("00", color = Color.Gray, fontSize = 14.sp)
                            }
                            innerTextField()
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                // AM/PM Selection Buttons
                Row(
                    modifier = Modifier
                        .background(Color(0x1AFFFFFF), RoundedCornerShape(8.dp))
                        .padding(2.dp)
                ) {
                    listOf("AM", "PM").forEach { period ->
                        val isSelected = if (period == "AM") isAm else !isAm
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) ElegantBronze else Color.Transparent)
                                .clickable { isAm = (period == "AM") }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = period,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else CafeCream
                            )
                        }
                    }
                }
            }
        }

        // Urgency Level Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Priority Urgency", fontSize = 12.sp, color = CafeCream)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf("High", "Medium", "Low").forEach { u ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (urgency == u) ElegantBronze else Color(0x1AFFFFFF))
                            .clickable { urgency = u }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text(text = u, fontSize = 11.sp, color = if (urgency == u) Color.White else CafeCream)
                    }
                }
            }
        }

        // Reminder premium iOS-compatible Notification Tone selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Ringtone Chime", fontSize = 12.sp, color = CafeCream)
            var showToneDropdown by remember { mutableStateOf(false) }
            Box {
                Row(
                    modifier = Modifier
                        .background(Color(0x1AFFFFFF), RoundedCornerShape(8.dp))
                        .clickable { showToneDropdown = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Rounded.MusicNote, contentDescription = null, tint = ElegantBronze, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = selectedTone, color = Color.White, fontSize = 11.sp)
                    Icon(imageVector = Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                }
                DropdownMenu(
                    expanded = showToneDropdown,
                    onDismissRequest = { showToneDropdown = false }
                ) {
                    tonesList.forEach { toneName ->
                        DropdownMenuItem(
                            text = { Text(toneName) },
                            onClick = { selectedTone = toneName; showToneDropdown = false }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        GlassButton(
            onClick = {
                val h12 = hourInput.toIntOrNull()?.coerceIn(1, 12) ?: 8
                val minVal = minInput.toIntOrNull()?.coerceIn(0, 59) ?: 0
                val hour24 = when {
                    isAm && h12 == 12 -> 0
                    isAm -> h12
                    !isAm && h12 == 12 -> 12
                    else -> h12 + 12
                }
                if (title.isNotBlank()) {
                    viewModel.addReminder(title, msg, hour24, minVal, urgency, selectedTone)
                    onComplete()
                }
            },
            text = "Set Task Reminder",
            modifier = Modifier.fillMaxWidth(),
            testTag = "submit_reminder_button"
        )
    }
}

// ==========================================
// REMINDER ITEM LIST ROW WITH SMART SNOOZE
// ==========================================

@Composable
fun ReminderItemRow(reminder: Reminder, viewModel: AuraViewModel) {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    val formattedTime = formatter.format(Date(reminder.timeMills))

    val urgencyColor = when (reminder.urgency) {
        "High" -> Color(0xFFE57373)
        "Low" -> Color(0xFF81C784)
        else -> ElegantBronze
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x0EFFFFFF), RoundedCornerShape(16.dp))
            .border(
                1.dp, 
                if (reminder.isTriggered) Color(0x7F8A6E56) else Color(0x11FFFFFF), 
                RoundedCornerShape(16.dp)
            )
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.toggleReminderCompleted(reminder) }) {
                        Icon(
                            imageVector = if (reminder.isCompleted) Icons.Rounded.CheckCircle else Icons.Rounded.Add,
                            contentDescription = "Check",
                            tint = if (reminder.isCompleted) ElegantBronze else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = reminder.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (reminder.isCompleted) Color.Gray else Color.White
                        )
                        Text(
                            text = reminder.message,
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(urgencyColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = reminder.urgency, fontSize = 9.sp, color = urgencyColor, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(onClick = { viewModel.deleteReminder(reminder.id) }) {
                        Icon(imageVector = Icons.Rounded.Delete, contentDescription = "Delete", tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Sub-bar showing Scheduled Time, Selected Tone, and Adaptive Snooze
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Rounded.AccessTime, contentDescription = null, tint = CafeCream, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = formattedTime, fontSize = 11.sp, color = CafeCream)
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(imageVector = Icons.Rounded.NotificationsActive, contentDescription = null, tint = CafeCream, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = reminder.tone, fontSize = 11.sp, color = CafeCream)
                }

                // Learn Snooze Count & Suggest Adaptive Duration
                if (!reminder.isCompleted) {
                    val adaptiveSnoozeMinutes = when (reminder.urgency) {
                        "High" -> 5
                        "Low" -> if (reminder.snoozeCount >= 2) 60 else 15
                        else -> if (reminder.snoozeCount >= 1) 30 else 15
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(0xFF2C1D11), RoundedCornerShape(8.dp))
                            .clickable { viewModel.snoozeReminder(reminder) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Rounded.Refresh, contentDescription = "Smart Snooze", tint = ElegantBronze, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Snooze ($adaptiveSnoozeMinutes m)",
                                fontSize = 10.sp,
                                color = ElegantBronze,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// HISTORY LOG LIST ROW (WITH TIME AND DATE AND DELETE ACTIONS)
// ==========================================

@Composable
fun HistoryLogItemRow(log: HistoryLog, viewModel: AuraViewModel) {
    val dateText = SimpleDateFormat("MMM d, HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x05FFFFFF), RoundedCornerShape(10.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = log.eventTitle,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(Color(0x1F8A6E56), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = log.eventType.uppercase(),
                        fontSize = 8.sp,
                        color = ElegantBronze,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = dateText,
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
        }

        IconButton(onClick = { viewModel.deleteHistoryLog(log.id) }) {
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = "Remove History",
                tint = Color.Gray.copy(alpha = 0.4f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

// ==========================================
// 2. HABIT ROUTINES TAB
// ==========================================

@Composable
fun RoutinesTab(viewModel: AuraViewModel, routines: List<Routine>) {
    var titleInput by remember { mutableStateOf("") }
    var hourInput by remember { mutableStateOf("07") }
    var minInput by remember { mutableStateOf("30") }
    var isAm by remember { mutableStateOf(true) }
    var selectedCategory by remember { mutableStateOf("General") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "routines",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = "Build compounding healthy behaviors daily.",
                fontSize = 13.sp,
                color = ElegantBronze,
                fontWeight = FontWeight.Bold
            )
        }

        // Add Routine Panel
        item {
            GlassCard {
                Text(
                    text = "CREATE NEW ROUTINE HABIT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeCream,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                GlassTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    placeholder = "Habit Name (e.g., Hydrate, Breathe)",
                    testTag = "routine_title_input"
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Time Selector Container (12h format AM/PM)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1.2f)
                            .background(Color(0x11FFFFFF), RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0x1F8A6E56), RoundedCornerShape(12.dp))
                            .padding(8.dp)
                    ) {
                        // Hour Input Box
                        BasicTextField(
                            value = hourInput,
                            onValueChange = { input ->
                                val filtered = input.filter { it.isDigit() }
                                if (filtered.length <= 2) {
                                    hourInput = filtered
                                }
                            },
                            textStyle = TextStyle(color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            cursorBrush = SolidColor(ElegantBronze),
                            modifier = Modifier
                                .width(32.dp)
                                .background(Color(0x1AFFFFFF), RoundedCornerShape(6.dp))
                                .padding(vertical = 6.dp),
                            decorationBox = { innerTextField ->
                                Box(contentAlignment = Alignment.Center) {
                                    if (hourInput.isEmpty()) {
                                        Text("12", color = Color.Gray, fontSize = 14.sp)
                                    }
                                    innerTextField()
                                }
                            }
                        )

                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = ":", color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))

                        // Minute Input Box
                        BasicTextField(
                            value = minInput,
                            onValueChange = { input ->
                                val filtered = input.filter { it.isDigit() }
                                if (filtered.length <= 2) {
                                    minInput = filtered
                                }
                            },
                            textStyle = TextStyle(color = Color.White, fontSize = 14.sp, textAlign = TextAlign.Center),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            cursorBrush = SolidColor(ElegantBronze),
                            modifier = Modifier
                                .width(32.dp)
                                .background(Color(0x1AFFFFFF), RoundedCornerShape(6.dp))
                                .padding(vertical = 6.dp),
                            decorationBox = { innerTextField ->
                                Box(contentAlignment = Alignment.Center) {
                                    if (minInput.isEmpty()) {
                                        Text("00", color = Color.Gray, fontSize = 14.sp)
                                    }
                                    innerTextField()
                                }
                            }
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // AM/PM Selection Buttons
                        Row(
                            modifier = Modifier
                                .background(Color(0x1AFFFFFF), RoundedCornerShape(8.dp))
                                .padding(2.dp)
                        ) {
                            listOf("AM", "PM").forEach { period ->
                                val isSelected = if (period == "AM") isAm else !isAm
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSelected) ElegantBronze else Color.Transparent)
                                        .clickable { isAm = (period == "AM") }
                                        .padding(horizontal = 6.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = period,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else CafeCream
                                    )
                                }
                            }
                        }
                    }

                    // Responsive space-saving category selector dropdown
                    var showCatDropdown by remember { mutableStateOf(false) }
                    val categories = listOf("Morning", "Work", "Night", "Health", "Social", "General")
                    Box(
                        modifier = Modifier
                            .weight(0.8f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x11FFFFFF))
                            .border(1.dp, Color(0x1F8A6E56), RoundedCornerShape(12.dp))
                            .clickable { showCatDropdown = true }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = selectedCategory,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = "Select Category",
                                tint = ElegantBronze,
                                modifier = Modifier.size(12.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showCatDropdown,
                            onDismissRequest = { showCatDropdown = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        selectedCategory = cat
                                        showCatDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                GlassButton(
                    onClick = {
                        if (titleInput.isNotBlank()) {
                            val h = hourInput.trim().toIntOrNull() ?: 7
                            val m = minInput.trim().toIntOrNull() ?: 0
                            val h24 = if (isAm) {
                                if (h == 12) 0 else h
                            } else {
                                if (h == 12) 12 else h + 12
                            }
                            val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", h24, m)
                            viewModel.addRoutine(titleInput, formattedTime, selectedCategory)
                            titleInput = ""
                            hourInput = "07"
                            minInput = "30"
                            isAm = true
                        }
                    },
                    text = "Insert Habit Routine",
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "submit_routine_button"
                )
            }
        }

        if (routines.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Your calendar is completely clean. Type a habit above to instantiate your routine list.", color = Color.Gray, fontSize = 13.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            items(routines) { routine ->
                val today = viewModel.getTodayDateStr()
                val completedToday = routine.lastCompletedDate == today

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x14FFFFFF), RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (completedToday) ElegantBronze else Color(0x11FFFFFF), 
                                        CircleShape
                                    )
                                    .clickable { viewModel.toggleRoutineCompletion(routine) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Complete Routine",
                                    tint = if (completedToday) Color.White else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = routine.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (completedToday) Color.Gray else Color.White
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color(0x1F8A6E56), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = routine.category, fontSize = 9.sp, color = ElegantBronze, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Rounded.Schedule,
                                        contentDescription = null,
                                        tint = Color.LightGray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = formatTimeTo12h(routine.timeStr), fontSize = 12.sp, color = Color.LightGray)
                                }
                            }
                        }

                        IconButton(onClick = { viewModel.deleteRoutine(routine.id) }) {
                            Icon(imageVector = Icons.Rounded.Delete, contentDescription = "Delete Routine", tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

fun formatTimeTo12h(timeStr: String): String {
    return try {
        val parts = timeStr.split(":")
        val hour24 = parts[0].trim().toInt()
        val minute = parts[1].trim().toInt()
        val isAm = hour24 < 12
        val hour12 = when (hour24) {
            0 -> 12
            in 1..12 -> hour24
            else -> hour24 - 12
        }
        val period = if (isAm) "AM" else "PM"
        String.format(Locale.getDefault(), "%d:%02d %s", hour12, minute, period)
    } catch (e: Exception) {
        timeStr
    }
}

// ==========================================
// 3. DAILY NOTES TAB
// ==========================================

@Composable
fun NotesTab(viewModel: AuraViewModel, notes: List<DailyNote>) {
    var noteContent by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("Thought") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "notes",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = "Unstructured mindful logs and task definitions.",
                fontSize = 13.sp,
                color = ElegantBronze,
                fontWeight = FontWeight.Bold
            )
        }

        // Add Note Input Panel
        item {
            GlassCard {
                Text(
                    text = "WRITE DAILY REFLECTION OR NOTE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeCream,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                GlassTextField(
                    value = noteContent,
                    onValueChange = { noteContent = it },
                    placeholder = "Type whatever is on your mind today...",
                    singleLine = false,
                    testTag = "note_content_input"
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tag selectors
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Thought", "Task", "Water", "Urgent").forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selectedTag == tag) ElegantBronze else Color(0x10FFFFFF))
                                    .clickable { selectedTag = tag }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text(text = tag, fontSize = 11.sp, color = if (selectedTag == tag) Color.White else CafeCream)
                            }
                        }
                    }

                    GlassIconButton(
                        onClick = {
                            if (noteContent.isNotBlank()) {
                                viewModel.addNote(noteContent, selectedTag)
                                noteContent = ""
                            }
                        },
                        icon = Icons.Rounded.Add,
                        contentDescription = "Save Note"
                    )
                }
            }
        }

        if (notes.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Your log flow is blank. Capture daily reflections to run AI Habit evaluations.", color = Color.Gray, fontSize = 13.sp, textAlign = TextAlign.Center)
                }
            }
        } else {
            items(notes) { note ->
                val dateText = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(Date(note.timestamp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x14FFFFFF), RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0x1F8A6E56), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(text = note.tag.uppercase(), fontSize = 9.sp, color = ElegantBronze, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = dateText, fontSize = 11.sp, color = Color.Gray)
                            }

                            IconButton(onClick = { viewModel.deleteNote(note.id) }) {
                                Icon(imageVector = Icons.Rounded.Delete, contentDescription = "Delete Note", tint = Color.Gray.copy(alpha = 0.5f), modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = note.content,
                            fontSize = 14.sp,
                            color = Color.White,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// ==========================================
// 4. CHAT TAB & AI HABIT ANALYZER
// ==========================================

@Composable
fun ChatTab(
    viewModel: AuraViewModel,
    messages: List<ChatMessage>,
    isAuraLoading: Boolean
) {
    val aiAnalysisResult by viewModel.aiAnalysisResult.collectAsState()
    val isAnalyzingPatterns by viewModel.isAnalyzingPatterns.collectAsState()
    val customApiKey by viewModel.customApiKey.collectAsState()

    var chatInput by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()
    var showApiKeyDialog by remember { mutableStateOf(false) }

    // Keep scrolling to newest chat message automatically
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(messages.size + 1) // accounts for the header cards
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Chat Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ai routine aura",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "Personalized pattern-recognition AI",
                    fontSize = 12.sp,
                    color = ElegantBronze,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Clear Chat",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier
                        .clickable { viewModel.clearChatHistory() }
                        .padding(8.dp)
                )

                IconButton(
                    onClick = { showApiKeyDialog = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Gemini Key Settings",
                        tint = CafeCream,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // HERO MODULE: AI Habit Pattern & Routine Analyzer Panel
            item {
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "🧠 AI ROUTINE ANALYZER",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Extract patterns & get personalized habit tweaks",
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                        }

                        if (isAnalyzingPatterns) {
                            CircularProgressIndicator(color = ElegantBronze, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF2C1D11), RoundedCornerShape(8.dp))
                                    .clickable { viewModel.runAIHabitAnalysis() }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(text = "Analyze", color = ElegantBronze, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    aiAnalysisResult?.let { analysis ->
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .background(Color(0x1A000000), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = analysis,
                                fontSize = 12.sp,
                                color = CafeCream,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            if (messages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aura chatbot ready. Ask questions about your habits, schedules or note suggestions.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(messages) { msg ->
                    val alignment = if (msg.isUser) Alignment.End else Alignment.Start
                    val bubbleColor = if (msg.isUser) Color(0xFF2C1D11) else Color(0x1AFFFFFF)
                    val textAlignment = if (msg.isUser) TextAlign.End else TextAlign.Start

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = alignment
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(
                                    RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (msg.isUser) 16.dp else 4.dp,
                                        bottomEnd = if (msg.isUser) 4.dp else 16.dp
                                    )
                                )
                                .background(bubbleColor)
                                .border(
                                    1.dp, 
                                    if (msg.isUser) Color(0x3B8A6E56) else Color.Transparent, 
                                    RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (msg.isUser) 16.dp else 4.dp,
                                        bottomEnd = if (msg.isUser) 4.dp else 16.dp
                                    )
                                )
                                .padding(12.dp)
                                .widthIn(max = 280.dp)
                        ) {
                            Text(
                                text = msg.text,
                                color = Color.White,
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // Input Messaging Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 110.dp, top = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GlassTextField(
                value = chatInput,
                onValueChange = { chatInput = it },
                placeholder = "Message your routine aura...",
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (chatInput.isNotBlank()) {
                        viewModel.askAura(chatInput)
                        chatInput = ""
                    }
                }),
                testTag = "chat_message_input"
            )

            GlassIconButton(
                onClick = {
                    if (chatInput.isNotBlank()) {
                        viewModel.askAura(chatInput)
                        chatInput = ""
                    }
                },
                icon = Icons.Rounded.Send,
                contentDescription = "Send Chat Message"
            )
        }
    }

    if (showApiKeyDialog) {
        var apiKeyInput by remember { mutableStateOf(customApiKey) }
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            containerColor = Color(0xFF1E1613),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = null,
                        tint = ElegantBronze,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gemini API Configuration",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Register your personal Gemini API key to run pattern-recognition and offline routine intelligence directly on your device.",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        placeholder = { Text("Enter AI Studio API Key (AIzaSy...)", color = Color.Gray, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = ElegantBronze,
                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                            focusedContainerColor = Color(0x1A000000),
                            unfocusedContainerColor = Color(0x1A000000)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveCustomApiKey(apiKeyInput.trim())
                        showApiKeyDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElegantBronze)
                ) {
                    Text("Save Key", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showApiKeyDialog = false }
                ) {
                    Text("Close", color = Color.LightGray)
                }
            }
        )
    }
}

// ==========================================
// 5. IMMERSIVE LOCK SCREEN OVERLAY
// ==========================================

@Composable
fun FocusOverlayLockScreen(
    focusTimeRemaining: Int,
    focusType: String,
    onCancel: () -> Unit
) {
    // Completely block hardware Back button presses to enforce lock screen
    androidx.activity.compose.BackHandler(enabled = true) {
        // Do nothing - blocks back navigation
    }

    val minutes = focusTimeRemaining / 60
    val seconds = focusTimeRemaining % 60
    val progressFraction = if (focusTimeRemaining > 0) focusTimeRemaining.toFloat() else 1f

    // Absolute premium full-screen deep brown/black glass blur block overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ObsidianBlack,
                        Color(0xFF160E08),
                        ObsidianBlack
                    )
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Rounded.Block,
                contentDescription = "Strict Lock active",
                tint = ElegantBronze,
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "STRICT WELL-BEING LOCK",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = ElegantBronze,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Large digital liquid glass glow-timer
            Text(
                text = String.format("%02d:%02d", minutes, seconds),
                fontSize = 72.sp,
                fontWeight = FontWeight.Thin,
                color = Color.White,
                letterSpacing = (-2).sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Focus: $focusType",
                fontSize = 15.sp,
                color = CafeCream,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .background(Color(0x1F8A6E56), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Snapchat, Instagram, and other distracting entertainment platforms are completely frozen.",
                        fontSize = 12.sp,
                        color = CafeCream,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Notifications are muted to protect focus blocks.",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Apple style translucent emergency unlock button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x12FFFFFF))
                    .clickable { onCancel() }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Interrupt & Emergency Unlock",
                    color = Color(0xFFC59B7F),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

data class MilestoneBadge(
    val name: String,
    val targetCount: Int,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun ReflectionStudioPanel(
    viewModel: AuraViewModel,
    notes: List<DailyNote>
) {
    val loggedMistakes = notes.filter { it.tag == "Mistake" }
    val loggedWorries = notes.filter { it.tag == "Worry" }

    val analyzing by viewModel.analyzingWorryOrMistake.collectAsState()
    val aiResponse by viewModel.worryOrMistakeResponse.collectAsState()

    var activeReflectionTab by remember { mutableStateOf("Mistakes") } // "Mistakes" or "Worries"
    var textInput by remember { mutableStateOf("") }

    GlassCard {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Rounded.SelfImprovement,
                    contentDescription = null,
                    tint = ElegantBronze,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "MIND REFLECTION STUDIO",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                    .padding(2.dp)
            ) {
                listOf("Mistakes", "Worries").forEach { tab ->
                    val isSelected = activeReflectionTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) ElegantBronze else Color.Transparent)
                            .clickable { activeReflectionTab = tab }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else CafeCream
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (activeReflectionTab == "Mistakes") {
            Text(
                text = "Mistakes are lessons in disguise. Log behaviors you wish to correct (e.g., procrastinating, sleeping late) to build bulletproof self-discipline.",
                fontSize = 12.sp,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            GlassTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = "e.g., Snoozed alarm 3 times and skipped gym",
                modifier = Modifier.fillMaxWidth(),
                testTag = "mistake_input_field"
            )

            Spacer(modifier = Modifier.height(10.dp))

            GlassButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        viewModel.addNote(textInput.trim(), tag = "Mistake")
                        textInput = ""
                    }
                },
                text = "Commit to Mistakes Ledger",
                icon = Icons.Rounded.Add,
                modifier = Modifier.fillMaxWidth()
            )

            if (loggedMistakes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "MY MISTAKES LEDGER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeCream,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    loggedMistakes.take(3).forEach { m ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x12FFFFFF), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = m.content,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Logged today",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                            IconButton(onClick = { viewModel.deleteNote(m.id) }) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = Color.Gray.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    GlassButton(
                        onClick = {
                            val contextText = loggedMistakes.joinToString("\n") { "- ${it.content}" }
                            viewModel.getWorryAndMistakesAnalysis(contextText, isWorry = false)
                        },
                        text = "Deconstruct Mistakes with AI",
                        icon = Icons.Rounded.Psychology,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            // Worries Tab
            Text(
                text = "Anxieties lose their grip when written down. Cast your worries to 'the void' to get compassionate Stoic reframing and immediate perspective.",
                fontSize = 12.sp,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(12.dp))

            GlassTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = "e.g., I'm anxious I will fail my coding interview tomorrow",
                modifier = Modifier.fillMaxWidth(),
                testTag = "worry_input_field"
            )

            Spacer(modifier = Modifier.height(10.dp))

            GlassButton(
                onClick = {
                    if (textInput.isNotBlank()) {
                        viewModel.addNote(textInput.trim(), tag = "Worry")
                        textInput = ""
                    }
                },
                text = "Release to Worry Box",
                icon = Icons.Rounded.CloudQueue,
                modifier = Modifier.fillMaxWidth()
            )

            if (loggedWorries.isNotEmpty()) {
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "ACTIVE WORRIES IN BOX",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeCream,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    loggedWorries.take(3).forEach { w ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x12FFFFFF), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = w.content,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Logged today",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                            IconButton(onClick = { viewModel.deleteNote(w.id) }) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "Delete",
                                    tint = Color.Gray.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    GlassButton(
                        onClick = {
                            val contextText = loggedWorries.joinToString("\n") { "- ${it.content}" }
                            viewModel.getWorryAndMistakesAnalysis(contextText, isWorry = true)
                        },
                        text = "Stoic Reframing by Aura AI",
                        icon = Icons.Rounded.CompassCalibration,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Display AI response
        if (analyzing || aiResponse != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0x24000000), RoundedCornerShape(16.dp))
                    .border(width = 1.dp, color = Color(0x3D8A6E56), shape = RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                if (analyzing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = ElegantBronze,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Aura is deconstructing with wisdom...",
                            fontSize = 12.sp,
                            color = CafeCream
                        )
                    }
                } else if (aiResponse != null) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "AURA GUIDANCE & REFLECTION",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ElegantBronze,
                                letterSpacing = 1.sp
                            )
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close",
                                tint = Color.Gray,
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { viewModel.clearWorryOrMistakeResponse() }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = aiResponse ?: "",
                            fontSize = 13.sp,
                            color = Color.White,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// REFLECTION PROMPT & STREAK ANALYTICS UI
// ==========================================

@Composable
fun ProductivityTrendChart(
    scores: List<Float>,
    modifier: Modifier = Modifier
) {
    val gradientColor = listOf(ElegantBronze, CafeCream)
    
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val paddingLeft = 16.dp.toPx()
        val paddingRight = 16.dp.toPx()
        val paddingTop = 16.dp.toPx()
        val paddingBottom = 16.dp.toPx()
        
        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom
        
        if (scores.isEmpty()) return@Canvas
        
        val points = scores.mapIndexed { index, score ->
            val x = paddingLeft + (index.toFloat() / (scores.size - 1)) * chartWidth
            val y = paddingTop + (1f - score) * chartHeight
            Offset(x, y)
        }
        
        val gridLines = listOf(0f, 0.5f, 1f)
        gridLines.forEach { ratio ->
            val y = paddingTop + ratio * chartHeight
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(paddingLeft, y),
                end = Offset(width - paddingRight, y),
                strokeWidth = 1.dp.toPx()
            )
        }
        
        val path = Path().apply {
            moveTo(points.first().x, height - paddingBottom)
            points.forEach { point ->
                lineTo(point.x, point.y)
            }
            lineTo(points.last().x, height - paddingBottom)
            close()
        }
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(
                    ElegantBronze.copy(alpha = 0.25f),
                    Color.Transparent
                ),
                startY = paddingTop,
                endY = height - paddingBottom
            )
        )
        
        val linePath = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points.first().x, points.first().y)
                for (i in 0 until points.size - 1) {
                    val p1 = points[i]
                    val p2 = points[i + 1]
                    val controlX = (p1.x + p2.x) / 2
                    cubicTo(controlX, p1.y, controlX, p2.y, p2.x, p2.y)
                }
            }
        }
        drawPath(
            path = linePath,
            brush = Brush.horizontalGradient(gradientColor),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        
        points.forEach { point ->
            drawCircle(
                color = ElegantBronze.copy(alpha = 0.35f),
                radius = 6.dp.toPx(),
                center = point
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = point
            )
        }
    }
}

fun getLast7DaysLetters(): List<String> {
    val sdf = SimpleDateFormat("EEEEE", Locale.getDefault())
    val days = ArrayList<String>()
    for (i in 6 downTo 0) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -i)
        days.add(sdf.format(cal.time))
    }
    return days
}

@Composable
fun StreakAndAnalyticsCard(
    viewModel: AuraViewModel,
    historyLogs: List<HistoryLog>,
    routines: List<Routine>
) {
    val streak = viewModel.calculateRoutineStreak(historyLogs)
    val trendScores = viewModel.calculateProductivityTrend(historyLogs, routines.size)
    
    val today = viewModel.getTodayDateStr()
    val completedToday = routines.count { it.lastCompletedDate == today }
    val totalEnabled = routines.count { it.isEnabled }
    
    val completionRatio = if (totalEnabled > 0) completedToday.toFloat() / totalEnabled.toFloat() else 0f

    LiquidGlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.TrendingUp,
                    contentDescription = null,
                    tint = ElegantBronze,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MOMENTUM ANALYTICS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }
            
            Row(
                modifier = Modifier
                    .background(Color(0x24FF9800), RoundedCornerShape(12.dp))
                    .border(width = 1.dp, color = Color(0x3DFF9800), shape = RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Whatshot,
                    contentDescription = "Streak",
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$streak-Day Streak",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFB74D)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x0AFFFFFF), RoundedCornerShape(16.dp))
                        .border(width = 1.dp, color = Color(0x1F8A6E56), shape = RoundedCornerShape(16.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { completionRatio },
                                color = ElegantBronze,
                                trackColor = Color(0x1AFFFFFF),
                                strokeWidth = 4.dp,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "${(completionRatio * 100).toInt()}%",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                        
                        Column {
                            Text(
                                text = "TODAY'S TASKRATE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = CafeCream
                            )
                            Text(
                                text = "$completedToday of $totalEnabled done",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0x0AFFFFFF), RoundedCornerShape(16.dp))
                        .border(width = 1.dp, color = Color(0x10FFFFFF), shape = RoundedCornerShape(16.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "AURA AURA",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = CafeCream,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (streak >= 5) Icons.Rounded.Whatshot else Icons.Rounded.TrendingUp,
                                contentDescription = null,
                                tint = if (streak >= 5) Color(0xFFFFB74D) else Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (streak >= 5) "1.5x Focus Multiplier" else "Keep building!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (streak >= 5) Color(0xFFFFB74D) else Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Consistency fuels mindfulness.",
                            fontSize = 9.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1.2f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "7-DAY TREND SCORE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeCream,
                    letterSpacing = 1.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ProductivityTrendChart(
                    scores = trendScores,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val days = getLast7DaysLetters()
                    days.forEachIndexed { index, day ->
                        val isToday = index == days.size - 1
                        Text(
                            text = day,
                            fontSize = 9.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                            color = if (isToday) ElegantBronze else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DailyReflectionSuitableCard(
    viewModel: AuraViewModel,
    routines: List<Routine>,
    notes: List<DailyNote>
) {
    val prompt by viewModel.dailyReflectionSuitable.collectAsState()
    val isOnline by viewModel.isSuitableOnline.collectAsState()
    val isLoading by viewModel.isSuitableLoading.collectAsState()

    LaunchedEffect(key1 = routines.size, key2 = notes.size) {
        if (prompt == "What is one small routine you can prioritize today to make the day feel like a victory?") {
            viewModel.generateDailyReflectionSuitable(routines, notes)
        }
    }

    LiquidGlassCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.SelfImprovement,
                    contentDescription = null,
                    tint = ElegantBronze,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "AURA RECONSTRUCT CUE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }

            Row(
                modifier = Modifier
                    .background(
                        Color(0x0EFFFFFF),
                        RoundedCornerShape(10.dp)
                    )
                    .border(
                        width = 0.8.dp,
                        color = Color(0x1AFFFFFF),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            ElegantBronze,
                            CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Aura Engine",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = CafeCream
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x14FFFFFF), RoundedCornerShape(16.dp))
                .border(width = 1.dp, color = Color(0x1A8A6E56), shape = RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            if (isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        color = ElegantBronze,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Aura is formulating wisdom...",
                        fontSize = 12.sp,
                        color = CafeCream
                    )
                }
            } else {
                Text(
                    text = "\"$prompt\"",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    lineHeight = 20.sp,
                    fontStyle = FontStyle.Italic
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var showWriteNoteDialog by remember { mutableStateOf(false) }
            var reflectionText by remember { mutableStateOf("") }

            if (showWriteNoteDialog) {
                AlertDialog(
                    onDismissRequest = { showWriteNoteDialog = false },
                    containerColor = Color(0xFF1E1613),
                    title = {
                        Text(
                            text = "Log Daily Reflection",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = prompt,
                                color = CafeCream,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            OutlinedTextField(
                                value = reflectionText,
                                onValueChange = { reflectionText = it },
                                placeholder = { Text("Write your thoughts...", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = ElegantBronze,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f),
                                    focusedContainerColor = Color(0x1A000000),
                                    unfocusedContainerColor = Color(0x1A000000)
                                )
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (reflectionText.isNotBlank()) {
                                    viewModel.addNote(
                                        content = "[Reflection: $prompt]\n\nMy thoughts:\n${reflectionText.trim()}",
                                        tag = "Reflection"
                                    )
                                    reflectionText = ""
                                    showWriteNoteDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElegantBronze)
                        ) {
                            Text("Save Entry", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showWriteNoteDialog = false }) {
                            Text("Cancel", color = Color.LightGray)
                        }
                    }
                )
            }

            GlassButton(
                onClick = { showWriteNoteDialog = true },
                text = "Commit thoughts to Ledger",
                icon = Icons.Rounded.Edit,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = { viewModel.generateDailyReflectionSuitable(routines, notes) },
                modifier = Modifier
                    .background(Color(0x12FFFFFF), RoundedCornerShape(12.dp))
                    .border(width = 1.dp, color = Color(0x1F8A6E56), shape = RoundedCornerShape(12.dp))
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Refresh Cue",
                    tint = ElegantBronze,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
