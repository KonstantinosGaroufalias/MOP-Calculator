package com.example.mop_calculator

data class SummaryStats(
    val total2F: Int,
    val totalHours2F: Double,
    val mop2F: Double,
    val total3F: Int,
    val totalHours3F: Double,
    val mop3F: Double,
    val finalMOP: Double  // (mop2F + mop3F) / 2
)