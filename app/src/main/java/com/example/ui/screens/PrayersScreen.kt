package com.prayertimesApp.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.res.stringResource
import com.prayertimesApp.R
import com.prayertimesApp.data.PrayerTimeItem
import com.prayertimesApp.ui.NotificationType
import com.prayertimesApp.ui.PrayerTimesViewModel
import com.prayertimesApp.ui.theme.ActivePrayerBg
import com.prayertimesApp.ui.theme.AmberAccent
import com.prayertimesApp.ui.theme.BorderColor
import com.prayertimesApp.ui.theme.DeepTeal
import com.prayertimesApp.ui.theme.WarmCreame

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayersScreen(
    modifier: Modifier = Modifier,
    viewModel: PrayerTimesViewModel,
    onNavigateToLocations: () -> Unit = {},
) {
    val locationName by viewModel.currentLocationName.collectAsState()
    val upcomingPrayer by viewModel.upcomingPrayerName.collectAsState()
    val upcomingTime by viewModel.upcomingPrayerTimeStr.collectAsState()
    val countdown by viewModel.countdownStr.collectAsState()
    val prayerTimes by viewModel.prayerTimes.collectAsState()
    val notificationSettings by viewModel.prayerNotifications.collectAsState()
    val isDayTime by viewModel.isDayTime.collectAsState()
    val sourceLabel by viewModel.sourceLabel.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hasLocations by viewModel.hasLocations.collectAsState()
    val locationsList by viewModel.locationsList.collectAsState()
    var showLocationSheet by remember { mutableStateOf(false) }

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
                .weight(1.2f)
                .clip(shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(DeepTeal, Color(0xFF132F2F))
                    )
                )
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(5.dp))
                // Location header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLocationSheet = true },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start,
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = stringResource(R.string.location_description),
                        tint = AmberAccent,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = locationName.uppercase(),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
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
                            text = stringResource(R.string.upcoming_prayer_label),
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
                            text = stringResource(R.string.starts_at, upcomingTime),
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
                            contentDescription = stringResource(R.string.visual_indicator_description),
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
                                contentDescription = stringResource(R.string.countdown_timer_description),
                                tint = Color.LightGray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.time_remaining_label),
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
            if (prayerTimes.isEmpty() && !hasLocations) {
                // No locations set — show placeholder
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = DeepTeal.copy(alpha = 0.3f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.no_location_title),
                            color = DeepTeal,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.no_location_subtitle),
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
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
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp, bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Source: $sourceLabel",
                                color = DeepTeal.copy(alpha = 0.55f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            IconButton(
                                onClick = { viewModel.forceRefresh() },
                                enabled = !isLoading
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh prayer times",
                                    tint = DeepTeal.copy(alpha = if (isLoading) 0.3f else 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLocationSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLocationSheet = false },
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Switch Location",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepTeal
                )
                Spacer(modifier = Modifier.height(16.dp))

                locationsList.forEach { config ->
                    val isSelected = config.name.equals(locationName, ignoreCase = true)
                    ListItem(
                        headlineContent = {
                            Text(
                                text = config.name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) DeepTeal else Color(0xFF333333)
                            )
                        },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (isSelected) AmberAccent else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingContent = {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = AmberAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        modifier = Modifier.clickable {
                            viewModel.selectLocationAndSync(config)
                            showLocationSheet = false
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = Color.LightGray.copy(alpha = 0.5f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                TextButton(onClick = {
                    showLocationSheet = false
                    onNavigateToLocations()
                }) {
                    Text("Manage locations", color = DeepTeal)
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
    onToggleNotification: () -> Unit,
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
                .padding(horizontal = 15.dp, vertical = 10.dp),
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
                                    text = stringResource(R.string.current_label),
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
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
                    contentDescription = stringResource(R.string.toggle_bell_description),
                    tint = tintColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}