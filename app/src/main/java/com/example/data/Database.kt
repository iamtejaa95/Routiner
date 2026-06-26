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
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat

// ==========================================
// 1. ROOM ENTITIES
// ==========================================

@Entity(tableName = "daily_notes")
data class DailyNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val content: String,
    val dateStr: String, // format "YYYY-MM-DD"
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String = "General"
)

@Entity(tableName = "routines")
data class Routine(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val timeStr: String, // format "HH:MM" e.g. "08:30"
    val isEnabled: Boolean = true,
    val lastCompletedDate: String = "", // YYYY-MM-DD if completed today
    val category: String = "General",
    val iconName: String = "Star"
)

@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val message: String,
    val timeMills: Long,
    val isCompleted: Boolean = false,
    val isTriggered: Boolean = false,
    val urgency: String = "Medium", // High, Medium, Low
    val tone: String = "Zen Chime", // Zen Chime, Amber Echo, Espresso Tone, Silent
    val snoozeCount: Int = 0
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "history_logs")
data class HistoryLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val eventTitle: String,
    val eventType: String, // e.g., "Note Added", "Routine Completed", "Reminder Snoozed", "Focus Session"
    val timestamp: Long = System.currentTimeMillis(),
    val dateStr: String // YYYY-MM-DD
)

@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String = "Work Focus",
    val blockedAppsCount: Int = 0
)

// ==========================================
// 2. ROOM DAOS
// ==========================================

@Dao
interface DailyNoteDao {
    @Query("SELECT * FROM daily_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<DailyNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: DailyNote): Long

    @Query("SELECT * FROM daily_notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Int): DailyNote?

    @Query("DELETE FROM daily_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Int)
}

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines ORDER BY timeStr ASC")
    fun getAllRoutines(): Flow<List<Routine>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: Routine): Long

    @Query("SELECT * FROM routines WHERE id = :id LIMIT 1")
    suspend fun getRoutineById(id: Int): Routine?

    @Update
    suspend fun updateRoutine(routine: Routine)

    @Query("DELETE FROM routines WHERE id = :id")
    suspend fun deleteRoutineById(id: Int)
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY timeMills ASC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE isCompleted = 0 AND isTriggered = 0 ORDER BY timeMills ASC")
    fun getActiveReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun getReminderById(id: Int): Reminder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminderById(id: Int)
}

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}

@Dao
interface HistoryLogDao {
    @Query("SELECT * FROM history_logs ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(log: HistoryLog)

    @Query("DELETE FROM history_logs WHERE id = :id")
    suspend fun deleteHistoryById(id: Int)

    @Query("DELETE FROM history_logs")
    suspend fun clearAllHistory()
}

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<FocusSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSession)
}

// ==========================================
// 3. DATABASE HOLDER
// ==========================================

@Database(
    entities = [DailyNote::class, Routine::class, Reminder::class, ChatMessage::class, HistoryLog::class, FocusSession::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dailyNoteDao(): DailyNoteDao
    abstract fun routineDao(): RoutineDao
    abstract fun reminderDao(): ReminderDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun historyLogDao(): HistoryLogDao
    abstract fun focusSessionDao(): FocusSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "routiner_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// ==========================================
// 4. REPOSITORY
// ==========================================

class AppRepository(private val db: AppDatabase) {
    val notes: Flow<List<DailyNote>> = db.dailyNoteDao().getAllNotes()
    val routines: Flow<List<Routine>> = db.routineDao().getAllRoutines()
    val reminders: Flow<List<Reminder>> = db.reminderDao().getAllReminders()
    val chatMessages: Flow<List<ChatMessage>> = db.chatMessageDao().getAllMessages()
    val historyLogs: Flow<List<HistoryLog>> = db.historyLogDao().getAllHistory()
    val focusSessions: Flow<List<FocusSession>> = db.focusSessionDao().getAllSessions()

    suspend fun getNoteById(id: Int) = db.dailyNoteDao().getNoteById(id)
    suspend fun insertNote(note: DailyNote): Long {
        val id = db.dailyNoteDao().insertNote(note)
        insertHistoryLog("Created Note: ${note.content.take(30)}...", "Note Added")
        return id
    }
    suspend fun deleteNoteById(id: Int) {
        val note = db.dailyNoteDao().getNoteById(id)
        if (note != null) {
            insertHistoryLog("Deleted Note: ${note.content.take(30)}...", "Note Deleted")
        }
        db.dailyNoteDao().deleteNoteById(id)
    }

    suspend fun getRoutineById(id: Int) = db.routineDao().getRoutineById(id)
    suspend fun insertRoutine(routine: Routine): Long {
        val id = db.routineDao().insertRoutine(routine)
        insertHistoryLog("Created Routine: ${routine.title}", "Routine Added")
        return id
    }
    suspend fun updateRoutine(routine: Routine) {
        db.routineDao().updateRoutine(routine)
    }
    suspend fun deleteRoutineById(id: Int) {
        val r = db.routineDao().getRoutineById(id)
        if (r != null) {
            insertHistoryLog("Deleted Routine: ${r.title}", "Routine Deleted")
        }
        db.routineDao().deleteRoutineById(id)
    }

    suspend fun getReminderById(id: Int) = db.reminderDao().getReminderById(id)
    suspend fun insertReminder(reminder: Reminder): Long {
        val id = db.reminderDao().insertReminder(reminder)
        insertHistoryLog("Created Reminder: ${reminder.title}", "Reminder Added")
        return id
    }
    suspend fun updateReminder(reminder: Reminder) {
        db.reminderDao().updateReminder(reminder)
    }
    suspend fun deleteReminderById(id: Int) {
        val r = db.reminderDao().getReminderById(id)
        if (r != null) {
            insertHistoryLog("Deleted Reminder: ${r.title}", "Reminder Deleted")
        }
        db.reminderDao().deleteReminderById(id)
    }

    suspend fun insertMessage(message: ChatMessage) = db.chatMessageDao().insertMessage(message)
    suspend fun clearChatHistory() = db.chatMessageDao().clearHistory()

    // History logs logic
    suspend fun insertHistoryLog(title: String, type: String) {
        val today = SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        db.historyLogDao().insertHistory(
            HistoryLog(eventTitle = title, eventType = type, dateStr = today)
        )
    }
    suspend fun deleteHistoryLogById(id: Int) = db.historyLogDao().deleteHistoryById(id)
    suspend fun clearAllHistory() = db.historyLogDao().clearAllHistory()

    // Focus sessions
    suspend fun insertFocusSession(session: FocusSession) {
        db.focusSessionDao().insertSession(session)
        insertHistoryLog("Completed ${session.durationMinutes} min ${session.type}", "Focus Completed")
    }
}
