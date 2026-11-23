package com.safarismart.app

import com.safarismart.app.domain.util.FareCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class FareCalculatorTest {
    @Test fun average_ofFares() =
        assertEquals(80.0, FareCalculator.average(listOf(50.0, 80.0, 110.0)), 0.001)

    @Test fun average_ofEmptyList_isZero() =
        assertEquals(0.0, FareCalculator.average(emptyList()), 0.001)

    @Test fun budgetUsed_isRoundedPercent() =
        assertEquals(75, FareCalculator.budgetUsedPercent(750.0, 1000.0))

    @Test fun budgetUsed_withZeroBudget_isZero() =
        assertEquals(0, FareCalculator.budgetUsedPercent(500.0, 0.0))

    @Test fun remaining_neverNegative() =
        assertEquals(0.0, FareCalculator.remaining(500.0, 800.0), 0.001)
}