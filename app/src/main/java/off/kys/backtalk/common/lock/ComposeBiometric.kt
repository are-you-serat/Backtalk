package off.kys.backtalk.common.lock

import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocal
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.fragment.app.FragmentActivity

/**
 * [CompositionLocal] used to provide a [BiometricPromptManager] to the composition hierarchy.
 *
 * This should be accessed within composables that need to trigger biometric authentication.
 * It is initialized via [setBiometricContent].
 *
 * @throws IllegalStateException if accessed before being provided.
 */
val LocalBiometricManager = staticCompositionLocalOf<BiometricPromptManager> {
    error("BiometricPromptManager not provided. Did you use setBiometricContent?")
}

/**
 * Sets the Compose content for a [FragmentActivity] while providing a [BiometricPromptManager]
 * through [LocalBiometricManager].
 *
 * This is a convenience wrapper around [setContent] that ensures biometric authentication
 * capabilities are available throughout the composition.
 *
 * @param content The Composable content of the activity.
 */
fun FragmentActivity.setBiometricContent(
    content: @Composable () -> Unit
) {
    setContent {
        val manager = remember(this) { BiometricPromptManager(this) }

        CompositionLocalProvider(LocalBiometricManager provides manager) {
            content()
        }
    }
}