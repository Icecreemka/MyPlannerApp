package com.uliana.myplanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

enum class TransactionType { EXPENSE, INCOME }

/** Одна запись о трате или доходе, привязанная к счёту. */
@Entity(tableName = "finance_transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val categoryId: Long?,
    val type: TransactionType,
    val amount: Double,
    val note: String = "",
    val date: LocalDate = LocalDate.now(),
    val createdAt: Long = System.currentTimeMillis()
)
