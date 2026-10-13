package com.prayertimesApp

import androidx.test.core.app.ApplicationProvider
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab
import com.prayertimesApp.data.AppHighLatitudeRule
import com.prayertimesApp.data.PrayerTimesCalculator
import com.prayertimesApp.data.UserPreferencesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.Date

@RunWith(RobolectricTestRunner::class)
class PrayerTimesViewModelTest {

    @Test
    fun testPrayerTimesCalculatorReturnsValidItems() {
        val date = Date()
        val items = PrayerTimesCalculator.calculateTimes(
            latitude = 51.5074,
            longitude = -0.1278,
            method = CalculationMethod.MOONSIGHTING_COMMITTEE,
            madhab = Madhab.SHAFI,
            date = date,
            is24HourFormat = true,
            highLatitudeRule = AppHighLatitudeRule.MIDDLE_OF_THE_NIGHT
        )

        assertNotNull(items)
        assertTrue(items.isNotEmpty())
        assertEquals(7, items.size) // Fajr, Sunrise, Dhuhr, Asr, Maghrib, Isha, Qiyam
    }

    @Test
    fun testUserPreferencesRepositoryDefaultValues() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val repository = UserPreferencesRepository(context)

        assertEquals(false, repository.is24HourFormat.value)
        assertEquals(0, repository.hijriDayOffset.value)
        assertEquals(10, repository.prePrayerReminderMinutes.value)
        assertEquals(AppHighLatitudeRule.MIDDLE_OF_THE_NIGHT, repository.highLatitudeRule.value)
        assertEquals("system", repository.appLanguage.value)
    }

    @Test
    fun testUserPreferencesRepositoryUpdates() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val repository = UserPreferencesRepository(context)

        repository.set24HourFormat(true)
        assertEquals(true, repository.is24HourFormat.value)

        repository.setHijriDayOffset(1)
        assertEquals(1, repository.hijriDayOffset.value)

        repository.setPrePrayerReminderMinutes(15)
        assertEquals(15, repository.prePrayerReminderMinutes.value)

        repository.setHighLatitudeRule(AppHighLatitudeRule.SEVENTH_OF_THE_NIGHT)
        assertEquals(AppHighLatitudeRule.SEVENTH_OF_THE_NIGHT, repository.highLatitudeRule.value)

        repository.setAppLanguage("ar")
        assertEquals("ar", repository.appLanguage.value)
    }
}
