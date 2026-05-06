package off.kys.backtalk.presentation.event

/**
 * Sealed class representing UI events for the threads screen.
 */
sealed class ThreadsUiEvent {
    /**
     * Event to load or refresh threads.
     */
    data object LoadThreads : ThreadsUiEvent()

    /**
     * Event to toggle the inclusion of replies as root posts in the browse list.
     */
    data class ToggleIncludeReplies(val include: Boolean) : ThreadsUiEvent()

    /**
     * Event to toggle grouping messages by time gap.
     */
    data class ToggleGroupByTime(val enabled: Boolean) : ThreadsUiEvent()
}
