package com.loopbreak.domain.detect

import com.loopbreak.domain.model.Session

/**
 * Distribution of session lengths and same-app return gaps, to help choose detection thresholds
 * from real usage instead of guessing. Pure Kotlin.
 */
data class CalibrationStats(
    val sessionCount: Int,
    /** Cumulative: how many sessions were shorter than each limit (seconds). */
    val shorterThanSec: Map<Int, Int>,
    /** Cumulative: how many returns to the same app came within each limit (minutes). */
    val returnWithinMin: Map<Int, Int>,
) {
    companion object {
        val SESSION_LIMITS_SEC = listOf(2, 5, 10, 30)
        val RETURN_LIMITS_MIN = listOf(1, 5, 10, 30)

        fun from(sessions: List<Session>): CalibrationStats {
            val shorter = SESSION_LIMITS_SEC.associateWith { limit ->
                sessions.count { it.durationMs < limit * 1_000L }
            }

            // Gap between the starts of consecutive sessions of the same app, like the re-open window.
            val gaps = sessions.groupBy { it.packageName }.values.flatMap { list ->
                list.sortedBy { it.start }.zipWithNext { a, b -> b.start - a.start }
            }
            val returns = RETURN_LIMITS_MIN.associateWith { limit ->
                gaps.count { it <= limit * 60_000L }
            }

            return CalibrationStats(sessions.size, shorter, returns)
        }
    }
}
