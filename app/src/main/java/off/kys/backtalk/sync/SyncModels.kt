package off.kys.backtalk.sync

import kotlinx.serialization.Serializable
import off.kys.backtalk.domain.model.BackupData

@Serializable
sealed class SyncPacket {
    @Serializable
    data class PairingRequest(val deviceName: String, val deviceId: String) : SyncPacket()

    @Serializable
    data class PairingResponse(val accepted: Boolean, val pinRequired: Boolean = false, val pin: String? = null) : SyncPacket()

    @Serializable
    data class PinVerification(val pin: String) : SyncPacket()

    @Serializable
    data class PinVerificationResponse(val success: Boolean, val sessionKey: String? = null) : SyncPacket()

    @Serializable
    data class DataUpdate(val backupData: BackupData) : SyncPacket()

    @Serializable
    data class SyncRequest(val requesterId: String) : SyncPacket()

    @Serializable
    data class Ack(val success: Boolean) : SyncPacket()

    @Serializable
    data class Disconnect(val deviceId: String) : SyncPacket()

    @Serializable
    data class Error(val message: String) : SyncPacket()
}

@Serializable
data class DeviceInfo(
    val id: String,
    val name: String,
    val address: String? = null,
    val port: Int = 0,
    val lastSyncTimestamp: Long = 0,
    val lastSeenTimestamp: Long = 0,
    val isPaired: Boolean = false,
    val isOnline: Boolean = false
)
