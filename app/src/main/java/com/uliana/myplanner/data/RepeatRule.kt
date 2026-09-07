package com.uliana.myplanner.data

import java.time.DayOfWeek
import java.time.LocalDate

enum class RepeatType {
    NONE,
    INTERVAL,
    WEEKLY,
    MONTHLY,
    YEARLY
}

enum class IntervalUnit { MINUTES, HOURS, DAYS, WEEKS }

enum class RepeatEndType {
    NEVER,
    UNTIL_DATE,
    COUNT
}

data class RepeatRule(
    val type: RepeatType = RepeatType.NONE,
    val intervalAmount: Int = 1,
    val intervalUnit: IntervalUnit = IntervalUnit.DAYS,
    val daysOfWeek: Set<DayOfWeek> = emptySet(),
    val endType: RepeatEndType = RepeatEndType.NEVER,
    val endDate: LocalDate? = null,
    val endCount: Int? = null,
    val pausedFrom: LocalDate? = null,
    val pausedUntil: LocalDate? = null,
    val isStoppedForever: Boolean = false,
    val stopAfterDate: LocalDate? = null
) {
    val isRepeating: Boolean get() = type != RepeatType.NONE

    fun isPausedOn(date: LocalDate): Boolean {
        if (isStoppedForever && stopAfterDate != null && !date.isBefore(stopAfterDate)) return true
        val from = pausedFrom ?: return false
        val until = pausedUntil
        return if (until == null) !date.isBefore(from) else !date.isBefore(from) && !date.isAfter(until)
    }
}
