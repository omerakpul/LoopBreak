package com.loopbreak.domain.detect

/**
 * Daily goal bookkeeping: "keep the loop score at or below [goal]". A goal of 0 means no goal.
 * Scores are ordered oldest first, today last, and must only contain days that were actually
 * tracked (days before the app started recording would otherwise look like perfect days).
 */
object GoalStreak {
    /** Consecutive days, counting back from the newest one, that stayed within the goal. */
    fun current(scores: List<Int>, goal: Int): Int {
        if (goal <= 0) return 0
        var days = 0
        for (score in scores.asReversed()) {
            if (score > goal) break
            days++
        }
        return days
    }

    fun daysWithin(scores: List<Int>, goal: Int): Int =
        if (goal <= 0) 0 else scores.count { it <= goal }
}
