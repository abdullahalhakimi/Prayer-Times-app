package com.batoulapps.adhan

import com.batoulapps.adhan.data.DateComponents
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

class PrayerTimes(
    val coordinates: Coordinates,
    val date: DateComponents,
    val parameters: CalculationParameters
) {
    lateinit var fajr: Date
    lateinit var sunrise: Date
    lateinit var dhuhr: Date
    lateinit var asr: Date
    lateinit var maghrib: Date
    lateinit var isha: Date

    init {
        calculateTimes()
    }

    fun fajr(): Date? = fajr
    fun sunrise(): Date? = sunrise
    fun dhuhr(): Date? = dhuhr
    fun asr(): Date? = asr
    fun maghrib(): Date? = maghrib
    fun isha(): Date? = isha

    private fun calculateTimes() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(Calendar.YEAR, date.year)
            set(Calendar.MONTH, date.month - 1)
            set(Calendar.DAY_OF_MONTH, date.day)
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

        // Calculate solar declination delta (degrees)
        val declination = 23.45 * Math.sin(Math.toRadians(360.0 / 365.0 * (284 + dayOfYear)))

        // Calculate Equation of Time EoT (minutes)
        val b = Math.toRadians(360.0 / 365.0 * (dayOfYear - 81))
        val eot = 9.87 * Math.sin(2.0 * b) - 7.53 * Math.cos(b) - 1.5 * Math.sin(b)

        // Solar Noon (transit) in hours (UTC)
        val transit = 12.0 - (coordinates.longitude / 15.0) - (eot / 60.0)

        // Sunrise/Sunset Hour Angle
        val hSunrise = getHourAngle(-0.833, coordinates.latitude, declination)
        val sunriseHours = transit - (hSunrise / 15.0)
        val sunsetHours = transit + (hSunrise / 15.0)

        // Calculations for Fajr
        val hFajr = getHourAngle(-parameters.fajrAngle, coordinates.latitude, declination)
        val fajrHours = transit - (hFajr / 15.0)

        // Calculations for Asr (Shafi'i or Hanafi shadow factor)
        val s = if (parameters.madhab == Madhab.HANAFI) 2.0 else 1.0
        val diffRad = Math.toRadians(Math.abs(coordinates.latitude - declination))
        var asrAlpha = Math.toDegrees(Math.atan(1.0 / (s + Math.tan(diffRad))))
        val hAsr = getHourAngle(asrAlpha, coordinates.latitude, declination)
        val asrHours = transit + (hAsr / 15.0)

        // Calculations for Isha
        var ishaHours = 0.0
        if (parameters.method == CalculationMethod.UMM_AL_QURA || parameters.method == CalculationMethod.QATAR) {
            // Umm Al-Qura / Qatar uses exactly 90 minutes after Maghrib (Sunset)
            ishaHours = sunsetHours + 1.5
        } else {
            val hIsha = getHourAngle(-parameters.ishaAngle, coordinates.latitude, declination)
            ishaHours = transit + (hIsha / 15.0)
        }

        // Convert the calculated Double UTC-relative hours to Date objects
        fajr = getDateFromHours(fajrHours, date.year, date.month, date.day)
        sunrise = getDateFromHours(sunriseHours, date.year, date.month, date.day)
        dhuhr = getDateFromHours(transit, date.year, date.month, date.day)
        asr = getDateFromHours(asrHours, date.year, date.month, date.day)
        maghrib = getDateFromHours(sunsetHours, date.year, date.month, date.day)
        isha = getDateFromHours(ishaHours, date.year, date.month, date.day)
    }

    private fun getHourAngle(alpha: Double, lat: Double, decl: Double): Double {
        val latRad = Math.toRadians(lat)
        val declRad = Math.toRadians(decl)
        val alphaRad = Math.toRadians(alpha)
        
        val cosH = (Math.sin(alphaRad) - Math.sin(latRad) * Math.sin(declRad)) / 
                   (Math.cos(latRad) * Math.cos(declRad))
                   
        if (cosH > 1.0) return 0.0
        if (cosH < -1.0) return 180.0
        return Math.toDegrees(Math.acos(cosH))
    }

    private fun getDateFromHours(hours: Double, year: Int, month: Int, day: Int): Date {
        var normalizedHours = hours
        while (normalizedHours < 0) normalizedHours += 24.0
        while (normalizedHours >= 24) normalizedHours -= 24.0

        val totalSeconds = (normalizedHours * 3600).toInt()
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month - 1)
            set(Calendar.DAY_OF_MONTH, day)
            add(Calendar.SECOND, totalSeconds)
        }
        return calendar.time
    }
}
