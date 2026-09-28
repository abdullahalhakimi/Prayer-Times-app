package com.prayertimesApp.ui

import android.app.Application
import android.content.Context
import android.location.Geocoder
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.prayertimesApp.R
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab
import com.prayertimesApp.data.*
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
import androidx.core.content.edit

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
    private val _currentLocationName = MutableStateFlow("")
    val currentLocationName: StateFlow<String> = _currentLocationName

    private val _currentLatitude = MutableStateFlow(0.0)
    val currentLatitude: StateFlow<Double> = _currentLatitude

    private val _currentLongitude = MutableStateFlow(0.0)
    val currentLongitude: StateFlow<Double> = _currentLongitude

    private val _isLocationEnabled = MutableStateFlow(true)
    val isLocationEnabled: StateFlow<Boolean> = _isLocationEnabled

    private val _currentMethod = MutableStateFlow(CalculationMethod.MOONSIGHTING_COMMITTEE)
    val currentMethod: StateFlow<CalculationMethod> = _currentMethod

    private val _currentMadhab = MutableStateFlow(Madhab.SHAFI)
    val currentMadhab: StateFlow<Madhab> = _currentMadhab

    // New Settings State
    private val _is24HourFormat = MutableStateFlow(false)
    val is24HourFormat: StateFlow<Boolean> = _is24HourFormat

    private val _hijriDayOffset = MutableStateFlow(0)
    val hijriDayOffset: StateFlow<Int> = _hijriDayOffset

    private val _prePrayerReminderMinutes = MutableStateFlow(10)
    val prePrayerReminderMinutes: StateFlow<Int> = _prePrayerReminderMinutes

    private val _highLatitudeRule = MutableStateFlow(AppHighLatitudeRule.MIDDLE_OF_THE_NIGHT)
    val highLatitudeRule: StateFlow<AppHighLatitudeRule> = _highLatitudeRule

    // Track whether any locations have been saved
    private val _hasLocations = MutableStateFlow(false)
    val hasLocations: StateFlow<Boolean> = _hasLocations

    /** Wrapper enum matching the active Adhan method. */
    private val currentPrayerMethod: PrayerCalculationMethod
        get() = when (_currentMethod.value) {
            CalculationMethod.UMM_AL_QURA -> PrayerCalculationMethod.UMM_AL_QURA
            CalculationMethod.MUSLIM_WORLD_LEAGUE -> PrayerCalculationMethod.MUSLIM_WORLD_LEAGUE
            CalculationMethod.EGYPTIAN -> PrayerCalculationMethod.EGYPTIAN
            CalculationMethod.KARACHI -> PrayerCalculationMethod.KARACHI
            CalculationMethod.NORTH_AMERICA -> PrayerCalculationMethod.NORTH_AMERICA
            CalculationMethod.MOONSIGHTING_COMMITTEE -> PrayerCalculationMethod.MOONSIGHTING_COMMITTEE
            CalculationMethod.KUWAIT -> PrayerCalculationMethod.KUWAIT
            CalculationMethod.QATAR -> PrayerCalculationMethod.QATAR
            CalculationMethod.SINGAPORE -> PrayerCalculationMethod.SINGAPORE
            CalculationMethod.TURKEY -> PrayerCalculationMethod.TURKEY
            else -> PrayerCalculationMethod.MOONSIGHTING_COMMITTEE
        }

    private val currentPrayerMadhab: PrayerMadhab
        get() = when (_currentMadhab.value) {
            Madhab.HANAFI -> PrayerMadhab.HANAFI
            else -> PrayerMadhab.STANDARD
        }

    // Calculated prayer times for current location & date
    private val _prayerTimes = MutableStateFlow<List<PrayerTimeItem>>(emptyList())
    val prayerTimes: StateFlow<List<PrayerTimeItem>> = _prayerTimes

    // Network status
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _lastUpdated = MutableStateFlow<Long?>(null)
    val lastUpdated: StateFlow<Long?> = _lastUpdated

    private val _sourceLabel = MutableStateFlow("Offline")
    val sourceLabel: StateFlow<String> = _sourceLabel

    private val _hijriDate = MutableStateFlow<String?>(null)
    val hijriDate: StateFlow<String?> = _hijriDate

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

        // Restore saved settings & preferences
        val sharedPrefs = application.getSharedPreferences("prayer_times_prefs", Context.MODE_PRIVATE)
        _is24HourFormat.value = sharedPrefs.getBoolean("is_24_hour_format", false)
        _hijriDayOffset.value = sharedPrefs.getInt("hijri_day_offset", 0)
        _prePrayerReminderMinutes.value = sharedPrefs.getInt("pre_prayer_reminder_mins", 10)
        
        val savedMadhabStr = sharedPrefs.getString("current_madhab", null)
        if (savedMadhabStr != null) {
            try { _currentMadhab.value = Madhab.valueOf(savedMadhabStr) } catch (_: Exception) {}
        }
        val savedRuleStr = sharedPrefs.getString("high_latitude_rule", null)
        if (savedRuleStr != null) {
            try { _highLatitudeRule.value = AppHighLatitudeRule.valueOf(savedRuleStr) } catch (_: Exception) {}
        }

        // Restore saved location if available
        if (_hasLocations.value) {
            val savedLocName = sharedPrefs.getString("saved_location", null)
            val savedConfig = savedLocName?.let { name ->
                _locationsList.value.find { it.name == name }
            } ?: _locationsList.value.firstOrNull()

            if (savedConfig != null) {
                selectLocationAndSync(savedConfig)
            }
        }
        // If no locations, _hasLocations is false — GPS or dialog will handle it

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
                _locationsList.value = emptyList()
            }
        }
        _hasLocations.value = _locationsList.value.isNotEmpty()
    }

    private fun saveLocations() {
        val sharedPrefs = getApplication<Application>().getSharedPreferences("prayer_times_prefs", Context.MODE_PRIVATE)
        val json = locationListAdapter.toJson(_locationsList.value)
        sharedPrefs.edit { putString("locations_list_json", json) }
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
        _locationsList.value += newConfig
        _hasLocations.value = true
        saveLocations()
    }

    fun deleteLocation(id: String) {
        _locationsList.value = _locationsList.value.filter { it.id != id }
        _hasLocations.value = _locationsList.value.isNotEmpty()
        saveLocations()
    }

    @Suppress("MissingPermission")
    fun addLocationFromGps(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            if (!LocationHelper.hasLocationPermission(getApplication()) || !LocationHelper.isLocationEnabled(getApplication())) {
                _isLocationEnabled.value = false
                onComplete(false)
                return@launch
            }
            _isLocationEnabled.value = true
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplication())
                val location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).await()

                if (location != null) {
                    val geocoder = Geocoder(getApplication(), Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val cityName = addresses?.firstOrNull()?.locality ?: "Current Location"

                    _currentLocationName.value = cityName
                    _currentLatitude.value = location.latitude
                    _currentLongitude.value = location.longitude
                    recalculate()

                    // Persist this location so it survives app restarts
                    val existingMatch = _locationsList.value.find {
                        it.latitude == location.latitude && it.longitude == location.longitude
                    }
                    if (existingMatch == null) {
                        addLocation(cityName, location.latitude, location.longitude, _currentMethod.value, _currentMadhab.value)
                    }

                    onComplete(true)
                } else {
                    onComplete(false)
                }
            } catch (e: SecurityException) {
                _isLocationEnabled.value = false
                onComplete(false)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    @Suppress("MissingPermission")
    fun fetchAndSetGpsLocation() {
        viewModelScope.launch {
            if (!LocationHelper.hasLocationPermission(getApplication()) || !LocationHelper.isLocationEnabled(getApplication())) {
                _isLocationEnabled.value = false
                return@launch
            }
            _isLocationEnabled.value = true
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplication())
                val location = fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).await()

                if (location != null) {
                    val geocoder = Geocoder(getApplication(), Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    val cityName = addresses?.firstOrNull()?.locality ?: "Current Location"

                    _currentLocationName.value = cityName
                    _currentLatitude.value = location.latitude
                    _currentLongitude.value = location.longitude
                    recalculate()
                } else {
                    _isLocationEnabled.value = false
                }
            } catch (_: SecurityException) {
                _isLocationEnabled.value = false
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
        sharedPrefs.edit { putString("saved_location", config.name) }

        Toast.makeText(getApplication(), getApplication<Application>().getString(R.string.switched_to_location, config.name), Toast.LENGTH_SHORT).show()
    }

    fun recalculate() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = PrayerTimesRepository.get(getApplication()).getTimes(
                latitude = _currentLatitude.value,
                longitude = _currentLongitude.value,
                method = currentPrayerMethod,
                madhab = currentPrayerMadhab,
                is24HourFormat = _is24HourFormat.value,
                highLatitudeRule = _highLatitudeRule.value
            )
            when (result) {
                is PrayerTimesRepository.FetchResult.Success -> {
                    _prayerTimes.value = result.items
                    _lastUpdated.value = System.currentTimeMillis()
                    _sourceLabel.value = when (result.source) {
                        PrayerTimesRepository.Source.NETWORK -> "Aladhan API"
                        PrayerTimesRepository.Source.CACHE -> "Cached"
                        PrayerTimesRepository.Source.OFFLINE -> "Offline"
                    }
                    if (result.hijriDate != null) _hijriDate.value = result.hijriDate
                }
                is PrayerTimesRepository.FetchResult.Failure -> {
                    _prayerTimes.value = result.items
                    _sourceLabel.value = "Offline"
                    Toast.makeText(getApplication(), result.error, Toast.LENGTH_SHORT).show()
                }
            }
            _isLoading.value = false
            updateCountdown()
        }
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

    /** Force a fresh fetch from the network, bypassing the local cache. */
    fun forceRefresh() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = PrayerTimesRepository.get(getApplication()).getTimes(
                latitude = _currentLatitude.value,
                longitude = _currentLongitude.value,
                method = currentPrayerMethod,
                madhab = currentPrayerMadhab,
                is24HourFormat = _is24HourFormat.value,
                highLatitudeRule = _highLatitudeRule.value
            )
            when (result) {
                is PrayerTimesRepository.FetchResult.Success -> {
                    _prayerTimes.value = result.items
                    _lastUpdated.value = System.currentTimeMillis()
                    _sourceLabel.value = "Aladhan API"
                    if (result.hijriDate != null) _hijriDate.value = result.hijriDate
                }
                is PrayerTimesRepository.FetchResult.Failure -> {
                    Toast.makeText(getApplication(), result.error, Toast.LENGTH_SHORT).show()
                }
            }
            _isLoading.value = false
            updateCountdown()
        }
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

    // --- Settings Mutators ---

    fun toggle24HourFormat(enabled: Boolean) {
        _is24HourFormat.value = enabled
        val sharedPrefs = getApplication<Application>().getSharedPreferences("prayer_times_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit { putBoolean("is_24_hour_format", enabled) }
        recalculate()
    }

    fun setMadhab(madhab: Madhab) {
        _currentMadhab.value = madhab
        val sharedPrefs = getApplication<Application>().getSharedPreferences("prayer_times_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit { putString("current_madhab", madhab.name) }
        recalculate()
    }

    fun setHijriDayOffset(offset: Int) {
        _hijriDayOffset.value = offset
        val sharedPrefs = getApplication<Application>().getSharedPreferences("prayer_times_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit { putInt("hijri_day_offset", offset) }
        recalculate()
    }

    fun setPrePrayerReminderMinutes(minutes: Int) {
        _prePrayerReminderMinutes.value = minutes
        val sharedPrefs = getApplication<Application>().getSharedPreferences("prayer_times_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit { putInt("pre_prayer_reminder_mins", minutes) }
    }

    fun setHighLatitudeRule(rule: AppHighLatitudeRule) {
        _highLatitudeRule.value = rule
        val sharedPrefs = getApplication<Application>().getSharedPreferences("prayer_times_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit { putString("high_latitude_rule", rule.name) }
        recalculate()
    }

    /**
     * Converts a LocalDate to formatted Hijri string (Native Chronology)
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun formatHijriDate(localDate: LocalDate): String {
        return try {
            val adjustedDate = localDate.plusDays(_hijriDayOffset.value.toLong())
            val hijrahDate = HijrahDate.from(adjustedDate)
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
            val adjustedDate = localDate.plusDays(_hijriDayOffset.value.toLong())
            val hijrahDate = HijrahDate.from(adjustedDate)
            hijrahDate.get(ChronoField.DAY_OF_MONTH)
        } catch (e: Exception) {
            ((localDate.dayOfMonth + 12 + _hijriDayOffset.value) % 30).let { if (it <= 0) it + 30 else it }
        }
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
    }
}
