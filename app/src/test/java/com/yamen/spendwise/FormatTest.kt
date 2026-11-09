package com.yamen.spendwise

import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit tests for the Forint formatting (run with: gradlew test). */
class FormatTest {

    @Test
    fun smallAmountHasNoSpace() {
        assertEquals("0", formatAmount(0))
        assertEquals("950", formatAmount(950))
    }

    @Test
    fun thousandsAreSeparatedBySpace() {
        assertEquals("4 500", formatAmount(4500))
        assertEquals("1 234 567", formatAmount(1234567))
    }
}
