package com.loopbreak.domain.detect

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DayDeltaTest {
    @Test
    fun `two empty days have nothing to compare`() {
        assertNull(DayDelta.of(0, 0))
    }

    @Test
    fun `fewer loops than yesterday is better and reported in percent`() {
        val d = DayDelta.of(7, 10)!!
        assertEquals(-3, d.delta)
        assertEquals(30, d.percent)
        assertTrue(d.isBetter)
        assertFalse(d.isWorse)
    }

    @Test
    fun `more loops than yesterday is worse`() {
        val d = DayDelta.of(15, 10)!!
        assertEquals(5, d.delta)
        assertEquals(50, d.percent)
        assertTrue(d.isWorse)
    }

    @Test
    fun `no loops yesterday has no percentage`() {
        val d = DayDelta.of(4, 0)!!
        assertEquals(4, d.delta)
        assertNull(d.percent)
        assertTrue(d.isWorse)
    }

    @Test
    fun `huge percentages fall back to the plain difference`() {
        val d = DayDelta.of(18, 1)!!
        assertEquals(17, d.delta)
        assertNull(d.percent)
        assertEquals(300, DayDelta.of(40, 10)!!.percent)
    }

    @Test
    fun `identical days are neither better nor worse`() {
        val d = DayDelta.of(6, 6)!!
        assertEquals(0, d.delta)
        assertEquals(0, d.percent)
        assertFalse(d.isBetter)
        assertFalse(d.isWorse)
    }

    @Test
    fun `percent is rounded to the nearest whole number`() {
        assertEquals(33, DayDelta.of(4, 6)!!.percent)
    }
}
