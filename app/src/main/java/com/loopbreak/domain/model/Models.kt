package com.loopbreak.domain.model

/** Foreground/background transition reported by the system for a single activity. */
enum class UsageEventType(val code: Int) {
    RESUMED(1),
    PAUSED(2);

    companion object {
        fun fromCode(code: Int): UsageEventType? = entries.firstOrNull { it.code == code }
    }
}

data class UsageEvent(
    val packageName: String,
    val type: UsageEventType,
    val timestamp: Long,
)

/** A continuous stretch of time in which [packageName] was in the foreground. */
data class Session(
    val packageName: String,
    val start: Long,
    val end: Long,
) {
    val durationMs: Long get() = end - start
}

enum class LoopType {
    /** Same app brought to the foreground several times within a short window. */
    RE_OPEN,

    /** A very short foreground session (reflex open). */
    QUICK_PEEK,

    /** Bouncing back and forth between two apps. */
    PING_PONG,
}

data class LoopEvent(
    val type: LoopType,
    val packageName: String,
    /** Second app for [LoopType.PING_PONG], otherwise null. */
    val otherPackage: String?,
    val startTime: Long,
    val endTime: Long,
    /**
     * RE_OPEN: number of sessions in the cluster. QUICK_PEEK: 1.
     * PING_PONG: number of switches between the two apps.
     */
    val count: Int,
)
