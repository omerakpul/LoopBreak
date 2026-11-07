package com.loopbreak.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.loopbreak.R
import com.loopbreak.ui.components.CardDescription
import com.loopbreak.ui.components.CardTitle
import com.loopbreak.ui.components.MutedText
import com.loopbreak.ui.components.ShadButton
import com.loopbreak.ui.components.ShadButtonVariant
import com.loopbreak.ui.components.ShadCard
import com.loopbreak.ui.components.ShadIconButton
import com.loopbreak.ui.components.ShadSeparator
import com.loopbreak.ui.screenPadding
import com.loopbreak.ui.theme.Shad

@Composable
fun SettingsScreen(
    contentPadding: PaddingValues,
    onOpenDebug: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val s = state.settings
    var confirmDelete by remember { mutableStateOf(false) }

    // Remembers which control asked for the notification permission, to continue once it is granted.
    var permissionFor by remember { mutableStateOf(NotificationFeature.Nudge) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            when (permissionFor) {
                NotificationFeature.Nudge -> viewModel.setNotificationsEnabled(true)
                NotificationFeature.Weekly -> viewModel.setWeeklySummaryEnabled(true)
                NotificationFeature.Preview -> viewModel.sendWeeklyPreview()
            }
        }
    }
    val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !viewModel.canPostNotifications()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = screenPadding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            ShadCard {
                CardTitle(stringResource(R.string.settings_thresholds))
                Spacer(Modifier.height(8.dp))
                Stepper(
                    label = stringResource(R.string.settings_re_open_count),
                    value = s.reOpenMinCount,
                    unit = "",
                    onChange = viewModel::setReOpenMinCount,
                )
                ShadSeparator()
                Stepper(
                    label = stringResource(R.string.settings_re_open_window),
                    value = s.reOpenWindowMin,
                    unit = stringResource(R.string.unit_min),
                    onChange = viewModel::setReOpenWindowMin,
                )
                ShadSeparator()
                Stepper(
                    label = stringResource(R.string.settings_quick_peek),
                    value = s.quickPeekMaxSec,
                    unit = stringResource(R.string.unit_sec),
                    onChange = viewModel::setQuickPeekMaxSec,
                )
            }
        }

        item {
            ShadCard {
                CardTitle(stringResource(R.string.settings_goal_title))
                CardDescription(stringResource(R.string.settings_goal_body))
                Spacer(Modifier.height(8.dp))
                Stepper(
                    label = stringResource(R.string.settings_goal_label),
                    value = s.dailyGoal,
                    unit = "",
                    onChange = viewModel::setDailyGoal,
                )
            }
        }

        state.calibration?.takeIf { it.sessionCount > 0 }?.let { stats ->
            item {
                ShadCard {
                    CardTitle(stringResource(R.string.settings_hint_title))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.settings_hint_sessions, stats.sessionCount),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    val shorter = stats.shorterThanSec.entries.map {
                        stringResource(R.string.settings_hint_shorter, it.key, it.value)
                    }
                    MutedText(shorter.joinToString("   ·   "))
                    Spacer(Modifier.height(12.dp))
                    Text(
                        stringResource(R.string.settings_hint_returns),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    val within = stats.returnWithinMin.entries.map {
                        stringResource(R.string.settings_hint_within, it.key, it.value)
                    }
                    MutedText(within.joinToString("   ·   "))
                    Spacer(Modifier.height(12.dp))
                    MutedText(stringResource(R.string.settings_hint_body))
                }
            }
        }

        item {
            ShadCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        CardTitle(stringResource(R.string.settings_notify_title))
                        CardDescription(stringResource(R.string.settings_notify_body))
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = s.notificationsEnabled,
                        onCheckedChange = { enable ->
                            if (enable && needsPermission) {
                                permissionFor = NotificationFeature.Nudge
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.setNotificationsEnabled(enable)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Shad.colors.primaryForeground,
                            checkedTrackColor = Shad.colors.primary,
                            checkedBorderColor = Color.Transparent,
                            uncheckedThumbColor = Shad.colors.foreground,
                            uncheckedTrackColor = Shad.colors.input,
                            uncheckedBorderColor = Color.Transparent,
                        ),
                    )
                }
            }
        }

        item {
            ShadCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        CardTitle(stringResource(R.string.settings_weekly_title))
                        CardDescription(stringResource(R.string.settings_weekly_body))
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = s.weeklySummaryEnabled,
                        onCheckedChange = { enable ->
                            if (enable && needsPermission) {
                                permissionFor = NotificationFeature.Weekly
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                viewModel.setWeeklySummaryEnabled(enable)
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Shad.colors.primaryForeground,
                            checkedTrackColor = Shad.colors.primary,
                            checkedBorderColor = Color.Transparent,
                            uncheckedThumbColor = Shad.colors.foreground,
                            uncheckedTrackColor = Shad.colors.input,
                            uncheckedBorderColor = Color.Transparent,
                        ),
                    )
                }
                Spacer(Modifier.height(12.dp))
                ShadButton(
                    text = stringResource(R.string.settings_weekly_preview),
                    onClick = {
                        if (needsPermission) {
                            permissionFor = NotificationFeature.Preview
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.sendWeeklyPreview()
                        }
                    },
                    variant = ShadButtonVariant.Outline,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        item {
            ShadCard {
                CardTitle(stringResource(R.string.settings_excluded))
                CardDescription(stringResource(R.string.settings_excluded_hint))
                state.candidates.forEach { app ->
                    Spacer(Modifier.height(8.dp))
                    ShadSeparator()
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.setExcluded(app.packageName, !app.excluded) }
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = app.excluded,
                            onCheckedChange = { viewModel.setExcluded(app.packageName, it) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Shad.colors.primary,
                                checkmarkColor = Shad.colors.primaryForeground,
                                uncheckedColor = Shad.colors.input,
                            ),
                        )
                        Text(app.label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }

        item {
            ShadCard {
                CardTitle(stringResource(R.string.settings_data))
                Spacer(Modifier.height(12.dp))
                ShadButton(
                    text = stringResource(R.string.action_raw_events),
                    onClick = onOpenDebug,
                    variant = ShadButtonVariant.Outline,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                ShadButton(
                    text = stringResource(R.string.settings_delete),
                    onClick = { confirmDelete = true },
                    variant = ShadButtonVariant.Destructive,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = Shad.colors.card,
            titleContentColor = Shad.colors.foreground,
            textContentColor = Shad.colors.mutedForeground,
            title = { Text(stringResource(R.string.settings_delete)) },
            text = { Text(stringResource(R.string.settings_delete_confirm)) },
            confirmButton = {
                ShadButton(
                    text = stringResource(R.string.settings_delete_yes),
                    onClick = {
                        confirmDelete = false
                        viewModel.deleteAllData()
                    },
                    variant = ShadButtonVariant.Destructive,
                )
            },
            dismissButton = {
                ShadButton(
                    text = stringResource(R.string.cancel),
                    onClick = { confirmDelete = false },
                    variant = ShadButtonVariant.Outline,
                )
            },
        )
    }
}

private enum class NotificationFeature { Nudge, Weekly, Preview }

@Composable
private fun Stepper(label: String, value: Int, unit: String, onChange: (Int) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        ShadIconButton("−", stringResource(R.string.action_decrease, label), onClick = { onChange(value - 1) })
        Text(
            "$value $unit".trim(),
            style = MaterialTheme.typography.titleSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(56.dp),
        )
        ShadIconButton("+", stringResource(R.string.action_increase, label), onClick = { onChange(value + 1) })
    }
}
