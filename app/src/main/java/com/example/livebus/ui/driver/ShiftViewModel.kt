package com.example.livebus.ui.driver

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ShiftViewModel : ViewModel() {
    private val _selectedRoute = MutableStateFlow("D-1 (ISBT - Clement Town Campus)")
    val selectedRoute: StateFlow<String> = _selectedRoute.asStateFlow()

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

    fun setRoute(route: String) {
        _selectedRoute.value = route
        logIncident("Assigned itinerary: $route")
    }

    fun setBus(busId: String) {
        _selectedBus.value = busId
        logIncident("Registered vehicle ID: #$busId")
    }

    fun startBroadcasting() {
        if (_isBroadcasting.value) return
        _isBroadcasting.value = true
        logIncident("🟢 TELEMETRY STARTED: Broadcasting GPS payload to /app/driver/update every 3s.")
        
        broadcastJob = viewModelScope.launch {
            val coords = when {
                _selectedRoute.value.contains("D-1") -> listOf(
                    Pair(30.2872, 77.9984),
                    Pair(30.2862, 78.0012),
                    Pair(30.2835, 78.0028),
                    Pair(30.2805, 78.0045),
                    Pair(30.2785, 78.0055),
                    Pair(30.2760, 78.0063),
                    Pair(30.2730, 78.0075),
                    Pair(30.2700, 78.0084),
                    Pair(30.2692, 78.0088)
                )
                _selectedRoute.value.contains("D-2") -> listOf(
                    Pair(30.3244, 78.0411),
                    Pair(30.3200, 78.0400),
                    Pair(30.3160, 78.0380),
                    Pair(30.3000, 78.0150),
                    Pair(30.2872, 77.9984),
                    Pair(30.2862, 78.0012),
                    Pair(30.2835, 78.0028),
                    Pair(30.2805, 78.0045),
                    Pair(30.2785, 78.0055),
                    Pair(30.2760, 78.0063),
                    Pair(30.2730, 78.0075),
                    Pair(30.2700, 78.0084),
                    Pair(30.2692, 78.0088)
                )
                else -> listOf(
                    Pair(30.2872, 77.9984),
                    Pair(30.2862, 78.0012),
                    Pair(30.2835, 78.0028),
                    Pair(30.2700, 78.0084)
                )
            }
            var index = 0
            while (true) {
                val (lat, lon) = coords[index]
                _telemetryCount.value += 1
                logIncident("📡 [TX #${_telemetryCount.value}] JSON payload sent -> busId: #${_selectedBus.value}, lat: $lat, lon: $lon")
                index = (index + 1) % coords.size
                delay(3000)
            }
        }
    }

    fun stopBroadcasting() {
        broadcastJob?.cancel()
        broadcastJob = null
        _isBroadcasting.value = false
        logIncident("🔴 TELEMETRY TERMINATED: Shift ended cleanly.")
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
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val formattedLog = "[$timestamp] $message"
        _incidentLogs.value = listOf(formattedLog) + _incidentLogs.value
    }
}