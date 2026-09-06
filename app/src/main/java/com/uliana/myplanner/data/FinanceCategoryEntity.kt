package com.uliana.myplanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Категория трат/доходов (Еда, Транспорт, Зарплата и т.д.) — отдельная от категорий дел. */
@Entity(tableName = "finance_categories")
data class FinanceCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String,
    val icon: String = "cart"
)

object DefaultFinanceCategories {
    val seed = listOf(
        FinanceCategoryEntity(name = "Еда", colorHex = "#C98474", icon = "food"),
        FinanceCategoryEntity(name = "Транспорт", colorHex = "#8AA1B1", icon = "transport"),
        FinanceCategoryEntity(name = "Развлечения", colorHex = "#D4A76A", icon = "fun"),
        FinanceCategoryEntity(name = "Здоровье", colorHex = "#7C9885", icon = "health"),
        FinanceCategoryEntity(name = "Покупки", colorHex = "#9B7EA8", icon = "cart"),
        FinanceCategoryEntity(name = "Зарплата", colorHex = "#6E9B8C", icon = "income")
    )
}
