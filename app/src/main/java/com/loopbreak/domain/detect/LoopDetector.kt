package com.loopbreak.domain.detect

import com.loopbreak.domain.model.LoopEvent
import com.loopbreak.domain.model.LoopType
import com.loopbreak.domain.model.Session

/** Finds loop behaviour in a list of sessions. Pure Kotlin. */
class LoopDetector(private val config: DetectionConfig = DetectionConfig()) {

    fun detect(sessions: List<Session>): List<LoopEvent> {
        val sorted = sessions.sortedBy { it.start }
        return (quickPeeks(sorted) + reOpens(sorted) + pingPongs(sorted)).sortedBy { it.startTime }
    }

    private fun quickPeeks(sessions: List<Session>): List<LoopEvent> =
        sessions
            .filter { it.durationMs < config.quickPeekMaxMs }
            .map { LoopEvent(LoopType.QUICK_PEEK, it.packageName, null, it.start, it.end, 1) }

    /**
     * Greedy clusters per app: the first unused session opens a window of [DetectionConfig.reOpenWindowMs];
     * every session of the same app starting inside it joins the cluster.
     */
    private fun reOpens(sessions: List<Session>): List<LoopEvent> {
        val out = mutableListOf<LoopEvent>()
        for ((pkg, list) in sessions.groupBy { it.packageName }) {
            var i = 0
            while (i < list.size) {
                var j = i
                while (j + 1 < list.size && list[j + 1].start - list[i].start <= config.reOpenWindowMs) j++
                val count = j - i + 1
                if (count >= config.reOpenMinCount) {
                    out += LoopEvent(LoopType.RE_OPEN, pkg, null, list[i].start, list[j].end, count)
                    i = j + 1
                } else {
                    i++
                }
            }
        }
        return out
    }

    /** Consecutive sessions alternating between exactly two apps, e.g. A→B→A→B. */
    private fun pingPongs(sessions: List<Session>): List<LoopEvent> {
        val out = mutableListOf<LoopEvent>()
        var i = 0
        while (i < sessions.size - 1) {
            val a = sessions[i].packageName
            val b = sessions[i + 1].packageName
            if (a == b || sessions[i + 1].start - sessions[i].start > config.pingPongWindowMs) {
                i++
                continue
            }
            var j = i + 1
            while (j + 1 < sessions.size &&
                sessions[j + 1].start - sessions[i].start <= config.pingPongWindowMs &&
                sessions[j + 1].packageName == (if ((j + 1 - i) % 2 == 0) a else b)
            ) {
                j++
            }
            val switches = j - i
            if (switches >= config.pingPongMinSwitches) {
                out += LoopEvent(LoopType.PING_PONG, a, b, sessions[i].start, sessions[j].end, switches)
                i = j + 1
            } else {
                i++
            }
        }
        return out
    }
}
