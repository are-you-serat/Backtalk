package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import off.kys.backtalk.R
import off.kys.backtalk.common.manager.VibrationManager
import org.koin.compose.koinInject
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Composable function that displays a message bubble with a swipe-to-reply feature.
 *
 * @param onSwipe The callback function to handle the swipe-to-reply action with the direction.
 * @param direction The allowed direction for the swipe.
 * @param content The content to be displayed inside the message bubble.
 */
@Composable
fun SwipeToReplyWrapper(
    onSwipe: (SwipeDirection) -> Unit,
    direction: SwipeDirection = SwipeDirection.RIGHT,
    content: @Composable () -> Unit
) {
    val vibrationManager = koinInject<VibrationManager>()
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    val actionThreshold = with(density) { 60.dp.toPx() }
    val maxDrag = with(density) { 100.dp.toPx() }

    val isPastThreshold by remember {
        derivedStateOf { abs(offsetX.value) >= actionThreshold }
    }

    val iconScale by animateFloatAsState(
        targetValue = if (isPastThreshold) 1.2f else 0.8f,
        animationSpec = spring(Spring.DampingRatioHighBouncy, Spring.StiffnessMedium),
        label = "IconScale"
    )

    val iconRotation by animateFloatAsState(
        targetValue = if (isPastThreshold) 0f else -15f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "IconRotation"
    )

    val iconAlpha by animateFloatAsState(
        targetValue = (abs(offsetX.value) / actionThreshold).coerceIn(0f, 1f),
        label = "IconAlpha"
    )

    val iconTint by animateColorAsState(
        targetValue = if (isPastThreshold)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.onSurfaceVariant,
        label = "IconTint"
    )

    val hasVibratedThreshold = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(direction) {
                detectHorizontalDragGestures(
                    onDragStart = { hasVibratedThreshold.value = false },
                    onDragEnd = {
                        scope.launch {
                            if (isPastThreshold) onSwipe(direction)
                            offsetX.animateTo(
                                0f,
                                spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMediumLow)
                            )
                        }
                    },
                    onDragCancel = { scope.launch { offsetX.animateTo(0f) } },
                    onHorizontalDrag = { change, dragAmount ->
                        // Apply resistance: $Drag_{new} = Drag_{old} + (Amount \times Resistance)$
                        // Resistance increases as we approach maxDrag
                        val dragFactor = 1f - (abs(offsetX.value) / (maxDrag * 1.5f))
                        val resistedDrag = dragAmount * dragFactor.coerceIn(0.2f, 1f)

                        val newOffset = if (direction == SwipeDirection.RIGHT) {
                            (offsetX.value + resistedDrag).coerceIn(0f, maxDrag)
                        } else {
                            (offsetX.value + resistedDrag).coerceIn(-maxDrag, 0f)
                        }

                        if (abs(newOffset) >= actionThreshold && !hasVibratedThreshold.value) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            vibrationManager.vibrate(50L)
                            hasVibratedThreshold.value = true
                        } else if (abs(newOffset) < actionThreshold) {
                            hasVibratedThreshold.value = false
                        }

                        scope.launch { offsetX.snapTo(newOffset) }
                        change.consume()
                    }
                )
            }
    ) {
        // Background Icon Layer
        Box(
            modifier = Modifier
                .align(if (direction == SwipeDirection.RIGHT) Alignment.CenterStart else Alignment.CenterEnd)
                .padding(horizontal = 16.dp)
                .graphicsLayer {
                    alpha = iconAlpha
                    scaleX = iconScale
                    scaleY = iconScale
                    rotationZ = if (direction == SwipeDirection.RIGHT) iconRotation else -iconRotation
                    translationX = if (direction == SwipeDirection.RIGHT) {
                        (offsetX.value - actionThreshold).coerceAtMost(0f) / 2f
                    } else {
                        (offsetX.value + actionThreshold).coerceAtLeast(0f) / 2f
                    }
                }
        ) {
            Icon(
                painter = painterResource(R.drawable.round_reply_24),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = iconTint
            )
        }

        // Content Layer
        Box(
            modifier = Modifier.offset {
                IntOffset(offsetX.value.roundToInt(), 0)
            }
        ) {
            content()
        }
    }
}