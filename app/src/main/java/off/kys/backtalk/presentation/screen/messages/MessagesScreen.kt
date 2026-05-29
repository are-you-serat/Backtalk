package off.kys.backtalk.presentation.screen.messages

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import off.kys.backtalk.presentation.screen.messages.components.MessagesScreenContent
import off.kys.backtalk.presentation.screen.preferences.SettingsScreen
import off.kys.backtalk.presentation.screen.reminders.RemindersScreen
import off.kys.backtalk.presentation.screen.statistics.StatisticsScreen
import off.kys.backtalk.presentation.screen.threads.ThreadsScreen
import off.kys.backtalk.presentation.viewmodel.MessagesViewModel
import off.kys.backtalk.util.AudioPlayer
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

class MessagesScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinViewModel<MessagesViewModel>()
        val audioPlayer = koinInject<AudioPlayer>()
        val state by viewModel.uiState

        MessagesScreenContent(
            state = state,
            onEvent = viewModel::onEvent,
            onSettingsClick = { navigator += SettingsScreen() },
            onThreadsClick = { navigator += ThreadsScreen() },
            onRemindersClick = { navigator += RemindersScreen() },
            onStatisticsClick = { navigator += StatisticsScreen() },
            onStopAudio = { audioPlayer.stop() }
        )
    }
}