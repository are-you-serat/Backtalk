package off.kys.backtalk.presentation.activity.components

import android.view.Window
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import off.kys.backtalk.presentation.viewmodel.MainViewModel

@Composable
fun AppLifecycleHandler(
    viewModel: MainViewModel,
    window: Window?,
    onPreferenceChange: (lockEnabled: Boolean, secureEnabled: Boolean) -> Unit
) {
    val prefs = viewModel.preferences

    LaunchedEffect(prefs.lockEnabled, prefs.secureScreenEnabled) {
        onPreferenceChange(prefs.lockEnabled, prefs.secureScreenEnabled)

        window?.let {
            if (prefs.secureScreenEnabled) {
                it.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
            } else {
                it.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }

    LaunchedEffect(prefs.keepScreenOn) {
        window?.let {
            if (prefs.keepScreenOn) {
                it.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                it.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }
}