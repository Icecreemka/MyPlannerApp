package com.uliana.myplanner.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? = value?.let { LocalDateTime.parse(it) }

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromTransactionType(value: TransactionType?): String? = value?.name

    @TypeConverter
    fun toTransactionType(value: String?): TransactionType? = value?.let { TransactionType.valueOf(it) }

    @TypeConverter
    fun fromRepeatRule(rule: RepeatRule?): String? {
        val r = rule ?: return null
        val shape = RepeatRuleGsonShape(
            type = r.type,
            intervalAmount = r.intervalAmount,
            intervalUnit = r.intervalUnit,
            daysOfWeek = r.daysOfWeek.map { it.name }.toSet(),
            endType = r.endType,
            endDate = r.endDate?.toString(),
            endCount = r.endCount,
            pausedFrom = r.pausedFrom?.toString(),
            pausedUntil = r.pausedUntil?.toString(),
            isStoppedForever = r.isStoppedForever,
            stopAfterDate = r.stopAfterDate?.toString()
        )
        return gson.toJson(shape)
    }

    @TypeConverter
    fun toRepeatRule(json: String?): RepeatRule? =
        if (json.isNullOrEmpty()) RepeatRule() else runCatching {
            gson.fromJson(json, RepeatRuleGsonShape::class.java).toRepeatRule()
        }.getOrDefault(RepeatRule())

    private data class RepeatRuleGsonShape(
        val type: RepeatType = RepeatType.NONE,
        val intervalAmount: Int = 1,
        val intervalUnit: IntervalUnit = IntervalUnit.DAYS,
        val daysOfWeek: Set<String> = emptySet(),
        val endType: RepeatEndType = RepeatEndType.NEVER,
        val endDate: String? = null,
        val endCount: Int? = null,
        val pausedFrom: String? = null,
        val pausedUntil: String? = null,
        val isStoppedForever: Boolean = false,
        val stopAfterDate: String? = null
    ) {
        fun toRepeatRule() = RepeatRule(
            type = type,
            intervalAmount = intervalAmount,
            intervalUnit = intervalUnit,
            daysOfWeek = daysOfWeek.map { DayOfWeek.valueOf(it) }.toSet(),
            endType = endType,
            endDate = endDate?.let { LocalDate.parse(it) },
            endCount = endCount,
            pausedFrom = pausedFrom?.let { LocalDate.parse(it) },
            pausedUntil = pausedUntil?.let { LocalDate.parse(it) },
            isStoppedForever = isStoppedForever,
            stopAfterDate = stopAfterDate?.let { LocalDate.parse(it) }
        )
    }
}
