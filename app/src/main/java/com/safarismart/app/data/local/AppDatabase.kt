package com.safarismart.app.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.safarismart.app.domain.util.DateUtils

@Database(entities = [Trip::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun tripDao(): TripDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        /** Singleton: one database connection for the whole app. */
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "safarismart.db"
                )
                    .addCallback(SeedCallback)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}

/** Runs once, the first time the database file is created: inserts demo trips. */
private object SeedCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        val seeds = listOf(
            Trip(origin = "Githurai 45", destination = "CBD", sacco = "Super Metro",
                fare = 80.0, isPeak = true, delayMinutes = 15, tripDate = DateUtils.daysAgo(0)),
            Trip(origin = "CBD", destination = "Githurai 45", sacco = "Super Metro",
                fare = 100.0, isPeak = true, delayMinutes = 25, tripDate = DateUtils.daysAgo(0)),
            Trip(origin = "Githurai 45", destination = "CBD", sacco = "Super Metro",
                fare = 50.0, isPeak = false, delayMinutes = 0, tripDate = DateUtils.daysAgo(1)),
            Trip(origin = "Githurai 45", destination = "CBD", sacco = "Super Metro",
                fare = 80.0, isPeak = true, delayMinutes = 10, tripDate = DateUtils.daysAgo(2)),
            Trip(origin = "CBD", destination = "Rongai", sacco = "Metro Trans",
                fare = 120.0, isPeak = true, delayMinutes = 30, tripDate = DateUtils.daysAgo(3)),
            Trip(origin = "CBD", destination = "Rongai", sacco = "Metro Trans",
                fare = 70.0, isPeak = false, delayMinutes = 5, tripDate = DateUtils.daysAgo(5))
        )
        seeds.forEach { t ->
            val values = ContentValues().apply {
                put("origin", t.origin)
                put("destination", t.destination)
                put("sacco", t.sacco)
                put("fare", t.fare)
                put("isPeak", if (t.isPeak) 1 else 0)   // SQLite has no boolean type
                put("delayMinutes", t.delayMinutes)
                put("tripDate", t.tripDate)
            }
            db.insert("trips", SQLiteDatabase.CONFLICT_NONE, values)
        }
    }
}