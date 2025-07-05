package com.example.mop_calculator

import androidx.room.*

@Dao
interface ShiftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: ShiftEntry)

    @Query("SELECT * FROM shift_entries WHERE date = :date AND type = :type")
    suspend fun getDayShifts(date: String, type: String): List<ShiftEntry>

    @Query("SELECT * FROM shift_entries WHERE date LIKE :month || '%' AND type = :type")
    suspend fun getMonthShifts(month: String, type: String): List<ShiftEntry>

    // NEW: Get both 2F and 3F for summary
    @Query("SELECT * FROM shift_entries WHERE date = :date")
    suspend fun getAllDayShifts(date: String): List<ShiftEntry>

    @Query("DELETE FROM shift_entries WHERE date = :date AND type = :type AND shift = :shift")
    suspend fun deleteShift(date: String, type: String, shift: String)
}
