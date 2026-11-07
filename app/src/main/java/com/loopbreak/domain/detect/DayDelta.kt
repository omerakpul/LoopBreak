package com.loopbreak.domain.detect

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Change of the loop score compared with the previous day.
 *
 * [delta] is current minus previous (negative is an improvement). [percent] is the change in whole
 * percent of the previous score, or null when the previous day had no loops to compare against.
 */
data class DayDelta(val delta: Int, val percent: Int?) {
    val isBetter: Boolean get() = delta < 0
    val isWorse: Boolean get() = delta > 0

    companion object {
        private const val MAX_PERCENT = 300

        /** Null when both days are empty: there is nothing worth saying. */
        fun of(current: Int, previous: Int): DayDelta? {
            if (current == 0 && previous == 0) return null
            val delta = current - previous
            val percent = if (previous > 0) (abs(delta) * 100f / previous).roundToInt() else null
            // A figure like +1700% is correct but not useful: past a point the plain difference reads better.
            return DayDelta(delta, percent?.takeIf { it <= MAX_PERCENT })
        }
    }
}
