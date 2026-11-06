package com.loopbreak.domain.detect

/** All detection thresholds live here. Defaults follow docs/PRODUCT.md. */
data class DetectionConfig(
    /** RE_OPEN: window, measured from the first open of a cluster. */
    val reOpenWindowMs: Long = 10 * 60_000L,
    /** RE_OPEN: minimum number of opens inside the window. */
    val reOpenMinCount: Int = 2,
    /** QUICK_PEEK: sessions shorter than this count as a reflex open. */
    val quickPeekMaxMs: Long = 5_000L,
    /** PING_PONG: the whole back-and-forth must start within this window. */
    val pingPongWindowMs: Long = 5 * 60_000L,
    /** PING_PONG: minimum number of switches (A→B→A→B is 3). */
    val pingPongMinSwitches: Int = 3,
    /** Two sessions of the same app closer than this are treated as one (activity transitions). */
    val mergeGapMs: Long = 500L,
    /**
     * If an app is resumed again after this long without any event while we still think it is
     * open, the earlier pause was lost and the old session is closed at its last event.
     */
    val staleMs: Long = 30 * 60_000L,
)
