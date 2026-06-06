package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.CompassViewModel
import com.example.ui.PrayerTimesViewModel
import com.example.ui.screens.AgendaScreen
import com.example.ui.screens.HijriScreen
import com.example.ui.screens.LocationsScreen
import com.example.ui.screens.PrayersScreen
import com.example.ui.screens.QiblaScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.ActivePrayerBg
import com.example.ui.theme.BorderColor
import com.example.ui.theme.DeepTeal
import com.example.ui.theme.InactiveTextColor
import com.example.ui.theme.MyApplicationTheme
import android.graphics.Color as AndroidColor

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Prayers : Screen("prayers", "Prayers", Icons.Default.AccessTime)
    object Qibla : Screen("qibla", "Qibla", Icons.Default.Explore)
    object Hijri : Screen("hijri", "Hijri", Icons.Default.CalendarToday)
    object Agenda : Screen("agenda", "Agenda", Icons.Default.Alarm)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object Locations : Screen("locations", "Locations", Icons.Default.LocationOn)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                DeepTeal.toArgb()
            ),
            navigationBarStyle = SystemBarStyle.light(
                AndroidColor.TRANSPARENT,
                AndroidColor.TRANSPARENT
            )
        )
        setContent {
            MyApplicationTheme {
                MainAppContainer()
            }
        }
    }
}

@Composable
fun MainAppContainer() {
    val navController = rememberNavController()
    val viewModel: PrayerTimesViewModel = viewModel()
    val compassViewModel: CompassViewModel = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val screens = listOf(
        Screen.Prayers,
        Screen.Qibla,
        Screen.Hijri,
        Screen.Agenda,
        Screen.Settings
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp, // No default heavy shadow, we draw our clean border line
                modifier = Modifier
                    .testTag("app_navigation_bar")
                    .drawBehind {
                        drawLine(
                            color = BorderColor,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 2.dp.toPx()
                        )
                    }
            ) {
                screens.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DeepTeal,
                            selectedTextColor = DeepTeal,
                            indicatorColor = ActivePrayerBg,
                            unselectedIconColor = InactiveTextColor,
                            unselectedTextColor = InactiveTextColor
                        ),
                        modifier = Modifier.testTag("nav_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Prayers.route,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(Screen.Prayers.route) {
                PrayersScreen(viewModel = viewModel)
            }
            composable(Screen.Qibla.route) {
                QiblaScreen(viewModel = compassViewModel)
            }
            composable(Screen.Hijri.route) {
                HijriScreen(viewModel = viewModel)
            }
            composable(Screen.Agenda.route) {
                AgendaScreen(viewModel = viewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToLocations = { navController.navigate(Screen.Locations.route) }
                )
            }
            composable(Screen.Locations.route) {
                LocationsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
