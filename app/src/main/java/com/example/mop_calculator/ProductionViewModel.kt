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
                var morningHours = 0.0
                var afternoon = 0
                var afternoonHours = 0.0
                var night = 0
                var nightHours = 0.0

                shifts.forEach { shift ->
                    when (shift.shift) {
                        "ΠΡΩΙ" -> {
                            morning = shift.quantity
                            morningHours = shift.hours
                        }
                        "ΑΠΟΓ" -> {
                            afternoon = shift.quantity
                            afternoonHours = shift.hours
                        }
                        "ΒΡΑΔ" -> {
                            night = shift.quantity
                            nightHours = shift.hours
                        }
                    }
                }

                val total = morning + afternoon + night
                val totalHours = morningHours + afternoonHours + nightHours
                val mop = if (totalHours > 0) total / totalHours else 0.0

                dayLive.postValue(
                    DayStats(
                        morning = morning,
                        morningHours = morningHours,
                        afternoon = afternoon,
                        afternoonHours = afternoonHours,
                        night = night,
                        nightHours = nightHours,
                        total = total,
                        totalHours = totalHours,
                        mop = mop
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
                dayLive.postValue(
                    DayStats(
                        morning = 0,
                        morningHours = 0.0,
                        afternoon = 0,
                        afternoonHours = 0.0,
                        night = 0,
                        nightHours = 0.0,
                        total = 0,
                        totalHours = 0.0,
                        mop = 0.0
                    )
                )
            }
        }
    }

    fun saveShift(date: LocalDate, shift: String, qty: Int, hours: Double) {
        viewModelScope.launch {
            try {
                val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val entry = ShiftEntry(
                    date = dateString,
                    type = type,
                    shift = shift,
                    quantity = qty,
                    hours = hours
                )
                dao.insertOrUpdate(entry)

                // Reload after save
                loadDay(date)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadMonth(yearMonth: String) {
        viewModelScope.launch {
            try {
                val shifts = dao.getMonthShifts(yearMonth, type)
                val dailyData = mutableMapOf<String, Pair<Int, Double>>() // production to hours

                shifts.forEach { shift ->
                    val current = dailyData[shift.date] ?: Pair(0, 0.0)
                    dailyData[shift.date] = Pair(
                        current.first + shift.quantity,
                        current.second + shift.hours
                    )
                }

                val dailyMOP = dailyData.values.map { (prod, hrs) ->
                    if (hrs > 0) prod / hrs else 0.0
                }
                val monthMOP = if (dailyMOP.isNotEmpty()) dailyMOP.average() else 0.0

                monthLive.postValue(MonthStats(dailyMOP, monthMOP))
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
