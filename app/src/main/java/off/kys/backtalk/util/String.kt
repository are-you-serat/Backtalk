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
inline fun emptyString(): String = ""

/**
 * Removes common Markdown formatting from a string.
 * Because apparently, bold text is just too much excitement for one day.
 */
fun String.stripMarkdown(): String {
    return this
        .replace(Regex("<[^>]*>"), "")
        .replace(Regex("^[=\\-]{2,}\\s*$", RegexOption.MULTILINE), "")
        .replace(Regex("^#+\\s+", RegexOption.MULTILINE), "")
        .replace(Regex("^[\\s\\t]*([*\\-_])(?:\\s*\\1){2,}\\s*$", RegexOption.MULTILINE), "")
        .replace(Regex("\\[(.*?)]\\(.*?\\)"), "$1")
        .replace(Regex("!\\[(.*?)]\\(.*?\\)"), "$1")
        .replace(Regex("([*_]{1,2})(.*?)\\1"), "$2")
        .replace(Regex("`(.+?)`"), "$1")
        .replace(Regex("^\\s*>\\s+", RegexOption.MULTILINE), "")
        .trim()
}