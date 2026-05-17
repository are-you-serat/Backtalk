package off.kys.backtalk.util.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import off.kys.backtalk.data.local.entity.MessageEntity

/**
 * Extracts a distinct, sorted list of hashtags from a list of messages.
 * Recomputes only when the underlying messages list changes.
 */
@Composable
fun rememberHashtags(messages: List<MessageEntity>): List<String> {
    return remember(messages) {
        val hashtagRegex = Regex("""#(\w+)""")
        messages.flatMap { message ->
            val text = message.editedText ?: message.text
            hashtagRegex.findAll(text).map { it.groupValues[1] }.toList()
        }.distinct().sorted()
    }
}