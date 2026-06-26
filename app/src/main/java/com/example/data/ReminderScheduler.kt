package com.example.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class ReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(reminder: Reminder) {
        if (reminder.timeMills <= System.currentTimeMillis()) {
            Log.w("ReminderScheduler", "Cannot schedule reminder in the past: ID ${reminder.id}")
            return
        }

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.example.ACTION_TRIGGER_REMINDER"
            putExtra("reminder_id", reminder.id)
            putExtra("reminder_title", reminder.title)
            putExtra("reminder_message", reminder.message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminder.timeMills,
                        pendingIntent
                    )
                    Log.d("ReminderScheduler", "Scheduled exact alarm for ID ${reminder.id} at ${reminder.timeMills}")
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        reminder.timeMills,
                        pendingIntent
                    )
                    Log.d("ReminderScheduler", "Scheduled inexact alarm (exact disallowed) for ID ${reminder.id}")
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminder.timeMills,
                    pendingIntent
                )
                Log.d("ReminderScheduler", "Scheduled exact alarm (pre-S) for ID ${reminder.id}")
            }
        } catch (e: Exception) {
            Log.e("ReminderScheduler", "Failed to schedule alarm", e)
            // Ultimate fallback
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                reminder.timeMills,
                pendingIntent
            )
        }
    }

    fun cancel(reminderId: Int) {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = "com.example.ACTION_TRIGGER_REMINDER"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("ReminderScheduler", "Cancelled alarm for ID $reminderId")
        }
    }
}
