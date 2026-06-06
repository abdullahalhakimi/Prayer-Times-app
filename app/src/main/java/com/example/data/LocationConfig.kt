package com.example.data

import com.batoulapps.adhan.CalculationMethod
import com.batoulapps.adhan.Madhab
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LocationConfig(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val method: CalculationMethod,
    val madhab: Madhab = Madhab.SHAFI
)
