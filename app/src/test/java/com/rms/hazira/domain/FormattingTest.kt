package com.rms.hazira.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class FormattingTest {

    @Test
    fun takaHasSymbolAndThousandsSeparator() {
        assertEquals("৳20,380", formatTaka(20_380))
    }

    @Test
    fun takaWithoutThousandsHasNoSeparator() {
        assertEquals("৳500", formatTaka(500))
        assertEquals("৳0", formatTaka(0))
    }

    @Test
    fun negativeTakaKeepsMinusInFrontOfSymbol() {
        assertEquals("−৳300", formatTaka(-300))
        assertEquals("−৳2,500", formatTaka(-2_500))
    }

    @Test
    fun wholeQuantityHasNoTrailingZeros() {
        assertEquals("1", formatQuantity(1.0))
        assertEquals("32", formatQuantity(32.0))
    }

    @Test
    fun fractionalQuantityKeepsItsFraction() {
        assertEquals("0.5", formatQuantity(0.5))
        assertEquals("2.25", formatQuantity(2.25))
    }

    @Test
    fun quantityWithUnitJoinsWithSpace() {
        assertEquals("2 litre", formatQuantityWithUnit(2.0, "litre"))
        assertEquals("0.5 litre", formatQuantityWithUnit(0.5, "litre"))
    }

    @Test
    fun quantityWithBlankUnitHasNoTrailingSpace() {
        assertEquals("3", formatQuantityWithUnit(3.0, ""))
        assertEquals("3", formatQuantityWithUnit(3.0, "  "))
    }
}
