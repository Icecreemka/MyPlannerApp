package com.uliana.myplanner.domain

import com.uliana.myplanner.data.SleepSchedule
import java.time.LocalDate

/**
 * Считает оставшееся свободное время за день (в минутах), учитывая занятые делами
 * промежутки и, по желанию пользователя, график сна (тогда сон тоже "съедает" день).
 */
object FreeTimeCalculator {

    private const val MINUTES_IN_DAY = 24 * 60

    /** Простой промежуток в минутах от начала суток [0, 1440]. Может выходить за границы дня — будет обрезан. */
    private data class MinuteRange(val startMin: Int, val endMin: Int)

    fun freeMinutesForDay(
        date: LocalDate,
        occurrences: List<TaskOccurrence>,
        sleepSchedule: SleepSchedule
    ): Long {
        val ranges = mutableListOf<MinuteRange>()

        occurrences.forEach { occ ->
            val startMin = minutesOfDayClamped(occ, date, isStart = true)
            val endMin = minutesOfDayClamped(occ, date, isStart = false)
            if (endMin > startMin) ranges += MinuteRange(startMin, endMin)
        }

        if (sleepSchedule.enabled) {
            ranges += sleepRangesForDay(sleepSchedule)
        }

        val busyMinutes = mergeAndSum(ranges)
        return (MINUTES_IN_DAY - busyMinutes).coerceAtLeast(0).toLong()
    }

    private fun minutesOfDayClamped(occ: TaskOccurrence, date: LocalDate, isStart: Boolean): Int {
        val dayStart = date.atStartOfDay()
        val dayEnd = date.plusDays(1).atStartOfDay()
        val point = if (isStart) occ.start else occ.end
        val clamped = when {
            point.isBefore(dayStart) -> dayStart
            point.isAfter(dayEnd) -> dayEnd
            else -> point
        }
        return (java.time.Duration.between(dayStart, clamped).toMinutes()).toInt()
    }

    /** Сон может занимать конец предыдущей ночи (00:00..sleepEnd) и начало этой ночи (sleepStart..24:00). */
    private fun sleepRangesForDay(schedule: SleepSchedule): List<MinuteRange> {
        val startMin = schedule.sleepStart.hour * 60 + schedule.sleepStart.minute
        val endMin = schedule.sleepEnd.hour * 60 + schedule.sleepEnd.minute
        return if (startMin <= endMin) {
            // Сон в пределах одного дня (например дневной сон 13:00-14:00)
            listOf(MinuteRange(startMin, endMin))
        } else {
            // Сон переходит через полночь: хвост утром + начало вечером
            listOf(
                MinuteRange(0, endMin),
                MinuteRange(startMin, MINUTES_IN_DAY)
            )
        }
    }

    private fun mergeAndSum(ranges: List<MinuteRange>): Int {
        if (ranges.isEmpty()) return 0
        val sorted = ranges.sortedBy { it.startMin }
        var total = 0
        var curStart = sorted[0].startMin
        var curEnd = sorted[0].endMin
        for (i in 1 until sorted.size) {
            val r = sorted[i]
            if (r.startMin <= curEnd) {
                curEnd = maxOf(curEnd, r.endMin)
            } else {
                total += curEnd - curStart
                curStart = r.startMin
                curEnd = r.endMin
            }
        }
        total += curEnd - curStart
        return total
    }
}
