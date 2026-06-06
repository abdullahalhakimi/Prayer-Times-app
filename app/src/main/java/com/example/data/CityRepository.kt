package com.example.data

import android.content.Context
import android.location.Geocoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import androidx.room.withTransaction
import java.util.Locale

object CityRepository {
    private val seedMutex = Mutex()
    @Volatile
    private var seeded = false

    private fun database(context: Context): CityDatabase {
        return CityDatabase.getInstance(context)
    }

    private suspend fun ensureSeeded(context: Context) {
        if (seeded) return

        seedMutex.withLock {
            if (seeded) return

            val db = database(context)
            val dao = db.cityDao()
            if (dao.countAll() == 0) {
                db.withTransaction {
                    CitySeedLoader.importFromAsset(context, dao)
                }
            }
            seeded = true
        }
    }

    suspend fun getCitiesForCountry(
        context: Context,
        country: Country,
        query: String = ""
    ): List<City> = withContext(Dispatchers.IO) {
        ensureSeeded(context)

        val dao = database(context).cityDao()
        val localResults = if (query.isBlank()) {
            dao.getCitiesForCountry(country.code)
        } else {
            dao.searchCities(country.code, query)
        }

        if (localResults.isNotEmpty()) {
            return@withContext localResults.map { it.toDomain() }
        }

        val geocoderResults = runCatching {
            val geocoder = Geocoder(context, Locale.getDefault())
            val lookup = if (query.isBlank()) country.name else "$query, ${country.name}"
            geocoder.getFromLocationName(lookup, 20)
                ?.mapNotNull { address ->
                    val cityName = address.locality ?: address.subAdminArea ?: address.featureName
                    cityName?.let {
                        CityEntity(
                            name = it,
                            lat = address.latitude,
                            lon = address.longitude,
                            countryCode = country.code
                        )
                    }
                }
                ?.distinctBy { it.name }
                .orEmpty()
        }.getOrDefault(emptyList())

        if (geocoderResults.isNotEmpty()) {
            dao.insertAll(geocoderResults)
        }

        geocoderResults.map { it.toDomain() }
    }
}
