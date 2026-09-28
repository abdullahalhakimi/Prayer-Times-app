package com.prayertimesApp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface CityDao {
    @Query("SELECT * FROM cities WHERE countryCode = :countryCode ORDER BY name COLLATE NOCASE")
    suspend fun getCitiesForCountry(countryCode: String): List<CityEntity>

    @Query(
        """
        SELECT * FROM cities
        WHERE countryCode = :countryCode
        AND name LIKE '%' || :query || '%' COLLATE NOCASE
        ORDER BY name COLLATE NOCASE
        """
    )
    suspend fun searchCities(countryCode: String, query: String): List<CityEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cities: List<CityEntity>)

    @Query("SELECT COUNT(*) FROM cities")
    suspend fun countAll(): Int
}
