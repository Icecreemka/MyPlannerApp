package com.uliana.myplanner.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        TaskEntity::class,
        TaskOccurrenceOverride::class,
        Category::class,
        ScenarioEntity::class,
        ScenarioStepEntity::class,
        BacklogTaskEntity::class,
        FinanceAccountEntity::class,
        FinanceCategoryEntity::class,
        TransactionEntity::class,
        SavingsGoalEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun categoryDao(): CategoryDao
    abstract fun scenarioDao(): ScenarioDao
    abstract fun backlogDao(): BacklogDao
    abstract fun financeAccountDao(): FinanceAccountDao
    abstract fun financeCategoryDao(): FinanceCategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun savingsGoalDao(): SavingsGoalDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "my_planner.db"
                ).addCallback(SeedCallback(context.applicationContext))

                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
    }

    private class SeedCallback(private val context: Context) : Callback() {
        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val instance = getInstance(context)
                instance.categoryDao().insertAll(DefaultCategories.seed)
                instance.financeAccountDao().insertAll(DefaultFinanceAccounts.seed)
                instance.financeCategoryDao().insertAll(DefaultFinanceCategories.seed)
            }
        }
    }
}
