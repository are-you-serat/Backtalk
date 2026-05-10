package off.kys.backtalk.presentation.activity.components

import android.view.WindowManager
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import off.kys.backtalk.BuildConfig
import off.kys.backtalk.presentation.activity.MainActivity
import off.kys.backtalk.presentation.event.MainUiEvent
import off.kys.backtalk.presentation.screen.messages.MessagesScreen
import off.kys.backtalk.presentation.state.MainUiState
import off.kys.backtalk.presentation.theme.BacktalkTheme
import off.kys.backtalk.presentation.viewmodel.MainViewModel

@Composable
fun MainView(
    viewModel: MainViewModel,
    isLoggedIn: Boolean
) {
    val mainActivity = LocalActivity.current as? MainActivity
    val window = mainActivity?.window
    val isDarkTheme = viewModel.preferences.themeMode.isDark(isSystemInDarkTheme())
    val dynamicColor = viewModel.preferences.dynamicColorEnabled
    val updateState by viewModel.mainUiState.collectAsStateWithLifecycle()

    // Side effect management stays with the UI definition
    LaunchedEffect(Unit) {
        if (!BuildConfig.IS_FDROID) {
            viewModel.onEvent(MainUiEvent.CheckUpdate)
        }
    }

    LaunchedEffect(viewModel.preferences.secureScreenEnabled) {
        val enabled = viewModel.preferences.secureScreenEnabled

        isAnonymousMode = enabled
        updateSystemFlags(enabled)
    }

    LaunchedEffect(viewModel.preferences.keepScreenOn) {
        if (window == null)
            return@LaunchedEffect
        val enabled = viewModel.preferences.keepScreenOn

        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    BacktalkTheme(
        darkTheme = isDarkTheme,
        dynamicColor = dynamicColor
    ) {
        Crossfade(targetState = isLoggedIn, label = "LoginState") { loggedIn ->
            if (loggedIn) {
                Navigator(MessagesScreen()) { navigator ->
                    SlideTransition(navigator)
                }

                (updateState as? MainUiState.UpdateAvailable)?.let { state ->
                    val url = state.result.downloadUrls.firstOrNull()?.browserDownloadUrl
                        ?: return@let
                    AppUpdateDialog(
                        updateResult = state.result,
                        onDismissRequest = { viewModel.onEvent(MainUiEvent.DismissDialog) },
                        onUpdateClick = { viewModel.onEvent(MainUiEvent.UpdateNow(url)) }
                    )
                }
            } else {
                LockView()
            }
        }
    }
}