package com.example.data

import androidx.room.Entity

@Entity(
    tableName = "cities",
    primaryKeys = ["countryCode", "name"]
)
data class CityEntity(
    val name: String,
    val lat: Double,
    val lon: Double,
    val countryCode: String
)

fun CityEntity.toDomain(): City {
    return City(
        name = name,
        lat = lat,
        lon = lon,
        countryCode = countryCode
    )
}

fun City.toEntity(): CityEntity {
    return CityEntity(
        name = name,
        lat = lat,
        lon = lon,
        countryCode = countryCode
    )
}
