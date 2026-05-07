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

    fun onEvent(event: ThreadsUiEvent) = when (event) {
        is ThreadsUiEvent.LoadThreads -> loadMessages()
    }

    private fun loadMessages() {
        viewModelScope.launch {
            useCases.getAllMessages().collectLatest { messages ->
                val grouped = groupMessages(
                    messages
                )
                _uiState.value = _uiState.value.copy(threads = grouped)
            }
        }
    }

    private fun groupMessages(messages: List<MessageEntity>): List<Thread> {
        if (messages.isEmpty()) return emptyList()

        val sorted = messages.sortedBy { it.timestamp }

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

        return groups.map { group ->
            Thread(root = group.first(), replies = group.drop(1))
        }.reversed()
    }
}
