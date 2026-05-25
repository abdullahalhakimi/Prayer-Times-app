package com.example.ui.screens

import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AgendaReminder
import com.example.data.PrayerTimeItem
import com.example.ui.PrayerTimesViewModel
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.DeepTeal
import com.example.ui.theme.WarmCreame
import com.example.ui.theme.BorderColor
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    viewModel: PrayerTimesViewModel,
    modifier: Modifier = Modifier
) {
    val remindersList by viewModel.agendaReminders.collectAsState()
    val currentPrayerTimes by viewModel.prayerTimes.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agenda & Alarms", fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepTeal)
            )
        },
        containerColor = WarmCreame,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            
            // Header Description card
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepTeal.copy(alpha = 0.06f)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DeepTeal.copy(alpha = 0.12f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info alert description",
                        tint = DeepTeal,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Programmatic alarms invoke AlarmManager's exact background loops to trigger even while your device is sleeping or idle.",
                        fontSize = 12.sp,
                        color = Color(0xFF444444),
                        lineHeight = 16.sp
                    )
                }
            }

            Text(
                text = "ACTIVE ALARMS & VOLUNTARY SCHEDULES",
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
            )

            // Scrollable Agenda Item List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("agenda_list"),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(remindersList) { reminder ->
                    val calculatedTime = getReminderFormattedTime(reminder, currentPrayerTimes)
                    
                    AgendaReminderRow(
                        reminder = reminder,
                        formattedExecutionTime = calculatedTime,
                        onToggle = { viewModel.toggleAgendaReminder(reminder.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AgendaReminderRow(
    reminder: AgendaReminder,
    formattedExecutionTime: String,
    onToggle: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("reminder_card_${reminder.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(2f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Icon sphere
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(
                            if (reminder.isEnabled) DeepTeal.copy(alpha = 0.08f) else Color.LightGray.copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (reminder.isEnabled) Icons.Default.NotificationsActive else Icons.Default.Alarm,
                        contentDescription = "Alarm state icon",
                        tint = if (reminder.isEnabled) AmberAccent else Color.Gray,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = reminder.title,
                        color = Color(0xFF222222),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = reminder.description,
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Display execution trigger context e.g. 40 minutes before Fajr
                    val relativeMinutesAbs = Math.abs(reminder.relativeMinutes)
                    val relWord = if (reminder.relativeMinutes < 0) "before" else "after"
                    Text(
                        text = "$relativeMinutesAbs min $relWord ${reminder.basePrayerName}",
                        color = DeepTeal,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Right side: calculated execution time and switch toggle
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = formattedExecutionTime,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (reminder.isEnabled) DeepTeal else Color.LightGray
                )
                Spacer(modifier = Modifier.height(6.dp))
                Switch(
                    checked = reminder.isEnabled,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = AmberAccent,
                        checkedTrackColor = DeepTeal,
                        uncheckedThumbColor = Color.LightGray,
                        uncheckedTrackColor = Color.LightGray.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier
                        .scaleUniform(0.85f)
                        .testTag("reminder_switch_${reminder.id}")
                )
            }
        }
    }
}

/**
 * Modifier helper to scale items evenly in Compose
 */
@Composable
fun Modifier.scaleUniform(scale: Float): Modifier = this.then(
    Modifier.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
)

fun getReminderFormattedTime(reminder: AgendaReminder, prayerTimes: List<PrayerTimeItem>): String {
    val baseTime = prayerTimes.find { it.name.equals(reminder.basePrayerName, ignoreCase = true) }?.date
    if (baseTime != null) {
        val calendar = Calendar.getInstance().apply { time = baseTime }
        calendar.add(Calendar.MINUTE, reminder.relativeMinutes)
        
        // Return structured weekday and simple AM/PM formatted output
        val df = java.text.SimpleDateFormat("EEE, h:mm a", java.util.Locale.getDefault())
        return df.format(calendar.time)
    }
    return "--:--"
}
