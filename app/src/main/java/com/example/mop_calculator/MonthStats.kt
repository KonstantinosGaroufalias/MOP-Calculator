package com.example.mop_calculator

data class MonthStats(
    val dailyMOP: List<Double>,
    val monthMOP: Double,
    val totalMonthlyProduction: Int,  // NEW: Total production for the month
    val totalMonthlyHours: Double     // NEW: Total hours for the month
)