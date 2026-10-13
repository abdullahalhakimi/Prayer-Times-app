package com.prayertimesApp.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _is24HourFormat = MutableStateFlow(prefs.getBoolean(KEY_24_HOUR, false))
    val is24HourFormat: StateFlow<Boolean> = _is24HourFormat.asStateFlow()

    private val _hijriDayOffset = MutableStateFlow(prefs.getInt(KEY_HIJRI_OFFSET, 0))
    val hijriDayOffset: StateFlow<Int> = _hijriDayOffset.asStateFlow()

    private val _prePrayerReminderMinutes = MutableStateFlow(prefs.getInt(KEY_PRE_PRAYER_MINS, 10))
    val prePrayerReminderMinutes: StateFlow<Int> = _prePrayerReminderMinutes.asStateFlow()

    private val _highLatitudeRule = MutableStateFlow(
        try {
            AppHighLatitudeRule.valueOf(
                prefs.getString(KEY_HIGH_LATITUDE_RULE, AppHighLatitudeRule.MIDDLE_OF_THE_NIGHT.name)
                    ?: AppHighLatitudeRule.MIDDLE_OF_THE_NIGHT.name
            )
        } catch (_: Exception) {
            AppHighLatitudeRule.MIDDLE_OF_THE_NIGHT
        }
    )
    val highLatitudeRule: StateFlow<AppHighLatitudeRule> = _highLatitudeRule.asStateFlow()

    private val _appLanguage = MutableStateFlow(prefs.getString(KEY_APP_LANGUAGE, "system") ?: "system")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    fun set24HourFormat(enabled: Boolean) {
        prefs.edit { putBoolean(KEY_24_HOUR, enabled) }
        _is24HourFormat.value = enabled
    }

    fun setHijriDayOffset(offset: Int) {
        prefs.edit { putInt(KEY_HIJRI_OFFSET, offset) }
        _hijriDayOffset.value = offset
    }

    fun setPrePrayerReminderMinutes(minutes: Int) {
        prefs.edit { putInt(KEY_PRE_PRAYER_MINS, minutes) }
        _prePrayerReminderMinutes.value = minutes
    }

    fun setHighLatitudeRule(rule: AppHighLatitudeRule) {
        prefs.edit { putString(KEY_HIGH_LATITUDE_RULE, rule.name) }
        _highLatitudeRule.value = rule
    }

    fun setAppLanguage(languageCode: String) {
        prefs.edit { putString(KEY_APP_LANGUAGE, languageCode) }
        _appLanguage.value = languageCode
    }

    companion object {
        private const val PREFS_NAME = "prayer_user_prefs"
        private const val KEY_24_HOUR = "is_24_hour_format"
        private const val KEY_HIJRI_OFFSET = "hijri_day_offset"
        private const val KEY_PRE_PRAYER_MINS = "pre_prayer_reminder_minutes"
        private const val KEY_HIGH_LATITUDE_RULE = "high_latitude_rule"
        private const val KEY_APP_LANGUAGE = "app_language"
    }
}
