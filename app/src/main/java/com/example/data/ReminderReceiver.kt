package com.example.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("ReminderReceiver", "Broadcast received: $action")

        if (action == "com.example.ACTION_TRIGGER_REMINDER" || action == Intent.ACTION_BOOT_COMPLETED) {
            val reminderId = intent.getIntExtra("reminder_id", -1)
            val title = intent.getStringExtra("reminder_title") ?: "routiner reminder"
            val message = intent.getStringExtra("reminder_message") ?: "Time for your scheduled routine!"

            if (reminderId != -1) {
                // Update database state first
                val db = AppDatabase.getDatabase(context)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val dao = db.reminderDao()
                        val repo = AppRepository(db)
                        val reminder = dao.getReminderById(reminderId)
                        if (reminder != null) {
                            val updated = reminder.copy(isTriggered = true)
                            dao.updateReminder(updated)
                            repo.insertHistoryLog("Triggered Reminder: ${reminder.title}", "Reminder Alarm")
                            
                            // Trigger notification with selected tone
                            showNotification(context, reminder, updated.tone)
                        } else {
                            // Fallback
                            showNotification(context, Reminder(id = reminderId, title = title, message = message, timeMills = System.currentTimeMillis()), "Zen Chime")
                        }
                    } catch (e: Exception) {
                        Log.e("ReminderReceiver", "Error updating database or showing notification", e)
                    }
                }
            }

            if (action == Intent.ACTION_BOOT_COMPLETED) {
                Log.d("ReminderReceiver", "Boot completed, restoring alarms...")
                val db = AppDatabase.getDatabase(context)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        db.reminderDao().getActiveReminders().collect { reminders ->
                            val scheduler = ReminderScheduler(context)
                            for (r in reminders) {
                                if (r.timeMills > System.currentTimeMillis()) {
                                    scheduler.schedule(r)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ReminderReceiver", "Error restoring alarms", e)
                    }
                }
            }
        }
    }

    private fun showNotification(context: Context, reminder: Reminder, toneName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "routiner_reminders_channel_${toneName.replace(" ", "_")}"

        // Create specific notification channel for the selected premium tone
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "routiner ($toneName)"
            val channel = NotificationChannel(
                channelId,
                name,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminds you about your habits using $toneName tone."
                enableVibration(true)
                
                // Set default system sound as fallback, or custom sounds
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                setSound(soundUri, null)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to_reminders", true)
            putExtra("reminder_id", reminder.id)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            reminder.id,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Generate specific tone text display
        val contentMessage = if (toneName != "Silent") {
            "${reminder.message} [🔔 Play $toneName]"
        } else {
            reminder.message
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(reminder.title)
            .setContentText(contentMessage)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(reminder.id, notification)

        // Try to play ringtone preview audio immediately
        try {
            val soundUri: Uri = when (toneName) {
                "Zen Chime" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                "Amber Echo" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                "Espresso Tone" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                "Silent" -> Uri.EMPTY
                else -> {
                    // Try parsing as custom Uri
                    try {
                        Uri.parse(toneName)
                    } catch (e: Exception) {
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    }
                }
            }

            if (soundUri != Uri.EMPTY) {
                val ringtone = RingtoneManager.getRingtone(context, soundUri)
                ringtone?.play()
            }
        } catch (e: Exception) {
            Log.e("ReminderReceiver", "Could not play Ringtone audio", e)
        }
    }
}
