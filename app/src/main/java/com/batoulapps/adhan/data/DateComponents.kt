package com.batoulapps.adhan.data

import java.util.Calendar
import java.util.Date

class DateComponents(val year: Int, val month: Int, val day: Int) {
    companion object {
        fun from(date: Date): DateComponents {
            val cal = Calendar.getInstance()
            cal.time = date
            return DateComponents(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DAY_OF_MONTH)
            )
        }
    }
}
