package com.example.mop_calculator

data class DayStats(
    val morning: Int,
    val morningHours: Double,
    val afternoon: Int,
    val afternoonHours: Double,
    val night: Int,
    val nightHours: Double,
    val total: Int,
    val totalHours: Double,
    val mop: Double  // production / hours
)