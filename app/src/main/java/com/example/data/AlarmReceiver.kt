package com.example.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import android.util.Log

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Prayer Reminder"
        val message = "It is time for your scheduled reminder."
        Log.d("AlarmReceiver", "Alarm received: $title")
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "prayer_reminders_channel"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Prayer Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Triggered alarms and prayer times alerts"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
            
        try {
            notificationManager.notify(101, notification)
        } catch (e: SecurityException) {
            Log.e("AlarmReceiver", "Failed to post notification due to permission check", e)
        }
    }
}
