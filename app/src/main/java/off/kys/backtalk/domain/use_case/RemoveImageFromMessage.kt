package off.kys.backtalk.domain.use_case

import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.domain.repository.MessagesRepository
import java.io.File

/**
 * Use case for removing a specific image from a message.
 *
 * @property repository The [MessagesRepository] used to interact with message data.
 */
class RemoveImageFromMessage(
    private val repository: MessagesRepository
) {
    /**
     * Executes the use case to remove an image with [imagePath] from the message with [messageId].
     *
     * @param messageId The identifier of the message.
     * @param imagePath The path of the image to be removed.
     */
    suspend operator fun invoke(messageId: MessageId, imagePath: String) {
        val message = repository.getMessageById(messageId) ?: return

        val newMediaPath = if (message.mediaPath == imagePath) null else message.mediaPath
        val newMediaPaths = message.mediaPaths
            ?.filter { it != imagePath }
            ?.ifEmpty { null }

        val hasNoMediaLeft = newMediaPath == null && newMediaPaths.isNullOrEmpty()
        // If all media is gone and there's no voice, delete the whole message.
        // Text in a media message is a caption — it has no meaning without the media.
        val hasNoContent = hasNoMediaLeft && message.voicePath == null

        if (hasNoContent) {
            repository.deleteMessageById(messageId)
        } else {
            repository.insertMessage(
                message.copy(
                    mediaPath = newMediaPath,
                    mediaPaths = newMediaPaths
                )
            )
        }

        // Delete file from disk only if no other message still references this path
        if (!repository.isPathReferenced(imagePath)) {
            File(imagePath).let { if (it.exists()) it.delete() }
        }
    }
}
