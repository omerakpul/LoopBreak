package com.loopbreak.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.loopbreak.ui.theme.Shad
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/** Shows short feedback messages; a newer message replaces the one on screen. */
class Toaster(private val host: SnackbarHostState, private val scope: CoroutineScope) {
    fun show(message: String) {
        scope.launch {
            host.currentSnackbarData?.dismiss()
            host.showSnackbar(message)
        }
    }
}

val LocalToaster = staticCompositionLocalOf<Toaster?> { null }

/** Snackbar host in the app's own look: card surface, quiet border, 14dp corners. */
@Composable
fun ShadSnackbarHost(state: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(state, modifier) { data ->
        Snackbar(
            modifier = Modifier.navigationBarsPadding().padding(start = 16.dp, end = 16.dp, bottom = 8.dp).border(1.dp, Shad.colors.border, RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            containerColor = Shad.colors.card,
            contentColor = Shad.colors.foreground,
        ) {
            Text(data.visuals.message)
        }
    }
}
