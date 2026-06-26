package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiService
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.ChatMessage
import com.example.data.DailyNote
import com.example.data.FocusSession
import com.example.data.HistoryLog
import com.example.data.Reminder
import com.example.data.ReminderScheduler
import com.example.data.Routine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AuraViewModel(
    application: Application,
    private val repository: AppRepository
) : AndroidViewModel(application) {

    private val scheduler = ReminderScheduler(application)
    private val geminiService = GeminiService()

    // 1. Reactive DB Observables
    val notes: StateFlow<List<DailyNote>> = repository.notes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val routines: StateFlow<List<Routine>> = repository.routines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<Reminder>> = repository.reminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val historyLogs: StateFlow<List<HistoryLog>> = repository.historyLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val focusSessions: StateFlow<List<FocusSession>> = repository.focusSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Local State Variables
    private val _isAuraLoading = MutableStateFlow(false)
    val isAuraLoading = _isAuraLoading.asStateFlow()

    private val _aiAnalysisResult = MutableStateFlow<String?>(null)
    val aiAnalysisResult = _aiAnalysisResult.asStateFlow()

    private val _isAnalyzingPatterns = MutableStateFlow(false)
    val isAnalyzingPatterns = _isAnalyzingPatterns.asStateFlow()

    private val _analyzingWorryOrMistake = MutableStateFlow(false)
    val analyzingWorryOrMistake = _analyzingWorryOrMistake.asStateFlow()

    private val _worryOrMistakeResponse = MutableStateFlow<String?>(null)
    val worryOrMistakeResponse = _worryOrMistakeResponse.asStateFlow()

    private val _dailyReflectionPrompt = MutableStateFlow<String>("What is one small routine you can prioritize today to make the day feel like a victory?")
    val dailyReflectionPrompt = _dailyReflectionPrompt.asStateFlow()

    private val _isPromptLoading = MutableStateFlow(false)
    val isPromptLoading = _isPromptLoading.asStateFlow()

    private val _isPromptOnline = MutableStateFlow(false)
    val isPromptOnline = _isPromptOnline.asStateFlow()

    private val offlineReflectionPrompts = listOf(
        "What is one routine that brings you the most focus, and how can you prioritize it today?",
        "Look at yesterday's logs: where did you lose 15 minutes of focus, and how can we introduce friction?",
        "Are you checking off routines just for completion, or do they serve a larger intent?",
        "What is a minor mistake from yesterday that you can transform into an actionable rule today?",
        "If your daily screentime was reduced by 30%, what mindful activity would you replace it with?",
        "What habit streak are you most proud of building right now, and why is it worth protecting?",
        "If you could finish only one important task today to feel satisfied, what would it be?",
        "What is one quiet worry you can release into the void today to reclaim your focus?"
    )

    // ==========================================
    // WELL-BEING & STRICT APP LIMITS FOCUS MODE
    // ==========================================
    private val _screenTimeMinutes = MutableStateFlow(128) // Simulated starting base (typical screen time)
    val screenTimeMinutes = _screenTimeMinutes.asStateFlow()

    // Interactive classification of apps
    private val _workApps = MutableStateFlow(listOf("Gmail", "Slack", "Notion", "Calendar", "Zoom", "routiner"))
    val workApps = _workApps.asStateFlow()

    private val _entertainmentApps = MutableStateFlow(listOf("Instagram", "Snapchat", "TikTok", "YouTube", "Netflix"))
    val entertainmentApps = _entertainmentApps.asStateFlow()

    // Screen locking / strict focus states
    private val _isFocusModeActive = MutableStateFlow(false)
    val isFocusModeActive = _isFocusModeActive.asStateFlow()

    private val _focusTimeRemaining = MutableStateFlow(0) // in seconds
    val focusTimeRemaining = _focusTimeRemaining.asStateFlow()

    private val _focusType = MutableStateFlow("Work focus")
    val focusType = _focusType.asStateFlow()

    private var focusTimerJob: Job? = null

    // Simulating app activity tracking ticker
    init {
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                delay(60000) // Increase simulated screen-time by 1 minute every actual minute
                if (!_isFocusModeActive.value) {
                    _screenTimeMinutes.value += 1
                }
            }
        }
    }

    fun startFocusMode(durationMinutes: Int, type: String = "Work Focus") {
        focusTimerJob?.cancel()
        _focusType.value = type
        _isFocusModeActive.value = true
        _focusTimeRemaining.value = durationMinutes * 60

        focusTimerJob = viewModelScope.launch(Dispatchers.Main) {
            viewModelScope.launch(Dispatchers.IO) {
                repository.insertHistoryLog("Initiated strict $type lock for $durationMinutes mins", "Focus Locked")
            }

            while (_focusTimeRemaining.value > 0) {
                delay(1000)
                _focusTimeRemaining.value -= 1
            }

            // Completed focus session!
            _isFocusModeActive.value = false
            viewModelScope.launch(Dispatchers.IO) {
                repository.insertFocusSession(
                    FocusSession(
                        durationMinutes = durationMinutes,
                        type = type,
                        blockedAppsCount = _entertainmentApps.value.size
                    )
                )
            }
        }
    }

    fun cancelFocusMode() {
        focusTimerJob?.cancel()
        _isFocusModeActive.value = false
        _focusTimeRemaining.value = 0
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertHistoryLog("Exited focus mode block screen manually", "Focus Interrupted")
        }
    }

    fun addAppToCategory(appName: String, isWork: Boolean) {
        if (appName.isBlank()) return
        val cleanName = appName.trim()
        if (isWork) {
            if (!_workApps.value.contains(cleanName)) {
                _workApps.value = _workApps.value + cleanName
                _entertainmentApps.value = _entertainmentApps.value - cleanName
            }
        } else {
            if (!_entertainmentApps.value.contains(cleanName)) {
                _entertainmentApps.value = _entertainmentApps.value + cleanName
                _workApps.value = _workApps.value - cleanName
            }
        }
    }

    fun removeAppFromCategory(appName: String, isWork: Boolean) {
        if (isWork) {
            _workApps.value = _workApps.value - appName
        } else {
            _entertainmentApps.value = _entertainmentApps.value - appName
        }
    }

    // Helper: Format date string
    fun getTodayDateStr(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    // ==========================================
    // NOTES OPERATIONS
    // ==========================================
    fun addNote(content: String, tag: String = "General") {
        if (content.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertNote(
                DailyNote(
                    content = content.trim(),
                    dateStr = getTodayDateStr(),
                    tag = tag
                )
            )
        }
    }

    fun deleteNote(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNoteById(id)
        }
    }

    fun getWorryAndMistakesAnalysis(text: String, isWorry: Boolean) {
        _analyzingWorryOrMistake.value = true
        _worryOrMistakeResponse.value = null
        viewModelScope.launch {
            val response = geminiService.analyzeWorryOrMistake(text, isWorry)
            _worryOrMistakeResponse.value = response
            _analyzingWorryOrMistake.value = false
        }
    }

    fun clearWorryOrMistakeResponse() {
        _worryOrMistakeResponse.value = null
    }

    // ==========================================
    // ROUTINES OPERATIONS
    // ==========================================
    fun addRoutine(title: String, timeStr: String, category: String = "General", iconName: String = "Star") {
        if (title.isBlank() || timeStr.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertRoutine(
                Routine(
                    title = title.trim(),
                    timeStr = timeStr.trim(),
                    category = category,
                    iconName = iconName
                )
            )
        }
    }

    fun toggleRoutineCompletion(routine: Routine) {
        viewModelScope.launch(Dispatchers.IO) {
            val today = getTodayDateStr()
            val completed = routine.lastCompletedDate == today
            val updatedRoutine = if (completed) {
                routine.copy(lastCompletedDate = "")
            } else {
                routine.copy(lastCompletedDate = today)
            }
            repository.updateRoutine(updatedRoutine)
            
            if (!completed) {
                repository.insertHistoryLog("Completed Routine: ${routine.title}", "Routine Done")
            } else {
                repository.insertHistoryLog("Unchecked Routine: ${routine.title}", "Routine Undo")
            }
        }
    }

    fun deleteRoutine(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteRoutineById(id)
        }
    }

    // ==========================================
    // ADVANCED REMINDERS & SMART SNOOZE
    // ==========================================
    fun addReminder(title: String, message: String, hour: Int, minute: Int, urgency: String = "Medium", tone: String = "Zen Chime") {
        if (title.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val reminder = Reminder(
                title = title.trim(),
                message = message.trim(),
                timeMills = calendar.timeInMillis,
                urgency = urgency,
                tone = tone,
                snoozeCount = 0
            )
            val generatedId = repository.insertReminder(reminder)
            val scheduled = reminder.copy(id = generatedId.toInt())
            scheduler.schedule(scheduled)
        }
    }

    /**
     * Adaptive Smart Snooze Logic:
     * - Evaluates how many times the reminder was already snoozed.
     * - High Urgency: Caps snooze to short bursts (e.g. 5 mins) to prevent task neglect.
     * - Low Urgency: Progressively offers wider windows (e.g. 15 mins -> 30 mins -> 60 mins) to match user work patterns.
     */
    fun snoozeReminder(reminder: Reminder, suggestedMinutes: Int? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            // Cancel current alarms
            scheduler.cancel(reminder.id)

            val nextSnoozeCount = reminder.snoozeCount + 1
            
            // Calculate adaptive snooze length based on urgency and prior snoozes
            val snoozeMinutes = suggestedMinutes ?: when (reminder.urgency) {
                "High" -> 5 // High priority always prompts quick alerts
                "Low" -> if (nextSnoozeCount >= 3) 60 else if (nextSnoozeCount == 2) 30 else 15
                else -> if (nextSnoozeCount >= 2) 30 else 15 // Medium
            }

            val snoozeTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000)

            val updated = reminder.copy(
                timeMills = snoozeTime,
                isTriggered = false,
                isCompleted = false,
                snoozeCount = nextSnoozeCount
            )
            
            repository.updateReminder(updated)
            scheduler.schedule(updated)
            repository.insertHistoryLog("Snoozed '${reminder.title}' adaptive for $snoozeMinutes mins (Snooze #$nextSnoozeCount)", "Reminder Snoozed")
        }
    }

    fun toggleReminderCompleted(reminder: Reminder) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = reminder.copy(isCompleted = !reminder.isCompleted, snoozeCount = 0, isTriggered = false)
            repository.updateReminder(updated)
            if (updated.isCompleted) {
                scheduler.cancel(reminder.id)
                repository.insertHistoryLog("Completed Reminder: ${reminder.title}", "Reminder Done")
            } else if (reminder.timeMills > System.currentTimeMillis()) {
                scheduler.schedule(updated)
            }
        }
    }

    fun deleteReminder(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            scheduler.cancel(id)
            repository.deleteReminderById(id)
        }
    }

    // ==========================================
    // HISTORY LOGS ACTIONS
    // ==========================================
    fun deleteHistoryLog(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteHistoryLogById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllHistory()
        }
    }

    // ==========================================
    // AI INTEGRATION - ANALYZER & CHAT
    // ==========================================
    fun runAIHabitAnalysis() {
        viewModelScope.launch(Dispatchers.IO) {
            _isAnalyzingPatterns.value = true
            try {
                val today = getTodayDateStr()
                val notesTextList = notes.value.map { "${it.content} (Tag: ${it.tag})" }
                val routinesTextList = routines.value.map {
                    val status = if (it.lastCompletedDate == today) "Completed" else "Pending"
                    "${it.title} at ${it.timeStr} - status: $status"
                }
                val remindersTextList = reminders.value.map {
                    val status = if (it.isCompleted) "Done" else "Scheduled"
                    "${it.title} at ${it.timeMills} [Priority: ${it.urgency}]"
                }

                val prompt = """
                    You are routiner's elite productivity pattern analyzer.
                    The user has provided their daily logged notes, current routine habits, and schedules.
                    Today's Date is $today.
                    
                    USER DATA DUMP:
                    --- LOGGED NOTES ---
                    ${if (notesTextList.isEmpty()) "No logs noted yet." else notesTextList.joinToString("\n")}
                    
                    --- HABIT ROUTINES ---
                    ${if (routinesTextList.isEmpty()) "No routines added." else routinesTextList.joinToString("\n")}
                    
                    --- SCHEDULED REMINDERS ---
                    ${if (remindersTextList.isEmpty()) "No active reminders." else remindersTextList.joinToString("\n")}
                    
                    TASK:
                    1. Read through the notes to spot underlying behaviors (e.g. complains of fatigue, mentions working late, logs ideas, water tracking patterns, etc.).
                    2. Contrast notes with routines. Is there a habit they note they want to do but have no routine for?
                    3. Output a crisp, elegant, minimalist iOS-style productivity review (strictly structured in brown-bronze aesthetic terms).
                    4. Suggest **3 highly customized, relevant new routines or tasks** that would help them.
                    5. Format the recommended suggestions using actionable headers. Include exact `[ADD_ROUTINE: Name, HH:MM]` tags so the user can easily instantiate them.
                """.trimIndent()

                val result = geminiService.askAura(
                    userMessage = prompt,
                    todayDate = today,
                    notesList = notesTextList,
                    routinesList = routinesTextList,
                    remindersList = remindersTextList
                )
                _aiAnalysisResult.value = result
            } catch (e: Exception) {
                Log.e("AuraViewModel", "AI pattern analysis failed", e)
                _aiAnalysisResult.value = "Pattern analysis is temporarily offline. Please verify your Gemini API Key in AI Studio secrets: ${e.localizedMessage}"
            } finally {
                _isAnalyzingPatterns.value = false
            }
        }
    }

    fun askAura(message: String) {
        if (message.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            val userMsg = ChatMessage(text = message, isUser = true)
            repository.insertMessage(userMsg)

            _isAuraLoading.value = true

            try {
                val today = getTodayDateStr()
                val notesTextList = notes.value.map { "${it.content} (${it.tag})" }
                val routinesTextList = routines.value.map {
                    val status = if (it.lastCompletedDate == today) "[Completed Today]" else "[Pending]"
                    "${it.title} at ${it.timeStr} - $status"
                }
                val remindersTextList = reminders.value.map {
                    val status = if (it.isCompleted) "[Completed]" else if (it.isTriggered) "[Pending Check]" else "[Scheduled]"
                    "${it.title} at ${it.timeMills} - $status"
                }

                val response = geminiService.askAura(
                    userMessage = message,
                    todayDate = today,
                    notesList = notesTextList,
                    routinesList = routinesTextList,
                    remindersList = remindersTextList
                )

                repository.insertMessage(ChatMessage(text = response, isUser = false))
                parseAndExecuteCommands(response)

            } catch (e: Exception) {
                Log.e("AuraViewModel", "Aura failed to reply", e)
                val errMsg = "I apologize, but I could not sync. Please ensure internet connectivity: ${e.localizedMessage}"
                repository.insertMessage(ChatMessage(text = errMsg, isUser = false))
            } finally {
                _isAuraLoading.value = false
            }
        }
    }

    private fun parseAndExecuteCommands(reply: String) {
        // Parse ADD_NOTE: [ADD_NOTE: content]
        val noteRegex = "\\[ADD_NOTE:\\s*(.*?)\\]".toRegex()
        noteRegex.findAll(reply).forEach { match ->
            val content = match.groupValues[1].trim()
            if (content.isNotEmpty()) {
                addNote(content, "AI Suggestion")
            }
        }

        // Parse ADD_ROUTINE: [ADD_ROUTINE: title, HH:MM]
        val routineRegex = "\\[ADD_ROUTINE:\\s*(.*?)\\s*,\\s*(\\d{2}:\\d{2})\\]".toRegex()
        routineRegex.findAll(reply).forEach { match ->
            val title = match.groupValues[1].trim()
            val timeStr = match.groupValues[2].trim()
            if (title.isNotEmpty()) {
                addRoutine(title, timeStr, "AI Recommended", "Auto")
            }
        }

        // Parse ADD_REMINDER: [ADD_REMINDER: title, HH:MM, message]
        val reminderRegex = "\\[ADD_REMINDER:\\s*(.*?)\\s*,\\s*(\\d{2}:\\d{2})\\s*,\\s*(.*?)\\]".toRegex()
        reminderRegex.findAll(reply).forEach { match ->
            val title = match.groupValues[1].trim()
            val timeStr = match.groupValues[2].trim()
            val message = match.groupValues[3].trim()
            if (title.isNotEmpty()) {
                val parts = timeStr.split(":")
                if (parts.size == 2) {
                    val hour = parts[0].toIntOrNull() ?: 12
                    val min = parts[1].toIntOrNull() ?: 0
                    addReminder(title, message, hour, min, "Medium", "Zen Chime")
                }
            }
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearChatHistory()
            repository.insertMessage(
                ChatMessage(
                    text = "Hello! I am routiner, your mindful companion. Write down daily notes, tick off routines, and ask me to help you stay organized or find things you might have forgotten.",
                    isUser = false
                )
            )
        }
    }

    // ==========================================
    // OFFLINE-FIRST REFLECTION & STREAK ANALYTICS
    // ==========================================
    fun generateDailyReflectionPrompt(routinesList: List<Routine>, notesList: List<DailyNote>) {
        _isPromptLoading.value = true
        viewModelScope.launch {
            try {
                val response = geminiService.generateDailyReflectionQuestion(
                    routines = routinesList.map { it.title },
                    notes = notesList.map { it.content }
                )
                if (response != null && response.isNotBlank() && !response.contains("Error") && !response.contains("Key")) {
                    _dailyReflectionPrompt.value = response
                    _isPromptOnline.value = true
                } else {
                    _dailyReflectionPrompt.value = offlineReflectionPrompts.random()
                    _isPromptOnline.value = false
                }
            } catch (e: Exception) {
                Log.e("AuraViewModel", "Failed to generate reflection prompt", e)
                _dailyReflectionPrompt.value = offlineReflectionPrompts.random()
                _isPromptOnline.value = false
            } finally {
                _isPromptLoading.value = false
            }
        }
    }

    fun calculateRoutineStreak(logs: List<HistoryLog>): Int {
        val routineDoneDates = logs
            .filter { it.eventType == "Routine Done" || it.eventType == "Routine Completed" || it.eventTitle.startsWith("Completed Routine") }
            .map { it.dateStr }
            .toSet()
        
        if (routineDoneDates.isEmpty()) return 0
        
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val cal = Calendar.getInstance()
        val todayStr = sdf.format(cal.time)
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStr = sdf.format(cal.time)
        
        var currentStreak = 0
        var checkDate = if (routineDoneDates.contains(todayStr)) {
            todayStr
        } else if (routineDoneDates.contains(yesterdayStr)) {
            yesterdayStr
        } else {
            return 0
        }
        
        val checkCal = Calendar.getInstance()
        if (checkDate == yesterdayStr) {
            checkCal.add(Calendar.DAY_OF_YEAR, -1)
        }
        
        while (true) {
            val dateStr = sdf.format(checkCal.time)
            if (routineDoneDates.contains(dateStr)) {
                currentStreak++
                checkCal.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return currentStreak
    }

    fun calculateProductivityTrend(logs: List<HistoryLog>, totalRoutines: Int): List<Float> {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val trend = ArrayList<Float>()
        val divisor = maxOf(1, totalRoutines)
        
        // Loop from 6 days ago up to today
        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val dateStr = sdf.format(cal.time)
            
            val completedCount = logs.count {
                (it.eventType == "Routine Done" || it.eventType == "Routine Completed" || it.eventTitle.startsWith("Completed Routine")) && it.dateStr == dateStr
            }
            val score = minOf(1.0f, completedCount.toFloat() / divisor.toFloat())
            trend.add(score)
        }
        return trend
    }
}

class AuraViewModelFactory(
    private val application: Application,
    private val repository: AppRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuraViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuraViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
