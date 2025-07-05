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

                // Separate 2F and 3F data
                val shifts2F = allShifts.filter { it.type == "2F" }
                val shifts3F = allShifts.filter { it.type == "3F" }

                // Calculate 2F totals
                val total2F = shifts2F.sumOf { it.quantity }
                val totalHours2F = shifts2F.sumOf { it.hours }
                val mop2F = if (totalHours2F > 0) total2F / totalHours2F else 0.0

                // Calculate 3F totals
                val total3F = shifts3F.sumOf { it.quantity }
                val totalHours3F = shifts3F.sumOf { it.hours }
                val mop3F = if (totalHours3F > 0) total3F / totalHours3F else 0.0

                // Calculate final MOP: (mop2F + mop3F) / 2
                val finalMOP = if (mop2F > 0 && mop3F > 0) {
                    (mop2F + mop3F) / 2.0
                } else if (mop2F > 0) {
                    mop2F
                } else if (mop3F > 0) {
                    mop3F
                } else {
                    0.0
                }

                summaryLive.postValue(
                    SummaryStats(
                        total2F = total2F,
                        totalHours2F = totalHours2F,
                        mop2F = mop2F,
                        total3F = total3F,
                        totalHours3F = totalHours3F,
                        mop3F = mop3F,
                        finalMOP = finalMOP
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
                summaryLive.postValue(
                    SummaryStats(0, 0.0, 0.0, 0, 0.0, 0.0, 0.0)
                )
            }
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
