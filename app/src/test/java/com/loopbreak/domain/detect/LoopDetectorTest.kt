package com.loopbreak.domain.detect

import com.loopbreak.domain.detect.LoopScore.score
import com.loopbreak.domain.model.LoopType
import com.loopbreak.domain.model.Session
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoopDetectorTest {
    private val detector = LoopDetector(DetectionConfig())
    private val sec = 1_000L
    private val min = 60_000L

    private fun session(pkg: String, startSec: Long, durSec: Long) =
        Session(pkg, startSec * sec, (startSec + durSec) * sec)

    private fun detect(vararg s: Session, type: LoopType) =
        detector.detect(s.toList()).filter { it.type == type }

    // --- RE_OPEN ---

    @Test
    fun `two opens within the window is a re-open`() {
        val r = detect(session("a", 0, 60), session("a", 120, 60), type = LoopType.RE_OPEN)
        assertEquals(1, r.size)
        assertEquals(2, r[0].count)
        assertEquals("a", r[0].packageName)
    }

    @Test
    fun `a single open is not a re-open`() {
        assertTrue(detect(session("a", 0, 60), type = LoopType.RE_OPEN).isEmpty())
    }

    @Test
    fun `opens further apart than the window are not a re-open`() {
        val r = detect(session("a", 0, 30), session("a", 11 * 60, 30), type = LoopType.RE_OPEN)
        assertTrue(r.isEmpty())
    }

    @Test
    fun `re-open window is measured from the first open of the cluster`() {
        // 0, 6min, 12min: the third is outside the window of the first, so it starts a new cluster.
        val r = detect(
            session("a", 0, 30), session("a", 6 * 60, 30), session("a", 12 * 60, 30),
            type = LoopType.RE_OPEN,
        )
        assertEquals(1, r.size)
        assertEquals(2, r[0].count)
    }

    @Test
    fun `re-opens of different apps are counted separately`() {
        val r = detect(
            session("a", 0, 60), session("b", 70, 60), session("a", 140, 60), session("b", 210, 60),
            type = LoopType.RE_OPEN,
        )
        assertEquals(setOf("a", "b"), r.map { it.packageName }.toSet())
    }

    // --- QUICK_PEEK ---

    @Test
    fun `session shorter than 5s is a quick peek`() {
        val r = detect(session("a", 0, 3), type = LoopType.QUICK_PEEK)
        assertEquals(1, r.size)
    }

    @Test
    fun `session of exactly 5s is not a quick peek`() {
        assertTrue(detect(session("a", 0, 5), type = LoopType.QUICK_PEEK).isEmpty())
    }

    // --- PING_PONG ---

    @Test
    fun `A B A B within the window is a ping-pong`() {
        val r = detect(
            session("a", 0, 20), session("b", 30, 20), session("a", 60, 20), session("b", 90, 20),
            type = LoopType.PING_PONG,
        )
        assertEquals(1, r.size)
        assertEquals("a", r[0].packageName)
        assertEquals("b", r[0].otherPackage)
        assertEquals(3, r[0].count)
    }

    @Test
    fun `A B A is not enough for a ping-pong`() {
        val r = detect(
            session("a", 0, 20), session("b", 30, 20), session("a", 60, 20),
            type = LoopType.PING_PONG,
        )
        assertTrue(r.isEmpty())
    }

    @Test
    fun `a third app breaks the ping-pong`() {
        val r = detect(
            session("a", 0, 20), session("b", 30, 20), session("c", 60, 20), session("b", 90, 20),
            type = LoopType.PING_PONG,
        )
        assertTrue(r.isEmpty())
    }

    @Test
    fun `slow alternation outside the window is not a ping-pong`() {
        val r = detect(
            session("a", 0, 20), session("b", 2 * 60, 20),
            session("a", 4 * 60, 20), session("b", 6 * 60, 20),
            type = LoopType.PING_PONG,
        )
        assertTrue(r.isEmpty())
    }

    // --- score ---

    @Test
    fun `score is returns plus quick peeks`() {
        // a: three opens in 10 min (2 returns), one of them a 2s peek (1 quick peek) => 3
        val events = detector.detect(
            listOf(session("a", 0, 60), session("a", 100, 2), session("a", 200, 60)),
        )
        assertEquals(3, events.sumOf { it.score() })
        assertEquals(3, LoopScore.of(events))
    }
}
