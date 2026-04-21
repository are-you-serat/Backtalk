package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import off.kys.backtalk.R
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.presentation.components.ManagedPopup
import off.kys.backtalk.presentation.components.PopupActionItem
import off.kys.backtalk.presentation.components.PopupState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    popupState: PopupState,
    messageEntity: MessageEntity,
    repliedMessageEntity: MessageEntity?,
    blinkMessageId: MessageId?,
    isTop: Boolean,
    isBottom: Boolean,
    selectMode: Boolean,
    isSelected: Boolean,
    onReplyPreviewClick: () -> Unit,
    onEditMessageClick: (MessageEntity) -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    var showExtraInfo by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

    val isBlinking = blinkMessageId == messageEntity.id
    val scale = remember { Animatable(1f) }
    val blinkAlpha = remember { Animatable(0f) }

    // Constants for logic
    val oneHourInMillis = 3600000L
    val canEdit = messageEntity.editedAt == null &&
            (System.currentTimeMillis() - messageEntity.timestamp) < oneHourInMillis

    LaunchedEffect(isBlinking) {
        if (isBlinking) {
            repeat(2) {
                launch { scale.animateTo(1.05f, tween(180)); scale.animateTo(1f, tween(300)) }
                blinkAlpha.animateTo(1f, tween(180))
                blinkAlpha.animateTo(0f, tween(300))
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = if (isTop) 4.dp else 1.dp, bottom = if (isBottom) 4.dp else 1.dp),
        horizontalAlignment = Alignment.End
    ) {
        ManagedPopup(
            state = popupState,
            anchor = {
                MessageSurface(
                    isSelected = isSelected,
                    isTop = isTop,
                    isBottom = isBottom,
                    blinkAlpha = blinkAlpha.value,
                    scale = scale.value,
                    modifier = Modifier.combinedClickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {
                            if (!selectMode) showExtraInfo = !showExtraInfo
                            onClick()
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLongClick()
                        }
                    )
                ) {
                    MessageContent(
                        message = messageEntity,
                        repliedMessage = repliedMessageEntity,
                        onReplyClick = onReplyPreviewClick,
                        showOriginal = showExtraInfo
                    )
                }
            }
        ) { state ->
            PopupActionItem(
                text = stringResource(R.string.edit),
                enabled = canEdit,
                onClick = {
                    onEditMessageClick(messageEntity)
                    state.hide()
                }
            )
        }

        MessageFooter(
            isVisible = showExtraInfo,
            timestamp = messageEntity.timestamp,
            editedAt = messageEntity.editedAt
        )
    }
}

@Composable
private fun MessageSurface(
    isSelected: Boolean,
    isTop: Boolean,
    isBottom: Boolean,
    blinkAlpha: Float,
    scale: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val bubbleColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.primary

    val shape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = if (isTop) 18.dp else 4.dp,
        bottomEnd = if (isBottom) 18.dp else 4.dp,
        bottomStart = 18.dp
    )

    Surface(
        modifier = modifier.graphicsLayer { scaleX = scale; scaleY = scale },
        color = bubbleColor,
        shape = shape,
        shadowElevation = 1.dp
    ) {
        Box {
            Surface(
                color = Color.White.copy(alpha = 0.3f * blinkAlpha),
                modifier = Modifier.matchParentSize()
            ) {}
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun MessageContent(
    message: MessageEntity,
    repliedMessage: MessageEntity?,
    onReplyClick: () -> Unit,
    showOriginal: Boolean
) {
    val contentColor = contentColorFor(MaterialTheme.colorScheme.primary)

    if (repliedMessage != null) {
        ReplyPreview(text = repliedMessage.text, onPreviewClick = onReplyClick)
        Spacer(modifier = Modifier.height(4.dp))
    }

    if (message.editedText != null && showOriginal) {
        Text(
            text = message.text,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor.copy(alpha = 0.6f),
            textDecoration = TextDecoration.LineThrough
        )
    }

    Text(
        text = message.editedText ?: message.text,
        color = contentColor,
        style = MaterialTheme.typography.bodyLarge
    )

    if (message.editedText != null) {
        Text(
            text = stringResource(R.string.edited),
            style = MaterialTheme.typography.labelSmall,
            fontStyle = FontStyle.Italic,
            color = contentColor.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
private fun MessageFooter(
    isVisible: Boolean,
    timestamp: Long,
    editedAt: Long?
) {
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.padding(end = 8.dp, top = 2.dp)
        ) {
            Text(
                text = "${if (editedAt != null) stringResource(R.string.sent_at) else ""} ${
                    timeFormat.format(
                        Date(
                            timestamp
                        )
                    )
                }".trim(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
            editedAt?.let {
                Text(
                    text = stringResource(R.string.edited_at, timeFormat.format(Date(it))),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}