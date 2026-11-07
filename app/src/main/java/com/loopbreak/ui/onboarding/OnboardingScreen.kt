package com.loopbreak.ui.onboarding

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.loopbreak.R
import com.loopbreak.ui.components.CardDescription
import com.loopbreak.ui.components.LoopMark
import com.loopbreak.ui.components.MutedText
import com.loopbreak.ui.components.ShadButton
import com.loopbreak.ui.components.ShadCard
import com.loopbreak.ui.components.heroBrush

@Composable
fun OnboardingScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ShadCard(brush = heroBrush()) {
            LoopMark(size = 72.dp)
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.onboarding_title), style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            CardDescription(stringResource(R.string.onboarding_body))
            Spacer(Modifier.height(12.dp))
            MutedText(stringResource(R.string.onboarding_privacy))
            Spacer(Modifier.height(20.dp))
            ShadButton(
                text = stringResource(R.string.onboarding_button),
                onClick = { openUsageAccessSettings(context) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun openUsageAccessSettings(context: Context) {
    val withPackage = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        .setData("package:${context.packageName}".toUri())
    try {
        context.startActivity(withPackage)
    } catch (_: ActivityNotFoundException) {
        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }
}
