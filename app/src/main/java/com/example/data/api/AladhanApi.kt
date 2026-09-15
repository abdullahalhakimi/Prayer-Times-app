package com.example.data.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Aladhan API - free public prayer times API. No key required.
 * Docs: https://aladhan.com/prayer-times-api
 */
interface AladhanApi {

    /**
     * Fetch prayer times for a specific date.
     * @param date Either "today" or DD-MM-YYYY (e.g. "15-04-2026")
     * @param method Calculation method id (see AladhanMethod enum)
     * @param school Madhab: 0 = Shafi (Standard), 1 = Hanafi
     * @param tune Optional comma-separated string to offset each prayer in minutes
     *             (Fajr,Sunrise,Dhuhr,Asr,Maghrib,Isha)
     */
    @GET("v1/timings/{date}")
    suspend fun getTimings(
        @Path("date") date: String,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int,
        @Query("school") school: Int = 0,
        @Query("timezonestring") timezone: String? = null,
        @Query("calendarMethod") calendarMethod: String? = null
    ): TimingsApiResponse

    /**
     * Fetch the full Hijri+Gregorian calendar for a month (timings included).
     * Useful for monthly caching.
     */
    @GET("v1/hijriCalendar/{year}/{month}")
    suspend fun getHijriCalendar(
        @Path("year") year: Int,
        @Path("month") month: Int,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int,
        @Query("school") school: Int = 0
    ): HijriCalendarApiResponse
}
