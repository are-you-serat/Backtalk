package off.kys.backtalk.common.lock

import androidx.annotation.StringRes
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import off.kys.backtalk.R
import off.kys.backtalk.util.emptyString

/**
 * A manager class that handles biometric authentication using [BiometricPrompt].
 * It simplifies the process of showing the biometric dialog and handling authentication results.
 *
 * @param activity The [FragmentActivity] used to host the biometric prompt.
 */
class BiometricPromptManager(private val activity: FragmentActivity) {

    /**
     * Starts the biometric authentication process using string resource IDs.
     *
     * @param titleRes The string resource ID for the title of the biometric prompt.
     * @param subtitleRes The optional string resource ID for the subtitle of the biometric prompt.
     * @param negativeButtonTextRes The string resource ID for the negative button text.
     * Only used if [BiometricManager.Authenticators.DEVICE_CREDENTIAL] is not included in [authenticators].
     * @param authenticators The types of authenticators allowed (e.g., biometric strong, device credential).
     * @param onResult A callback invoked with the [BiometricResult] of the authentication attempt.
     */
    fun authenticate(
        @StringRes titleRes: Int,
        @StringRes subtitleRes: Int? = null,
        @StringRes negativeButtonTextRes: Int = R.string.common_cancel,
        authenticators: Int = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL,
        onResult: (BiometricResult) -> Unit
    ) {
        authenticate(
            title = activity.getString(titleRes),
            subtitle = subtitleRes?.let { activity.getString(it) } ?: emptyString(),
            negativeButtonText = activity.getString(negativeButtonTextRes),
            authenticators = authenticators,
            onResult = onResult
        )
    }

    /**
     * Starts the biometric authentication process.
     *
     * @param title The title for the biometric prompt.
     * @param subtitle The subtitle for the biometric prompt.
     * @param negativeButtonText The text for the negative button (e.g., 'Cancel').
     * Only used if [BiometricManager.Authenticators.DEVICE_CREDENTIAL] is not included in [authenticators].
     * @param authenticators The types of authenticators allowed (e.g., biometric strong, device credential).
     * @param onResult A callback invoked with the [BiometricResult] of the authentication attempt.
     */
    fun authenticate(
        title: String,
        subtitle: String = emptyString(),
        negativeButtonText: String = activity.getString(R.string.common_cancel),
        authenticators: Int = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL,
        onResult: (BiometricResult) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onResult(BiometricResult.Success)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onResult(BiometricResult.Error(errorCode, errString))
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onResult(BiometricResult.Failed)
                }
            }
        )

        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(authenticators)

        if ((authenticators and BiometricManager.Authenticators.DEVICE_CREDENTIAL) == 0) {
            builder.setNegativeButtonText(negativeButtonText)
        }

        biometricPrompt.authenticate(builder.build())
    }
}