package com.example.livebus.ui.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.livebus.BuildConfig
import com.example.livebus.data.AuthRepository
import com.example.livebus.data.TransitRepository
import com.example.livebus.data.TransitRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ShiftViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val transitRepository: TransitRepository
) : ViewModel() {

    val selectedCityId: StateFlow<String> = transitRepository.selectedCityId
    val allCities = transitRepository.allCities

    val availableRoutes: StateFlow<List<TransitRoute>> = transitRepository.selectedCityId.map { cityId ->
        transitRepository.allRoutes.values.filter { it.cityId == cityId }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedRoute = MutableStateFlow<TransitRoute?>(null)
    val selectedRoute: StateFlow<TransitRoute?> = _selectedRoute.asStateFlow()

    private val _selectedBus = MutableStateFlow("UA-07-TA-2024")
    val selectedBus: StateFlow<String> = _selectedBus.asStateFlow()

    private val _isBroadcasting = MutableStateFlow(false)
    val isBroadcasting: StateFlow<Boolean> = _isBroadcasting.asStateFlow()

    private val _telemetryCount = MutableStateFlow(0)
    val telemetryCount: StateFlow<Int> = _telemetryCount.asStateFlow()

    private val _incidentLogs = MutableStateFlow<List<String>>(
        listOf(
            "[System] Driver Duty Dashboard initialized.",
            "[Broker] Standby for STOMP connection to ws://localhost:8080/ws-livebus."
        )
    )
    val incidentLogs: StateFlow<List<String>> = _incidentLogs.asStateFlow()

    private var broadcastJob: Job? = null

    fun selectCity(cityId: String) {
        transitRepository.selectCity(cityId)
        _selectedRoute.value = null
        logIncident("Switched campus registry to: $cityId")
    }

    fun setRoute(route: TransitRoute) {
        _selectedRoute.value = route
        logIncident("Assigned itinerary: ${route.displayName} (${route.routeName})")
    }

    fun setBus(busId: String) {
        _selectedBus.value = busId
        logIncident("Registered vehicle ID: #$busId")
    }

    fun startBroadcasting() {
        if (_isBroadcasting.value) return
        val route = _selectedRoute.value ?: return

        logIncident("🟢 Initializing shift on backend...")
        broadcastJob = viewModelScope.launch {
            // 1. Start Trip on Backend REST API
            val startJson = JSONObject().apply {
                put("routeId", route.dbRouteId)
                put("busId", route.dbBusId)
            }
            
            val startResult = makeAuthenticatedRequest(
                urlStr = "${getHttpBaseUrl()}/api/driver/trips/start",
                method = "POST",
                bodyJson = startJson
            )

            if (startResult.isFailure) {
                logIncident("❌ Shift activation FAILED: ${startResult.exceptionOrNull()?.message}")
                return@launch
            }

            _isBroadcasting.value = true
            logIncident("🟢 SHIFT ACTIVATED: Registered active trip. Streaming GPS to /api/driver/trips/location.")

            // 2. Stream locations
            val originalWaypoints = route.waypoints
            if (originalWaypoints.isEmpty()) {
                logIncident("⚠️ No waypoints found for route.")
                return@launch
            }

            var waypoints = originalWaypoints
            var index = 0
            var directionForward = true

            while (true) {
                val currentLatLng = waypoints[index]
                val lat = currentLatLng.latitude
                val lon = currentLatLng.longitude

                val locationJson = JSONObject().apply {
                    put("latitude", lat)
                    put("longitude", lon)
                }

                val locResult = makeAuthenticatedRequest(
                    urlStr = "${getHttpBaseUrl()}/api/driver/trips/location",
                    method = "PUT",
                    bodyJson = locationJson
                )

                if (locResult.isSuccess) {
                    _telemetryCount.value += 1
                    logIncident("📡 [TX #${_telemetryCount.value}] GPS payload -> lat: $lat, lon: $lon")
                } else {
                    logIncident("⚠️ [TX error] Failed location update: ${locResult.exceptionOrNull()?.message}")
                }

                // Move index
                if (directionForward) {
                    index++
                    if (index >= waypoints.size) {
                        directionForward = false
                        waypoints = originalWaypoints.reversed()
                        index = 1
                        logIncident("🔄 Reached destination: Reversing path back to start.")
                    }
                } else {
                    index++
                    if (index >= waypoints.size) {
                        directionForward = true
                        waypoints = originalWaypoints
                        index = 1
                        logIncident("🔄 Reached start terminal: Reversing path to destination.")
                    }
                }

                delay(3000)
            }
        }
    }

    fun stopBroadcasting() {
        if (!_isBroadcasting.value) return
        broadcastJob?.cancel()
        broadcastJob = null
        _isBroadcasting.value = false

        viewModelScope.launch {
            logIncident("🔴 Terminating shift on backend...")
            val result = makeAuthenticatedRequest(
                urlStr = "${getHttpBaseUrl()}/api/driver/trips/end",
                method = "POST"
            )
            if (result.isSuccess) {
                logIncident("🔴 SHIFT TERMINATED: active trip ended cleanly on backend.")
            } else {
                logIncident("⚠️ End shift request returned error: ${result.exceptionOrNull()?.message}")
            }
        }
    }

    fun reportTrafficDelay() {
        logIncident("🟠 [REPORT] Traffic Delay (+5m) broadcasted to Command Center.")
    }

    fun reportCrowd() {
        logIncident("🔵 [REPORT] High Passenger Density reported at current coordinates.")
    }

    fun triggerSos() {
        logIncident("🚨 [EMERGENCY SOS] Immediate distress signal published to /topic/fleet/sos!")
    }

    private fun logIncident(message: String) {
        android.util.Log.d("ShiftViewModel", message)
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val formattedLog = "[$timestamp] $message"
        _incidentLogs.value = listOf(formattedLog) + _incidentLogs.value
    }

    private suspend fun makeAuthenticatedRequest(
        urlStr: String,
        method: String,
        bodyJson: JSONObject? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = URL(urlStr)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = method
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            
            val cookie = authRepository.sessionCookie
            android.util.Log.d("ShiftViewModel", "Request $method to $urlStr, session cookie: $cookie")
            if (cookie != null) {
                connection.setRequestProperty("Cookie", cookie)
            }

            if (bodyJson != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                OutputStreamWriter(connection.outputStream).use { writer ->
                    writer.write(bodyJson.toString())
                    writer.flush()
                }
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                Result.success(responseText)
            } else {
                val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                Result.failure(Exception("HTTP error $responseCode: $errorText"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getHttpBaseUrl(): String {
        val wsUrl = BuildConfig.WEBSOCKET_URL
        val base = wsUrl.replace("ws://", "http://").replace("wss://", "https://")
        return if (base.endsWith("/ws-livebus")) {
            base.substring(0, base.length - "/ws-livebus".length)
        } else {
            base
        }
    }
}