package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab
import com.example.R
import com.example.data.LocationConfig
import com.example.ui.PrayerTimesViewModel
import com.example.ui.theme.ActivePrayerBg
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.BorderColor
import com.example.ui.theme.DeepTeal
import com.example.ui.theme.WarmCreame
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun LocationsScreen(
    viewModel: PrayerTimesViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val locations by viewModel.locationsList.collectAsState()
    val activeName by viewModel.currentLocationName.collectAsState()
    var showSheet by remember { mutableStateOf(false) }
    var showManualAdd by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val context = LocalContext.current
    
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Managed Locations", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepTeal)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showSheet = true },
                containerColor = DeepTeal,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add City")
            }
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
            if (locations.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.no_locations_saved),
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.no_locations_hint),
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
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
                            text = "Selecting any city updates calculation variables globally, using region-authorized methods.",
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
                            onSelect = { viewModel.selectLocationAndSync(config) },
                            onDelete = { viewModel.deleteLocation(config.id) }
                        )
                    }
                }
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Add New Location",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepTeal
                )
                
                OutlinedCard(
                    onClick = {
                        showSheet = false
                        showManualAdd = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = DeepTeal)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Manually", fontWeight = FontWeight.Bold)
                            Text("Search by country and city", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }

                OutlinedCard(
                    onClick = {
                        showSheet = false
                        if (locationPermissionState.status.isGranted) {
                            viewModel.addLocationFromGps { success ->
                                if (!success) {
                                    android.widget.Toast.makeText(context, "Failed to get location", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            locationPermissionState.launchPermissionRequest()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.GpsFixed, contentDescription = null, tint = DeepTeal)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Using GPS", fontWeight = FontWeight.Bold)
                            Text("Auto-detect your current city", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }

    if (showManualAdd) {
        Dialog(
            onDismissRequest = { showManualAdd = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            ManualAddLocationScreen(
                onDismiss = { showManualAdd = false },
                onAdd = { name, lat, lon, method ->
                    viewModel.addLocation(name, lat, lon, method, Madhab.SHAFI)
                    showManualAdd = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculationMethodDropdown(
    selectedMethod: CalculationMethod,
    onMethodSelected: (CalculationMethod) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val methods = CalculationMethod.entries

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = getFriendlyMethodName(selectedMethod),
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DeepTeal,
                unfocusedBorderColor = Color.LightGray
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            methods.forEach { method ->
                DropdownMenuItem(
                    text = { Text(getFriendlyMethodName(method)) },
                    onClick = {
                        onMethodSelected(method)
                        expanded = false
                    }
                )
            }
        }
    }
}

fun getFriendlyMethodName(method: CalculationMethod): String {
    return when (method) {
        CalculationMethod.UMM_AL_QURA -> "Umm Al-Qura"
        CalculationMethod.MOONSIGHTING_COMMITTEE -> "Moonsighting Committee"
        CalculationMethod.EGYPTIAN -> "Egyptian General"
        CalculationMethod.NORTH_AMERICA -> "ISNA (North America)"
        CalculationMethod.MUSLIM_WORLD_LEAGUE -> "Muslim World League"
        CalculationMethod.KARACHI -> "Karachi"
        CalculationMethod.KUWAIT -> "Kuwait"
        CalculationMethod.QATAR -> "Qatar"
        CalculationMethod.SINGAPORE -> "Singapore"
        CalculationMethod.TURKEY -> "Turkey"
    }
}

@Composable
fun LocationCardRow(
    config: LocationConfig,
    times: List<com.example.data.PrayerTimeItem>,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
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
                } else {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete location",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle: Method Used
            Text(
                text = "Method: ${getFriendlyMethodName(config.method)}",
                color = Color.Gray,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )

            HorizontalDivider(
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
