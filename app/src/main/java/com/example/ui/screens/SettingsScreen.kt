package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cabin
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LocationConfig
import com.example.ui.PrayerTimesViewModel
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.DeepTeal
import com.example.ui.theme.WarmCreame
import com.example.ui.theme.ActivePrayerBg
import com.example.ui.theme.BorderColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: PrayerTimesViewModel,
    modifier: Modifier = Modifier
) {
    val locations by viewModel.locationsList.collectAsState()
    val activeName by viewModel.currentLocationName.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Multi-City Calculations", fontWeight = FontWeight.Bold, color = Color.White) },
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
            
            // Header note card
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepTeal.copy(alpha = 0.06f)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DeepTeal.copy(alpha = 0.12f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Setting notes description",
                        tint = DeepTeal,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Selecting any city updates calculation variables globally, using region-authorized methods (e.g. Umm Al-Qura for Makkah).",
                        fontSize = 12.sp,
                        color = Color(0xFF444444),
                        lineHeight = 16.sp
                    )
                }
            }

            Text(
                text = "SAVED PLACES & TIME COMPARISONS",
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
            )

            // Scrollable list of locations
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("locations_list"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(locations) { config ->
                    val isSelected = config.name.equals(activeName, ignoreCase = true)
                    val correspondingTimes = viewModel.calculateSpecificLocationTimes(config)

                    LocationCardRow(
                        config = config,
                        times = correspondingTimes,
                        isSelected = isSelected,
                        onSelect = { viewModel.selectLocationAndSync(config) }
                    )
                }
            }
        }
    }
}

@Composable
fun LocationCardRow(
    config: LocationConfig,
    times: List<com.example.data.PrayerTimeItem>,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) ActivePrayerBg else Color.White
        ),
        shape = RoundedCornerShape(22.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isSelected) DeepTeal.copy(alpha = 0.2f) else BorderColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 0.dp else 1.dp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("location_card_${config.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row (City and Selection indicator)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Place indicator icon",
                        tint = if (isSelected) AmberAccent else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = config.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) DeepTeal else Color(0xFF333333)
                    )
                }

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected location checked indicator",
                        tint = AmberAccent,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle: Method Used
            val methodName = when (config.method) {
                com.batoulapps.adhan.CalculationMethod.UMM_AL_QURA -> "Umm Al-Qura"
                com.batoulapps.adhan.CalculationMethod.MOONSIGHTING_COMMITTEE -> "Moonsighting Committee"
                com.batoulapps.adhan.CalculationMethod.EGYPTIAN -> "Egyptian General"
                com.batoulapps.adhan.CalculationMethod.NORTH_AMERICA -> "ISNA"
                else -> "Standard Calculation Method"
            }
            Text(
                text = "Method: $methodName",
                color = Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )

            Divider(
                color = Color.LightGray.copy(alpha = 0.3f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            // Grid content for calculated times
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val relevantNames = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
                
                relevantNames.forEach { pName ->
                    val pTime = times.find { it.name.equals(pName, ignoreCase = true) }?.formattedTime ?: "--:--"
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = pName,
                            fontSize = 11.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = pTime.replace(" ", "\n").lowercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) DeepTeal else Color(0xFF555555),
                            maxLines = 2,
                            lineHeight = 13.sp
                        )
                    }
                }
            }
        }
    }
}
