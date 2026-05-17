package off.kys.backtalk.presentation.screen.messages.components

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.presentation.state.MessagesUiState
import off.kys.backtalk.util.emptyString

/**
 * Composable function that displays the primary content of the messages screen, including
 * the list of messages and relevant dialogs for permissions and deletions.
 *
 * @param modifier The [Modifier] to be applied to the layout.
 * @param state The current UI state containing messages, selection data, and visibility flags for dialogs.
 * @param listState The [LazyListState] used to control and observe the scroll position of the message list.
 * @param onEditMessage Callback invoked when a message is selected for editing.
 * @param onReply Callback invoked when a user intends to reply to a specific message.
 * @param onToggleSelect Callback invoked to toggle the selection status of a message by its [MessageId].
 * @param onDismissRationale Callback to dismiss the permission rationale dialog.
 * @param onConfirmDelete Callback invoked to confirm and execute the deletion of selected messages.
 * @param onDismissDelete Callback to dismiss the delete confirmation dialog.
 */
@Composable
fun MessagesContent(
    modifier: Modifier,
    state: MessagesUiState,
    listState: LazyListState,
    onEditMessage: (MessageEntity?) -> Unit,
    onReply: (MessageEntity?) -> Unit,
    onToggleSelect: (MessageId) -> Unit,
    onDismissRationale: () -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit,
    onTagClick: (String) -> Unit
) {
    val context = LocalContext.current

    Column(modifier = modifier) {
        if (state.showPermissionRationale) {
            PermissionRationaleDialog(
                onDismiss = onDismissRationale,
                onConfirm = {
                    onDismissRationale()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                }
            )
        }

        if (state.showDeleteConfirmation) {
            val selectedCount = state.selectedMessageIds.size
            DeleteConfirmationDialog(
                selectedCount = selectedCount,
                onConfirm = onConfirmDelete,
                onDismiss = onDismissDelete
            )
        }

        MessagesList(
            messages = state.messages,
            selectedMessageIds = state.selectedMessageIds,
            listState = listState,
            onEditMessage = onEditMessage,
            onReply = onReply,
            onToggleSelect = onToggleSelect,
            searchQuery = if (state.isSearchActive) state.searchQuery else emptyString(),
            selectedTag = state.selectedTag,
            onTagClick = onTagClick
        )
    }
}
