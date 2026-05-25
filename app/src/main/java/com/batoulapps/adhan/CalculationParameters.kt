package com.batoulapps.adhan

class CalculationParameters(
    var fajrAngle: Double,
    var ishaAngle: Double,
    var madhab: Madhab = Madhab.SHAFI,
    var method: CalculationMethod = CalculationMethod.NORTH_AMERICA
) {
    fun withMethodAdjustments(method: CalculationMethod): CalculationParameters {
        this.method = method
        return this
    }
}
