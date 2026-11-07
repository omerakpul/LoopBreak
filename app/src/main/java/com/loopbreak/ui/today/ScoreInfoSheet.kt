package com.loopbreak.ui.today

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.loopbreak.R
import com.loopbreak.domain.detect.DetectionConfig
import com.loopbreak.ui.components.MutedText
import com.loopbreak.ui.components.ShadSheet
import com.loopbreak.ui.theme.Shad

/** Explains what the score means, using the thresholds the user has actually set. */
@Composable
fun ScoreInfoSheet(state: TodayUiState, onDismiss: () -> Unit) {
    // Ping-pong has fixed thresholds (not user-configurable).
    val pingPong = DetectionConfig()

    ShadSheet(onDismiss = onDismiss) {
        Text(stringResource(R.string.score_info_title), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        MutedText(stringResource(R.string.score_info_intro))
        Spacer(Modifier.height(12.dp))

        InfoRow(
            dot = Shad.colors.primary,
            title = stringResource(R.string.loop_type_re_open),
            text = stringResource(R.string.score_info_re_open, state.reOpenMinCount, state.reOpenWindowMin),
        )
        InfoRow(
            dot = Shad.colors.warning,
            title = stringResource(R.string.loop_type_quick_peek),
            text = stringResource(R.string.score_info_quick_peek, state.quickPeekMaxSec),
        )
        InfoRow(
            dot = Shad.colors.violet,
            title = stringResource(R.string.loop_type_ping_pong),
            text = stringResource(
                R.string.score_info_ping_pong,
                pingPong.pingPongMinSwitches,
                (pingPong.pingPongWindowMs / 60_000L).toInt(),
            ),
        )

        Spacer(Modifier.height(8.dp))
        MutedText(stringResource(R.string.score_info_footer))
    }
}

@Composable
private fun InfoRow(dot: Color, title: String, text: String) {
    Row(Modifier.padding(vertical = 8.dp)) {
        Box(
            Modifier
                .padding(top = 6.dp, end = 12.dp)
                .size(10.dp)
                .clip(RoundedCornerShape(50))
                .background(dot),
        )
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall)
            MutedText(text)
        }
    }
}
