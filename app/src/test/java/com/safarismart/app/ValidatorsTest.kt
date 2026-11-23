package com.safarismart.app

import com.safarismart.app.domain.util.Validators
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidatorsTest {
    @Test fun emptyStage_isRejected() =
        assertNotNull(Validators.validateStage(" ", "Origin"))

    @Test fun validStage_isAccepted() =
        assertNull(Validators.validateStage("Githurai 45", "Origin"))

    @Test fun zeroFare_isRejected() =
        assertEquals("Fare must be more than 0", Validators.validateFare("0"))

    @Test fun negativeFare_isRejected() =
        assertNotNull(Validators.validateFare("-50"))

    @Test fun nonNumericFare_isRejected() =
        assertEquals("Enter a valid fare", Validators.validateFare("abc"))

    @Test fun validFare_isAccepted() =
        assertNull(Validators.validateFare("80"))

    @Test fun sameStages_areRejected() =
        assertNotNull(Validators.validateDifferent("CBD", "cbd"))

    @Test fun blankDelay_isAllowed() =
        assertNull(Validators.validateDelay(""))

    @Test fun negativeDelay_isRejected() =
        assertNotNull(Validators.validateDelay("-5"))
}