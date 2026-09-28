package com.prayertimesApp.data

data class AgendaReminder(
    val id: String,
    val title: String,
    val description: String,
    val relativeMinutes: Int, // e.g., -40 for 40 minutes before, +15 for 15 minutes after
    val basePrayerName: String, // e.g., "Fajr", "Maghrib"
    val isEnabled: Boolean
)
