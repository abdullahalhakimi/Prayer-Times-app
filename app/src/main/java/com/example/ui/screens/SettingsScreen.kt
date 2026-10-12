package com.prayertimesApp.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
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
    val appLanguage by viewModel.appLanguage.collectAsState()

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showMadhabDialog by remember { mutableStateOf(false) }
    var showHijriDialog by remember { mutableStateOf(false) }
    var showHighLatDialog by remember { mutableStateOf(false) }
    var showPrePrayerDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

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
                subtitle = if (currentLocationName.isNotEmpty()) stringResource(R.string.active_city_label, currentLocationName) else stringResource(R.string.managed_locations_subtitle),
                onClick = onNavigateToLocations
            )

            // Language Selector Item
            val languageValueLabel = when (appLanguage) {
                "ar" -> stringResource(R.string.lang_arabic)
                "en" -> stringResource(R.string.lang_english)
                else -> stringResource(R.string.lang_system_default)
            }
            SettingsItemValue(
                icon = Icons.Default.Language,
                title = stringResource(R.string.language_settings_title),
                subtitle = stringResource(R.string.language_settings_subtitle),
                valueText = languageValueLabel,
                onClick = { showLanguageDialog = true }
            )

            // --- SECTION 2: CALCULATION & FIQH ---
            SettingsSectionHeader(title = stringResource(R.string.fiqh_settings_header))

            val madhabLabel = stringResource(if (currentMadhab == Madhab.HANAFI) R.string.madhab_hanafi else R.string.madhab_standard)
            SettingsItemValue(
                icon = Icons.Default.Book,
                title = stringResource(R.string.madhab_title),
                subtitle = stringResource(R.string.madhab_subtitle),
                valueText = madhabLabel,
                onClick = { showMadhabDialog = true }
            )

            val offsetLabel = when {
                hijriDayOffset > 0 -> "+$hijriDayOffset"
                hijriDayOffset < 0 -> "$hijriDayOffset"
                else -> "0"
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
                valueText = stringResource(highLatitudeRule.nameResId),
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

            val reminderLabel = if (prePrayerReminderMinutes == 0) stringResource(R.string.notification_silent) else "$prePrayerReminderMinutes mins"
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

            SettingsItem(
                icon = Icons.Default.Security,
                title = stringResource(R.string.privacy_policy_title),
                subtitle = stringResource(R.string.privacy_policy_subtitle),
                onClick = { showPrivacyDialog = true }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // --- DIALOGS ---

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.language_settings_title), fontWeight = FontWeight.Bold, color = DeepTeal) },
            text = {
                Column {
                    listOf(
                        "system" to stringResource(R.string.lang_system_default),
                        "en" to stringResource(R.string.lang_english),
                        "ar" to stringResource(R.string.lang_arabic)
                    ).forEach { (code, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (appLanguage == code),
                                    onClick = {
                                        viewModel.setAppLanguage(code)
                                        showLanguageDialog = false
                                    }
                                )
                                .padding(vertical = 12.dp)
                        ) {
                            RadioButton(
                                selected = (appLanguage == code),
                                onClick = {
                                    viewModel.setAppLanguage(code)
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, fontSize = 14.sp, color = Color.DarkGray)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.cancel), color = DeepTeal, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showMadhabDialog) {
        AlertDialog(
            onDismissRequest = { showMadhabDialog = false },
            title = { Text(stringResource(R.string.madhab_title), fontWeight = FontWeight.Bold, color = DeepTeal) },
            text = {
                Column {
                    listOf(
                        Madhab.SHAFI to stringResource(R.string.madhab_standard),
                        Madhab.HANAFI to stringResource(R.string.madhab_hanafi)
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
                    Text(stringResource(R.string.cancel), color = DeepTeal, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showHijriDialog) {
        AlertDialog(
            onDismissRequest = { showHijriDialog = false },
            title = { Text(stringResource(R.string.hijri_offset_title), fontWeight = FontWeight.Bold, color = DeepTeal) },
            text = {
                Column {
                    Text(stringResource(R.string.offset_note), fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf(-2, -1, 0, 1, 2).forEach { offset ->
                        val label = when {
                            offset > 0 -> "+$offset"
                            offset < 0 -> "$offset"
                            else -> "0"
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
                    Text(stringResource(R.string.cancel), color = DeepTeal, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showHighLatDialog) {
        AlertDialog(
            onDismissRequest = { showHighLatDialog = false },
            title = { Text(stringResource(R.string.high_latitude_title), fontWeight = FontWeight.Bold, color = DeepTeal) },
            text = {
                Column {
                    AppHighLatitudeRule.entries.forEach { rule ->
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
                            Text(text = stringResource(rule.nameResId), fontSize = 14.sp, color = Color.DarkGray)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHighLatDialog = false }) {
                    Text(stringResource(R.string.cancel), color = DeepTeal, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showPrePrayerDialog) {
        AlertDialog(
            onDismissRequest = { showPrePrayerDialog = false },
            title = { Text(stringResource(R.string.pre_prayer_warning_title), fontWeight = FontWeight.Bold, color = DeepTeal) },
            text = {
                Column {
                    listOf(
                        0 to stringResource(R.string.notification_silent),
                        5 to "5 mins",
                        10 to "10 mins",
                        15 to "15 mins"
                    ).forEach { (mins, label) ->
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
                    Text(stringResource(R.string.cancel), color = DeepTeal, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(stringResource(R.string.about_app_title), fontWeight = FontWeight.Bold, color = DeepTeal) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.app_name) + " v1.0.0", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Built with Kotlin, Jetpack Compose, and Material 3.", fontSize = 13.sp, color = Color.Gray)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(stringResource(R.string.calculation_method) + ":", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DeepTeal)
                    Text("• Batoul Apps Adhan Engine", fontSize = 12.sp, color = Color.DarkGray)
                    Text("• Aladhan REST API", fontSize = 12.sp, color = Color.DarkGray)
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text(stringResource(R.string.confirm), color = DeepTeal, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text(stringResource(R.string.privacy_policy_title), fontWeight = FontWeight.Bold, color = DeepTeal) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(stringResource(R.string.privacy_policy_title), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DeepTeal)
                    Text(stringResource(R.string.privacy_policy_subtitle), fontSize = 12.sp, color = Color.DarkGray)
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text(stringResource(R.string.cancel), color = DeepTeal, fontWeight = FontWeight.Bold)
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
