package com.loopbreak.domain.detect

import com.loopbreak.domain.model.Session
import com.loopbreak.domain.model.UsageEvent
import com.loopbreak.domain.model.UsageEventType.PAUSED
import com.loopbreak.domain.model.UsageEventType.RESUMED
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionBuilderTest {
    private val builder = SessionBuilder(DetectionConfig())

    private fun resumed(pkg: String, t: Long) = UsageEvent(pkg, RESUMED, t)
    private fun paused(pkg: String, t: Long) = UsageEvent(pkg, PAUSED, t)

    @Test
    fun `resumed then paused makes one session`() {
        val s = builder.build(listOf(resumed("a", 1_000), paused("a", 5_000)))
        assertEquals(listOf(Session("a", 1_000, 5_000)), s)
    }

    @Test
    fun `activity transition inside one app is merged`() {
        val s = builder.build(
            listOf(
                resumed("a", 0), paused("a", 1_000),
                resumed("a", 1_100), paused("a", 4_000),
            ),
        )
        assertEquals(listOf(Session("a", 0, 4_000)), s)
    }

    @Test
    fun `resume before previous pause is one session`() {
        val s = builder.build(
            listOf(resumed("a", 0), resumed("a", 100), paused("a", 150), paused("a", 200)),
        )
        assertEquals(listOf(Session("a", 0, 200)), s)
    }

    @Test
    fun `another app resuming closes a session whose pause was lost`() {
        val s = builder.build(
            listOf(resumed("a", 0), resumed("b", 1_000), paused("b", 2_000)),
        )
        assertEquals(listOf(Session("a", 0, 1_000), Session("b", 1_000, 2_000)), s)
    }

    @Test
    fun `excluded packages are ignored`() {
        val s = builder.build(
            listOf(resumed("launcher", 0), paused("launcher", 500), resumed("a", 600), paused("a", 9_000)),
            excluded = setOf("launcher"),
        )
        assertEquals(listOf(Session("a", 600, 9_000)), s)
    }

    @Test
    fun `open session is dropped without openUntil and closed with it`() {
        val events = listOf(resumed("a", 1_000))
        assertTrue(builder.build(events).isEmpty())
        assertEquals(listOf(Session("a", 1_000, 7_000)), builder.build(events, openUntil = 7_000))
    }

    @Test
    fun `pause without a matching resume is ignored`() {
        assertTrue(builder.build(listOf(paused("a", 100))).isEmpty())
    }

    @Test
    fun `stale resume closes the old session at its last event`() {
        val stale = DetectionConfig().staleMs
        val s = builder.build(
            listOf(resumed("a", 0), resumed("a", stale + 10), paused("a", stale + 20)),
        )
        assertEquals(listOf(Session("a", 0, 0), Session("a", stale + 10, stale + 20)), s)
    }

    @Test
    fun `long session with a single pause stays long`() {
        val twoHours = 2 * 3_600_000L
        val s = builder.build(listOf(resumed("a", 0), paused("a", twoHours)))
        assertEquals(listOf(Session("a", 0, twoHours)), s)
    }
}
