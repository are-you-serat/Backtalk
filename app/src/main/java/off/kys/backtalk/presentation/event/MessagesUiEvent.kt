package off.kys.backtalk.presentation.event

import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId

/**
 * Sealed interface representing the different UI events related to messages.
 */
sealed interface MessagesUiEvent {
    /**
     * UI event to load messages from the data source.
     */
    data object LoadMessages : MessagesUiEvent

    /**
     * UI event to send a message.
     *
     * @param text The text of the message to send.
     */
    data class SendMessage(val text: String) : MessagesUiEvent
    
    /**
     * UI event to edit a message.
     *
     * @param message The message to edit, or null if no message is being edited.
     */
    data class EditMessage(val message: MessageEntity?) : MessagesUiEvent

    /**
     * UI event to reply to a message.
     *
     * @param message The message to reply to, or null if no message is being replied to.
     */
    data class ReplyTo(val message: MessageEntity?) : MessagesUiEvent

    /**
     * UI event to select a message.
     *
     * @param id The ID of the message to select
     */
    data class ToggleSelection(val id: MessageId) : MessagesUiEvent

    /**
     * UI event to clear the current selection of messages.
     */
    object ClearSelection : MessagesUiEvent

    /**
     * UI event to delete the currently selected messages.
     */
    object DeleteSelected : MessagesUiEvent

    /**
     * UI event to copy the currently selected messages to the clipboard.
     */
    object CopySelected : MessagesUiEvent
}