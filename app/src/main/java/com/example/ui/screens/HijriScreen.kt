package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.EventReminder
import com.example.ui.PrayerTimesViewModel
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.DeepTeal
import com.example.ui.theme.WarmCreame
import com.example.ui.theme.BorderColor
import java.time.LocalDate
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HijriScreen(
    viewModel: PrayerTimesViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf("calendar") } // calendar or events
    
    // Dynamic month selection
    var currentCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    val currentYear = currentCalendar.get(Calendar.YEAR)
    val currentMonth = currentCalendar.get(Calendar.MONTH) // 0-based
    val currentMonthName = currentCalendar.getDisplayName(Calendar.MONTH, Calendar.LONG, java.util.Locale.getDefault()) ?: "May"

    val today = LocalDate.now()
    val todayHijriStr = viewModel.formatHijriDate(today)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hijri Chronology", fontWeight = FontWeight.Bold, color = Color.White) },
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // Header: Today's Date overview card
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepTeal),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "TODAY'S HIJRI DATE",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = todayHijriStr,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(Color.White.copy(alpha = 0.1f), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Hijri Icon",
                            tint = AmberAccent
                        )
                    }
                }
            }

            // Top Segmented Tab Toggle
            TabRow(
                selectedTabIndex = if (activeTab == "calendar") 0 else 1,
                containerColor = Color.White,
                contentColor = DeepTeal,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                    .padding(bottom = 1.dp)
                    .padding(bottom = 16.dp)
            ) {
                Tab(
                    selected = activeTab == "calendar",
                    onClick = { activeTab = "calendar" },
                    modifier = Modifier.testTag("tab_calendar")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Calendar Tab",
                            tint = if (activeTab == "calendar") DeepTeal else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Dual Calendar",
                            fontWeight = FontWeight.Bold,
                            color = if (activeTab == "calendar") DeepTeal else Color.Gray
                        )
                    }
                }

                Tab(
                    selected = activeTab == "events",
                    onClick = { activeTab = "events" },
                    modifier = Modifier.testTag("tab_events")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventNote,
                            contentDescription = "Events Tab",
                            tint = if (activeTab == "events") DeepTeal else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Islamic Events",
                            fontWeight = FontWeight.Bold,
                            color = if (activeTab == "events") DeepTeal else Color.Gray
                        )
                    }
                }
            }

            // Content Area
            if (activeTab == "calendar") {
                // Calendar Controller (Month switching)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        val prev = Calendar.getInstance().apply {
                            time = currentCalendar.time
                            add(Calendar.MONTH, -1)
                        }
                        currentCalendar = prev
                    }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Month", tint = DeepTeal)
                    }

                    Text(
                        text = "$currentMonthName $currentYear".uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = DeepTeal,
                        letterSpacing = 1.sp,
                        fontSize = 16.sp
                    )

                    IconButton(onClick = {
                        val next = Calendar.getInstance().apply {
                            time = currentCalendar.time
                            add(Calendar.MONTH, 1)
                        }
                        currentCalendar = next
                    }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Month", tint = DeepTeal)
                    }
                }

                // Week Day Headers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val days = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
                    days.forEach { d ->
                        Text(
                            text = d,
                            color = Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Month Dual Grid calculation
                val tempCal = Calendar.getInstance().apply {
                    time = currentCalendar.time
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                val startDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday...
                val maxDays = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)

                // Total cells = start padding + total days
                val totalCells = (startDayOfWeek - 1) + maxDays
                val rowPaddedDates = (1..totalCells).map { cellIndex ->
                    val dayNum = cellIndex - (startDayOfWeek - 1)
                    if (dayNum > 0) dayNum else null
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                        .testTag("dual_calendar_grid")
                ) {
                    items(rowPaddedDates) { dayNum ->
                        if (dayNum != null) {
                            val cellDate = LocalDate.of(currentYear, currentMonth + 1, dayNum)
                            val isSelected = cellDate.isEqual(today)
                            val hijriDay = viewModel.getHijriDay(cellDate)

                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) DeepTeal else Color.Transparent
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Color.Transparent else Color.LightGray.copy(alpha = 0.4f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$dayNum",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isSelected) Color.White else Color(0xFF333333)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$hijriDay",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) AmberAccent else DeepTeal
                                    )
                                }
                            }
                        } else {
                            // Blank cell
                            Box(modifier = Modifier.aspectRatio(1f))
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                // Info footer
                Text(
                    text = "* Smaller numbers at bottom represent respective Hijri days.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // Islamic Events List view
                val eventsList by viewModel.islamicEvents.collectAsState()
                
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("events_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(eventsList) { event ->
                        IslamicEventRow(
                            event = event,
                            onToggle = { viewModel.toggleEventReminder(event.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IslamicEventRow(
    event: EventReminder,
    onToggle: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("event_card_${event.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1.5f)
            ) {
                // Colored emblem
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(DeepTeal.copy(alpha = 0.08f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val isFasting = event.title.contains("Fasting", ignoreCase = true) || event.title.contains("Ramadan", ignoreCase = true)
                    Icon(
                        imageVector = if (isFasting) Icons.Default.EventNote else Icons.Default.CalendarMonth,
                        contentDescription = "Event bullet",
                        tint = if (isFasting) AmberAccent else DeepTeal,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = event.title,
                        color = Color(0xFF222222),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = event.hijriDateStr,
                        color = DeepTeal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Right side notification toggle switch
            IconButton(
                onClick = onToggle,
                modifier = Modifier.testTag("event_alarm_${event.id}")
            ) {
                Icon(
                    imageVector = if (event.isEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                    contentDescription = "Notification Alarm switch",
                    tint = if (event.isEnabled) AmberAccent else Color.LightGray,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
