package com.safarismart.app.domain.util

import kotlin.math.roundToInt

object FareCalculator {
    fun average(fares: List<Double>): Double =
        if (fares.isEmpty()) 0.0 else fares.sum() / fares.size

    /** 750 spent of 1000 budget -> 75 */
    fun budgetUsedPercent(spent: Double, budget: Double): Int =
        if (budget <= 0.0) 0 else ((spent / budget) * 100).roundToInt()

    fun remaining(budget: Double, spent: Double): Double =
        (budget - spent).coerceAtLeast(0.0)
}