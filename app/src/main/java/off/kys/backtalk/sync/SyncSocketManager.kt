package off.kys.backtalk.sync

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

private const val TAG = "SyncSocketManager"

class SyncSocketManager {
    private var serverSocket: ServerSocket? = null
    private val json = Json { ignoreUnknownKeys = true }
    private var serverJob: Job? = null
    private val serverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    suspend fun startServer(onPacketReceived: suspend (SyncPacket) -> SyncPacket?): Int =
        withContext(Dispatchers.IO) {
            val server = ServerSocket(0)
            serverSocket = server
            val port = server.localPort

            // Launch the server loop in a managed coroutine scope
            serverJob = serverScope.launch {
                server.use { server ->
                    while (isActive && !server.isClosed) {
                        val socket = try {
                            server.accept()
                        } catch (e: Exception) {
                            Log.e(TAG, e.message.orEmpty(), e)
                            null
                        }

                        socket?.let {
                            launch { handleClient(it, onPacketReceived) }
                        }
                    }
                }
            }

            port
        }

    private suspend fun handleClient(
        socket: Socket,
        onPacketReceived: suspend (SyncPacket) -> SyncPacket?,
    ) = withContext(Dispatchers.IO) {
        socket.use { s ->
            try {
                s.soTimeout = 30000
                val reader = BufferedReader(InputStreamReader(s.getInputStream()))
                val writer = PrintWriter(s.getOutputStream(), true)

                // readLine is blocking, but properly scoped here
                val line = reader.readLine() ?: return@withContext
                val packet = json.decodeFromString<SyncPacket>(line)

                val response = onPacketReceived(packet)
                if (response != null) {
                    writer.println(json.encodeToString(response))
                }
            } catch (e: Exception) {
                Log.e("SyncSocketManager", "Handle client error", e)
            }
        }
    }

    suspend fun sendPacket(
        address: String,
        port: Int,
        packet: SyncPacket,
        timeoutMillis: Int = 10000
    ): SyncPacket? = withContext(Dispatchers.IO) {
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(address, port), timeoutMillis)
                socket.soTimeout = timeoutMillis
                val writer = PrintWriter(socket.getOutputStream(), true)
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                writer.println(json.encodeToString(packet))
                val responseLine = reader.readLine()
                responseLine?.let { json.decodeFromString<SyncPacket>(it) }
            }
        } catch (e: Exception) {
            Log.e("SyncSocketManager", "Send packet error to $address:$port", e)
            null
        }
    }

    suspend fun sendPacketWithRetry(
        address: String,
        port: Int,
        packet: SyncPacket,
        maxRetries: Int = 3,
        initialDelayMillis: Long = 1000
    ): SyncPacket? {
        var currentDelay = initialDelayMillis
        repeat(maxRetries) { attempt ->
            val result = sendPacket(address, port, packet)
            if (result != null) return result

            if (attempt < (maxRetries - 1)) {
                Log.d(
                    "SyncSocketManager",
                    "Retrying packet send to $address:$port, attempt ${attempt + 1}"
                )
                delay(currentDelay)
                currentDelay *= 2
            }
        }
        return null
    }

    fun stopServer() {
        serverJob?.cancel()
        try {
            serverSocket?.close()
        } catch (e: Exception) {
            Log.e("SyncSocketManager", "Stop server error", e)
        }
        serverSocket = null
    }

    fun generatePin(): String = (100000..999999).random().toString()
}
