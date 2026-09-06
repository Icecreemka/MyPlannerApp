package com.uliana.myplanner

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.uliana.myplanner.data.AppDatabase
import com.uliana.myplanner.data.FinanceRepository
import com.uliana.myplanner.data.PlannerRepository
import com.uliana.myplanner.data.SettingsRepository

class MyPlannerApp : Application() {

    lateinit var repository: PlannerRepository
        private set
    lateinit var financeRepository: FinanceRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(this)
        repository = PlannerRepository(
            taskDao = db.taskDao(),
            categoryDao = db.categoryDao(),
            scenarioDao = db.scenarioDao(),
            backlogDao = db.backlogDao(),
            settingsRepository = SettingsRepository(this)
        )
        financeRepository = FinanceRepository(
            accountDao = db.financeAccountDao(),
            categoryDao = db.financeCategoryDao(),
            transactionDao = db.transactionDao(),
            savingsGoalDao = db.savingsGoalDao()
        )
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_desc)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "my_planner_reminders"
    }
}
