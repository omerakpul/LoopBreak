package com.loopbreak.domain.detect

import org.junit.Assert.assertEquals
import org.junit.Test

class GoalStreakTest {
    @Test
    fun `no goal means no streak and nothing within goal`() {
        assertEquals(0, GoalStreak.current(listOf(1, 2, 3), goal = 0))
        assertEquals(0, GoalStreak.daysWithin(listOf(1, 2, 3), goal = 0))
    }

    @Test
    fun `streak counts back from today until the first miss`() {
        // oldest -> newest: miss, ok, ok, ok
        assertEquals(3, GoalStreak.current(listOf(20, 8, 10, 0), goal = 10))
    }

    @Test
    fun `a miss today ends the streak immediately`() {
        assertEquals(0, GoalStreak.current(listOf(1, 2, 12), goal = 10))
    }

    @Test
    fun `exactly at the goal still counts`() {
        assertEquals(2, GoalStreak.current(listOf(10, 10), goal = 10))
    }

    @Test
    fun `days within goal ignores order`() {
        assertEquals(2, GoalStreak.daysWithin(listOf(20, 8, 12, 0), goal = 10))
    }

    @Test
    fun `empty history has no streak`() {
        assertEquals(0, GoalStreak.current(emptyList(), goal = 10))
    }
}
