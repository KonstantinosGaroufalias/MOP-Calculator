package com.example.mop_calculator

import androidx.room.*

@Dao
interface ShiftDao {
    @Query("DELETE FROM shift_entries WHERE date = :date AND type = :type AND shift = :shift")
    suspend fun deleteExisting(date: String, type: String, shift: String)

    @Insert
    suspend fun insertNew(entry: ShiftEntry)

    @Transaction
    suspend fun insertOrUpdate(entry: ShiftEntry) {
        deleteExisting(entry.date, entry.type, entry.shift)
        insertNew(entry)
    }

    @Query("SELECT * FROM shift_entries WHERE date = :date AND type = :type")
    suspend fun getDayShifts(date: String, type: String): List<ShiftEntry>

    @Query("SELECT * FROM shift_entries WHERE date LIKE :month || '%' AND type = :type")
    suspend fun getMonthShifts(month: String, type: String): List<ShiftEntry>

    @Query("SELECT * FROM shift_entries WHERE date = :date")
    suspend fun getAllDayShifts(date: String): List<ShiftEntry>
}
