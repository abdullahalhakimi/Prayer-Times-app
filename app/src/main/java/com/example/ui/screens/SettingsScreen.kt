package com.prayertimesApp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batoulapps.adhan.Madhab
import com.prayertimesApp.R
import com.prayertimesApp.data.AppHighLatitudeRule
import com.prayertimesApp.ui.PrayerTimesViewModel
import com.prayertimesApp.ui.theme.ActivePrayerBg
import com.prayertimesApp.ui.theme.AmberAccent
import com.prayertimesApp.ui.theme.DeepTeal
import com.prayertimesApp.ui.theme.WarmCreame

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: PrayerTimesViewModel,
    onNavigateToLocations: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLocationName by viewModel.currentLocationName.collectAsState()
    val is24HourFormat by viewModel.is24HourFormat.collectAsState()
    val currentMadhab by viewModel.currentMadhab.collectAsState()
    val hijriDayOffset by viewModel.hijriDayOffset.collectAsState()
    val prePrayerReminderMinutes by viewModel.prePrayerReminderMinutes.collectAsState()
    val highLatitudeRule by viewModel.highLatitudeRule.collectAsState()

    var showMadhabDialog by remember { mutableStateOf(false) }
    var showHijriDialog by remember { mutableStateOf(false) }
    var showHighLatDialog by remember { mutableStateOf(false) }
    var showPrePrayerDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Bold, color = Color.White) },
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
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header note card
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepTeal.copy(alpha = 0.06f)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DeepTeal.copy(alpha = 0.12f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = stringResource(R.string.setting_notes_description),
                        tint = DeepTeal,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.settings_header_note),
                        fontSize = 12.sp,
                        color = Color(0xFF444444),
                        lineHeight = 16.sp
                    )
                }
            }

            // --- SECTION 1: GENERAL & LOCATIONS ---
            SettingsSectionHeader(title = stringResource(R.string.general_settings_header))

            SettingsItem(
                icon = Icons.Default.LocationOn,
                title = stringResource(R.string.managed_locations_title),
                subtitle = if (currentLocationName.isNotEmpty()) "Active city: $currentLocationName" else stringResource(R.string.managed_locations_subtitle),
                onClick = onNavigateToLocations
            )

            // --- SECTION 2: CALCULATION & FIQH ---
            SettingsSectionHeader(title = stringResource(R.string.fiqh_settings_header))

            val madhabLabel = if (currentMadhab == Madhab.HANAFI) "Hanafi" else "Shafi / Standard"
            SettingsItemValue(
                icon = Icons.Default.Book,
                title = stringResource(R.string.madhab_title),
                subtitle = stringResource(R.string.madhab_subtitle),
                valueText = madhabLabel,
                onClick = { showMadhabDialog = true }
            )

            val offsetLabel = when {
                hijriDayOffset > 0 -> "+$hijriDayOffset ${if (hijriDayOffset == 1) "day" else "days"}"
                hijriDayOffset < 0 -> "$hijriDayOffset ${if (hijriDayOffset == -1) "day" else "days"}"
                else -> "Standard (0)"
            }
            SettingsItemValue(
                icon = Icons.Default.CalendarMonth,
                title = stringResource(R.string.hijri_offset_title),
                subtitle = stringResource(R.string.hijri_offset_subtitle),
                valueText = offsetLabel,
                onClick = { showHijriDialog = true }
            )

            SettingsItemValue(
                icon = Icons.Default.Public,
                title = stringResource(R.string.high_latitude_title),
                subtitle = stringResource(R.string.high_latitude_subtitle),
                valueText = highLatitudeRule.displayName,
                onClick = { showHighLatDialog = true }
            )

            // --- SECTION 3: DISPLAY & TIME FORMAT ---
            SettingsSectionHeader(title = stringResource(R.string.display_settings_header))

            SettingsItemSwitch(
                icon = Icons.Default.AccessTime,
                title = stringResource(R.string.time_format_title),
                subtitle = stringResource(R.string.time_format_subtitle),
                isChecked = is24HourFormat,
                onCheckedChange = { viewModel.toggle24HourFormat(it) }
            )

            // --- SECTION 4: NOTIFICATIONS & REMINDERS ---
            SettingsSectionHeader(title = stringResource(R.string.notification_settings_header))

            val reminderLabel = if (prePrayerReminderMinutes == 0) "Disabled" else "$prePrayerReminderMinutes mins before"
            SettingsItemValue(
                icon = Icons.Default.NotificationsActive,
                title = stringResource(R.string.pre_prayer_warning_title),
                subtitle = stringResource(R.string.pre_prayer_warning_subtitle),
                valueText = reminderLabel,
                onClick = { showPrePrayerDialog = true }
            )

            // --- SECTION 5: ABOUT & CREDITS ---
            SettingsSectionHeader(title = stringResource(R.string.about_settings_header))

            SettingsItem(
                icon = Icons.Default.AutoAwesome,
                title = stringResource(R.string.about_app_title),
                subtitle = stringResource(R.string.about_app_subtitle),
                onClick = { showAboutDialog = true }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // --- DIALOGS ---

    if (showMadhabDialog) {
        AlertDialog(
            onDismissRequest = { showMadhabDialog = false },
            title = { Text("Select Asr Madhab", fontWeight = FontWeight.Bold, color = DeepTeal) },
            text = {
                Column {
                    listOf(
                        Madhab.SHAFI to "Shafi, Maliki, Hanbali (Standard 1x shadow)",
                        Madhab.HANAFI to "Hanafi (2x shadow length)"
                    ).forEach { (madhabOption, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (currentMadhab == madhabOption),
                                    onClick = {
                                        viewModel.setMadhab(madhabOption)
                                        showMadhabDialog = false
                                    }
                                )
                                .padding(vertical = 12.dp)
                        ) {
                            RadioButton(
                                selected = (currentMadhab == madhabOption),
                                onClick = {
                                    viewModel.setMadhab(madhabOption)
                                    showMadhabDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, fontSize = 14.sp, color = Color.DarkGray)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMadhabDialog = false }) {
                    Text("Close", color = DeepTeal, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showHijriDialog) {
        AlertDialog(
            onDismissRequest = { showHijriDialog = false },
            title = { Text("Hijri Date Correction", fontWeight = FontWeight.Bold, color = DeepTeal) },
            text = {
                Column {
                    Text("Adjust Hijri calendar by ± days to align with local moon sighting:", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf(-2, -1, 0, 1, 2).forEach { offset ->
                        val label = when {
                            offset > 0 -> "+$offset ${if (offset == 1) "day" else "days"}"
                            offset < 0 -> "$offset ${if (offset == -1) "day" else "days"}"
                            else -> "0 (Default standard)"
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (hijriDayOffset == offset),
                                    onClick = {
                                        viewModel.setHijriDayOffset(offset)
                                        showHijriDialog = false
                                    }
                                )
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = (hijriDayOffset == offset),
                                onClick = {
                                    viewModel.setHijriDayOffset(offset)
                                    showHijriDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, fontSize = 14.sp, color = Color.DarkGray)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHijriDialog = false }) {
                    Text("Cancel", color = DeepTeal, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showHighLatDialog) {
        AlertDialog(
            onDismissRequest = { showHighLatDialog = false },
            title = { Text("High Latitude Rule", fontWeight = FontWeight.Bold, color = DeepTeal) },
            text = {
                Column {
                    AppHighLatitudeRule.values().forEach { rule ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (highLatitudeRule == rule),
                                    onClick = {
                                        viewModel.setHighLatitudeRule(rule)
                                        showHighLatDialog = false
                                    }
                                )
                                .padding(vertical = 10.dp)
                        ) {
                            RadioButton(
                                selected = (highLatitudeRule == rule),
                                onClick = {
                                    viewModel.setHighLatitudeRule(rule)
                                    showHighLatDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = rule.displayName, fontSize = 14.sp, color = Color.DarkGray)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHighLatDialog = false }) {
                    Text("Close", color = DeepTeal, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showPrePrayerDialog) {
        AlertDialog(
            onDismissRequest = { showPrePrayerDialog = false },
            title = { Text("Pre-Adhan Warning Alert", fontWeight = FontWeight.Bold, color = DeepTeal) },
            text = {
                Column {
                    listOf(0 to "Disabled", 5 to "5 minutes before", 10 to "10 minutes before", 15 to "15 minutes before").forEach { (mins, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (prePrayerReminderMinutes == mins),
                                    onClick = {
                                        viewModel.setPrePrayerReminderMinutes(mins)
                                        showPrePrayerDialog = false
                                    }
                                )
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = (prePrayerReminderMinutes == mins),
                                onClick = {
                                    viewModel.setPrePrayerReminderMinutes(mins)
                                    showPrePrayerDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, fontSize = 14.sp, color = Color.DarkGray)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrePrayerDialog = false }) {
                    Text("Close", color = DeepTeal, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About Prayer Times", fontWeight = FontWeight.Bold, color = DeepTeal) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Prayer Times App v1.0.0", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Built with Kotlin, Jetpack Compose, and Material 3.", fontSize = 13.sp, color = Color.Gray)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Calculation Engines:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DeepTeal)
                    Text("• Batoul Apps Adhan Engine (Offline high-precision calculation)", fontSize = 12.sp, color = Color.DarkGray)
                    Text("• Aladhan REST API (Online synchronized monthly calendar)", fontSize = 12.sp, color = Color.DarkGray)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("OK", color = DeepTeal, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 11.sp,
        color = Color.Gray,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(DeepTeal.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = DeepTeal)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepTeal)
                Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.LightGray
            )
        }
    }
}

@Composable
fun SettingsItemValue(
    icon: ImageVector,
    title: String,
    subtitle: String,
    valueText: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(DeepTeal.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = DeepTeal)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepTeal)
                Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            
            Surface(
                color = ActivePrayerBg,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = valueText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberAccent,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun SettingsItemSwitch(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(DeepTeal.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = DeepTeal)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepTeal)
                Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = DeepTeal,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.LightGray
                )
            )
        }
    }
}
