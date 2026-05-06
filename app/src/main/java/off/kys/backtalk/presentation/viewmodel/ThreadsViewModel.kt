package off.kys.backtalk.presentation.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import off.kys.backtalk.common.Constants
import off.kys.backtalk.data.local.entity.MessageEntity
import off.kys.backtalk.domain.model.Thread
import off.kys.backtalk.domain.use_case_bundle.MessagesUseCases
import off.kys.backtalk.presentation.event.ThreadsUiEvent
import off.kys.backtalk.presentation.state.ThreadsUiState

class ThreadsViewModel(
    private val useCases: MessagesUseCases
) : ViewModel() {

    private val _uiState = mutableStateOf(ThreadsUiState())
    val uiState: State<ThreadsUiState> = _uiState

    init {
        loadMessages()
    }

    fun onEvent(event: ThreadsUiEvent) {
        when (event) {
            is ThreadsUiEvent.LoadThreads -> loadMessages()
            is ThreadsUiEvent.ToggleIncludeReplies -> {
                _uiState.value = _uiState.value.copy(includeReplies = event.include)
                loadMessages() // Re-group
            }
            is ThreadsUiEvent.ToggleGroupByTime -> {
                _uiState.value = _uiState.value.copy(groupByTime = event.enabled)
                loadMessages() // Re-group
            }
        }
    }

    private fun loadMessages() {
        viewModelScope.launch {
            useCases.getAllMessages().collectLatest { messages ->
                val grouped = groupMessages(
                    messages,
                    _uiState.value.includeReplies,
                    _uiState.value.groupByTime
                )
                _uiState.value = _uiState.value.copy(threads = grouped)
            }
        }
    }

    private fun groupMessages(
        messages: List<MessageEntity>,
        includeReplies: Boolean,
        groupByTime: Boolean
    ): List<Thread> {
        if (messages.isEmpty()) return emptyList()

        val sorted = messages.sortedBy { it.timestamp }

        return if (groupByTime) {
            val groups = mutableListOf<MutableList<MessageEntity>>()
            var currentGroup = mutableListOf<MessageEntity>()

            sorted.forEach { message ->
                if (currentGroup.isEmpty()) {
                    currentGroup.add(message)
                } else {
                    val last = currentGroup.last()
                    if (message.timestamp - last.timestamp < Constants.TIME_GAP_FOR_HEADER) {
                        currentGroup.add(message)
                    } else {
                        groups.add(currentGroup)
                        currentGroup = mutableListOf(message)
                    }
                }
            }
            if (currentGroup.isNotEmpty()) groups.add(currentGroup)

            groups.map { group ->
                Thread(root = group.first(), replies = group.drop(1), isTimeGrouped = true)
            }.reversed()
        } else {
            val threads = mutableListOf<Thread>()
            val rootMessages = if (includeReplies) {
                sorted
            } else {
                sorted.filter { it.repliedToId == null }
            }

            rootMessages.forEach { root ->
                val replies = mutableListOf<MessageEntity>()
                fun collectReplies(parentId: off.kys.backtalk.domain.model.MessageId) {
                    val directReplies = sorted.filter { it.repliedToId == parentId }
                    replies.addAll(directReplies)
                    directReplies.forEach { collectReplies(it.id) }
                }
                collectReplies(root.id)
                threads.add(Thread(root, replies.distinctBy { it.id }.sortedBy { it.timestamp }))
            }
            threads.sortedByDescending { it.root.timestamp }
        }
    }
}
