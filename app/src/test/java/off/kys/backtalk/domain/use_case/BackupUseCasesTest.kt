package off.kys.backtalk.domain.use_case

import android.content.Context
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
import org.mockito.kotlin.eq
import java.io.File

@RunWith(MockitoJUnitRunner::class)
class BackupUseCasesTest {

    @Mock
    private lateinit var context: Context

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
        importBackup = ImportBackup(context, messagesDao, preferences, backupRepository)
        Mockito.`when`(context.filesDir).thenReturn(File("/tmp"))
    }

    @Test
    fun exportBackupSerializesDataCorrectly() = runTest {
        Mockito.`when`(messagesRepository.getAllMessages()).thenReturn(flowOf(testMessages))
        Mockito.`when`(preferences.themeMode).thenReturn(ThemeMode.DARK)
        Mockito.`when`(backupRepository.writeBackup(eq(testUri), any<ByteArray>())).thenReturn(Result.success(Unit))

        val result = exportBackup(testUri, null)

        Assert.assertTrue(result.isSuccess)
        Mockito.verify(backupRepository).writeBackup(eq(testUri), any<ByteArray>())
    }

    @Test
    fun importOldBackupRestoresDataCorrectly() = runTest {
        val backupData = BackupData(
            messages = testMessages,
            preferences = mapOf(BacktalkPreferences.KEY_THEME_MODE to "DARK")
        )
        val json = Json.encodeToString(backupData)

        Mockito.`when`(backupRepository.readBackupBytes(testUri)).thenReturn(Result.success(json.toByteArray()))

        val result = importBackup(testUri, null, clearExisting = true)

        Assert.assertTrue(result.isSuccess)
        Assert.assertEquals(ImportBackup.ImportResult.SuccessWithWarning, result.getOrThrow())
        Mockito.verify(messagesDao).deleteAllMessages()
        Mockito.verify(messagesDao).insertMessage(any())
    }
}
