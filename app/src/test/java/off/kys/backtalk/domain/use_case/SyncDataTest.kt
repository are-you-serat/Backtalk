package off.kys.backtalk.domain.use_case

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import off.kys.backtalk.data.local.dao.MessagesDao
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.BackupData
import off.kys.backtalk.domain.model.MessageId
import org.junit.Before
import org.junit.Test

class SyncDataTest {
    private lateinit var messagesDao: MessagesDao
    private lateinit var syncData: SyncData

    @Before
    fun setUp() {
        messagesDao = mockk(relaxed = true)
        syncData = SyncData(messagesDao)
    }

    @Test
    fun `sync inserts new message when not present locally`() = runBlocking {
        val id = MessageId(1L)
        val message = MessageEntity(id = id, text = "Hello", timestamp = 1000L, repliedToId = null)
        val backupData = BackupData(messages = listOf(message), preferences = emptyMap())
        
        coEvery { messagesDao.getMessage(id) } returns null
        
        syncData(backupData)
        
        coVerify { messagesDao.insertMessage(message) }
    }

    @Test
    fun `sync updates message when remote timestamp is newer`() = runBlocking {
        val id = MessageId(1L)
        val localMessage = MessageEntity(id = id, text = "Hello", timestamp = 1000L, repliedToId = null)
        val remoteMessage = MessageEntity(id = id, text = "Hello Updated", timestamp = 1000L, repliedToId = null, editedAt = 2000L)
        val backupData = BackupData(messages = listOf(remoteMessage), preferences = emptyMap())
        
        coEvery { messagesDao.getMessage(id) } returns localMessage
        
        syncData(backupData)
        
        coVerify { messagesDao.insertMessage(remoteMessage) }
    }

    @Test
    fun `sync does not update message when local timestamp is newer`() = runBlocking {
        val id = MessageId(1L)
        val localMessage = MessageEntity(id = id, text = "Hello Local", timestamp = 1000L, repliedToId = null, editedAt = 3000L)
        val remoteMessage = MessageEntity(id = id, text = "Hello Remote", timestamp = 1000L, repliedToId = null, editedAt = 2000L)
        val backupData = BackupData(messages = listOf(remoteMessage), preferences = emptyMap())
        
        coEvery { messagesDao.getMessage(id) } returns localMessage
        
        syncData(backupData)
        
        coVerify(exactly = 0) { messagesDao.insertMessage(any()) }
    }
}
