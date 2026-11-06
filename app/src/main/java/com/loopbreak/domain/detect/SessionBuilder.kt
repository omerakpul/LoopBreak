package com.loopbreak.domain.detect

import com.loopbreak.domain.model.Session
import com.loopbreak.domain.model.UsageEvent
import com.loopbreak.domain.model.UsageEventType

/** Turns raw RESUMED/PAUSED events into foreground [Session]s. Pure Kotlin. */
class SessionBuilder(private val config: DetectionConfig = DetectionConfig()) {

    private class OpenSession(val start: Long) {
        var depth: Int = 1
        var lastEvent: Long = start
    }

    /**
     * @param excluded packages that are ignored completely (launcher, system UI, ...).
     * @param openUntil if set, sessions still open after the last event are closed at this time;
     *   otherwise they are dropped.
     */
    fun build(
        events: List<UsageEvent>,
        excluded: Set<String> = emptySet(),
        openUntil: Long? = null,
    ): List<Session> {
        val open = LinkedHashMap<String, OpenSession>()
        val raw = mutableListOf<Session>()

        fun close(pkg: String, end: Long) {
            val o = open.remove(pkg) ?: return
            raw += Session(pkg, o.start, maxOf(end, o.start))
        }

        for (e in events.sortedBy { it.timestamp }) {
            if (e.packageName in excluded) continue
            when (e.type) {
                UsageEventType.RESUMED -> {
                    // Only one app is in the foreground: anything else still open lost its pause.
                    for (other in open.keys.filter { it != e.packageName }) close(other, e.timestamp)

                    val o = open[e.packageName]
                    when {
                        o == null -> open[e.packageName] = OpenSession(e.timestamp)
                        e.timestamp - o.lastEvent > config.staleMs -> {
                            close(e.packageName, o.lastEvent)
                            open[e.packageName] = OpenSession(e.timestamp)
                        }
                        else -> {
                            o.depth++
                            o.lastEvent = e.timestamp
                        }
                    }
                }

                UsageEventType.PAUSED -> {
                    val o = open[e.packageName] ?: continue
                    o.depth--
                    o.lastEvent = e.timestamp
                    if (o.depth <= 0) close(e.packageName, e.timestamp)
                }
            }
        }

        if (openUntil != null) {
            for (pkg in open.keys.toList()) close(pkg, openUntil)
        }

        return merge(raw.sortedBy { it.start })
    }

    private fun merge(sorted: List<Session>): List<Session> {
        val out = mutableListOf<Session>()
        for (s in sorted) {
            val last = out.lastOrNull()
            if (last != null && last.packageName == s.packageName &&
                s.start - last.end <= config.mergeGapMs
            ) {
                out[out.lastIndex] = last.copy(end = maxOf(last.end, s.end))
            } else {
                out += s
            }
        }
        return out
    }
}
