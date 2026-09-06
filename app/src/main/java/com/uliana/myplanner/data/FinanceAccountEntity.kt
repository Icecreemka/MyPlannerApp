package com.uliana.myplanner.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Счёт (наличные, карта, вклад и т.д.) с текущим балансом. */
@Entity(tableName = "finance_accounts")
data class FinanceAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val balance: Double = 0.0,
    val colorHex: String = "#7C9885",
    val icon: String = "wallet",
    val createdAt: Long = System.currentTimeMillis()
)

object DefaultFinanceAccounts {
    val seed = listOf(
        FinanceAccountEntity(name = "Наличные", icon = "cash", colorHex = "#C98474"),
        FinanceAccountEntity(name = "Карта", icon = "card", colorHex = "#7C9885")
    )
}
