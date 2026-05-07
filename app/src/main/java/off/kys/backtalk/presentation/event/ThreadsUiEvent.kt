package off.kys.backtalk.presentation.event

/**
 * Sealed class representing UI events for the threads screen.
 */
sealed class ThreadsUiEvent {
    /**
     * Event to load or refresh threads.
     */
    data object LoadThreads : ThreadsUiEvent()

}
