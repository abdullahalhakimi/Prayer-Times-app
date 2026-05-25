package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PrayerTimeItem
import com.example.ui.NotificationType
import com.example.ui.PrayerTimesViewModel
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.DeepTeal
import com.example.ui.theme.WarmCreame
import com.example.ui.theme.ActivePrayerBg
import com.example.ui.theme.BorderColor

@Composable
fun PrayersScreen(
    viewModel: PrayerTimesViewModel,
    modifier: Modifier = Modifier
) {
    val locationName by viewModel.currentLocationName.collectAsState()
    val upcomingPrayer by viewModel.upcomingPrayerName.collectAsState()
    val upcomingTime by viewModel.upcomingPrayerTimeStr.collectAsState()
    val countdown by viewModel.countdownStr.collectAsState()
    val prayerTimes by viewModel.prayerTimes.collectAsState()
    val notificationSettings by viewModel.prayerNotifications.collectAsState()
    val isDayTime by viewModel.isDayTime.collectAsState()

    // Rotating pulse animation for sun/moon graphic
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WarmCreame)
    ) {
        // Top Section: Dark Teal Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.8f)
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DeepTeal, Color(0xFF132F2F))
                    )
                )
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Location header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = AmberAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = locationName.uppercase(),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }

                // Sun / Moon graphic and bold upcoming name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "UPCOMING PRAYER",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = upcomingPrayer,
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Starts at $upcomingTime",
                            color = AmberAccent,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Rotating Sun / Glowing Moon Box
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color.White.copy(alpha = 0.08f), shape = CircleShape)
                            .rotate(rotationAngle)
                    ) {
                        Icon(
                            imageVector = if (isDayTime) Icons.Default.LightMode else Icons.Default.NightsStay,
                            contentDescription = "Visual indication indicator",
                            tint = if (isDayTime) AmberAccent else Color(0xFFE2F0D9),
                            modifier = Modifier.size(54.dp)
                        )
                    }
                }

                // Countdown Timer Box
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = "Countdown Timer",
                                tint = Color.LightGray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Time remaining:",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = countdown,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.testTag("countdown_label")
                        )
                    }
                }
            }
        }

        // Bottom Section: Scrollable Prayer Times List
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2.2f)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(prayerTimes) { prayer ->
                    val notificationType = notificationSettings[prayer.name] ?: NotificationType.SILENT
                    val isActive = upcomingPrayer == prayer.name.uppercase()

                    PrayerTimeItemRow(
                        prayer = prayer,
                        notificationType = notificationType,
                        isActive = isActive,
                        onToggleNotification = { viewModel.togglePrayerNotification(prayer.name) }
                    )
                }
            }
        }
    }
}

@Composable
fun PrayerTimeItemRow(
    prayer: PrayerTimeItem,
    notificationType: NotificationType,
    isActive: Boolean,
    onToggleNotification: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) ActivePrayerBg else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isActive) 0.dp else 1.dp
        ),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isActive) DeepTeal.copy(alpha = 0.2f) else BorderColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("prayer_card_${prayer.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1.5f)
            ) {
                // Rounded Colored indicator dots
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = if (isActive) AmberAccent else DeepTeal,
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = prayer.name,
                            color = DeepTeal,
                            fontSize = 16.sp,
                            fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold
                        )
                        if (isActive) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(DeepTeal, shape = RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "CURRENT",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                    // Display category labels or sun/prayer descriptions
                    if (prayer.name == "Sunrise") {
                        Text("Shuruq", color = if (isActive) DeepTeal.copy(alpha = 0.7f) else Color.Gray, fontSize = 11.sp)
                    } else if (prayer.name == "Qiyam") {
                        Text("Last third of night", color = if (isActive) DeepTeal.copy(alpha = 0.7f) else Color.Gray, fontSize = 11.sp)
                    } else {
                        val desc = when(prayer.name) {
                            "Fajr" -> "Dawn Prayer"
                            "Dhuhr" -> "Noon Prayer"
                            "Asr" -> "Afternoon Prayer"
                            "Maghrib" -> "Sunset Prayer"
                            "Isha" -> "Night Prayer"
                            else -> null
                        }
                        if (desc != null) {
                            Text(desc, color = if (isActive) DeepTeal.copy(alpha = 0.7f) else Color.Gray, fontSize = 11.sp)
                        }
                    }
                }
            }

            Text(
                text = prayer.formattedTime,
                color = DeepTeal,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Notification Bell Toggle button
            FilledIconButton(
                onClick = onToggleNotification,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = when (notificationType) {
                        NotificationType.SILENT -> Color.LightGray.copy(alpha = 0.25f)
                        NotificationType.BEEP -> AmberAccent.copy(alpha = 0.15f)
                        NotificationType.ADHAN -> DeepTeal.copy(alpha = 0.1f)
                    }
                ),
                modifier = Modifier
                    .size(40.dp)
                    .testTag("bell_${prayer.name.lowercase()}")
            ) {
                val icon = when (notificationType) {
                    NotificationType.SILENT -> Icons.Default.NotificationsOff
                    NotificationType.BEEP -> Icons.Default.Notifications
                    NotificationType.ADHAN -> Icons.Default.NotificationsActive
                }
                
                val tintColor = when (notificationType) {
                    NotificationType.SILENT -> Color.Gray
                    NotificationType.BEEP -> AmberAccent
                    NotificationType.ADHAN -> DeepTeal
                }

                Icon(
                    imageVector = icon,
                    contentDescription = "Toggle Bell Notification type",
                    tint = tintColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
