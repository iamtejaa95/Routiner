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

    suspend fun askAura(
        userMessage: String,
        todayDate: String,
        notesList: List<String>,
        routinesList: List<String>,
        remindersList: List<String>
    ): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Aura requires a valid Gemini API Key. Please configure your key in the AI Studio Secrets panel."
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
            replyText ?: "Aura is resting. Please try again in a moment."
        } catch (e: Exception) {
            Log.e("GeminiService", "API Call failed", e)
            "Aura is currently offline. Please ensure internet access or verify your Gemini API key: ${e.localizedMessage}"
        }
    }

    suspend fun analyzeWorryOrMistake(text: String, isWorry: Boolean): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Aura requires a valid Gemini API Key. Please configure your key in the AI Studio Secrets panel."
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
            replyText ?: "Aura is resting. Please try again in a moment."
        } catch (e: Exception) {
            Log.e("GeminiService", "API Call failed", e)
            "Could not connect to Aura. Please ensure internet access or verify your Gemini API key."
        }
    }

    suspend fun generateDailyReflectionQuestion(routines: List<String>, notes: List<String>): String? {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return null
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
        } catch (e: Exception) {
            Log.e("GeminiService", "Prompt API Call failed", e)
            null
        }
    }
}
