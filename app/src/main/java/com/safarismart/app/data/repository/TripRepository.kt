package com.safarismart.app.data.repository

import com.safarismart.app.data.local.Trip
import com.safarismart.app.data.local.TripDao
import com.safarismart.app.domain.util.DateUtils
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for trip data. ViewModels only know this class,
 * so the storage details (Room) stay hidden from the UI layer.
 */
class TripRepository(private val dao: TripDao) {

    /** All trips, newest first. Updates automatically when the table changes. */
    val trips: Flow<List<Trip>> = dao.observeAll()

    /** Total fare for the current Monday-to-Sunday week (bounds are fixed when called). */
    fun observeWeeklyTotal(): Flow<Double> =
        dao.observeFareBetween(DateUtils.startOfWeek(), DateUtils.endOfWeek())

    /** Same total as a one-off value, for the background worker. */
    suspend fun weeklyTotalNow(): Double =
        dao.sumFareBetween(DateUtils.startOfWeek(), DateUtils.endOfWeek())

    suspend fun getTrip(id: Int): Trip? = dao.getById(id)

    suspend fun addTrip(trip: Trip): Long = dao.insert(trip)

    suspend fun updateTrip(trip: Trip) = dao.update(trip)

    suspend fun deleteTrip(trip: Trip) = dao.delete(trip)

    suspend fun isDuplicate(trip: Trip): Boolean =
        dao.countDuplicates(
            origin = trip.origin.trim(),
            destination = trip.destination.trim(),
            sacco = trip.sacco.trim(),
            tripDate = trip.tripDate,
            isPeak = trip.isPeak,
            excludeId = trip.id
        ) > 0
}