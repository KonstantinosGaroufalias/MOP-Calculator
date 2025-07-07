package com.example.mop_calculator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class SummaryViewModel(application: Application) : AndroidViewModel(application) {

    val summaryLive = MutableLiveData<SummaryStats>()

    private val database = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "mop_database"
    ).build()

    private val dao = database.shiftDao()

    init {
        loadSummary(LocalDate.now())
    }

    fun loadSummary(date: LocalDate) {
        viewModelScope.launch {
            try {
                val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val allShifts = dao.getAllDayShifts(dateString)

                // Separate 2F and 3F data for current day
                val shifts2F = allShifts.filter { it.type == "2F" }
                val shifts3F = allShifts.filter { it.type == "3F" }

                // Calculate 2F totals for current day
                val total2F = shifts2F.sumOf { it.quantity }
                val totalHours2F = shifts2F.sumOf { it.hours }
                val mop2F = if (totalHours2F > 0) total2F / totalHours2F else 0.0

                // Calculate 3F totals for current day
                val total3F = shifts3F.sumOf { it.quantity }
                val totalHours3F = shifts3F.sumOf { it.hours }
                val mop3F = if (totalHours3F > 0) total3F / totalHours3F else 0.0

                // Calculate final MOP for current day: (mop2F + mop3F) / 2
                val finalMOP = if (mop2F > 0 && mop3F > 0) {
                    (mop2F + mop3F) / 2.0
                } else if (mop2F > 0) {
                    mop2F
                } else if (mop3F > 0) {
                    mop3F
                } else {
                    0.0
                }

                // NEW: Calculate monthly MOP
                val monthlyMOP = calculateMonthlyMOP(date)

                summaryLive.postValue(
                    SummaryStats(
                        total2F = total2F,
                        totalHours2F = totalHours2F,
                        mop2F = mop2F,
                        total3F = total3F,
                        totalHours3F = totalHours3F,
                        mop3F = mop3F,
                        finalMOP = finalMOP,
                        monthlyMOP = monthlyMOP
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
                summaryLive.postValue(
                    SummaryStats(0, 0.0, 0.0, 0, 0.0, 0.0, 0.0, 0.0)
                )
            }
        }
    }

    private suspend fun calculateMonthlyMOP(currentDate: LocalDate): Double {
        try {
            val yearMonth = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM"))

            // Get all shifts for the month
            val allMonthShifts2F = dao.getMonthShifts(yearMonth, "2F")
            val allMonthShifts3F = dao.getMonthShifts(yearMonth, "3F")

            // Group by date and calculate daily final MOPs
            val dailyMOPs = mutableListOf<Double>()

            // Get all dates from 1st to current day
            for (day in 1..currentDate.dayOfMonth) {
                val checkDate = LocalDate.of(currentDate.year, currentDate.month, day)
                val checkDateString = checkDate.format(DateTimeFormatter.ISO_LOCAL_DATE)

                // Get shifts for this specific day
                val day2FShifts = allMonthShifts2F.filter { it.date == checkDateString }
                val day3FShifts = allMonthShifts3F.filter { it.date == checkDateString }

                // Calculate 2F MOP for this day
                val dayTotal2F = day2FShifts.sumOf { it.quantity }
                val dayHours2F = day2FShifts.sumOf { it.hours }
                val dayMOP2F = if (dayHours2F > 0) dayTotal2F / dayHours2F else 0.0

                // Calculate 3F MOP for this day
                val dayTotal3F = day3FShifts.sumOf { it.quantity }
                val dayHours3F = day3FShifts.sumOf { it.hours }
                val dayMOP3F = if (dayHours3F > 0) dayTotal3F / dayHours3F else 0.0

                // Calculate final MOP for this day
                val dayFinalMOP = if (dayMOP2F > 0 && dayMOP3F > 0) {
                    (dayMOP2F + dayMOP3F) / 2.0
                } else if (dayMOP2F > 0) {
                    dayMOP2F
                } else if (dayMOP3F > 0) {
                    dayMOP3F
                } else {
                    0.0
                }

                // Only include days with actual data
                if (dayFinalMOP > 0) {
                    dailyMOPs.add(dayFinalMOP)
                }
            }

            // Calculate average of all daily MOPs
            return if (dailyMOPs.isNotEmpty()) {
                dailyMOPs.average()
            } else {
                0.0
            }

        } catch (e: Exception) {
            e.printStackTrace()
            return 0.0
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SummaryViewModel::class.java)) {
                return SummaryViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
