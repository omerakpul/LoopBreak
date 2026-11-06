package com.loopbreak.domain.detect

import com.loopbreak.domain.model.Session
import org.junit.Assert.assertEquals
import org.junit.Test

class CalibrationStatsTest {
    private fun s(pkg: String, startSec: Long, durSec: Long) =
        Session(pkg, startSec * 1_000, (startSec + durSec) * 1_000)

    @Test
    fun `session lengths are counted cumulatively`() {
        val stats = CalibrationStats.from(listOf(s("a", 0, 1), s("a", 100, 4), s("b", 200, 20), s("b", 400, 90)))
        assertEquals(4, stats.sessionCount)
        assertEquals(1, stats.shorterThanSec[2])
        assertEquals(2, stats.shorterThanSec[5])
        assertEquals(2, stats.shorterThanSec[10])
        assertEquals(3, stats.shorterThanSec[30])
    }

    @Test
    fun `return gaps are measured per app between session starts`() {
        // a: gaps 30s and 4 min. b: gap 20 min. Different apps never form a gap.
        val stats = CalibrationStats.from(
            listOf(s("a", 0, 5), s("a", 30, 5), s("a", 30 + 240, 5), s("b", 10, 5), s("b", 10 + 1_200, 5)),
        )
        assertEquals(1, stats.returnWithinMin[1])
        assertEquals(2, stats.returnWithinMin[5])
        assertEquals(2, stats.returnWithinMin[10])
        assertEquals(3, stats.returnWithinMin[30])
    }

    @Test
    fun `empty input gives zeros`() {
        val stats = CalibrationStats.from(emptyList())
        assertEquals(0, stats.sessionCount)
        assertEquals(0, stats.shorterThanSec[5])
        assertEquals(0, stats.returnWithinMin[10])
    }
}
