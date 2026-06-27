package com.example.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("ReminderReceiver", "Broadcast received: $action")

        if (action == "com.example.ACTION_COMPLETE_ROUTINE") {
            val routineId = intent.getIntExtra("routine_id", -1)
            Log.d("ReminderReceiver", "ACTION_COMPLETE_ROUTINE for id: $routineId")
            if (routineId != -1) {
                val db = AppDatabase.getDatabase(context)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val dao = db.routineDao()
                        val repo = AppRepository(db)
                        val routine = dao.getRoutineById(routineId)
                        if (routine != null) {
                            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            val updated = routine.copy(lastCompletedDate = today)
                            dao.updateRoutine(updated)
                            repo.insertHistoryLog("Completed Routine via Notification: ${routine.title}", "Routine Done")
                        }
                        cancelRoutineNotification(context, routineId)
                    } catch (e: Exception) {
                        Log.e("ReminderReceiver", "Error completing routine from notification", e)
                    }
                }
            }
            return
        }

        if (action == "com.example.ACTION_DISMISS_ROUTINE") {
            val routineId = intent.getIntExtra("routine_id", -1)
            Log.d("ReminderReceiver", "ACTION_DISMISS_ROUTINE for id: $routineId")
            if (routineId != -1) {
                cancelRoutineNotification(context, routineId)
            }
            return
        }

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

        // Try to play ringtone or alarm audio immediately (bypassing silent mode for high urgency)
        try {
            val soundUri: Uri = when (toneName) {
                "Zen Chime" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                "Amber Echo" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                "Espresso Tone" -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                "Silent" -> Uri.EMPTY
                else -> {
                    try {
                        Uri.parse(toneName)
                    } catch (e: Exception) {
                        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    }
                }
            }

            if (soundUri != Uri.EMPTY) {
                val isHighUrgency = reminder.urgency.equals("High", ignoreCase = true)
                try {
                    val mediaPlayer = MediaPlayer().apply {
                        setDataSource(context, soundUri)
                        val audioAttributes = AudioAttributes.Builder()
                            .setUsage(if (isHighUrgency) AudioAttributes.USAGE_ALARM else AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                        setAudioAttributes(audioAttributes)
                        if (isHighUrgency) {
                            setVolume(1.0f, 1.0f) // Max volume
                        }
                        prepare()
                        start()
                    }
                    mediaPlayer.setOnCompletionListener { mp ->
                        try { mp.release() } catch (e: Exception) {}
                    }
                    Log.d("ReminderReceiver", "Played sound via MediaPlayer with high-urgency=$isHighUrgency")
                } catch (mpEx: Exception) {
                    Log.e("ReminderReceiver", "MediaPlayer failed, falling back to RingtoneManager", mpEx)
                    val ringtone = RingtoneManager.getRingtone(context, soundUri)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && isHighUrgency) {
                        ringtone.audioAttributes = AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .build()
                    }
                    ringtone?.play()
                }
            }
        } catch (e: Exception) {
            Log.e("ReminderReceiver", "Could not play alarm sound", e)
        }
    }

    companion object {
        fun showOngoingRoutineNotification(context: Context, routineId: Int, title: String, timeStr: String) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "ongoing_routines_channel"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = "Ongoing Habits & Tasks"
                val channel = NotificationChannel(
                    channelId,
                    name,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Persistent tasks that keep you productive and focused."
                }
                notificationManager.createNotificationChannel(channel)
            }

            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                routineId + 100000,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Complete action broadcast
            val completeIntent = Intent(context, ReminderReceiver::class.java).apply {
                action = "com.example.ACTION_COMPLETE_ROUTINE"
                putExtra("routine_id", routineId)
            }
            val completePendingIntent = PendingIntent.getBroadcast(
                context,
                routineId + 200000,
                completeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Dismiss action broadcast
            val dismissIntent = Intent(context, ReminderReceiver::class.java).apply {
                action = "com.example.ACTION_DISMISS_ROUTINE"
                putExtra("routine_id", routineId)
            }
            val dismissPendingIntent = PendingIntent.getBroadcast(
                context,
                routineId + 300000,
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("📌 Task Pending: $title")
                .setContentText("Scheduled time: $timeStr | Keep your streak alive!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setOngoing(true) // CANNOT BE SWIPED AWAY
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.checkbox_on_background, "Mark Done", completePendingIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", dismissPendingIntent)
                .build()

            notificationManager.notify(routineId + 100000, notification)
        }

        fun cancelRoutineNotification(context: Context, routineId: Int) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(routineId + 100000)
        }
    }
}
