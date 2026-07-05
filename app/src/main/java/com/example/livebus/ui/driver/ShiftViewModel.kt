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
    private val _selectedRoute = MutableStateFlow("216W (HITEC City Express)")
    val selectedRoute: StateFlow<String> = _selectedRoute.asStateFlow()

    private val _selectedBus = MutableStateFlow("TG-09-Z-4052")
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
            val coords = listOf(
                Pair(34.0522, -118.2437),
                Pair(34.0532, -118.2447),
                Pair(34.0542, -118.2457),
                Pair(34.0552, -118.2467),
                Pair(34.0562, -118.2477)
            )
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