package off.kys.backtalk.domain.use_case

import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.domain.repository.MessagesRepository

/**
 * Use case for toggling the pinned status of a message.
 */
class TogglePinMessage(
    private val repository: MessagesRepository
) {
    /**
     * Toggles the pinned status of a message.
     *
     * @param id The ID of the message to toggle.
     * @param isPinned The new pinned status.
     */
    suspend operator fun invoke(id: MessageId, isPinned: Boolean) {
        repository.updatePinnedStatus(id, isPinned)
    }
}
