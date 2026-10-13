package com.prayertimesApp.data

import androidx.annotation.StringRes
import com.batoulapps.adhan.Coordinates
import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab
import com.batoulapps.adhan.PrayerTimes
import com.batoulapps.adhan.data.DateComponents
import com.prayertimesApp.R
import java.util.Calendar
import java.util.Date

enum class PrayerCalculationMethod(
    val displayName: String,
    val method: CalculationMethod,
    @StringRes val nameResId: Int
) {
    UMM_AL_QURA("Umm Al-Qura", CalculationMethod.UMM_AL_QURA, R.string.calc_method_umm_al_qura),
    MUSLIM_WORLD_LEAGUE("Muslim World League", CalculationMethod.MUSLIM_WORLD_LEAGUE, R.string.calc_method_mwl),
    EGYPTIAN("Egyptian General Authority", CalculationMethod.EGYPTIAN, R.string.calc_method_egyptian),
    KARACHI("Univ. of Islamic Sciences, Karachi", CalculationMethod.KARACHI, R.string.calc_method_karachi),
    NORTH_AMERICA("ISNA (North America)", CalculationMethod.NORTH_AMERICA, R.string.calc_method_isna),
    MOONSIGHTING_COMMITTEE("Moonsighting Committee", CalculationMethod.MOONSIGHTING_COMMITTEE, R.string.calc_method_moonsighting),
    KUWAIT("Kuwait", CalculationMethod.KUWAIT, R.string.calc_method_kuwait),
    QATAR("Qatar", CalculationMethod.QATAR, R.string.calc_method_qatar),
    SINGAPORE("Singapore", CalculationMethod.SINGAPORE, R.string.calc_method_singapore),
    TURKEY("Turkey", CalculationMethod.TURKEY, R.string.calc_method_turkey);

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

enum class PrayerMadhab(
    val displayName: String,
    val madhab: Madhab,
    @StringRes val nameResId: Int
) {
    STANDARD("Shafi, Maliki, Hanbali (Standard)", Madhab.SHAFI, R.string.madhab_standard),
    HANAFI("Hanafi", Madhab.HANAFI, R.string.madhab_hanafi);

    /** Aladhan school id: 0 = Shafi, 1 = Hanafi. */
    val aladhanSchool: Int
        get() = when (this) {
            STANDARD -> 0
            HANAFI -> 1
        }
}

enum class AppHighLatitudeRule(
    val displayName: String,
    @StringRes val nameResId: Int
) {
    MIDDLE_OF_THE_NIGHT("Middle of the Night", R.string.high_lat_middle_night),
    SEVENTH_OF_THE_NIGHT("Seventh of the Night", R.string.high_lat_seventh_night),
    TWILIGHT_ANGLE("Twilight Angle", R.string.high_lat_twilight_angle)
}

data class PrayerTimeItem(
    val name: String,
    val date: Date,
    val formattedTime: String,
    @StringRes val nameResId: Int = when (name) {
        "Fajr" -> R.string.prayer_fajr
        "Sunrise" -> R.string.prayer_sunrise
        "Dhuhr" -> R.string.prayer_dhuhr
        "Asr" -> R.string.prayer_asr
        "Maghrib" -> R.string.prayer_maghrib
        "Isha" -> R.string.prayer_isha
        "Qiyam" -> R.string.prayer_qiyam
        else -> R.string.prayer_fajr
    }
)

object PrayerTimesCalculator {

    /**
     * Calculates prayer times for specific coordinates, calculation method, madhab, and date.
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
        prayerTimes.fajr()?.let { list.add(PrayerTimeItem("Fajr", it, df.format(it), R.string.prayer_fajr)) }
        prayerTimes.sunrise()?.let { list.add(PrayerTimeItem("Sunrise", it, df.format(it), R.string.prayer_sunrise)) }
        prayerTimes.dhuhr()?.let { list.add(PrayerTimeItem("Dhuhr", it, df.format(it), R.string.prayer_dhuhr)) }
        prayerTimes.asr()?.let { list.add(PrayerTimeItem("Asr", it, df.format(it), R.string.prayer_asr)) }
        prayerTimes.maghrib()?.let { list.add(PrayerTimeItem("Maghrib", it, df.format(it), R.string.prayer_maghrib)) }
        prayerTimes.isha()?.let { list.add(PrayerTimeItem("Isha", it, df.format(it), R.string.prayer_isha)) }
        list.add(PrayerTimeItem("Qiyam", qiyamTime, df.format(qiyamTime), R.string.prayer_qiyam))

        return list
    }
}
