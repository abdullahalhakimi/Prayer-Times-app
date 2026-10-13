package com.prayertimesApp.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// --- /timings/{date} ---

@JsonClass(generateAdapter = true)
data class TimingsApiResponse(
    val code: Int,
    val status: String,
    val data: TimingsResponse
)

@JsonClass(generateAdapter = true)
data class TimingsResponse(
    val timings: Timings,
    val date: AladhanDate,
    val meta: Meta? = null
)

@JsonClass(generateAdapter = true)
data class Timings(
    @Json(name = "Fajr") val fajr: String,
    @Json(name = "Sunrise") val sunrise: String,
    @Json(name = "Dhuhr") val dhuhr: String,
    @Json(name = "Asr") val asr: String,
    @Json(name = "Sunset") val sunset: String? = null,
    @Json(name = "Maghrib") val maghrib: String,
    @Json(name = "Isha") val isha: String,
    @Json(name = "Imsak") val imsak: String? = null,
    @Json(name = "Midnight") val midnight: String? = null
)

@JsonClass(generateAdapter = true)
data class AladhanDate(
    val readable: String? = null,
    val timestamp: String? = null,
    @Json(name = "gregorian") val gregorian: DateBlock,
    @Json(name = "hijri") val hijri: DateBlock
)

@JsonClass(generateAdapter = true)
data class DateBlock(
    val date: String? = null,
    val format: String? = null,
    val day: String? = null,
    val weekday: Weekday? = null,
    val month: MonthInfo,
    val year: String? = null,
    val designation: Designation? = null
)

@JsonClass(generateAdapter = true)
data class Weekday(
    val en: String? = null,
    val ar: String? = null
)

@JsonClass(generateAdapter = true)
data class MonthInfo(
    val number: Int,
    val en: String,
    val ar: String? = null
)

@JsonClass(generateAdapter = true)
data class Designation(
    val abbreviated: String? = null,
    val expanded: String? = null
)

@JsonClass(generateAdapter = true)
data class Meta(
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timezone: String? = null,
    val method: MethodInfo? = null,
    val school: String? = null,
    val offset: Map<String, Int>? = null
)

@JsonClass(generateAdapter = true)
data class MethodInfo(
    val id: Int? = null,
    val name: String? = null,
    val params: Map<String, Any>? = null
)

// --- /hijriCalendar/{year}/{month} ---

@JsonClass(generateAdapter = true)
data class HijriCalendarApiResponse(
    val code: Int,
    val status: String,
    val data: List<CalendarEntry>? = null
)

@JsonClass(generateAdapter = true)
data class CalendarEntry(
    val date: AladhanDate,
    val timings: Timings
)
