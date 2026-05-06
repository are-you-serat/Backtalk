package off.kys.backtalk.domain.model

import off.kys.backtalk.data.local.entity.MessageEntity

/**
 * Represents a thread of messages.
 *
 * @property root The starting message of the thread.
 * @property replies The subsequent messages in the thread.
 * @property isTimeGrouped Whether this thread was grouped by time gap instead of reply chains.
 */
data class Thread(
    val root: MessageEntity,
    val replies: List<MessageEntity>,
    val isTimeGrouped: Boolean = false
) {
    /**
     * The total number of messages in the thread.
     */
    val size: Int get() = 1 + replies.size

    /**
     * The latest timestamp in the thread.
     */
    val latestTimestamp: Long get() = replies.lastOrNull()?.timestamp ?: root.timestamp
}
