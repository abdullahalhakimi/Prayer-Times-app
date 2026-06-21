package com.example.ui

import android.app.Application
import android.content.Context
import android.location.Geocoder
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.R
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab
import com.example.data.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.util.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlin.time.Duration.Companion.milliseconds

enum class NotificationType(val label: String) {
    SILENT("Silent"),
    BEEP("Beep"),
    ADHAN("Adhan")
}

data class EventReminder(
    val id: String,
    val title: String,
    val hijriDateStr: String,
    val isEnabled: Boolean
)

class PrayerTimesViewModel(application: Application) : AndroidViewModel(application) {

    private val scheduler = SuhoorAlarmScheduler(application)

    // Current location configurations
    private val _currentLocationName = MutableStateFlow("City of London")
    val currentLocationName: StateFlow<String> = _currentLocationName

    private val _currentLatitude = MutableStateFlow(51.5074)
    val currentLatitude: StateFlow<Double> = _currentLatitude

    private val _currentLongitude = MutableStateFlow(-0.1278)
    val currentLongitude: StateFlow<Double> = _currentLongitude

    private val _currentMethod = MutableStateFlow(CalculationMethod.MOONSIGHTING_COMMITTEE)
    val currentMethod: StateFlow<CalculationMethod> = _currentMethod

    private val _currentMadhab = MutableStateFlow(Madhab.SHAFI)
    val currentMadhab: StateFlow<Madhab> = _currentMadhab

    // Calculated prayer times for current location & date
    private val _prayerTimes = MutableStateFlow<List<PrayerTimeItem>>(emptyList())
    val prayerTimes: StateFlow<List<PrayerTimeItem>> = _prayerTimes

    // Upcoming prayer tracking
    private val _upcomingPrayerName = MutableStateFlow("DHUHR")
    val upcomingPrayerName: StateFlow<String> = _upcomingPrayerName

    private val _upcomingPrayerTimeStr = MutableStateFlow("12:15 PM")
    val upcomingPrayerTimeStr: StateFlow<String> = _upcomingPrayerTimeStr

    private val _countdownStr = MutableStateFlow("0 hours 0m left")
    val countdownStr: StateFlow<String> = _countdownStr

    private val _isDayTime = MutableStateFlow(true)
    val isDayTime: StateFlow<Boolean> = _isDayTime

    // Notification states for current day prayers
    private val _prayerNotifications = MutableStateFlow<Map<String, NotificationType>>(emptyMap())
    val prayerNotifications: StateFlow<Map<String, NotificationType>> = _prayerNotifications

    // Locations for settings
    private val _locationsList = MutableStateFlow<List<LocationConfig>>(emptyList())
    val locationsList: StateFlow<List<LocationConfig>> = _locationsList

    // Alarms/Reminders list (Agenda)
    private val _agendaReminders = MutableStateFlow<List<AgendaReminder>>(emptyList())
    val agendaReminders: StateFlow<List<AgendaReminder>> = _agendaReminders

    // Hijri Islamic events
    private val _islamicEvents = MutableStateFlow<List<EventReminder>>(emptyList())
    val islamicEvents: StateFlow<List<EventReminder>> = _islamicEvents

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val locationListAdapter = moshi.adapter<List<LocationConfig>>(
        Types.newParameterizedType(List::class.java, LocationConfig::class.java)
    )

    private var tickerJob: Job? = null

    init {
        // Initialize lists first to have available locations
        initLocations()
        initReminders()
        initEvents()
        initNotificationStates()

        // Pre-populate location preferences saved states
        val sharedPrefs = application.getSharedPreferences("prayer_times_prefs", Context.MODE_PRIVATE)
        val savedLocName = sharedPrefs.getString("saved_location", "City of London") ?: "City of London"
        
        val savedConfig = _locationsList.value.find { it.name == savedLocName }
        if (savedConfig != null) {
            selectLocationAndSync(savedConfig)
        } else {
            // Fallback to first available or London if empty
            val fallback = _locationsList.value.firstOrNull()
            if (fallback != null) {
                selectLocationAndSync(fallback)
            } else {
                setToLondon()
            }
        }

        // Start real-time countdown updates
        startTicker()
    }

    private fun initNotificationStates() {
        _prayerNotifications.value = mapOf(
            "Fajr" to NotificationType.ADHAN,
            "Sunrise" to NotificationType.SILENT,
            "Dhuhr" to NotificationType.BEEP,
            "Asr" to NotificationType.ADHAN,
            "Maghrib" to NotificationType.ADHAN,
            "Isha" to NotificationType.ADHAN,
            "Qiyam" to NotificationType.SILENT
        )
    }

    private fun initLocations() {
        val sharedPrefs = getApplication<Application>().getSharedPreferences("prayer_times_prefs", Context.MODE_PRIVATE)
        val savedLocationsJson = sharedPrefs.getString("locations_list_json", null)
        
        if (savedLocationsJson != null) {
            try {
                _locationsList.value = locationListAdapter.fromJson(savedLocationsJson) ?: emptyList()
            } catch (e: Exception) {
                loadDefaultLocations()
            }
        } else {
            loadDefaultLocations()
        }
    }

    private fun loadDefaultLocations() {
        _locationsList.value = listOf(
            LocationConfig("london", "City of London", 51.5074, -0.1278, CalculationMethod.MOONSIGHTING_COMMITTEE, Madhab.SHAFI),
            LocationConfig("makkah", "Makkah", 21.4225, 39.8262, CalculationMethod.UMM_AL_QURA, Madhab.SHAFI),
            LocationConfig("medina", "Medina", 24.4672, 39.6111, CalculationMethod.UMM_AL_QURA, Madhab.SHAFI),
            LocationConfig("cairo", "Cairo", 30.0444, 31.2357, CalculationMethod.EGYPTIAN, Madhab.SHAFI),
            LocationConfig("newyork", "New York", 40.7128, -74.0060, CalculationMethod.NORTH_AMERICA, Madhab.SHAFI)
        )
        saveLocations()
    }

    private fun saveLocations() {
        val sharedPrefs = getApplication<Application>().getSharedPreferences("prayer_times_prefs", Context.MODE_PRIVATE)
        val json = locationListAdapter.toJson(_locationsList.value)
        sharedPrefs.edit().putString("locations_list_json", json).apply()
    }

    fun addLocation(name: String, lat: Double, lon: Double, method: CalculationMethod, madhab: Madhab) {
        val newConfig = LocationConfig(
            id = UUID.randomUUID().toString(),
            name = name,
            latitude = lat,
            longitude = lon,
            method = method,
            madhab = madhab
        )
        _locationsList.value = _locationsList.value + newConfig
        saveLocations()
    }

    fun deleteLocation(id: String) {
        // Prevent deleting currently selected location if possible, or handle it
        _locationsList.value = _locationsList.value.filter { it.id != id }
        saveLocations()
    }

    fun addLocationFromGps(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplication<Application>())
                val location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()
                
                if (location != null) {
                    val geocoder = Geocoder(getApplication(), Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val cityName = addresses?.firstOrNull()?.locality ?: "Current Location"
                    
                    _currentLocationName.value = cityName
                    _currentLatitude.value = location.latitude
                    _currentLongitude.value = location.longitude
                    recalculate()
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    fun fetchAndSetGpsLocation() {
        viewModelScope.launch {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplication<Application>())
                val location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).await()

                if (location != null) {
                    val geocoder = Geocoder(getApplication(), Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val cityName = addresses?.firstOrNull()?.locality ?: "Current Location"

                    _currentLocationName.value = cityName
                    _currentLatitude.value = location.latitude
                    _currentLongitude.value = location.longitude
                    recalculate()
                }
            } catch (_: Exception) {
                // GPS unavailable or permission not granted — keep saved location
            }
        }
    }

    private fun initReminders() {
        _agendaReminders.value = listOf(
            AgendaReminder("suhoor", "Suhoor Reminder", "Fasting preparation window", -40, "Fajr", true),
            AgendaReminder("tahajjud", "Tahajjud Alert", "Late night voluntary prayer", -60, "Fajr", false),
            AgendaReminder("ishraq", "Ishrāq Prayer time", "Voluntary morning prayer", 15, "Sunrise", true),
            AgendaReminder("iftar_beep", "Iftar Reminder", "Breaking of fast pre-alarm", -5, "Maghrib", true)
        )
    }

    private fun initEvents() {
        _islamicEvents.value = listOf(
            EventReminder("e1", "Ayyamul Bidh Fasting (1st day)", "13 Sha'ban", true),
            EventReminder("e2", "Ayyamul Bidh Fasting (2nd day)", "14 Sha'ban", true),
            EventReminder("e3", "Ayyamul Bidh Fasting (3rd day)", "15 Sha'ban", true),
            EventReminder("e4", "First of Ramadan", "1 Ramadan", true),
            EventReminder("e5", "Eid al-Fitr Celebration", "1 Shawwal", false),
            EventReminder("e6", "Day of Arafah (Hajj)", "9 Dhu al-Hijjah", true),
            EventReminder("e7", "Eid al-Adha", "10 Dhu al-Hijjah", false),
            EventReminder("e8", "Islamic New Year", "1 Muharram", false)
        )
    }

    fun selectLocationAndSync(config: LocationConfig) {
        _currentLocationName.value = config.name
        _currentLatitude.value = config.latitude
        _currentLongitude.value = config.longitude
        _currentMethod.value = config.method
        _currentMadhab.value = config.madhab
        recalculate()

        val sharedPrefs = getApplication<Application>().getSharedPreferences("prayer_times_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("saved_location", config.name).apply()

        Toast.makeText(getApplication(), getApplication<Application>().getString(R.string.switched_to_location, config.name), Toast.LENGTH_SHORT).show()
    }

    fun setToLondon() {
        _currentLocationName.value = "City of London"
        _currentLatitude.value = 51.5074
        _currentLongitude.value = -0.1278
        _currentMethod.value = CalculationMethod.MOONSIGHTING_COMMITTEE
        _currentMadhab.value = Madhab.SHAFI
        recalculate()
    }

    fun setToMakkah() {
        _currentLocationName.value = "Makkah"
        _currentLatitude.value = 21.4225
        _currentLongitude.value = 39.8262
        _currentMethod.value = CalculationMethod.UMM_AL_QURA
        _currentMadhab.value = Madhab.SHAFI
        recalculate()
    }

    fun setToMedina() {
        _currentLocationName.value = "Medina"
        _currentLatitude.value = 24.4672
        _currentLongitude.value = 39.6111
        _currentMethod.value = CalculationMethod.UMM_AL_QURA
        _currentMadhab.value = Madhab.SHAFI
        recalculate()
    }

    fun recalculate() {
        _prayerTimes.value = PrayerTimesCalculator.calculateTimes(
            latitude = _currentLatitude.value,
            longitude = _currentLongitude.value,
            method = _currentMethod.value,
            madhab = _currentMadhab.value
        )
        updateCountdown()
    }

    fun togglePrayerNotification(prayerName: String) {
        val currentMap = _prayerNotifications.value.toMutableMap()
        val currentType = currentMap[prayerName] ?: NotificationType.SILENT
        val nextType = when (currentType) {
            NotificationType.SILENT -> NotificationType.BEEP
            NotificationType.BEEP -> NotificationType.ADHAN
            NotificationType.ADHAN -> NotificationType.SILENT
        }
        currentMap[prayerName] = nextType
        _prayerNotifications.value = currentMap
    }

    fun toggleAgendaReminder(id: String) {
        _agendaReminders.value = _agendaReminders.value.map {
            if (it.id == id) {
                val nextState = !it.isEnabled
                if (nextState) {
                    // Programmatically schedule using scheduler
                    val triggerTime = getReminderTriggerTimeMillis(it)
                    if (triggerTime != null) {
                        scheduler.scheduleAlarm(triggerTime, it.title)
                    }
                } else {
                    scheduler.cancelAlarm()
                }
                it.copy(isEnabled = nextState)
            } else it
        }
    }

    fun toggleEventReminder(id: String) {
        _islamicEvents.value = _islamicEvents.value.map {
            if (it.id == id) {
                it.copy(isEnabled = !it.isEnabled)
            } else it
        }
    }

    // Calculates the trigger time dynamically in absolute epoch millis
    fun getReminderTriggerTimeMillis(reminder: AgendaReminder): Long? {
        val baseTime = _prayerTimes.value.find { it.name.equals(reminder.basePrayerName, ignoreCase = true) }?.date
        if (baseTime != null) {
            val calendar = Calendar.getInstance().apply { time = baseTime }
            calendar.add(Calendar.MINUTE, reminder.relativeMinutes)
            // Ensure the alarm is in the future
            if (calendar.timeInMillis < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            return calendar.timeInMillis
        }
        return null
    }

    fun calculateSpecificLocationTimes(config: LocationConfig): List<PrayerTimeItem> {
        return PrayerTimesCalculator.calculateTimes(
            latitude = config.latitude,
            longitude = config.longitude,
            method = config.method,
            madhab = config.madhab
        )
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (isActive) {
                updateCountdown()
                delay(duration = 1000.milliseconds)
            }
        }
    }

    private fun updateCountdown() {
        val times = _prayerTimes.value
        if (times.isEmpty()) {
            return
        }

        val now = Date()
        var upcomingItem = times.firstOrNull { it.date.after(now) }
        
        if (upcomingItem == null) {
            // All of today's prayers passed, next is tomorrow's Fajr
            val tomorrow = Calendar.getInstance().apply {
                add(Calendar.DATE, 1)
            }.time
            val tomorrowTimes = PrayerTimesCalculator.calculateTimes(
                latitude = _currentLatitude.value,
                longitude = _currentLongitude.value,
                method = _currentMethod.value,
                madhab = _currentMadhab.value,
                date = tomorrow
            )
            upcomingItem = tomorrowTimes.firstOrNull { it.name == "Fajr" } ?: times.first()
        }

        _upcomingPrayerName.value = upcomingItem.name.uppercase()
        _upcomingPrayerTimeStr.value = upcomingItem.formattedTime

        val diffMs = upcomingItem.date.time - now.time
        if (diffMs > 0) {
            val totalSeconds = diffMs / 1000
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            _countdownStr.value = getApplication<Application>().getString(R.string.countdown_format, hours, minutes)
        } else {
            _countdownStr.value = getApplication<Application>().getString(R.string.countdown_format, 0, 0)
        }

        // Determine day vs night graphics
        val sunriseItem = times.find { it.name == "Sunrise" }
        val maghribItem = times.find { it.name == "Maghrib" }
        if (sunriseItem != null && maghribItem != null) {
            _isDayTime.value = now.after(sunriseItem.date) && now.before(maghribItem.date)
        } else {
            _isDayTime.value = true
        }
    }

    /**
     * Converts a LocalDate to formatted Hijri string (Native Chronology)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun formatHijriDate(localDate: LocalDate): String {
        return try {
            val hijrahDate = HijrahDate.from(localDate)
            val monthNames = arrayOf(
                "Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani",
                "Jumada al-Awwal", "Jumada al-Thani", "Rajab", "Sha'ban",
                "Ramadan", "Shawwal", "Dhu al-Qa'dah", "Dhu al-Hijjah"
            )
            val monthIndex = (hijrahDate.get(ChronoField.MONTH_OF_YEAR) - 1).coerceIn(0, 11)
            val day = hijrahDate.get(ChronoField.DAY_OF_MONTH)
            val year = hijrahDate.get(ChronoField.YEAR)
            "$day ${monthNames[monthIndex]} $year AH"
        } catch (e: Exception) {
            "15 Sha'ban 1447 AH" // Safe fallback in case of standard timezone constraints in emulation
        }
    }

    /**
     * Helper to get Hijri Day Integer for double-grid calendar view
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun getHijriDay(localDate: LocalDate): Int {
        return try {
            val hijrahDate = HijrahDate.from(localDate)
            hijrahDate.get(ChronoField.DAY_OF_MONTH)
        } catch (e: Exception) {
            (localDate.dayOfMonth + 12) % 30 + 1 // pseudo fallback
        }
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
    }
}
