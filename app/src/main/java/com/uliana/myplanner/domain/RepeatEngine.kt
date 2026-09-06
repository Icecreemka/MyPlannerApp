package com.uliana.myplanner.domain

import com.uliana.myplanner.data.IntervalUnit
import com.uliana.myplanner.data.RepeatType
import com.uliana.myplanner.data.TaskEntity
import com.uliana.myplanner.data.TaskOccurrenceOverride
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Конкретное вхождение дела на временной шкале — то, что реально рисуется в планировщике.
 */
data class TaskOccurrence(
    val task: TaskEntity,
    val originalStart: LocalDateTime,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val isMoved: Boolean,
    val isSkipped: Boolean,
    val isCompleted: Boolean
) {
    val durationMinutes: Long get() = task.durationMinutes
}

object RepeatEngine {

    /**
     * Генерирует все вхождения дела в диапазоне [rangeStart, rangeEnd] (включительно),
     * применяя паузы из RepeatRule и индивидуальные overrides.
     */
    fun occurrencesInRange(
        task: TaskEntity,
        overrides: List<TaskOccurrenceOverride>,
        rangeStart: LocalDate,
        rangeEnd: LocalDate
    ): List<TaskOccurrence> {
        val overrideByStart = overrides.associateBy { it.originalStart }
        val rule = task.repeatRule
        val rawStarts = mutableListOf<LocalDateTime>()

        val windowStart = rangeStart.atStartOfDay()
        val windowEnd = rangeEnd.plusDays(1).atStartOfDay()

        when (rule.type) {
            RepeatType.NONE -> {
                if (!task.anchorStart.isBefore(windowStart) && task.anchorStart.isBefore(windowEnd)) {
                    rawStarts += task.anchorStart
                }
            }

            RepeatType.INTERVAL -> {
                var current = task.anchorStart
                var count = 0
                val step: (LocalDateTime) -> LocalDateTime = { dt ->
                    when (rule.intervalUnit) {
                        IntervalUnit.MINUTES -> dt.plusMinutes(rule.intervalAmount.toLong())
                        IntervalUnit.HOURS -> dt.plusHours(rule.intervalAmount.toLong())
                        IntervalUnit.DAYS -> dt.plusDays(rule.intervalAmount.toLong())
                        IntervalUnit.WEEKS -> dt.plusWeeks(rule.intervalAmount.toLong())
                    }
                }
                // Защита от бесконечного цикла: ограничиваем количество итераций.
                var safety = 0
                while (current.isBefore(windowEnd) && safety < 20000) {
                    safety++
                    if (rule.endType == com.uliana.myplanner.data.RepeatEndType.COUNT &&
                        rule.endCount != null && count >= rule.endCount
                    ) break
                    if (rule.endType == com.uliana.myplanner.data.RepeatEndType.UNTIL_DATE &&
                        rule.endDate != null && current.toLocalDate().isAfter(rule.endDate)
                    ) break

                    if (!current.isBefore(windowStart)) {
                        rawStarts += current
                    }
                    count++
                    current = step(current)
                }
            }

            RepeatType.WEEKLY -> {
                var date = maxOf(rangeStart, task.anchorStart.toLocalDate())
                while (!date.isAfter(rangeEnd)) {
                    if (date.dayOfWeek in rule.daysOfWeek && !date.isBefore(task.anchorStart.toLocalDate())) {
                        if (isWithinEnd(rule, date)) {
                            rawStarts += LocalDateTime.of(date, task.anchorStart.toLocalTime())
                        }
                    }
                    date = date.plusDays(1)
                }
            }

            RepeatType.MONTHLY -> {
                var monthCursor = LocalDate.of(rangeStart.year, rangeStart.month, 1)
                val lastMonth = LocalDate.of(rangeEnd.year, rangeEnd.month, 1)
                while (!monthCursor.isAfter(lastMonth)) {
                    val day = minOf(task.anchorStart.dayOfMonth, monthCursor.lengthOfMonth())
                    val date = monthCursor.withDayOfMonth(day)
                    if (!date.isBefore(task.anchorStart.toLocalDate()) &&
                        !date.isBefore(rangeStart) && !date.isAfter(rangeEnd) && isWithinEnd(rule, date)
                    ) {
                        rawStarts += LocalDateTime.of(date, task.anchorStart.toLocalTime())
                    }
                    monthCursor = monthCursor.plusMonths(1)
                }
            }

            RepeatType.YEARLY -> {
                var yearCursor = rangeStart.year
                while (yearCursor <= rangeEnd.year) {
                    val maxDay = LocalDate.of(yearCursor, task.anchorStart.month, 1).lengthOfMonth()
                    val date = LocalDate.of(yearCursor, task.anchorStart.month, minOf(task.anchorStart.dayOfMonth, maxDay))
                    if (!date.isBefore(task.anchorStart.toLocalDate()) &&
                        !date.isBefore(rangeStart) && !date.isAfter(rangeEnd) && isWithinEnd(rule, date)
                    ) {
                        rawStarts += LocalDateTime.of(date, task.anchorStart.toLocalTime())
                    }
                    yearCursor++
                }
            }
        }

        return rawStarts
            .filterNot { rule.isPausedOn(it.toLocalDate()) }
            .map { originalStart ->
                val override = overrideByStart[originalStart]
                val start = override?.newStart ?: originalStart
                val end = override?.newEnd ?: originalStart.plusMinutes(task.durationMinutes)
                TaskOccurrence(
                    task = task,
                    originalStart = originalStart,
                    start = start,
                    end = end,
                    isMoved = override?.newStart != null,
                    isSkipped = override?.isSkipped == true,
                    isCompleted = override?.isCompleted == true || (rule.type == RepeatType.NONE && task.isCompleted)
                )
            }
            .filterNot { it.isSkipped }
            .sortedBy { it.start }
    }

    private fun isWithinEnd(rule: com.uliana.myplanner.data.RepeatRule, date: LocalDate): Boolean {
        if (rule.endType == com.uliana.myplanner.data.RepeatEndType.UNTIL_DATE && rule.endDate != null) {
            return !date.isAfter(rule.endDate)
        }
        return true
    }
}
