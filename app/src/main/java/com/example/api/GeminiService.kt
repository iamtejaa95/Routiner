package com.example.api

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// ==========================================
// 1. GEMINI API DATA STRUCTURES (MOSHI)
// ==========================================

data class Part(
    @Json(name = "text") val text: String? = null
)

data class Content(
    @Json(name = "parts") val parts: List<Part>
)

data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

data class Candidate(
    @Json(name = "content") val content: Content
)

data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>?
)

// ==========================================
// 2. RETROFIT SERVICE
// ==========================================

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

// ==========================================
// 3. SERVICE WRAPPER WITH PROACTIVE PROMPTING
// ==========================================

class GeminiService {

    companion object {
        var customApiKey: String? = null
    }

    private fun getApiKey(): String {
        val custom = customApiKey
        if (!custom.isNullOrBlank() && custom != "MY_GEMINI_API_KEY") {
            return custom
        }
        return BuildConfig.GEMINI_API_KEY
    }

    private fun generateLocalOfflineResponse(
        userMessage: String,
        todayDate: String,
        notesList: List<String>,
        routinesList: List<String>,
        remindersList: List<String>
    ): String {
        val msgLower = userMessage.lowercase()

        // 1. Add Routine Detection
        if (msgLower.contains("add routine") || msgLower.contains("add habit") || msgLower.contains("create routine") || msgLower.contains("new habit")) {
            val title = userMessage.replace(Regex("(?i)add routine|add habit|create routine|new habit|at|\\d{2}:\\d{2}"), "").trim().trim { !it.isLetterOrDigit() }
            val time = Regex("\\d{2}:\\d{2}").find(userMessage)?.value ?: "08:00"
            val cleanTitle = if (title.isBlank()) "New Habit" else title
            return """
                **AURA COMPANION • SECURE OFFLINE REGISTRY**
                
                I have registered your new daily routine habit: **$cleanTitle** scheduled at **$time**. 
                
                Adding consistent time-anchored habits is a powerful strategy for structural neuroplasticity.
                
                [ADD_ROUTINE: $cleanTitle, $time]
            """.trimIndent()
        }

        // 2. Add Note Detection
        if (msgLower.contains("add note") || msgLower.contains("log note") || msgLower.contains("write down") || msgLower.contains("log thought")) {
            val noteContent = userMessage.replace(Regex("(?i)add note|log note|write down|log thought"), "").trim().trim { !it.isLetterOrDigit() }
            val cleanNote = if (noteContent.isBlank()) "Mindful reflection logged." else noteContent
            return """
                **AURA COMPANION • OFFLINE MIND DUMP**
                
                Captured your reflection to keep your mental workspace clear and focused:
                
                *"$cleanNote"*
                
                [ADD_NOTE: $cleanNote]
            """.trimIndent()
        }

        // 3. Add Reminder Detection
        if (msgLower.contains("reminder") || msgLower.contains("set reminder") || msgLower.contains("alert") || msgLower.contains("alarm")) {
            val title = userMessage.replace(Regex("(?i)set reminder|reminder|alert|alarm|at|\\d{2}:\\d{2}"), "").trim().trim { !it.isLetterOrDigit() }
            val time = Regex("\\d{2}:\\d{2}").find(userMessage)?.value ?: "12:00"
            val cleanTitle = if (title.isBlank()) "Mindful Reminder" else title
            return """
                **AURA COMPANION • OFFLINE TIME ANCHOR**
                
                I have configured an active system notification reminder for **$cleanTitle** at **$time**.
                
                [ADD_REMINDER: $cleanTitle, $time, Time to check in with Aura]
            """.trimIndent()
        }

        // 4. Routines status check
        if (msgLower.contains("routine") || msgLower.contains("habit") || msgLower.contains("forget") || msgLower.contains("left") || msgLower.contains("pending") || msgLower.contains("status")) {
            val completed = routinesList.filter { it.contains("[Completed Today]") || it.contains("completed") }
            val pending = routinesList.filter { !it.contains("[Completed Today]") && !it.contains("completed") }
            
            val compText = if (completed.isEmpty()) "• No routines completed today." else completed.joinToString("\n") { "• " + it.replace(" - [Completed Today]", "") }
            val pendText = if (pending.isEmpty()) "• All daily routines completed! Excellent work." else pending.joinToString("\n") { "• " + it.replace(" - [Pending]", "") }
            
            return """
                **AURA OFFLINE INTEL • HYBRID HABIT MATRIX**
                
                Here is your secure, localized routine status for **$todayDate**:
                
                **Completed Today:**
                $compText
                
                **Remaining Focus Anchors:**
                $pendText
                
                *Remember: Excellence is not an act, but a habit. Consistent tiny steps yield compounding long-term results.*
            """.trimIndent()
        }

        // 5. Default conversational chat
        val notesContext = if (notesList.isNotEmpty()) {
            "\n\n**Based on your secure local reflections today:**\n" + notesList.take(3).joinToString("\n") { "• " + it.substringBefore(" (") }
        } else ""

        return """
            **AURA COMPANION • SECURE HYBRID INTELLIGENCE**
            
            Greetings. I am running in local-cognitive mode because external API services are optional. I am completely online, operational, and keeping your mind clear.
            
            How is your focus shaping up today? You can:
            • Ask me "**What routines do I have left?**"
            • Ask me to "**add routine [Name] at [HH:MM]**"
            • Ask me to "**add note [Reflection]**"
            • Chat with me about your goals, habits, and self-discipline.$notesContext
        """.trimIndent()
    }

    private fun generateLocalOfflineWorryOrMistake(text: String, isWorry: Boolean): String {
        return if (isWorry) {
            """
            **AURA COGNITIVE ANCHOR • SECURE OFFLINE STOIC GUIDE**
            
            I have analyzed your worry: "${text.trim()}"
            
            • **Within Your Control**: Your current reaction, your focus, and the deliberate choice to frame this hurdle as a training ground for resilience.
            • **Outside Your Control**: The future outcomes, past events, and external reactions.
            
            **Mindful Action Step**: Write down a single, minute action step you can execute in the next 120 seconds to anchor yourself in the present.
            """.trimIndent()
        } else {
            """
            **AURA BEHAVIORAL ANALYTICS • SECURE OFFLINE ACTION PLAN**
            
            I have mapped your logged behavior: "${text.trim()}"
            
            • **The Behavioral Loop**: The human brain defaults to the path of least resistance during times of fatigue or lack of friction.
            • **Actionable Hack 1**: Add positive friction. Keep the source of distraction at least two rooms away.
            • **Actionable Hack 2**: Commit to a 5-minute pre-commitment contract. Start the constructive task for just 300 seconds.
            
            Every recognized mistake is a feedback loop guiding you toward ironclad self-discipline.
            """.trimIndent()
        }
    }

    private val offlineReflectionPrompts = listOf(
        "What is the single most important task you are putting off today, and why?",
        "If you had only 4 hours of focused work today, what would you prioritize?",
        "What habit triggered yesterday's highest energy levels, and how can you repeat it?",
        "Where did you notice a friction point in your routine yesterday?",
        "How can you make your most important habit 1% easier to perform today?",
        "What unproductive trigger was most active today, and how can we neutralize it?"
    )

    suspend fun askAura(
        userMessage: String,
        todayDate: String,
        notesList: List<String>,
        routinesList: List<String>,
        remindersList: List<String>
    ): String {
        val apiKey = getApiKey()
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return generateLocalOfflineResponse(userMessage, todayDate, notesList, routinesList, remindersList)
        }

        // 1. Build context strings
        val notesCtx = if (notesList.isEmpty()) "• No notes logged for today." else notesList.joinToString("\n") { "• $it" }
        val routinesCtx = if (routinesList.isEmpty()) "• No routines configured." else routinesList.joinToString("\n") { "• $it" }
        val remindersCtx = if (remindersList.isEmpty()) "• No active reminders scheduled." else remindersList.joinToString("\n") { "• $it" }

        // 2. Formulate the dynamic prompt
        val prompt = """
            You are Aura, an elegant, proactive daily routine and note advisor with a sleek Apple-style personality.
            The user wants to track routines, notes, and reminders easily.
            Today is $todayDate.
            
            USER CURRENT CONTEXT:
            --- TODAY'S DAILY NOTES ---
            $notesCtx
            
            --- DAILY ROUTINES (HABITS) ---
            $routinesCtx
            
            --- ACTIVE REMINDERS ---
            $remindersCtx
            
            USER INQUIRY:
            "$userMessage"
            
            INSTRUCTIONS:
            - Respond in a warm, clean, minimalist iOS design voice. Be brief, professional, and elegant.
            - Focus on helping the user stay organized. If they ask "What did I forget?" or "What's left?", check their routines context and identify any routine that is NOT completed (e.g., routines showing as "[Pending]" or containing "last completed: none" or not matching today's date).
            - Support adding things on the fly! If they ask you to write down a note, add a routine, or set an alarm, acknowledge it with visual refinement.
            - CRITICAL: At the very end of your response, if you decided to help them add something, append a structured command so the app can create it in their database. Use EXACTLY these formats (on a new line):
              * To add a Note: `[ADD_NOTE: contents]`
              * To add a Routine: `[ADD_ROUTINE: title, HH:MM]` (24-hour format, e.g. `[ADD_ROUTINE: Meditate, 07:15]`)
              * To add a Reminder: `[ADD_REMINDER: title, HH:MM, message]` (e.g. `[ADD_REMINDER: Take medicine, 21:00, Remember to take your daily vitamins]`)
            - If they did not ask to add anything, do NOT output any `[ADD_...]` tags.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = "You are Aura, an elite Apple-style routines advisor. Talk elegantly, concisely, and with premium composure. Use bullet points and clean structure."))
            )
        )

        return try {
            val response = GeminiClient.service.generateContent(apiKey, request)
            val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            replyText ?: generateLocalOfflineResponse(userMessage, todayDate, notesList, routinesList, remindersList)
        } catch (e: Exception) {
            Log.e("GeminiService", "API Call failed, falling back to secure offline AI response", e)
            generateLocalOfflineResponse(userMessage, todayDate, notesList, routinesList, remindersList)
        }
    }

    suspend fun analyzeWorryOrMistake(text: String, isWorry: Boolean): String {
        val apiKey = getApiKey()
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return generateLocalOfflineWorryOrMistake(text, isWorry)
        }

        val prompt = if (isWorry) {
            """
            You are a wise, comforting Stoic Philosopher and compassionate guide.
            The user is sharing a private worry/anxiety with you:
            "$text"
            
            Deliver a profound, deeply supportive, and therapeutic response.
            1. Apply Stoic principles: Help them separate what is in their control (their thoughts, actions, choices) from what is not (the past, future, actions of others).
            2. Reframe the situation with wisdom: Encourage them to see this worry as a challenge they can grow from.
            3. End with a single, highly actionable step they can take *right now* to reclaim their calm and focus.
            Keep it brief, comforting, elegant, and structured with clean bullet points.
            """.trimIndent()
        } else {
            """
            You are an elite, supportive behavioral scientist and self-discipline coach.
            The user is logging a personal mistake/unproductive behavior they committed:
            "$text"
            
            Analyze this mistake deeply but constructively.
            1. Deconstruct the trigger: Explain why we often make this specific mistake (e.g. lack of friction, instant gratification trap, decision fatigue).
            2. Share the "lesson learned": Formulate the underlying wisdom of this mistake in a memorable, empowering rule.
            3. Provide two highly practical, concrete hacks to prevent this mistake from happening again (e.g., adding friction, setting pre-commitments).
            Keep it concise, professional, empowering, and styled like an elite Apple product design advice card.
            """.trimIndent()
        }

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = "You are Aura, a supportive daily guide. Speak concisely, elegantly, and with premium composure."))
            )
        )

        return try {
            val response = GeminiClient.service.generateContent(apiKey, request)
            val replyText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            replyText ?: generateLocalOfflineWorryOrMistake(text, isWorry)
        } catch (e: Exception) {
            Log.e("GeminiService", "API Call failed, falling back to secure offline worry/mistake response", e)
            generateLocalOfflineWorryOrMistake(text, isWorry)
        }
    }

    suspend fun generateDailyReflectionQuestion(routines: List<String>, notes: List<String>): String? {
        val apiKey = getApiKey()
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return offlineReflectionPrompts.random()
        }

        val prompt = """
            You are Aura, a mindful lifestyle architect and self-actualization coach.
            The user is tracking the following routines:
            ${if (routines.isEmpty()) "[No custom routines configured yet]" else routines.joinToString(", ")}
            
            They have also logged the following thoughts/notes recently:
            ${if (notes.isEmpty()) "[No recent notes logged]" else notes.take(3).joinToString("\n- ")}
            
            Task: Ask ONE deeply personalized, highly engaging, and mindful self-reflection question to start their day.
            - Relate it directly to their routines or notes if possible.
            - Focus on self-mastery, focus, or emotional alignment.
            - Ensure it is exactly ONE short, punchy sentence (maximum 16 words).
            - Do not include any introductory remarks, markdown formatting, or labels. Return ONLY the question.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(parts = listOf(Part(text = prompt)))
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = "You are Aura. You write exactly one elegant, short reflection question."))
            )
        )

        return try {
            val response = GeminiClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
                ?: offlineReflectionPrompts.random()
        } catch (e: Exception) {
            Log.e("GeminiService", "Prompt API Call failed, returning offline reflection prompt", e)
            offlineReflectionPrompts.random()
        }
    }
}
