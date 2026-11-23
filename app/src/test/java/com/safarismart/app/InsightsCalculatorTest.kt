package com.safarismart.app

import com.safarismart.app.data.local.Trip
import com.safarismart.app.domain.util.BudgetLevel
import com.safarismart.app.domain.util.DateUtils
import com.safarismart.app.domain.util.InsightsCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InsightsCalculatorTest {
    private val now = System.currentTimeMillis()

    private fun trip(
        fare: Double,
        peak: Boolean,
        delay: Int = 0,
        from: String = "A",
        to: String = "B"
    ) = Trip(
        origin = from,
        destination = to,
        sacco = "S",
        fare = fare,
        isPeak = peak,
        delayMinutes = delay,
        tripDate = DateUtils.startOfDay(now)
    )

    @Test fun peakExtraPercent_comparesAverages() {
        val result = InsightsCalculator.compute(listOf(trip(100.0, true), trip(50.0, false)), now)
        assertEquals(100, result.peakExtraPercent)
    }

    @Test fun peakExtraPercent_isNull_whenOnlyPeakTripsExist() {
        val result = InsightsCalculator.compute(listOf(trip(100.0, true)), now)
        assertNull(result.peakExtraPercent)
    }

    @Test fun mostDelayedRoute_comesFirst() {
        val trips = listOf(
            trip(80.0, true, delay = 10),
            trip(90.0, true, delay = 30, from = "C", to = "D")
        )
        assertEquals("C → D", InsightsCalculator.compute(trips, now).routeDelays.first().route)
    }

    @Test fun budgetLevel_changesAtThresholds() {
        assertEquals(BudgetLevel.OK, InsightsCalculator.budgetLevel(500.0, 1000.0))
        assertEquals(BudgetLevel.WARNING, InsightsCalculator.budgetLevel(850.0, 1000.0))
        assertEquals(BudgetLevel.OVER, InsightsCalculator.budgetLevel(1000.0, 1000.0))
    }
}