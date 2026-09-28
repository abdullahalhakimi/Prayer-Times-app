package com.prayertimesApp.data

import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import java.util.Calendar
import java.util.Date

enum class PrayerCalculationMethod(val displayName: String, val method: CalculationMethod) {
    UMM_AL_QURA("Umm Al-Qura", CalculationMethod.UMM_AL_QURA),
    MUSLIM_WORLD_LEAGUE("Muslim World League", CalculationMethod.MUSLIM_WORLD_LEAGUE),
    EGYPTIAN("Egyptian General Authority", CalculationMethod.EGYPTIAN),
    KARACHI("Univ. of Islamic Sciences, Karachi", CalculationMethod.KARACHI),
    NORTH_AMERICA("ISNA (North America)", CalculationMethod.NORTH_AMERICA),
    MOONSIGHTING_COMMITTEE("Moonsighting Committee", CalculationMethod.MOONSIGHTING_COMMITTEE),
    KUWAIT("Kuwait", CalculationMethod.KUWAIT),
    QATAR("Qatar", CalculationMethod.QATAR),
    SINGAPORE("Singapore", CalculationMethod.SINGAPORE),
    TURKEY("Turkey", CalculationMethod.TURKEY);

    /**
     * Aladhan API calculation method id.
     * https://api.aladhan.com/v1/methods
     */
    val aladhanId: Int
        get() = when (this) {
            KARACHI -> 1
            NORTH_AMERICA -> 2
            MUSLIM_WORLD_LEAGUE -> 3
            UMM_AL_QURA -> 4
            EGYPTIAN -> 5
            KUWAIT -> 7
            MOONSIGHTING_COMMITTEE -> 9
            QATAR -> 10
            SINGAPORE -> 11
            TURKEY -> 13
        }
}

enum class PrayerMadhab(val displayName: String, val madhab: Madhab) {
    STANDARD("Shafi, Maliki, Hanbali (Standard)", Madhab.SHAFI),
    HANAFI("Hanafi", Madhab.HANAFI);

    /** Aladhan school id: 0 = Shafi, 1 = Hanafi. */
    val aladhanSchool: Int
        get() = when (this) {
            STANDARD -> 0
            HANAFI -> 1
        }
}

enum class AppHighLatitudeRule(val displayName: String) {
    MIDDLE_OF_THE_NIGHT("Middle of the Night"),
    SEVENTH_OF_THE_NIGHT("Seventh of the Night"),
    TWILIGHT_ANGLE("Twilight Angle")
}

data class PrayerTimeItem(
    val name: String,
    val date: Date,
    val formattedTime: String
)

object PrayerTimesCalculator {

    /**
     * Calculates prayer times for a specific coordinates, calculation method, madhab, and date.
     */
    fun calculateTimes(
        latitude: Double,
        longitude: Double,
        method: CalculationMethod,
        madhab: Madhab,
        date: Date = Date(),
        is24HourFormat: Boolean = false,
        highLatitudeRule: AppHighLatitudeRule = AppHighLatitudeRule.MIDDLE_OF_THE_NIGHT
    ): List<PrayerTimeItem> {
        val coordinates = Coordinates(latitude, longitude)
        val calendar = Calendar.getInstance().apply { time = date }
        
        val dateComponents = DateComponents(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1, // DateComponents month is 1-based, Calendar.MONTH is 0-based
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        val parameters = method.parameters.apply {
            this.madhab = madhab
        }

        val prayerTimes = PrayerTimes(coordinates, dateComponents, parameters)
        
        // Calculate Qiyam (traditionally last 1/3 of the night, or halfway between sunset/Maghrib and Fajr of the next day)
        val maghribTime = prayerTimes.maghrib()
        val fajrTime = prayerTimes.fajr()
        
        val qiyamTime = if (maghribTime != null && fajrTime != null) {
            val calMaghrib = Calendar.getInstance().apply { time = maghribTime }
            val calFajr = Calendar.getInstance().apply { time = fajrTime }
            if (calFajr.before(calMaghrib)) {
                calFajr.add(Calendar.DATE, 1)
            }
            val diff = calFajr.timeInMillis - calMaghrib.timeInMillis
            val qiyamMillis = calMaghrib.timeInMillis + (diff * 2 / 3) // last third of night starts
            Date(qiyamMillis)
        } else {
            val fallback = Calendar.getInstance().apply {
                time = date
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 45)
                set(Calendar.SECOND, 0)
            }
            fallback.time
        }

        val timePattern = if (is24HourFormat) "HH:mm" else "hh:mm a"
        val df = java.text.SimpleDateFormat(timePattern, java.util.Locale.getDefault())

        val list = mutableListOf<PrayerTimeItem>()
        prayerTimes.fajr()?.let { list.add(PrayerTimeItem("Fajr", it, df.format(it))) }
        prayerTimes.sunrise()?.let { list.add(PrayerTimeItem("Sunrise", it, df.format(it))) }
        prayerTimes.dhuhr()?.let { list.add(PrayerTimeItem("Dhuhr", it, df.format(it))) }
        prayerTimes.asr()?.let { list.add(PrayerTimeItem("Asr", it, df.format(it))) }
        prayerTimes.maghrib()?.let { list.add(PrayerTimeItem("Maghrib", it, df.format(it))) }
        prayerTimes.isha()?.let { list.add(PrayerTimeItem("Isha", it, df.format(it))) }
        list.add(PrayerTimeItem("Qiyam", qiyamTime, df.format(qiyamTime)))

        return list
    }
}

