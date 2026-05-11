package off.kys.backtalk.domain.use_case

import off.kys.backtalk.data.local.dao.MessagesDao
import off.kys.backtalk.domain.model.BackupData

class SyncData(
    private val messagesDao: MessagesDao
) {
    /**
     * Syncs remote data with local data using "latest-wins" strategy.
     * @param remoteData The data received from another device.
     */
    suspend operator fun invoke(remoteData: BackupData) {
        remoteData.messages.forEach { remoteMsg ->
            val localMsg = messagesDao.getMessage(remoteMsg.id)
            if (localMsg == null) {
                // New message, just insert
                messagesDao.insertMessage(remoteMsg)
            } else {
                // Both have it, compare timestamps
                val remoteTime = remoteMsg.editedAt ?: remoteMsg.timestamp
                val localTime = localMsg.editedAt ?: localMsg.timestamp
                
                if (remoteTime > localTime) {
                    messagesDao.insertMessage(remoteMsg)
                }
            }
        }
    }
}
