package com.example.data

import android.content.Context
import android.util.Log
import com.example.data.api.AladhanApi
import com.example.data.api.NetworkModule
import com.example.data.api.TimingsResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import androidx.core.content.edit

/**
 * Fetches prayer times from the free Aladhan API and caches monthly results
 * locally. Falls back to the on-device Adhan library when offline.
 */
class PrayerTimesRepository private constructor(context: Context) {

    private val appContext: Context = context.applicationContext
    private val api: AladhanApi = NetworkModule.aladhanApi
    private val prefs = appContext.getSharedPreferences("aladhan_cache", Context.MODE_PRIVATE)

    private val moshi: Moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val dailyAdapter = moshi.adapter(TimingsResponse::class.java)
    private val monthlyAdapter = moshi.adapter<List<DailyTimings>>(
        Types.newParameterizedType(List::class.java, DailyTimings::class.java)
    )

    data class DailyTimings(
        val date: String,    // yyyy-MM-dd
        val fajr: String,
        val sunrise: String,
        val dhuhr: String,
        val asr: String,
        val maghrib: String,
        val isha: String
    )

    sealed class FetchResult {
        data class Success(
            val items: List<PrayerTimeItem>,
            val source: Source,
            val hijriDate: String?
        ) : FetchResult()
        data class Failure(val items: List<PrayerTimeItem>, val error: String) : FetchResult()
    }

    enum class Source { NETWORK, OFFLINE, CACHE }

    /**
     * Get prayer times for a single date. Tries the Aladhan API first, then
     * the local calculator if the network call fails.
     */
    suspend fun getTimes(
        latitude: Double,
        longitude: Double,
        method: PrayerCalculationMethod,
        madhab: PrayerMadhab,
        date: Date = Date()
    ): FetchResult = withContext(Dispatchers.IO) {
        val cacheKey = buildDailyKey(latitude, longitude, method, madhab, date)
        val cachedJson = prefs.getString(cacheKey, null)
        if (cachedJson != null && isSameDay(date, prefs.getLong(cacheKey + "_ts", 0))) {
            try {
                val cached = dailyAdapter.fromJson(cachedJson)
                if (cached != null) {
                    return@withContext FetchResult.Success(
                        items = toPrayerTimeItems(cached.timings, date),
                        source = Source.CACHE,
                        hijriDate = formatHijriFromApi(cached.date)
                    )
                }
            } catch (_: Exception) {
                // cache corrupted, fall through
            }
        }

        try {
            val dateParam = formatDateParam(date)
            val apiResponse = api.getTimings(
                date = dateParam,
                latitude = latitude,
                longitude = longitude,
                method = method.aladhanId,
                school = madhab.aladhanSchool
            )
            val response = apiResponse.data
            prefs.edit()
                .putString(cacheKey, dailyAdapter.toJson(response))
                .putLong(cacheKey + "_ts", System.currentTimeMillis())
                .apply()
            FetchResult.Success(
                items = toPrayerTimeItems(response.timings, date),
                source = Source.NETWORK,
                hijriDate = formatHijriFromApi(response.date)
            )
        } catch (e: HttpException) {
            Log.w(TAG, "Aladhan HTTP ${e.code()}; falling back to local calc", e)
            FetchResult.Failure(
                items = PrayerTimesCalculator.calculateTimes(
                    latitude, longitude, method.method, madhab.madhab, date
                ),
                error = "Server error ${e.code()}, used offline calculation."
            )
        } catch (e: IOException) {
            Log.w(TAG, "Aladhan network error; falling back to local calc", e)
            FetchResult.Failure(
                items = PrayerTimesCalculator.calculateTimes(
                    latitude, longitude, method.method, madhab.madhab, date
                ),
                error = "Offline mode: using local calculation."
            )
        } catch (e: Exception) {
            Log.e(TAG, "Aladhan unexpected error; falling back to local calc", e)
            FetchResult.Failure(
                items = PrayerTimesCalculator.calculateTimes(
                    latitude, longitude, method.method, madhab.madhab, date
                ),
                error = "Network unavailable, used offline calculation."
            )
        }
    }

    /**
     * Get a full month's calendar for a location, caching the result in
     * SharedPreferences so subsequent views are instant and offline.
     */
    suspend fun getMonthlyCalendar(
        latitude: Double,
        longitude: Double,
        method: PrayerCalculationMethod,
        madhab: PrayerMadhab,
        year: Int,
        month: Int
    ): List<DailyTimings> = withContext(Dispatchers.IO) {
        val key = buildMonthlyKey(latitude, longitude, method, madhab, year, month)
        val cached = prefs.getString(key, null)
        if (cached != null) {
            try {
                return@withContext monthlyAdapter.fromJson(cached) ?: emptyList()
            } catch (_: Exception) {
                // fall through
            }
        }

        try {
            val apiResponse = api.getHijriCalendar(
                year = year,
                month = month,
                latitude = latitude,
                longitude = longitude,
                method = method.aladhanId,
                school = madhab.aladhanSchool
            )
            val response = apiResponse.data
            val list = response.hijri.orEmpty().map { entry ->
                DailyTimings(
                    date = entry.date.gregorian.date.orEmpty(),
                    fajr = entry.timings.fajr,
                    sunrise = entry.timings.sunrise,
                    dhuhr = entry.timings.dhuhr,
                    asr = entry.timings.asr,
                    maghrib = entry.timings.maghrib,
                    isha = entry.timings.isha
                )
            }
            prefs.edit { putString(key, monthlyAdapter.toJson(list)) }
            list
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch monthly calendar; returning empty", e)
            emptyList()
        }
    }

    // --- helpers ---

    private fun toPrayerTimeItems(
        timings: com.example.data.api.Timings,
        date: Date
    ): List<PrayerTimeItem> {
        val df = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val tz = TimeZone.getDefault()
        df.timeZone = tz

        val fajr = parseApiTime(timings.fajr, date, tz)
        val sunrise = parseApiTime(timings.sunrise, date, tz)
        val dhuhr = parseApiTime(timings.dhuhr, date, tz)
        val asr = parseApiTime(timings.asr, date, tz)
        val maghrib = parseApiTime(timings.maghrib, date, tz)
        val isha = parseApiTime(timings.isha, date, tz)

        val qiyam = if (maghrib != null && fajr != null) {
            val nextFajr = if (fajr.before(maghrib)) {
                Calendar.getInstance().apply {
                    time = fajr
                    add(Calendar.DATE, 1)
                }.time
            } else fajr
            val diff = nextFajr.time - maghrib.time
            Date(maghrib.time + (diff * 2L / 3L))
        } else {
            Calendar.getInstance().apply {
                time = date
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 45)
                set(Calendar.SECOND, 0)
            }.time
        }

        val list = mutableListOf<PrayerTimeItem>()
        fajr?.let { list.add(PrayerTimeItem("Fajr", it, df.format(it))) }
        sunrise?.let { list.add(PrayerTimeItem("Sunrise", it, df.format(it))) }
        dhuhr?.let { list.add(PrayerTimeItem("Dhuhr", it, df.format(it))) }
        asr?.let { list.add(PrayerTimeItem("Asr", it, df.format(it))) }
        maghrib?.let { list.add(PrayerTimeItem("Maghrib", it, df.format(it))) }
        isha?.let { list.add(PrayerTimeItem("Isha", it, df.format(it))) }
        list.add(PrayerTimeItem("Qiyam", qiyam, df.format(qiyam)))
        return list
    }

    private fun parseApiTime(value: String, baseDate: Date, tz: TimeZone): Date? {
        val cleaned = value.substringBefore(' ').trim() // strip "(CEST)" etc.
        val parts = cleaned.split(":")
        if (parts.size < 2) return null
        val h = parts[0].toIntOrNull() ?: return null
        val m = parts[1].toIntOrNull() ?: return null
        val cal = Calendar.getInstance().apply {
            timeZone = tz
            time = baseDate
            set(Calendar.HOUR_OF_DAY, h)
            set(Calendar.MINUTE, m)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.time
    }

    private fun formatHijriFromApi(date: com.example.data.api.AladhanDate): String? {
        return try {
            val hijri = date.hijri
            val day = hijri.day
            val month = hijri.month.en
            val year = hijri.year
            "$day $month $year AH"
        } catch (_: Exception) {
            null
        }
    }

    private fun formatDateParam(date: Date): String {
        val df = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        df.timeZone = TimeZone.getDefault()
        return df.format(date)
    }

    private fun isSameDay(date: Date, ts: Long): Boolean {
        if (ts == 0L) return false
        val a = Calendar.getInstance().apply { time = date }
        val b = Calendar.getInstance().apply { timeInMillis = ts }
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
    }

    private fun buildDailyKey(
        lat: Double, lon: Double, m: PrayerCalculationMethod, md: PrayerMadhab, date: Date
    ): String {
        val df = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return "daily|${m.aladhanId}|${md.aladhanSchool}|${"%.4f".format(lat)}|${"%.4f".format(lon)}|${df.format(date)}"
    }

    private fun buildMonthlyKey(
        lat: Double, lon: Double, m: PrayerCalculationMethod, md: PrayerMadhab, year: Int, month: Int
    ): String {
        return "monthly|${m.aladhanId}|${md.aladhanSchool}|${"%.4f".format(lat)}|${"%.4f".format(lon)}|$year|${"%02d".format(month)}"
    }

    companion object {
        private const val TAG = "PrayerTimesRepo"

        @Volatile private var INSTANCE: PrayerTimesRepository? = null

        fun get(context: Context): PrayerTimesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PrayerTimesRepository(context).also { INSTANCE = it }
            }
        }
    }
}
