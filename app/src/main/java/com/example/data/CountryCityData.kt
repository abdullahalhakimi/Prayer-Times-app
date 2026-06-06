package com.example.data

data class Country(val name: String, val code: String)
data class City(val name: String, val lat: Double, val lon: Double, val countryCode: String)

object CountryCityProvider {
    val countries = listOf(
        Country("United Kingdom", "UK"),
        Country("Saudi Arabia", "SA"),
        Country("Egypt", "EG"),
        Country("United States", "US"),
        Country("Turkey", "TR"),
        Country("United Arab Emirates", "UAE"),
        Country("Malaysia", "MY"),
        Country("Pakistan", "PK"),
        Country("Indonesia", "ID")
    ).sortedBy { it.name }

    val cities = listOf(
        City("London", 51.5074, -0.1278, "UK"),
        City("Manchester", 53.4808, -2.2426, "UK"),
        City("Birmingham", 52.4862, -1.8904, "UK"),
        City("Makkah", 21.4225, 39.8262, "SA"),
        City("Medina", 24.4672, 39.6111, "SA"),
        City("Riyadh", 24.7136, 46.6753, "SA"),
        City("Cairo", 30.0444, 31.2357, "EG"),
        City("Alexandria", 31.2001, 29.9187, "EG"),
        City("New York", 40.7128, -74.0060, "US"),
        City("Los Angeles", 34.0522, -118.2437, "US"),
        City("Chicago", 41.8781, -87.6298, "US"),
        City("Istanbul", 41.0082, 28.9784, "TR"),
        City("Ankara", 39.9334, 32.8597, "TR"),
        City("Dubai", 25.2048, 55.2708, "UAE"),
        City("Abu Dhabi", 24.4539, 54.3773, "UAE"),
        City("Kuala Lumpur", 3.1390, 101.6869, "MY"),
        City("Karachi", 24.8607, 67.0011, "PK"),
        City("Lahore", 31.5204, 74.3587, "PK"),
        City("Jakarta", -6.2088, 106.8456, "ID")
    ).sortedBy { it.name }

    fun getCitiesForCountry(countryCode: String): List<City> {
        return cities.filter { it.countryCode == countryCode }
    }
}
