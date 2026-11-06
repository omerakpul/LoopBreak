package com.loopbreak.domain.detect

import com.loopbreak.domain.model.LoopEvent
import com.loopbreak.domain.model.LoopType

/**
 * Daily loop score: re-opens (returns to an app after the first open) plus quick peeks.
 * Ping-pong is reported separately and not part of the score yet.
 */
object LoopScore {
    fun of(events: List<LoopEvent>): Int = events.sumOf { it.score() }

    fun LoopEvent.score(): Int = when (type) {
        LoopType.RE_OPEN -> count - 1
        LoopType.QUICK_PEEK -> 1
        LoopType.PING_PONG -> 0
    }
}
