package com.uliana.myplanner.data

@androidx.room.Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val colorHex: String = "#D4A76A",
    val note: String = "",
    val isPurchased: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val purchasedAt: Long? = null
) {
    val progress: Float
        get() = if (targetAmount <= 0.0) 0f else (savedAmount / targetAmount).toFloat().coerceIn(0f, 1f)
}
