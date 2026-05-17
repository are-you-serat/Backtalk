package off.kys.backtalk.presentation.screen.messages.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import off.kys.backtalk.R
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.util.MarkdownParser
import off.kys.backtalk.util.emptyString
import org.koin.compose.koinInject
import kotlin.math.abs

/**
 * A custom [Text] composable that handles Markdown, links, and mentions with Telegram-style rounded highlights.
 */
@Composable
fun SmartText(
    text: String,
    modifier: Modifier = Modifier,
    clickableLink: Boolean = true,
    fontSize: TextUnit = TextUnit.Unspecified,
    color: Color = MaterialTheme.colorScheme.background,
    style: TextStyle = LocalTextStyle.current,
    textDecoration: TextDecoration = TextDecoration.None,
    maxLines: Int = Int.MAX_VALUE,
    lineHeight: TextUnit = TextUnit.Unspecified,
    highlightQuery: String? = null,
    onMentionClicked: (String) -> Unit = {}
) {
    val uriHandler = LocalUriHandler.current
    val preferences = koinInject<BacktalkPreferences>()
    val showSafetyDialog = remember { mutableStateOf(false) }
    var pendingUrl by remember { mutableStateOf(emptyString()) }

    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val highlightColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.6f)

    val linkStyles = TextLinkStyles(
        style = SpanStyle(
            color = MaterialTheme.colorScheme.inversePrimary,
            textDecoration = TextDecoration.Underline
        )
    )

    val annotatedString = MarkdownParser.toAnnotatedString(
        text = text,
        linkStyles = linkStyles,
        onAnnotationClicked = { annotation ->
            when (annotation) {
                is LinkAnnotation.Url -> {
                    if (clickableLink) {
                        if (preferences.externalLinkWarningEnabled) {
                            pendingUrl = annotation.url
                            showSafetyDialog.value = true
                        } else {
                            uriHandler.openUri(annotation.url)
                        }
                    }
                }

                is LinkAnnotation.Clickable -> {
                    onMentionClicked(annotation.tag)
                }
            }
        }
    )

    Text(
        text = annotatedString,
        onTextLayout = { textLayoutResult = it },
        modifier = modifier.drawBehind {
            val layout = textLayoutResult ?: return@drawBehind
            if (highlightQuery.isNullOrBlank()) return@drawBehind

            val terms = highlightQuery.lowercase().split(" ").filter { it.isNotBlank() }
            val lowerText = annotatedString.text.lowercase()

            for (term in terms) {
                var index = lowerText.indexOf(term)
                while (index != -1) {
                    val start = index
                    val end = index + term.length

                    // Guard clause against mismatched async rendering updates
                    if (end <= layout.layoutInput.text.length) {
                        val startLine = layout.getLineForOffset(start)
                        val endLine = layout.getLineForOffset(end)

                        for (line in startLine..endLine) {
                            val lineStart =
                                if (line == startLine) start else layout.getLineStart(line)
                            val lineEnd = if (line == endLine) end else layout.getLineEnd(line)

                            val left =
                                layout.getHorizontalPosition(lineStart, usePrimaryDirection = true)
                            val right =
                                layout.getHorizontalPosition(lineEnd, usePrimaryDirection = true)
                            val top = layout.getLineTop(line)
                            val bottom = layout.getLineBottom(line)

                            drawRoundRect(
                                color = highlightColor,
                                topLeft = Offset(minOf(left, right), top),
                                size = Size(abs(right - left), bottom - top),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )
                        }
                    }
                    index = lowerText.indexOf(term, index + term.length)
                }
            }
        },
        maxLines = maxLines,
        lineHeight = lineHeight,
        style = style.copy(
            textDirection = TextDirection.Content,
            color = if (color != Color.Unspecified) color else style.color,
            fontSize = if (fontSize != TextUnit.Unspecified) fontSize else style.fontSize,
            textDecoration = textDecoration,
            textAlign = TextAlign.Start,
        )
    )

    if (showSafetyDialog.value) {
        LinkSafetyDialog(
            url = pendingUrl,
            onConfirm = {
                uriHandler.openUri(pendingUrl)
                showSafetyDialog.value = false
            },
            onDismiss = {
                showSafetyDialog.value = false
            }
        )
    }
}

@Composable
private fun LinkSafetyDialog(
    url: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                painter = painterResource(R.drawable.round_warning_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(text = stringResource(R.string.link_safety_title))
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.link_safety_message),
                    style = MaterialTheme.typography.bodyMedium
                )

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = url,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(12.dp),
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm
            ) {
                Text(text = stringResource(R.string.link_safety_open))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.link_safety_cancel))
            }
        }
    )
}