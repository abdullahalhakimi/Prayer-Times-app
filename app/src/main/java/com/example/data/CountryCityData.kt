package com.prayertimesApp.data

import java.util.Locale

data class Country(val name: String, val code: String)
data class City(val name: String, val lat: Double, val lon: Double, val countryCode: String)

object CountryCityProvider {
    val countries: List<Country> by lazy {
        Locale.getISOCountries().map { code ->
            val locale = Locale.Builder().setRegion(code).build()
            Country(locale.displayCountry, code)
        }.filter { it.name.isNotEmpty() }.sortedBy { it.name }
    }

    // Seed cities for the local offline cache.
    // This list is intentionally small; the Room-backed cache can grow as users search.
    private val seedCities = listOf(
        City("Sanaa", 15.3694, 44.1910, "YE"),
        City("Aden", 12.7855, 45.0186, "YE"),
        City("Taiz", 13.5795, 44.0209, "YE"),
        City("Al Hudaydah", 14.7979, 42.9530, "YE"),
        City("Mukalla", 14.5425, 49.1242, "YE"),
        City("Ibb", 13.9667, 44.1833, "YE"),
        City("Dhamar", 14.5425, 44.4053, "YE"),
        City("London", 51.5074, -0.1278, "GB"),
        City("Manchester", 53.4808, -2.2426, "GB"),
        City("Birmingham", 52.4862, -1.8904, "GB"),
        City("Makkah", 21.4225, 39.8262, "SA"),
        City("Medina", 24.4672, 39.6111, "SA"),
        City("Riyadh", 24.7136, 46.6753, "SA"),
        City("Cairo", 30.0444, 31.2357, "EG"),
        City("Alexandria", 31.2001, 29.9187, "EG"),
        City("New York", 40.7128, -74.0060, "US"),
        City("Los Angeles", 34.0522, -118.2437, "US"),
        City("Istanbul", 41.0082, 28.9784, "TR"),
        City("Ankara", 39.9334, 32.8597, "TR"),
        City("Dubai", 25.2048, 55.2708, "AE"),
        City("Abu Dhabi", 24.4539, 54.3773, "AE"),
        City("Jakarta", -6.2088, 106.8456, "ID"),
        City("Kuala Lumpur", 3.1390, 101.6869, "MY"),
        City("Karachi", 24.8607, 67.0011, "PK")
    )

    fun getSeedCities(): List<City> {
        return seedCities
    }

    fun getCitiesForCountry(countryCode: String): List<City> {
        return seedCities
            .filter { it.countryCode.equals(countryCode, ignoreCase = true) }
            .sortedBy { it.name }
    }
}
