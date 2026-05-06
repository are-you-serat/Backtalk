package off.kys.backtalk.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle

/**
 * A Markdown-ish parser that doesn't give up when things get complicated.
 */
object MarkdownParser {

    private data class StyleDef(
        val delimiter: String,
        val style: SpanStyle
    )

    // Order matters: longer delimiters must come before shorter ones sharing the same prefix.
    private val STYLES = listOf(
        StyleDef("**", SpanStyle(fontWeight = FontWeight.Bold)),
        StyleDef("__", SpanStyle(textDecoration = TextDecoration.Underline)),
        StyleDef("~~", SpanStyle(textDecoration = TextDecoration.LineThrough)),
        StyleDef("*", SpanStyle(fontStyle = FontStyle.Italic)),
        StyleDef("`", SpanStyle(fontFamily = FontFamily.Monospace))
    )

    fun toAnnotatedString(text: String): AnnotatedString = buildAnnotatedString {
        parseRecursive(text, this)
    }

    private fun parseRecursive(text: String, builder: AnnotatedString.Builder) {
        if (text.isEmpty()) return

        var earliestMatch = -1
        var bestStyle: StyleDef? = null
        var bestClosingIndex = -1

        // Scan character by character to find the absolute earliest valid tag
        for (i in text.indices) {
            for (styleDef in STYLES) {
                if (text.startsWith(styleDef.delimiter, i)) {
                    val closingIndex = findClosingTag(text, i + styleDef.delimiter.length, styleDef.delimiter)
                    if (closingIndex != -1) {
                        earliestMatch = i
                        bestStyle = styleDef
                        bestClosingIndex = closingIndex
                        break // Break out of STYLES loop
                    }
                }
            }
            if (earliestMatch != -1) break // Break out of text.indices loop
        }

        if (bestStyle == null || earliestMatch == -1) {
            builder.append(text)
            return
        }

        val delimiter = bestStyle.delimiter

        // 1. Append everything before the tag
        builder.append(text.substring(0, earliestMatch))

        // 2. Apply style and recurse inside the tags (allows nesting)
        builder.withStyle(bestStyle.style) {
            parseRecursive(text.substring(earliestMatch + delimiter.length, bestClosingIndex), this)
        }

        // 3. Recurse on everything after the tag
        parseRecursive(text.substring(bestClosingIndex + delimiter.length), builder)
    }

    private fun findClosingTag(text: String, startIndex: Int, delimiter: String): Int {
        var i = startIndex
        while (i <= text.length - delimiter.length) {
            if (text.startsWith(delimiter, i)) {
                // Prevent shorter tags from accidentally matching inside longer tags (e.g., "*" matching inside "**")
                val largerDelimiter = STYLES.find {
                    it.delimiter != delimiter &&
                            it.delimiter.startsWith(delimiter) &&
                            text.startsWith(it.delimiter, i)
                }

                if (largerDelimiter != null) {
                    // Skip past the larger delimiter entirely so we don't accidentally match inside it
                    i += largerDelimiter.delimiter.length
                    continue
                }
                return i
            }
            i++
        }
        return -1
    }
}