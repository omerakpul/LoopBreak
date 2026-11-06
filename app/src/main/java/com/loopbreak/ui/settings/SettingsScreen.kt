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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.loopbreak.R

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

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        viewModel.setNotificationsEnabled(granted)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding() + 8.dp,
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { Header(stringResource(R.string.settings_thresholds)) }
        item {
            Stepper(
                label = stringResource(R.string.settings_re_open_count),
                value = s.reOpenMinCount,
                unit = "",
                onChange = viewModel::setReOpenMinCount,
            )
        }
        item {
            Stepper(
                label = stringResource(R.string.settings_re_open_window),
                value = s.reOpenWindowMin,
                unit = stringResource(R.string.unit_min),
                onChange = viewModel::setReOpenWindowMin,
            )
        }
        item {
            Stepper(
                label = stringResource(R.string.settings_quick_peek),
                value = s.quickPeekMaxSec,
                unit = stringResource(R.string.unit_sec),
                onChange = viewModel::setQuickPeekMaxSec,
            )
        }

        state.calibration?.takeIf { it.sessionCount > 0 }?.let { stats ->
            item { Header(stringResource(R.string.settings_hint_title)) }
            item {
                val parts = stats.shorterThanSec.entries.map {
                    stringResource(R.string.settings_hint_shorter, it.key, it.value)
                }
                Text(
                    stringResource(R.string.settings_hint_sessions, stats.sessionCount) +
                        "\n" + parts.joinToString("  ·  "),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            item {
                val parts = stats.returnWithinMin.entries.map {
                    stringResource(R.string.settings_hint_within, it.key, it.value)
                }
                Text(
                    stringResource(R.string.settings_hint_returns) +
                        "\n" + parts.joinToString("  ·  "),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            item {
                Text(
                    stringResource(R.string.settings_hint_body),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item { Header(stringResource(R.string.settings_notifications)) }
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.settings_notify_title), style = MaterialTheme.typography.bodyLarge)
                    Text(
                        stringResource(R.string.settings_notify_body),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = s.notificationsEnabled,
                    onCheckedChange = { enable ->
                        if (enable && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !viewModel.canPostNotifications()) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.setNotificationsEnabled(enable)
                        }
                    },
                )
            }
        }

        item { Header(stringResource(R.string.settings_excluded)) }
        item {
            Text(
                stringResource(R.string.settings_excluded_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        items(state.candidates, key = { it.packageName }) { app ->
            Row(
                Modifier.fillMaxWidth().clickable { viewModel.setExcluded(app.packageName, !app.excluded) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(checked = app.excluded, onCheckedChange = { viewModel.setExcluded(app.packageName, it) })
                Text(app.label, style = MaterialTheme.typography.bodyLarge)
            }
        }

        item { Header(stringResource(R.string.settings_data)) }
        item {
            OutlinedButton(onClick = onOpenDebug, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.action_raw_events))
            }
        }
        item {
            OutlinedButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.settings_delete))
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.settings_delete)) },
            text = { Text(stringResource(R.string.settings_delete_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.deleteAllData()
                }) { Text(stringResource(R.string.settings_delete_yes)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

@Composable
private fun Header(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
}

@Composable
private fun Stepper(label: String, value: Int, unit: String, onChange: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        OutlinedButton(onClick = { onChange(value - 1) }) { Text("−") }
        Text(
            "$value $unit".trim(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        OutlinedButton(onClick = { onChange(value + 1) }) { Text("+") }
    }
}
