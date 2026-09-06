package com.uliana.myplanner.data

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Тип повторения дела.
 */
enum class RepeatType {
    NONE,           // не повторяется
    INTERVAL,       // повторять каждые intervalAmount intervalUnit (например каждые 3 дня)
    WEEKLY,         // повторять по дням недели daysOfWeek
    MONTHLY,        // повторять каждый месяц в тот же день месяца
    YEARLY          // повторять каждый год в ту же дату
}

enum class IntervalUnit { MINUTES, HOURS, DAYS, WEEKS }

/**
 * Условие окончания серии повторений.
 */
enum class RepeatEndType {
    NEVER,          // повторять постоянно
    UNTIL_DATE,     // повторять до конкретной даты
    COUNT           // повторять фиксированное количество раз
}

/**
 * Полное описание повторения дела.
 * Хранится в TaskEntity в виде JSON через Converters.
 *
 * pausedFrom/pausedUntil — временная остановка повторений ("прекратить на какое-то время").
 * isStoppedForever — полная остановка серии повторений начиная с stopAfterDate.
 */
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

    /** Активна ли пауза на указанную дату. */
    fun isPausedOn(date: LocalDate): Boolean {
        if (isStoppedForever && stopAfterDate != null && !date.isBefore(stopAfterDate)) return true
        val from = pausedFrom ?: return false
        val until = pausedUntil
        return if (until == null) !date.isBefore(from) else !date.isBefore(from) && !date.isAfter(until)
    }
}
