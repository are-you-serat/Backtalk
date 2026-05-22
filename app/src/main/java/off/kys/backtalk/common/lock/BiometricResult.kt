package off.kys.backtalk.common.lock

/**
 * Represents the result of a biometric authentication attempt.
 */
sealed interface BiometricResult {
    /**
     * Indicates that authentication was successful.
     */
    data object Success : BiometricResult

    /**
     * Indicates that an error occurred during authentication.
     *
     * @property errorCode The specific error code returned by the biometric system.
     * @property errString A human-readable description of the error.
     */
    data class Error(val errorCode: Int, val errString: CharSequence) : BiometricResult

    /**
     * Indicates that authentication failed (e.g., fingerprint not recognized).
     */
    data object Failed : BiometricResult
}