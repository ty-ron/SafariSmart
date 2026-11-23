package com.safarismart.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/** Data Access Object: every SQL query the app runs lives here. */
@Dao
interface TripDao {

    // ---- READ ----
    /** Flow re-emits automatically whenever the trips table changes. */
    @Query("SELECT * FROM trips ORDER BY tripDate DESC, id DESC")
    fun observeAll(): Flow<List<Trip>>

    @Query("SELECT * FROM trips WHERE id = :id")
    suspend fun getById(id: Int): Trip?

    /** Live weekly total for the dashboard. COALESCE returns 0 when there are no rows. */
    @Query("SELECT COALESCE(SUM(fare), 0) FROM trips WHERE tripDate BETWEEN :from AND :to")
    fun observeFareBetween(from: Long, to: Long): Flow<Double>

    /** One-off total, used by the background worker. */
    @Query("SELECT COALESCE(SUM(fare), 0) FROM trips WHERE tripDate BETWEEN :from AND :to")
    suspend fun sumFareBetween(from: Long, to: Long): Double

    /** Duplicate = same route, SACCO, day and peak tag. excludeId lets an edit ignore itself. */
    @Query(
        """
        SELECT COUNT(*) FROM trips
        WHERE LOWER(origin) = LOWER(:origin)
          AND LOWER(destination) = LOWER(:destination)
          AND LOWER(sacco) = LOWER(:sacco)
          AND tripDate = :tripDate
          AND isPeak = :isPeak
          AND id != :excludeId
        """
    )
    suspend fun countDuplicates(
        origin: String,
        destination: String,
        sacco: String,
        tripDate: Long,
        isPeak: Boolean,
        excludeId: Int
    ): Int

    // ---- CREATE / UPDATE / DELETE ----
    @Insert
    suspend fun insert(trip: Trip): Long

    @Update
    suspend fun update(trip: Trip)

    @Delete
    suspend fun delete(trip: Trip)
}