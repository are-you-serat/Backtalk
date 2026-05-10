package off.kys.backtalk.presentation.activity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import off.kys.backtalk.common.base.BaseLockActivity
import off.kys.backtalk.presentation.activity.components.AppLifecycleHandler
import off.kys.backtalk.presentation.activity.components.MainView
import off.kys.backtalk.presentation.event.MainUiEvent
import off.kys.backtalk.presentation.viewmodel.MainViewModel
import off.kys.backtalk.presentation.viewmodel.MessagesViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.time.Duration.Companion.minutes

class MainActivity : BaseLockActivity() {

    private val viewModel by viewModel<MainViewModel>()
    private val messagesViewModel by viewModel<MessagesViewModel>()

    override var autoLockTimeout: Long = 1.minutes.inWholeMilliseconds
    override var isAuthRequired: Boolean = true
    override var isAnonymousMode: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        isAuthRequired = viewModel.preferences.lockEnabled
        isAnonymousMode = viewModel.preferences.secureScreenEnabled

        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            !isLoggedIn || messagesViewModel.uiState.value.isLoading
        }

        enableEdgeToEdge()

        setContent {
            AppLifecycleHandler(
                viewModel = viewModel,
                window = window
            ) { lockEnabled, secureEnabled ->
                isAuthRequired = lockEnabled
                isAnonymousMode = secureEnabled
            }

            MainView(
                viewModel = viewModel,
                isLoggedIn = isLoggedIn
            )
        }
    }

    fun checkForUpdates() {
        viewModel.onEvent(MainUiEvent.CheckUpdate)
    }
}