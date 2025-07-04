package com.example.mop_calculator

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shift_entries")
data class ShiftEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // ISO format: "2025-07-04"
    val type: String, // "2F" or "3F"
    val shift: String, // "ΠΡΩΙ", "ΑΠΟΓ", "ΒΡΑΔ"
    val quantity: Int
)
