package com.example.mop_calculator

import android.app.Application
import android.util.Log
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

                Log.d("ProductionViewModel", "Loading day $dateString for type $type: found ${shifts.size} shifts")

                var morning = 0
                var morningHours = 0.0
                var afternoon = 0
                var afternoonHours = 0.0
                var night = 0
                var nightHours = 0.0

                shifts.forEach { shift ->
                    Log.d("ProductionViewModel", "Shift: ${shift.shift} = ${shift.quantity}, ${shift.hours}h")
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

                val stats = DayStats(
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

                Log.d("ProductionViewModel", "Posted stats: $stats")
                dayLive.postValue(stats)

                // Auto-load month data when day changes
                val yearMonth = date.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                loadMonth(yearMonth)

            } catch (e: Exception) {
                Log.e("ProductionViewModel", "Error loading day", e)
                dayLive.postValue(
                    DayStats(0, 0.0, 0, 0.0, 0, 0.0, 0, 0.0, 0.0)
                )
            }
        }
    }

    fun saveShift(date: LocalDate, shift: String, qty: Int, hours: Double) {
        viewModelScope.launch {
            try {
                val dateString = date.format(DateTimeFormatter.ISO_LOCAL_DATE)

                Log.d("ProductionViewModel", "Saving shift: $dateString, $type, $shift = $qty, ${hours}h")

                // Delete existing entry first
                dao.deleteShift(dateString, type, shift)

                // Insert new entry if values are not zero
                if (qty > 0 || hours > 0) {
                    val entry = ShiftEntry(
                        date = dateString,
                        type = type,
                        shift = shift,
                        quantity = qty,
                        hours = hours
                    )
                    dao.insertOrReplace(entry)
                    Log.d("ProductionViewModel", "Inserted entry: $entry")
                }

                // Reload data immediately
                loadDay(date)

            } catch (e: Exception) {
                Log.e("ProductionViewModel", "Error saving shift", e)
            }
        }
    }

    fun loadMonth(yearMonth: String) {
        viewModelScope.launch {
            try {
                val shifts = dao.getMonthShifts(yearMonth, type)
                val dailyData = mutableMapOf<String, Pair<Int, Double>>()

                // Calculate total monthly production and hours
                var totalMonthlyProduction = 0
                var totalMonthlyHours = 0.0

                shifts.forEach { shift ->
                    totalMonthlyProduction += shift.quantity
                    totalMonthlyHours += shift.hours

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

                monthLive.postValue(
                    MonthStats(
                        dailyMOP = dailyMOP,
                        monthMOP = monthMOP,
                        totalMonthlyProduction = totalMonthlyProduction,
                        totalMonthlyHours = totalMonthlyHours
                    )
                )
            } catch (e: Exception) {
                Log.e("ProductionViewModel", "Error loading month", e)
                monthLive.postValue(MonthStats(emptyList(), 0.0, 0, 0.0))
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
