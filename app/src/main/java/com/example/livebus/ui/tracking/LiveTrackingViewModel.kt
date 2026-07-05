package com.example.livebus.ui.tracking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import javax.inject.Inject
import org.json.JSONObject
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

data class LatLng(val latitude: Double, val longitude: Double)

data class RouteDetails(
    val routeName: String = "Route 216W",
    val destination: String = "IIIT Gachibowli",
    val direction: String = "Westbound"
)

@HiltViewModel
class LiveTrackingViewModel @Inject constructor() : ViewModel() {

    private val stompClient: StompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://10.0.2.2:8080/transit-ws")
    private val compositeDisposable = CompositeDisposable()

    // Default initial coordinates for Hyderabad (Mehdipatnam Bus Depot)
    private val _busLocation = MutableStateFlow<LatLng?>(LatLng(17.3916, 78.4356))
    val busLocation: StateFlow<LatLng?> = _busLocation.asStateFlow()

    // Authoritative road intersection waypoints along Route 216W (Mehdipatnam to Gachibowli corridor)
    private val route216Waypoints = listOf(
        LatLng(17.3916, 78.4356), // Start: Mehdipatnam Bus Depot
        LatLng(17.4018, 78.4111), // Tolichowki X Roads
        LatLng(17.4065, 78.3912), // Shaikpet Dargah
        LatLng(17.4242, 78.3816), // Raidurg Bio-Diversity X Roads
        LatLng(17.4401, 78.3611), // Gachibowli Stadium X Roads
        LatLng(17.4455, 78.3489)  // Stop: IIIT Hyderabad Campus
    )

    private val _routeWaypoints = MutableStateFlow(route216Waypoints)
    val routeWaypoints: StateFlow<List<LatLng>> = _routeWaypoints.asStateFlow()

    private val _userStopLocation = MutableStateFlow(LatLng(17.4455, 78.3489))
    val userStopLocation: StateFlow<LatLng> = _userStopLocation.asStateFlow()

    private val _routeDetails = MutableStateFlow(RouteDetails())
    val routeDetails: StateFlow<RouteDetails> = _routeDetails.asStateFlow()

    private val _eta = MutableStateFlow(5)
    val eta: StateFlow<Int> = _eta.asStateFlow()

    private val _distance = MutableStateFlow(1.2)
    val distance: StateFlow<Double> = _distance.asStateFlow()

    private val _busStatus = MutableStateFlow(BusStatus.ON_TIME)
    val busStatus: StateFlow<BusStatus> = _busStatus.asStateFlow()

    private val _isAlertActive = MutableStateFlow(false)
    val isAlertActive: StateFlow<Boolean> = _isAlertActive.asStateFlow()

    init {
        connectStomp()
    }

    fun startSimulating() {
        fetchDirectionsAndStartSimulation()
        viewModelScope.launch {
            var currentSegment = 0
            var progress = 0.0
            while (true) {
                kotlinx.coroutines.delay(1200)
                val waypoints = _routeWaypoints.value
                if (waypoints.size >= 2 && currentSegment < waypoints.size - 1) {
                    val currentStart = waypoints[currentSegment]
                    val currentEnd = waypoints[kotlin.math.min(currentSegment + 1, waypoints.size - 1)]
                    progress += 0.25
                    if (progress >= 1.0) {
                        progress = 0.0
                        currentSegment++
                        if (currentSegment >= waypoints.size - 1) {
                            currentSegment = 0 // Loop route simulation for continuous demonstration
                        }
                    }
                    val activeStart = waypoints[currentSegment]
                    val activeEnd = waypoints[kotlin.math.min(currentSegment + 1, waypoints.size - 1)]
                    val newLat = activeStart.latitude + (activeEnd.latitude - activeStart.latitude) * progress
                    val newLon = activeStart.longitude + (activeEnd.longitude - activeStart.longitude) * progress
                    _busLocation.value = LatLng(newLat, newLon)

                    val remainingSegments = (waypoints.size - 1 - currentSegment).coerceAtLeast(1)
                    _eta.value = remainingSegments
                    _distance.value = kotlin.math.round((remainingSegments * 0.24) * 10) / 10.0
                }
            }
        }
    }

    private fun fetchDirectionsAndStartSimulation() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val apiKey = com.example.livebus.BuildConfig.MAPS_API_KEY
                if (apiKey.isNotBlank() && !apiKey.contains("PASTE_YOUR")) {
                    val urlStr = "https://maps.googleapis.com/maps/api/directions/json?origin=17.3916,78.4356&destination=17.4455,78.3489&mode=driving&key=$apiKey"
                    val url = java.net.URL(urlStr)
                    val conn = url.openConnection() as java.net.HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.connectTimeout = 5000
                    conn.readTimeout = 5000
                    if (conn.responseCode == 200) {
                        val response = conn.inputStream.bufferedReader().use { it.readText() }
                        val json = JSONObject(response)
                        val routes = json.optJSONArray("routes")
                        if (routes != null && routes.length() > 0) {
                            val overviewPolyline = routes.getJSONObject(0).optJSONObject("overview_polyline")
                            val pointsStr = overviewPolyline?.optString("points")
                            if (!pointsStr.isNullOrBlank()) {
                                val decoded = decodePolyline(pointsStr)
                                if (decoded.size > 2) {
                                    _routeWaypoints.value = decoded
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println("Directions API fetch error, using default waypoints: ${e.message}")
            }
        }
    }

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0
        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lat += dlat
            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lng += dlng
            poly.add(LatLng(lat / 1E5, lng / 1E5))
        }
        return poly
    }

    private fun connectStomp() {
        stompClient.connect()

        val disposable = stompClient.topic("/topic/route/216W")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { it.payload }
            .subscribe(Consumer { payload ->
                viewModelScope.launch {
                    parseAndApplyMessage(payload)
                }
            }, Consumer { error ->
                println("Error subscribing to STOMP topic: ${error.message}")
            })
        compositeDisposable.add(disposable)
    }

    fun parseAndApplyMessage(payload: String) {
        try {
            val json = JSONObject(payload)
            val lat = json.getDouble("latitude")
            val lon = json.getDouble("longitude")
            _busLocation.value = LatLng(lat, lon)
            if (json.has("eta")) _eta.value = json.getInt("eta")
            if (json.has("distance")) _distance.value = json.getDouble("distance")
            if (json.has("status")) _busStatus.value = BusStatus.valueOf(json.getString("status"))
        } catch (e: Exception) {
            println("Error parsing STOMP payload: ${e.message}")
        }
    }

    fun toggleAlert() {
        _isAlertActive.value = !_isAlertActive.value
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        stompClient.disconnect()
        super.onCleared()
    }
}
