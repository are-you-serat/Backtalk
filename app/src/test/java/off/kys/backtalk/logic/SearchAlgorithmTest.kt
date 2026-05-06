package off.kys.backtalk.logic

import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchAlgorithmTest {

    private fun mockMessage(id: Long, text: String, editedText: String? = null): MessageEntity {
        return MessageEntity(
            id = MessageId(id),
            text = text,
            timestamp = System.currentTimeMillis(),
            repliedToId = null,
            editedText = editedText
        )
    }

    private fun performSearch(query: String, messages: List<MessageEntity>): List<MessageId> {
        if (query.isBlank()) return emptyList()
        val terms = query.lowercase().split(" ").filter { it.isNotBlank() }
        return messages.filter { message ->
            val text = (message.editedText ?: message.text).lowercase()
            terms.all { term -> text.contains(term) }
        }.map { it.id }.reversed()
    }

    @Test
    fun `search with single term matches correctly`() {
        val messages = listOf(
            mockMessage(1L, "Hello world"),
            mockMessage(2L, "Goodbye world"),
            mockMessage(3L, "Kotlin is fun")
        )

        val results = performSearch("world", messages)

        assertEquals(2, results.size)
        assertEquals(MessageId(2L), results[0]) // Reversed order
        assertEquals(MessageId(1L), results[1])
    }

    @Test
    fun `search is case-insensitive`() {
        val messages = listOf(
            mockMessage(1L, "Hello WORLD")
        )

        val results = performSearch("world", messages)

        assertEquals(1, results.size)
        assertEquals(MessageId(1L), results[0])
    }

    @Test
    fun `search with multiple terms (AND logic) works`() {
        val messages = listOf(
            mockMessage(1L, "Hello beautiful world"),
            mockMessage(2L, "Hello world"),
            mockMessage(3L, "Beautiful day")
        )

        val results = performSearch("hello world", messages)

        assertEquals(2, results.size)
        assertEquals(MessageId(2L), results[0])
        assertEquals(MessageId(1L), results[1])

        val results2 = performSearch("beautiful day", messages)
        assertEquals(1, results2.size)
        assertEquals(MessageId(3L), results2[0])
    }

    @Test
    fun `search matches edited text instead of original`() {
        val messages = listOf(
            mockMessage(1L, "Old text", editedText = "New updated text")
        )

        val results1 = performSearch("old", messages)
        assertEquals(0, results1.size)

        val results2 = performSearch("updated", messages)
        assertEquals(1, results2.size)
        assertEquals(MessageId(1L), results2[0])
    }
}