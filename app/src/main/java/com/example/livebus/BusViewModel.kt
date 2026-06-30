package com.example.livebus

import androidx.lifecycle.ViewModel
import com.example.livebus.data.LocationData
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BusViewModel : ViewModel() {
    private val webSocketClient = WebSocketClient()
    private val gson = Gson()

    private val _locationData = MutableStateFlow<LocationData?>(LocationData("101-A", 37.7749, -122.4194, "5 mins", "1.2 km"))
    val locationData: StateFlow<LocationData?> = _locationData

    private val _busId = MutableStateFlow<String?>("101-A")
    val busId: StateFlow<String?> = _busId

    private val _eta = MutableStateFlow<String?>("5 mins")
    val eta: StateFlow<String?> = _eta

    private val _distance = MutableStateFlow<String?>("1.2 km")
    val distance: StateFlow<String?> = _distance

    init {
        connectWebSocket()
    }

    fun connectWebSocket() {
        webSocketClient.connect("ws://localhost:8080/gs-guide-websocket")
        webSocketClient.subscribeToTopic("/topic/route/101-A") { message ->
            try {
                val parsedLocation = gson.fromJson(message, LocationData::class.java)
                _locationData.value = parsedLocation
                parsedLocation.busId?.let { _busId.value = it }
                parsedLocation.eta?.let { _eta.value = it }
                parsedLocation.distance?.let { _distance.value = it }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun disconnectWebSocket() {
        webSocketClient.disconnect()
    }

    override fun onCleared() {
        super.onCleared()
        disconnectWebSocket()
    }

    fun updateBusData(location: LocationData?, busId: String?, eta: String?, distance: String?) {
        _locationData.value = location
        _busId.value = busId
        _eta.value = eta
        _distance.value = distance
    }
}
