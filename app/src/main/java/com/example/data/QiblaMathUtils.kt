package com.example.data

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object QiblaMathUtils {
    const val KAABA_LAT = 21.4225
    const val KAABA_LON = 39.8262

    /**
     * Calculates the Qibla bearing (in degrees, from True North) from the user's location.
     * Formula:
     * q = atan2(sin(Δlon), cos(lat1) * tan(lat2) - sin(lat1) * cos(Δlon))
     */
    fun calculateQiblaBearing(userLat: Double, userLon: Double): Double {
        val lat1 = Math.toRadians(userLat)
        val lon1 = Math.toRadians(userLon)
        val lat2 = Math.toRadians(KAABA_LAT)
        val lon2 = Math.toRadians(KAABA_LON)

        val deltaLon = lon2 - lon1

        val y = sin(deltaLon)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLon)

        val bearingRad = atan2(y, x)
        var qiblaDegrees = Math.toDegrees(bearingRad)
        qiblaDegrees = (qiblaDegrees + 360) % 360
        return qiblaDegrees
    }
}
