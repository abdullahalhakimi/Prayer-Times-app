package com.prayertimesApp

import com.prayertimesApp.data.api.HijriCalendarApiResponse
import com.prayertimesApp.data.api.NetworkModule
import com.prayertimesApp.data.api.TimingsApiResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AladhanApiParsingTest {

    @Test
    fun testParseTimingsApiResponse() {
        val json = """
            {
              "code": 200,
              "status": "OK",
              "data": {
                "timings": {
                  "Fajr": "04:36",
                  "Sunrise": "05:57",
                  "Dhuhr": "12:19",
                  "Asr": "15:48",
                  "Sunset": "18:42",
                  "Maghrib": "18:42",
                  "Isha": "20:12",
                  "Imsak": "04:26",
                  "Midnight": "00:20"
                },
                "date": {
                  "readable": "15 Apr 2026",
                  "timestamp": "1776222000",
                  "hijri": {
                    "date": "27-10-1447",
                    "format": "DD-MM-YYYY",
                    "day": "27",
                    "weekday": { "en": "Al Arba'a", "ar": "الاربعاء" },
                    "month": { "number": 10, "en": "Shawwāl", "ar": "شَوّال", "days": 29 },
                    "year": "1447",
                    "designation": { "abbreviated": "AH", "expanded": "Anno Hegirae" }
                  },
                  "gregorian": {
                    "date": "15-04-2026",
                    "format": "DD-MM-YYYY",
                    "day": "15",
                    "weekday": { "en": "Wednesday" },
                    "month": { "number": 4, "en": "April" },
                    "year": "2026",
                    "designation": { "abbreviated": "AD", "expanded": "Anno Domini" }
                  }
                },
                "meta": {
                  "latitude": 25.09538,
                  "longitude": 55.16171,
                  "timezone": "Asia/Dubai",
                  "method": {
                    "id": 4,
                    "name": "Umm Al-Qura University, Makkah",
                    "params": {
                      "Fajr": 18.5,
                      "Isha": "90 min"
                    }
                  },
                  "school": "STANDARD"
                }
              }
            }
        """.trimIndent()

        val adapter = NetworkModule.moshi.adapter(TimingsApiResponse::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertEquals(200, response?.code)
        assertEquals("04:36", response?.data?.timings?.fajr)
        assertEquals("27", response?.data?.date?.hijri?.day)
        assertEquals("Shawwāl", response?.data?.date?.hijri?.month?.en)
        assertEquals(4, response?.data?.meta?.method?.id)
        assertEquals(18.5, response?.data?.meta?.method?.params?.get("Fajr"))
        assertEquals("90 min", response?.data?.meta?.method?.params?.get("Isha"))
    }

    @Test
    fun testParseHijriCalendarApiResponse() {
        val json = """
            {
              "code": 200,
              "status": "OK",
              "data": [
                {
                  "timings": {
                    "Fajr": "05:05 (+04)",
                    "Sunrise": "06:24 (+04)",
                    "Dhuhr": "12:27 (+04)",
                    "Asr": "15:52 (+04)",
                    "Sunset": "18:30 (+04)",
                    "Maghrib": "18:30 (+04)",
                    "Isha": "20:00 (+04)"
                  },
                  "date": {
                    "readable": "20 Mar 2026",
                    "gregorian": {
                      "date": "20-03-2026",
                      "day": "20",
                      "month": { "number": 3, "en": "March" },
                      "year": "2026"
                    },
                    "hijri": {
                      "date": "01-10-1447",
                      "day": "1",
                      "month": { "number": 10, "en": "Shawwāl" },
                      "year": "1447"
                    }
                  }
                }
              ]
            }
        """.trimIndent()

        val adapter = NetworkModule.moshi.adapter(HijriCalendarApiResponse::class.java)
        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertEquals(200, response?.code)
        assertEquals(1, response?.data?.size)
        assertEquals("05:05 (+04)", response?.data?.get(0)?.timings?.fajr)
    }
}
