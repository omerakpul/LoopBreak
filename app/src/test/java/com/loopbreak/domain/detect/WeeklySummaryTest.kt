package com.loopbreak.domain.detect

import com.loopbreak.domain.model.LoopEvent
import com.loopbreak.domain.model.LoopType
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WeeklySummaryTest {
    private fun peek(pkg: String) = LoopEvent(LoopType.QUICK_PEEK, pkg, null, 0, 1, 1)
    private fun reOpen(pkg: String, count: Int) = LoopEvent(LoopType.RE_OPEN, pkg, null, 0, 1, count)

    @Test
    fun `summary adds up both weeks and finds the top app`() {
        val thisWeek = listOf(reOpen("a", 4), peek("b"), peek("b"))   // a: 3, b: 2
        val lastWeek = listOf(peek("a"), peek("a"), peek("a"), peek("a"), peek("a"), peek("a"), peek("a"), peek("a"), peek("a"), peek("a")) // 10
        val s = WeeklySummary.of(thisWeek, lastWeek)
        assertEquals(5, s.total)
        assertEquals(10, s.previousTotal)
        assertEquals("a", s.topPackage)
        assertEquals(3, s.topScore)
        assertTrue(s.delta!!.isBetter)
        assertEquals(50, s.delta!!.percent)
    }

    @Test
    fun `an empty week has no top app`() {
        val s = WeeklySummary.of(emptyList(), listOf(peek("a")))
        assertEquals(0, s.total)
        assertNull(s.topPackage)
        assertFalse(s.isEmpty)
    }

    @Test
    fun `two empty weeks are not worth a notification`() {
        assertTrue(WeeklySummary.of(emptyList(), emptyList()).isEmpty)
    }

    @Test
    fun `ping-pong alone does not make an app the top app`() {
        val pong = LoopEvent(LoopType.PING_PONG, "a", "b", 0, 1, 3)
        assertNull(WeeklySummary.of(listOf(pong), emptyList()).topPackage)
    }

    // --- schedule ---

    private val zone = ZoneId.of("Europe/Istanbul")
    private fun at(y: Int, m: Int, d: Int, h: Int, min: Int = 0) = ZonedDateTime.of(y, m, d, h, min, 0, 0, zone)

    @Test
    fun `on a Saturday the next send is Sunday 19-00`() {
        // 2026-11-07 is a Saturday
        val delay = WeeklySchedule.delayUntilNext(at(2026, 11, 7, 12))
        assertEquals(Duration.ofHours(31).toMillis(), delay)
    }

    @Test
    fun `on Sunday before 19-00 it is the same day`() {
        val delay = WeeklySchedule.delayUntilNext(at(2026, 11, 8, 18, 30))
        assertEquals(Duration.ofMinutes(30).toMillis(), delay)
    }

    @Test
    fun `on Sunday after 19-00 it waits a week`() {
        val delay = WeeklySchedule.delayUntilNext(at(2026, 11, 8, 20))
        assertEquals(Duration.ofDays(6).plusHours(23).toMillis(), delay)
    }

    @Test
    fun `exactly 19-00 on Sunday waits a full week`() {
        val delay = WeeklySchedule.delayUntilNext(at(2026, 11, 8, 19))
        assertEquals(Duration.ofDays(7).toMillis(), delay)
    }
}
