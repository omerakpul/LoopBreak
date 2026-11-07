package com.loopbreak.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.loopbreak.R
import com.loopbreak.ui.components.LoopMark
import com.loopbreak.ui.components.ShadTabs
import com.loopbreak.ui.debug.DebugScreen
import com.loopbreak.ui.detail.AppDetailScreen
import com.loopbreak.ui.history.HistoryScreen
import com.loopbreak.ui.onboarding.OnboardingScreen
import com.loopbreak.ui.settings.SettingsScreen
import com.loopbreak.ui.theme.Shad
import com.loopbreak.ui.today.TodayScreen

private const val ROUTE_TODAY = "today"
private const val ROUTE_HISTORY = "history"
private const val ROUTE_SETTINGS = "settings"
private const val ROUTE_DEBUG = "debug"
private const val ROUTE_APP = "app/{pkg}"

private val tabRoutes = listOf(ROUTE_TODAY, ROUTE_HISTORY, ROUTE_SETTINGS)
private val tabTitles = listOf(R.string.tab_today, R.string.tab_history, R.string.tab_settings)

@Composable
fun AppRoot(viewModel: MainViewModel = hiltViewModel()) {
    val hasPermission by viewModel.hasPermission.collectAsStateWithLifecycle()
    val sync by viewModel.sync.collectAsStateWithLifecycle()

    // Coming back from the usage-access settings page lands here.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }

    if (!hasPermission) {
        Scaffold(Modifier.fillMaxSize(), containerColor = Shad.colors.background) { padding ->
            OnboardingScreen(Modifier.padding(padding))
        }
        return
    }

    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val selectedTab = tabRoutes.indexOf(backStack?.destination?.route)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Shad.colors.background,
        topBar = {
            // Always reserves the status bar; the header is only shown on the three main screens.
            Column(Modifier.background(Shad.colors.background).statusBarsPadding()) {
                if (selectedTab >= 0) {
                    Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)) {
                        Row(
                            Modifier.padding(bottom = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            LoopMark(size = 30.dp)
                            Text(
                                stringResource(R.string.app_name),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                        ShadTabs(
                            titles = tabTitles.map { stringResource(it) },
                            selected = selectedTab,
                            onSelect = { index ->
                                navController.navigate(tabRoutes[index]) {
                                    popUpTo(ROUTE_TODAY) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(navController = navController, startDestination = ROUTE_TODAY) {
            composable(ROUTE_TODAY) {
                TodayScreen(
                    contentPadding = padding,
                    sync = sync,
                    onRefresh = viewModel::refresh,
                    onOpenApp = { navController.navigate("app/$it") },
                )
            }
            composable(ROUTE_HISTORY) {
                HistoryScreen(
                    contentPadding = padding,
                    onOpenApp = { navController.navigate("app/$it") },
                )
            }
            composable(ROUTE_SETTINGS) {
                SettingsScreen(
                    contentPadding = padding,
                    onOpenDebug = { navController.navigate(ROUTE_DEBUG) },
                )
            }
            composable(
                ROUTE_APP,
                arguments = listOf(navArgument("pkg") { type = NavType.StringType }),
            ) {
                AppDetailScreen(
                    contentPadding = padding,
                    onBack = { navController.popBackStack() },
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

/** Shared list padding: 16dp gutters, room for the top bar and the system bars. */
fun screenPadding(outer: PaddingValues): PaddingValues = PaddingValues(
    start = 16.dp,
    end = 16.dp,
    top = outer.calculateTopPadding() + 12.dp,
    bottom = outer.calculateBottomPadding() + 24.dp,
)
