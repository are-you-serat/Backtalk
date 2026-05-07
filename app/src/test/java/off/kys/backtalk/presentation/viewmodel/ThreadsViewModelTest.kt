package off.kys.backtalk.presentation.viewmodel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import off.kys.backtalk.common.Constants
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.MessageId
import off.kys.backtalk.domain.use_case_bundle.MessagesUseCases
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

@OptIn(ExperimentalCoroutinesApi::class)
class ThreadsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var useCases: MessagesUseCases
    private lateinit var getAllMessages: off.kys.backtalk.domain.use_case.GetAllMessages
    private lateinit var viewModel: ThreadsViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        useCases = mock(MessagesUseCases::class.java)
        getAllMessages = mock(off.kys.backtalk.domain.use_case.GetAllMessages::class.java)
        `when`(useCases.getAllMessages).thenReturn(getAllMessages)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `groupMessages groups messages by time gap correctly`() = runTest {
        val msg1 = MessageEntity(MessageId(1), "msg1", 1000, null)
        val msg2 = MessageEntity(MessageId(2), "msg2", 2000, null) // within gap
        val msg3 = MessageEntity(MessageId(3), "msg3", 1000 + Constants.TIME_GAP_FOR_HEADER + 1000, null) // after gap

        `when`(getAllMessages()).thenReturn(flowOf(listOf(msg1, msg2, msg3)))

        viewModel = ThreadsViewModel(useCases)
        advanceUntilIdle()

        val threads = viewModel.uiState.value.threads
        assertEquals(2, threads.size)
        // Threads are reversed, so latest (msg3) is first
        assertEquals(msg3.id, threads[0].root.id)
        assertEquals(msg1.id, threads[1].root.id)
        assertEquals(1, threads[1].replies.size)
        assertEquals(msg2.id, threads[1].replies[0].id)
    }

    @Test
    fun `groupMessages groups replies even if time gap is large`() = runTest {
        val msg1 = MessageEntity(MessageId(1), "msg1", 1000, null)
        val msg2 = MessageEntity(MessageId(2), "msg2", 1000 + Constants.TIME_GAP_FOR_HEADER + 1000, MessageId(1)) // reply to msg1 after gap

        `when`(getAllMessages()).thenReturn(flowOf(listOf(msg1, msg2)))

        viewModel = ThreadsViewModel(useCases)
        advanceUntilIdle()

        val threads = viewModel.uiState.value.threads
        assertEquals(1, threads.size)
        assertEquals(msg1.id, threads[0].root.id)
        assertEquals(1, threads[0].replies.size)
        assertEquals(msg2.id, threads[0].replies[0].id)
    }

    @Test
    fun `groupMessages handles multiple threads with mixed grouping`() = runTest {
        val t1m1 = MessageEntity(MessageId(11), "t1m1", 1000, null)
        val t1m2 = MessageEntity(MessageId(12), "t1m2", 1500, null)
        
        val t2m1 = MessageEntity(MessageId(21), "t2m1", 1000 + Constants.TIME_GAP_FOR_HEADER + 1000, null)
        val t1m3 = MessageEntity(MessageId(13), "t1m3", t2m1.timestamp + 100, MessageId(11)) // reply to t1m1, but sent after t2m1 started

        `when`(getAllMessages()).thenReturn(flowOf(listOf(t1m1, t1m2, t2m1, t1m3)))

        viewModel = ThreadsViewModel(useCases)
        advanceUntilIdle()

        val threads = viewModel.uiState.value.threads
        assertEquals(2, threads.size)
        
        val thread1 = threads.find { it.root.id == MessageId(11) }!!
        val thread2 = threads.find { it.root.id == MessageId(21) }!!
        
        assertEquals(2, thread1.replies.size) // t1m2, t1m3
        assertEquals(0, thread2.replies.size)
    }

    @Test
    fun `getSubThread correctly identifies nested descendants`() = runTest {
        val m1 = MessageEntity(MessageId(1), "m1", 1000, null)
        val m2 = MessageEntity(MessageId(2), "m2", 2000, MessageId(1))
        val m3 = MessageEntity(MessageId(3), "m3", 3000, MessageId(2))
        val m4 = MessageEntity(MessageId(4), "m4", 4000, MessageId(1))
        val m5 = MessageEntity(MessageId(5), "m5", 5000, null) // unrelated

        `when`(getAllMessages()).thenReturn(flowOf(listOf(m1, m2, m3, m4, m5)))

        viewModel = ThreadsViewModel(useCases)
        advanceUntilIdle()

        val subThread = viewModel.getSubThread(m1)
        
        assertEquals(m1.id, subThread.root.id)
        assertEquals(3, subThread.replies.size)
        // Descendants: m2 (reply to m1), m3 (reply to m2), m4 (reply to m1)
        val replyIds = subThread.replies.map { it.id }
        assert(replyIds.contains(MessageId(2)))
        assert(replyIds.contains(MessageId(3)))
        assert(replyIds.contains(MessageId(4)))
        assert(!replyIds.contains(MessageId(5)))
    }
}
