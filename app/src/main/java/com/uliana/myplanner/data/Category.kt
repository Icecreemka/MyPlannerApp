package com.uliana.myplanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorHex: String,
    val icon: String = "leaf"
)

object DefaultCategories {
    val seed = listOf(
        Category(name = "Работа", colorHex = "#7C9885", icon = "work"),
        Category(name = "Учёба", colorHex = "#C98474", icon = "study"),
        Category(name = "Дом", colorHex = "#A6B37D", icon = "home"),
        Category(name = "Здоровье", colorHex = "#8AA1B1", icon = "health"),
        Category(name = "Отдых", colorHex = "#D4A76A", icon = "rest")
    )
}
