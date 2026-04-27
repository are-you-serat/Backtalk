package off.kys.backtalk.domain.use_case

import android.net.Uri
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import off.kys.backtalk.common.ThemeMode
import off.kys.backtalk.common.pref.BacktalkPreferences
import off.kys.backtalk.data.local.dao.MessagesDao
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.BackupData
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.domain.repository.BackupRepository
import off.kys.backtalk.domain.repository.MessagesRepository
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any

@RunWith(MockitoJUnitRunner::class)
class BackupUseCasesTest {

    @Mock
    private lateinit var messagesRepository: MessagesRepository

    @Mock
    private lateinit var messagesDao: MessagesDao

    @Mock
    private lateinit var preferences: BacktalkPreferences

    @Mock
    private lateinit var backupRepository: BackupRepository

    private lateinit var exportBackup: ExportBackup
    private lateinit var importBackup: ImportBackup

    private val testMessages = listOf(
        MessageEntity(id = MessageId(1), text = "Hello", timestamp = 1000, repliedToId = null)
    )
    private val testUri = Mockito.mock(Uri::class.java)

    @Before
    fun setup() {
        exportBackup = ExportBackup(messagesRepository, preferences, backupRepository)
        importBackup = ImportBackup(messagesDao, preferences, backupRepository)
    }

    @Test
    fun exportBackupSerializesDataCorrectly() = runTest {
        Mockito.`when`(messagesRepository.getAllMessages()).thenReturn(flowOf(testMessages))
        Mockito.`when`(preferences.themeMode).thenReturn(ThemeMode.DARK)
        Mockito.`when`(backupRepository.writeBackup(any(), any())).thenReturn(Result.success(Unit))

        val result = exportBackup(testUri, null)

        Assert.assertTrue(result.isSuccess)
        Mockito.verify(backupRepository).writeBackup(any(), any())
    }

    @Test
    fun importBackupRestoresDataCorrectly() = runTest {
        val backupData = BackupData(
            messages = testMessages,
            preferences = mapOf(BacktalkPreferences.KEY_THEME_MODE to "DARK")
        )
        val json = Json.encodeToString(backupData)

        Mockito.`when`(backupRepository.readBackup(testUri)).thenReturn(Result.success(json))

        val result = importBackup(testUri, null, clearExisting = true)

        Assert.assertTrue(result.isSuccess)
        Mockito.verify(messagesDao).deleteAllMessages()
        Mockito.verify(messagesDao).insertMessage(testMessages[0])
    }
}