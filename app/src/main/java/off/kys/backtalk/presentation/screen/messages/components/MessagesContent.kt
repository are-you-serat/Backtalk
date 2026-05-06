package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.presentation.state.MessagesUiState

/**
 * Composable function that displays the messages content.
 *
 * @param modifier The modifier to be applied to the layout.
 * @param state The current state of the messages screen.
 * @param onEditMessage The callback function to handle editing a message.
 * @param onReply The callback function to handle replying to a message.
 * @param onToggleSelect The callback function to toggle the selection state of a message.
 * @param onSend The callback function to handle sending a message.
 */
@Composable
fun MessagesContent(
    modifier: Modifier,
    state: MessagesUiState,
    listState: LazyListState,
    onEditMessage: (MessageEntity?) -> Unit,
    onReply: (MessageEntity?) -> Unit,
    onToggleSelect: (MessageId) -> Unit,
    onSend: (String) -> Unit
) {
    Column(modifier = modifier) {
        MessagesList(
            messages = state.messages,
            selectedMessageIds = state.selectedMessageIds,
            listState = listState,
            onEditMessage = onEditMessage,
            onReply = onReply,
            onToggleSelect = onToggleSelect
        )

        InputBar(
            messageInput = state.editingMessage?.let { it.editedText ?: it.text }.orEmpty(),
            replyingTo = state.replyingTo,
            editingMessage = state.editingMessage,
            onCancelReply = { onReply(null) },
            onCancelEdit = { onEditMessage(null) },
            onMessageSend = onSend
        )
    }
}