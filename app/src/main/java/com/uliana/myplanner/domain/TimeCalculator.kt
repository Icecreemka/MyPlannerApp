package com.uliana.myplanner.domain

import java.time.Duration
import java.time.LocalDateTime

object TimeCalculator {

    data class Result(val start: LocalDateTime, val end: LocalDateTime, val durationMinutes: Long)

    fun fromStartAndDuration(start: LocalDateTime, durationMinutes: Long): Result =
        Result(start, start.plusMinutes(durationMinutes), durationMinutes)

    fun fromEndAndDuration(end: LocalDateTime, durationMinutes: Long): Result =
        Result(end.minusMinutes(durationMinutes), end, durationMinutes)

    fun fromStartAndEnd(start: LocalDateTime, end: LocalDateTime): Result {
        val duration = Duration.between(start, end).toMinutes().coerceAtLeast(1)
        return Result(start, start.plusMinutes(duration), duration)
    }

    fun moveKeepingDuration(newStart: LocalDateTime, durationMinutes: Long): Result =
        fromStartAndDuration(newStart, durationMinutes)
}
