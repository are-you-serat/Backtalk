@file:Suppress("NOTHING_TO_INLINE")

package off.kys.backtalk.util

/**
 * Returns an empty string.
 *
 * This is a utility function to provide a more descriptive way of creating an empty string
 * than using `""`.
 *
 * @return An empty string ("").
 */
@Suppress("SameReturnValue")
inline fun emptyString(): String = ""

/**
 * Removes common Markdown formatting from a string.
 */
fun String.stripMarkdown(): String = this
    .replace(Regex("<[^>]*>"), emptyString())
    .replace(Regex("^[=\\-]{2,}\\s*$", RegexOption.MULTILINE), emptyString())
    .replace(Regex("^#+\\s+", RegexOption.MULTILINE), emptyString())
    .replace(Regex("^[\\s\\t]*([*\\-_])(?:\\s*\\1){2,}\\s*$", RegexOption.MULTILINE), emptyString())
    .replace(Regex("\\[(.*?)]\\(.*?\\)"), "$1")
    .replace(Regex("!\\[(.*?)]\\(.*?\\)"), "$1")
    .replace(Regex("([*_]{1,2})(.*?)\\1"), "$2")
    .replace(Regex("`(.+?)`"), "$1")
    .replace(Regex("^\\s*>\\s+", RegexOption.MULTILINE), emptyString())
    .trim()