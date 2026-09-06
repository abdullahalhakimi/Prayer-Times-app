package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Top-level response shape used by all Aladhan v1 endpoints:
 * { "code": 200, "status": "OK", "data": <payload> }
 */
@JsonClass(generateAdapter = true)
data class AladhanResponse<T>(
    val code: Int,
    val status: String,
    val data: T
)

// --- /timings/{date} ---

@JsonClass(generateAdapter = true)
data class TimingsResponse(
    val timings: Timings,
    val date: AladhanDate,
    val meta: Meta
)

@JsonClass(generateAdapter = true)
data class Timings(
    @Json(name = "Fajr") val fajr: String,
    @Json(name = "Sunrise") val sunrise: String,
    @Json(name = "Dhuhr") val dhuhr: String,
    @Json(name = "Asr") val asr: String,
    @Json(name = "Sunset") val sunset: String?,
    @Json(name = "Maghrib") val maghrib: String,
    @Json(name = "Isha") val isha: String,
    @Json(name = "Imsak") val imsak: String?,
    @Json(name = "Midnight") val midnight: String?
)

@JsonClass(generateAdapter = true)
data class AladhanDate(
    val readable: String,
    val timestamp: String,
    @Json(name = "gregorian") val gregorian: DateBlock,
    @Json(name = "hijri") val hijri: DateBlock
)

@JsonClass(generateAdapter = true)
data class DateBlock(
    val date: String?,
    val format: String?,
    val day: String,
    val weekday: Weekday?,
    val month: MonthInfo,
    val year: String,
    val designation: Designation?
)

@JsonClass(generateAdapter = true)
data class Weekday(
    val en: String?
)

@JsonClass(generateAdapter = true)
data class MonthInfo(
    val number: Int,
    val en: String,
    val ar: String?
)

@JsonClass(generateAdapter = true)
data class Designation(
    val abbreviated: String,
    val expanded: String
)

@JsonClass(generateAdapter = true)
data class Meta(
    val latitude: Double,
    val longitude: Double,
    val timezone: String?,
    val method: MethodInfo,
    val school: String?,
    val offset: Map<String, Int>?
)

@JsonClass(generateAdapter = true)
data class MethodInfo(
    val id: Int,
    val name: String,
    val params: Map<String, Int>?
)

// --- /hijriCalendar/{year}/{month} ---

@JsonClass(generateAdapter = true)
data class HijriCalendarResponse(
    val hijri: List<CalendarEntry>?
)

@JsonClass(generateAdapter = true)
data class CalendarEntry(
    val date: AladhanDate,
    val timings: Timings
)
