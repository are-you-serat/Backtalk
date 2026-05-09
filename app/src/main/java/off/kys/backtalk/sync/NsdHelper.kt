package off.kys.backtalk.sync

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import java.util.concurrent.Executor

class NsdHelper(context: Context) {
    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val mainExecutor: Executor = ContextCompat.getMainExecutor(context)

    private var multicastLock: WifiManager.MulticastLock? = null
    private val serviceType = "_backtalk_sync._tcp"
    private var serviceName: String? = null

    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null

    fun registerService(port: Int, deviceName: String, deviceId: String) {
        unregisterService()

        val serviceInfo = NsdServiceInfo().apply {
            this.serviceName = deviceName
            this.serviceType = this@NsdHelper.serviceType
            this.port = port
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setAttribute("deviceId", deviceId)
            }
        }

        registrationListener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(info: NsdServiceInfo) {
                serviceName = info.serviceName
                Log.d("NsdHelper", "Service registered: ${info.serviceName}")
            }

            override fun onRegistrationFailed(info: NsdServiceInfo, errorCode: Int) {
                Log.e("NsdHelper", "Registration failed: $errorCode")
            }

            override fun onServiceUnregistered(info: NsdServiceInfo) {
                Log.d("NsdHelper", "Service unregistered: ${info.serviceName}")
            }

            override fun onUnregistrationFailed(info: NsdServiceInfo, errorCode: Int) {
                Log.e("NsdHelper", "Unregistration failed: $errorCode")
            }
        }

        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
    }

    fun discoverServices(
        onDeviceDiscovered: (NsdServiceInfo) -> Unit,
        onDeviceLost: (NsdServiceInfo) -> Unit
    ) {
        stopDiscovery()

        multicastLock = wifiManager.createMulticastLock("BacktalkNsdLock").apply {
            setReferenceCounted(true)
            acquire()
        }

        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                Log.d("NsdHelper", "Discovery started")
            }

            override fun onServiceFound(service: NsdServiceInfo) {
                val discoveredType = service.serviceType.trim('.')
                val targetType = serviceType.trim('.')

                if (discoveredType == targetType && service.serviceName != serviceName) {

                    // 1. Check for Android 14+ (API 34) - The "Callback" way
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        nsdManager.registerServiceInfoCallback(
                            service,
                            mainExecutor,
                            object : NsdManager.ServiceInfoCallback {
                                override fun onServiceUpdated(info: NsdServiceInfo) {
                                    onDeviceDiscovered(info)
                                }

                                override fun onServiceInfoCallbackRegistrationFailed(errorCode: Int) {
                                    Log.e("NsdHelper", "Callback failed: $errorCode")
                                }

                                override fun onServiceLost() {
                                    onDeviceLost(service)
                                }
                                override fun onServiceInfoCallbackUnregistered() {}
                            })
                    }
                    // 2. Check for Android 13 (API 33) - The "Executor" way
                    else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        @Suppress("DEPRECATION")
                        nsdManager.resolveService(
                            service,
                            mainExecutor,
                            object : NsdManager.ResolveListener {
                                override fun onResolveFailed(info: NsdServiceInfo, errorCode: Int) {
                                    Log.e("NsdHelper", "Resolve failed: $errorCode")
                                }

                                override fun onServiceResolved(info: NsdServiceInfo) {
                                    onDeviceDiscovered(info)
                                }
                            })
                    }
                    // 3. Fallback for everything else (Legacy)
                    else {
                        @Suppress("DEPRECATION")
                        nsdManager.resolveService(service, object : NsdManager.ResolveListener {
                            override fun onResolveFailed(info: NsdServiceInfo, errorCode: Int) {
                                Log.e("NsdHelper", "Legacy resolve failed: $errorCode")
                            }

                            override fun onServiceResolved(info: NsdServiceInfo) {
                                onDeviceDiscovered(info)
                            }
                        })
                    }
                }
            }

            override fun onServiceLost(service: NsdServiceInfo) {
                Log.d("NsdHelper", "Service lost: ${service.serviceName}")
                onDeviceLost(service)
            }

            override fun onDiscoveryStopped(regType: String) {
                Log.d("NsdHelper", "Discovery stopped")
            }

            override fun onStartDiscoveryFailed(regType: String, errorCode: Int) {
                nsdManager.stopServiceDiscovery(this)
            }

            override fun onStopDiscoveryFailed(regType: String, errorCode: Int) {
                nsdManager.stopServiceDiscovery(this)
            }
        }

        nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
    }

    fun stopDiscovery() {
        discoveryListener?.let {
            try {
                nsdManager.stopServiceDiscovery(it)
            } catch (e: Exception) {
                Log.e("NsdHelper", "Error stopping discovery", e)
            }
            discoveryListener = null
        }
        multicastLock?.let {
            if (it.isHeld) it.release()
            multicastLock = null
        }
    }

    fun unregisterService() {
        registrationListener?.let {
            nsdManager.unregisterService(it)
            registrationListener = null
        }
    }
}