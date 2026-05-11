package off.kys.backtalk.common.manager

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import off.kys.backtalk.domain.repository.MessagesRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Receiver that reschedules alarms after a device reboot.
 */
class BootReceiver : BroadcastReceiver(), KoinComponent {

    private val repository: MessagesRepository by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val alarmScheduler = AlarmScheduler(context)
            scope.launch {
                val scheduledMessages = repository.getAllScheduledMessagesSync()
                scheduledMessages.forEach {
                    if (it.scheduledTimestamp > System.currentTimeMillis()) {
                        alarmScheduler.schedule(it)
                    }
                }
            }
        }
    }
}
