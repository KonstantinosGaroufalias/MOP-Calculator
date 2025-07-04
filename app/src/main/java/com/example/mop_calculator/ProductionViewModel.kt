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

class ProductionViewModel(
    application: Application,
    private val type: String
) : AndroidViewModel(application) {

    val dayLive = MutableLiveData<DayStats>()
    val monthLive = MutableLiveData<MonthStats>()

    private val database = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "mop_database"
    ).build()

    private val dao = database.shiftDao()

    init {
        loadDay(LocalDate.now())
    }

    fun loadDay(date: LocalDate) {
        viewModelScope.launch {
            try {
                val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val shifts = dao.getDayShifts(dateString, type)

                var morning = 0
                var afternoon = 0
                var night = 0

                shifts.forEach { shift ->
                    when (shift.shift) {
                        "ΠΡΩΙ" -> morning = shift.quantity
                        "ΑΠΟΓ" -> afternoon = shift.quantity
                        "ΒΡΑΔ" -> night = shift.quantity
                    }
                }

                val total = morning + afternoon + night
                val avg = if (total > 0) total / 3.0 else 0.0

                dayLive.postValue(DayStats(morning, afternoon, night, total, avg))
            } catch (e: Exception) {
                e.printStackTrace()
                dayLive.postValue(DayStats(0, 0, 0, 0, 0.0))
            }
        }
    }

    fun saveShift(date: LocalDate, shift: String, qty: Int) {
        viewModelScope.launch {
            try {
                val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val entry = ShiftEntry(
                    date = dateString,
                    type = type,
                    shift = shift,
                    quantity = qty
                )
                dao.insertOrUpdate(entry)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadMonth(yearMonth: String) {
        viewModelScope.launch {
            try {
                val shifts = dao.getMonthShifts(yearMonth, type)
                val dailyTotals = mutableMapOf<String, Int>()

                shifts.forEach { shift ->
                    val currentTotal = dailyTotals[shift.date] ?: 0
                    dailyTotals[shift.date] = currentTotal + shift.quantity
                }

                val averages = dailyTotals.values.map { it / 3.0 }
                val monthAvg = if (averages.isNotEmpty()) averages.average() else 0.0

                monthLive.postValue(MonthStats(averages, monthAvg))
            } catch (e: Exception) {
                e.printStackTrace()
                monthLive.postValue(MonthStats(emptyList(), 0.0))
            }
        }
    }

    class Factory(
        private val application: Application,
        private val type: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProductionViewModel::class.java)) {
                return ProductionViewModel(application, type) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
