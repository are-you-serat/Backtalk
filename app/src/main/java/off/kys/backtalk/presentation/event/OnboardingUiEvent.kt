package off.kys.backtalk.presentation.event

/**
 * Sealed interface representing the different UI events for the Onboarding screen.
 */
sealed interface OnboardingUiEvent {
    /**
     * UI event to update the permission states.
     */
    data object UpdatePermissions : OnboardingUiEvent

    /**
     * UI event to complete the onboarding process.
     */
    data object CompleteOnboarding : OnboardingUiEvent
}
