package com.example.data

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class SuhoorAlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleAlarm(triggerTimeMillis: Long, reminderTitle: String) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("title", reminderTitle)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                } else {
                    // Fallback to non-exact scheduling to prevent crash if permission not granted
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            }
            Log.d("SuhoorAlarmScheduler", "Alarm scheduled for timestamp: $triggerTimeMillis")
        } catch (e: SecurityException) {
            Log.e("SuhoorAlarmScheduler", "Could not schedule exact alarm due to security policy", e)
            // Final fallback to inexact in case of exception
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMillis,
                pendingIntent
            )
        }
    }

    fun cancelAlarm() {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1001,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            Log.d("SuhoorAlarmScheduler", "Cancelled previous scheduled alarm")
        }
    }
}
