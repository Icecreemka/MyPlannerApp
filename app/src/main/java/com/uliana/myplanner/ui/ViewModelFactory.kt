package com.uliana.myplanner.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.uliana.myplanner.MyPlannerApp
import com.uliana.myplanner.data.FinanceRepository
import com.uliana.myplanner.data.PlannerRepository

@Composable
fun rememberRepository(): PlannerRepository {
    val context = LocalContext.current
    val app = context.applicationContext as MyPlannerApp
    return app.repository
}

@Composable
fun rememberFinanceRepository(): FinanceRepository {
    val context = LocalContext.current
    val app = context.applicationContext as MyPlannerApp
    return app.financeRepository
}
