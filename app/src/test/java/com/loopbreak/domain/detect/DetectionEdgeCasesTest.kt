package com.loopbreak.domain.detect

import com.loopbreak.domain.model.LoopType
import com.loopbreak.domain.model.Session
import com.loopbreak.domain.model.UsageEvent
import com.loopbreak.domain.model.UsageEventType.PAUSED
import com.loopbreak.domain.model.UsageEventType.RESUMED
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DetectionEdgeCasesTest {
    private val sec = 1_000L
    private val hour = 3_600_000L

    private fun session(pkg: String, startMs: Long, durSec: Long) =
        Session(pkg, startMs, startMs + durSec * sec)

    @Test
    fun `custom config raises the re-open threshold`() {
        val strict = LoopDetector(DetectionConfig(reOpenMinCount = 3))
        val two = listOf(session("a", 0, 30), session("a", 60 * sec, 30))
        val three = two + session("a", 120 * sec, 30)
        assertTrue(strict.detect(two).none { it.type == LoopType.RE_OPEN })
        assertEquals(1, strict.detect(three).count { it.type == LoopType.RE_OPEN })
    }

    @Test
    fun `custom config widens the quick peek limit`() {
        val lenient = LoopDetector(DetectionConfig(quickPeekMaxMs = 10_000L))
        val peeks = lenient.detect(listOf(session("a", 0, 8))).filter { it.type == LoopType.QUICK_PEEK }
        assertEquals(1, peeks.size)
    }

    @Test
    fun `a loop that straddles midnight is still one cluster`() {
        // Timestamps are plain epoch milliseconds: the detector does not care about day boundaries.
        val midnight = 24 * hour
        val r = LoopDetector().detect(
            listOf(session("a", midnight - 2 * 60 * sec, 20), session("a", midnight + 2 * 60 * sec, 20)),
        ).filter { it.type == LoopType.RE_OPEN }
        assertEquals(1, r.size)
        assertEquals(2, r[0].count)
    }

    @Test
    fun `a long ping-pong run is reported once, not once per window`() {
        val sessions = (0 until 6).map { i -> session(if (i % 2 == 0) "a" else "b", i * 30 * sec, 10) }
        val r = LoopDetector().detect(sessions).filter { it.type == LoopType.PING_PONG }
        assertEquals(1, r.size)
        assertEquals(5, r[0].count)
    }

    @Test
    fun `sessions are found even when given out of order`() {
        val shuffled = listOf(session("a", 120 * sec, 30), session("a", 0, 30), session("a", 60 * sec, 30))
        val r = LoopDetector().detect(shuffled).filter { it.type == LoopType.RE_OPEN }
        assertEquals(1, r.size)
        assertEquals(3, r[0].count)
    }

    @Test
    fun `merge gap boundary - 500 ms merges and 501 ms does not`() {
        val builder = SessionBuilder(DetectionConfig())
        fun build(gapMs: Long) = builder.build(
            listOf(
                UsageEvent("a", RESUMED, 0), UsageEvent("a", PAUSED, 1_000),
                UsageEvent("a", RESUMED, 1_000 + gapMs), UsageEvent("a", PAUSED, 5_000),
            ),
        )
        assertEquals(1, build(500).size)
        assertEquals(2, build(501).size)
    }
}
