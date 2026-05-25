package com.batoulapps.adhan

enum class CalculationMethod {
    UMM_AL_QURA,
    MOONSIGHTING_COMMITTEE,
    EGYPTIAN,
    NORTH_AMERICA,
    MUSLIM_WORLD_LEAGUE,
    KARACHI,
    KUWAIT,
    QATAR,
    SINGAPORE,
    TURKEY;

    val parameters: CalculationParameters
        get() = when (this) {
            UMM_AL_QURA -> CalculationParameters(18.5, 0.0, method = this)
            MOONSIGHTING_COMMITTEE -> CalculationParameters(18.0, 18.0, method = this)
            EGYPTIAN -> CalculationParameters(19.5, 17.5, method = this)
            NORTH_AMERICA -> CalculationParameters(15.0, 15.0, method = this)
            MUSLIM_WORLD_LEAGUE -> CalculationParameters(18.0, 17.0, method = this)
            KARACHI -> CalculationParameters(18.0, 18.0, method = this)
            KUWAIT -> CalculationParameters(18.0, 17.5, method = this)
            QATAR -> CalculationParameters(18.0, 0.0, method = this)
            SINGAPORE -> CalculationParameters(20.0, 18.0, method = this)
            TURKEY -> CalculationParameters(18.0, 17.0, method = this)
        }
}
