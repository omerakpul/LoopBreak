package com.loopbreak.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.loopbreak.R
import com.loopbreak.ui.debug.DebugScreen
import com.loopbreak.ui.history.HistoryScreen
import com.loopbreak.ui.onboarding.OnboardingScreen
import com.loopbreak.ui.settings.SettingsScreen
import com.loopbreak.ui.today.TodayScreen

private const val ROUTE_TODAY = "today"
private const val ROUTE_HISTORY = "history"
private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_DEBUG = "debug"

private data class Tab(val route: String, val titleRes: Int)

private val tabs = listOf(
    Tab(ROUTE_TODAY, R.string.tab_today),
    Tab(ROUTE_HISTORY, R.string.tab_history),
    Tab(ROUTE_SETTINGS, R.string.tab_settings),
)

@Composable
fun AppRoot(viewModel: MainViewModel = hiltViewModel()) {
    val hasPermission by viewModel.hasPermission.collectAsStateWithLifecycle()
    val sync by viewModel.sync.collectAsStateWithLifecycle()

    // Coming back from the usage-access settings page lands here.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }

    if (!hasPermission) {
        Scaffold(Modifier.fillMaxSize()) { padding ->
            OnboardingScreen(Modifier.padding(padding))
        }
        return
    }

    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route
    val selectedTab = tabs.indexOfFirst { it.route == currentRoute }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // Always reserves the status bar; the tab row is only shown on the three main screens.
            Column(Modifier.statusBarsPadding()) {
                if (selectedTab >= 0) {
                    PrimaryTabRow(selectedTabIndex = selectedTab) {
                        tabs.forEachIndexed { index, tab ->
                            Tab(
                                selected = index == selectedTab,
                                onClick = {
                                    navController.navigate(tab.route) {
                                        popUpTo(ROUTE_TODAY) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                text = { Text(stringResource(tab.titleRes)) },
                            )
                        }
                    }
                }
            }
        },
    ) { padding ->
        NavHost(navController = navController, startDestination = ROUTE_TODAY) {
            composable(ROUTE_TODAY) {
                TodayScreen(contentPadding = padding, sync = sync, onRefresh = viewModel::refresh)
            }
            composable(ROUTE_HISTORY) {
                HistoryScreen(contentPadding = padding)
            }
            composable(ROUTE_SETTINGS) {
                SettingsScreen(
                    contentPadding = padding,
                    onOpenDebug = { navController.navigate(ROUTE_DEBUG) },
                )
            }
            composable(ROUTE_DEBUG) {
                DebugScreen(
                    contentPadding = padding,
                    onBack = { navController.popBackStack() },
                    onReset = viewModel::resetAndResync,
                )
            }
        }
    }
}
