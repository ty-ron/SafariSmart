package com.safarismart.app.domain.util

import com.safarismart.app.data.local.Trip
import kotlin.math.roundToInt

enum class BudgetLevel { OK, WARNING, OVER }

data class RouteDelay(val route: String, val averageDelay: Double, val trips: Int)

data class Insights(
    val monthTotal: Double,
    val averagePeakFare: Double,
    val averageOffPeakFare: Double,
    val peakExtraPercent: Int?,       // null = not enough data to compare
    val mostUsedRoute: String?,
    val routeDelays: List<RouteDelay> // only routes with delays, worst first
)

/** Pure Kotlin logic: no Android classes, so it is easy to unit test. */
object InsightsCalculator {

    fun compute(trips: List<Trip>, now: Long = System.currentTimeMillis()): Insights {
        val monthStart = DateUtils.startOfMonth(now)
        val peakFares = trips.filter { it.isPeak }.map { it.fare }
        val offPeakFares = trips.filterNot { it.isPeak }.map { it.fare }

        val avgPeak = FareCalculator.average(peakFares)
        val avgOffPeak = FareCalculator.average(offPeakFares)
        val peakExtra = if (peakFares.isNotEmpty() && offPeakFares.isNotEmpty() && avgOffPeak > 0.0)
            (((avgPeak - avgOffPeak) / avgOffPeak) * 100).roundToInt() else null

        val byRoute = trips.groupBy { "${it.origin.trim()} → ${it.destination.trim()}" }

        return Insights(
            monthTotal = trips.filter { it.tripDate >= monthStart }.sumOf { it.fare },
            averagePeakFare = avgPeak,
            averageOffPeakFare = avgOffPeak,
            peakExtraPercent = peakExtra,
            mostUsedRoute = byRoute.maxByOrNull { it.value.size }?.key,
            routeDelays = byRoute
                .map { (route, list) ->
                    RouteDelay(route, list.map { it.delayMinutes }.average(), list.size)
                }
                .filter { it.averageDelay > 0.0 }
                .sortedByDescending { it.averageDelay }
        )
    }

    /** WARNING from 80% of the budget, OVER at 100%. */
    fun budgetLevel(spent: Double, budget: Double): BudgetLevel = when {
        budget <= 0.0 -> BudgetLevel.OK
        spent >= budget -> BudgetLevel.OVER
        spent >= budget * 0.8 -> BudgetLevel.WARNING
        else -> BudgetLevel.OK
    }
}