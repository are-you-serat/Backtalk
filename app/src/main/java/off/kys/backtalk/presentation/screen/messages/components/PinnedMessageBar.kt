package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import off.kys.backtalk.R
import off.kys.backtalk.data.local.entity.MessageEntity

/**
 * An expressive, floating bar displaying the currently active pinned message
 * with fluid transitions and Material 3 surface tinting.
 */
@Composable
fun PinnedMessageBar(
    modifier: Modifier = Modifier,
    pinnedMessages: List<MessageEntity>,
    activeIndex: Int,
    onClick: () -> Unit,
    onListClick: () -> Unit
) {
    if (pinnedMessages.isEmpty()) return

    val currentPinned = pinnedMessages.getOrNull(activeIndex) ?: return

    // Express total queue context if there's more than one pinned message
    val labelText = if (pinnedMessages.size > 1) {
        "Pinned Message (${activeIndex + 1}/${pinnedMessages.size})"
    } else {
        "Pinned Message"
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clickable(
                    onClick = onClick,
                    role = Role.Button,
                    onClickLabel = "Jump to pinned message"
                )
                .padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.round_push_pin_24),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                AnimatedContent(
                    targetState = labelText,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    label = "PinnedLabelAnimation"
                ) { text ->
                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }

                AnimatedContent(
                    targetState = currentPinned,
                    transitionSpec = {
                        (slideInVertically { height -> height } + fadeIn()) togetherWith
                                (slideOutVertically { height -> -height } + fadeOut())
                    },
                    label = "PinnedMessageAnimation"
                ) { message ->
                    SmartText(
                        text = message.editedText ?: message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = onListClick,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.round_pinboard_24px),
                    contentDescription = "View all ${pinnedMessages.size} pinned messages",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}