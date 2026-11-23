package com.safarismart.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/** One commute trip. Each property becomes a column in the "trips" SQLite table. */
@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val origin: String,
    val destination: String,
    val sacco: String,
    val fare: Double,
    val isPeak: Boolean,
    val delayMinutes: Int = 0,
    val tripDate: Long
)