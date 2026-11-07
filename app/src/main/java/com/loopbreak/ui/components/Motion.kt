package com.loopbreak.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember

/**
 * A value that runs from 0 to 1 when the composable first appears and again whenever [key]
 * changes. Charts multiply their sizes by it to grow in. Read it inside a draw block so only the
 * drawing is invalidated each frame. Android's "remove animations" setting is respected.
 */
@Composable
fun rememberEntrance(key: Any?, durationMs: Int = 700): State<Float> {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(key) {
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMs, easing = FastOutSlowInEasing))
    }
    return progress.asState()
}
