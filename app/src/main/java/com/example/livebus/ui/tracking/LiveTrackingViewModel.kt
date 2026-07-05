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

import com.example.livebus.data.TransitRepository
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Job

data class LatLng(val latitude: Double, val longitude: Double)

data class RouteDetails(
    val routeName: String = "Route 216W",
    val destination: String = "IIIT Gachibowli",
    val direction: String = "Westbound"
)

data class ActiveBus(
    val busId: String,
    val location: LatLng,
    val etaMinutes: Int,
    val distanceKm: Double,
    val status: BusStatus
)

@HiltViewModel
class LiveTrackingViewModel @Inject constructor(
    private val transitRepository: TransitRepository
) : ViewModel() {

    private val stompClient: StompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://10.0.2.2:8080/transit-ws")
    private val compositeDisposable = CompositeDisposable()
    private var routeDisposable: Disposable? = null
    private var simulationJob: Job? = null

    private val _activeBuses = MutableStateFlow<List<ActiveBus>>(emptyList())
    val activeBuses: StateFlow<List<ActiveBus>> = _activeBuses.asStateFlow()

    private val _busLocation = MutableStateFlow<LatLng?>(transitRepository.getActiveRoute()?.initialBusLocation)
    val busLocation: StateFlow<LatLng?> = _busLocation.asStateFlow()

    private val _routeWaypoints = MutableStateFlow(transitRepository.getActiveRoute()?.waypoints ?: emptyList())
    val routeWaypoints: StateFlow<List<LatLng>> = _routeWaypoints.asStateFlow()

    private val _userStopLocation = MutableStateFlow(transitRepository.getActiveRoute()?.userStopLocation ?: transitRepository.getSelectedCity().centerLatLng)
    val userStopLocation: StateFlow<LatLng> = _userStopLocation.asStateFlow()

    private val _routeDetails = MutableStateFlow(
        RouteDetails(
            routeName = transitRepository.getActiveRoute()?.displayName ?: "No Route Selected",
            destination = transitRepository.getActiveRoute()?.destination ?: "Pick from Plan Trip",
            direction = transitRepository.getActiveRoute()?.direction ?: "${transitRepository.getSelectedCity().name} (${transitRepository.getSelectedCity().defaultLocationName})"
        )
    )
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
        stompClient.connect()
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            kotlinx.coroutines.flow.combine(
                transitRepository.activeRouteId,
                transitRepository.selectedCityId
            ) { routeId, cityId ->
                Pair(transitRepository.getActiveRoute(), transitRepository.getSelectedCity())
            }.collect { (route, city) ->
                if (route != null) {
                    _busLocation.value = route.initialBusLocation
                    _userStopLocation.value = route.userStopLocation
                    _routeWaypoints.value = route.waypoints
                    _routeDetails.value = RouteDetails(
                        routeName = route.displayName,
                        destination = route.destination,
                        direction = route.direction
                    )
                    subscribeToRouteTopic(route.stompTopic)
                    startSimulating()
                } else {
                    _busLocation.value = null
                    _userStopLocation.value = city.centerLatLng
                    _routeWaypoints.value = emptyList()
                    _activeBuses.value = emptyList()
                    _routeDetails.value = RouteDetails(
                        routeName = "No Route Selected",
                        destination = "Pick from Plan Trip",
                        direction = "${city.name} (${city.defaultLocationName})"
                    )
                    routeDisposable?.dispose()
                    simulationJob?.cancel()
                }
            }
        }
    }

    fun startSimulating() {
        simulationJob?.cancel()
        val activeRoute = transitRepository.getActiveRoute() ?: return
        fetchDirectionsForActiveRoute(activeRoute)
        simulationJob = viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            val baseBusId = activeRoute.busId
            val bus2Id = if (baseBusId.contains("-")) baseBusId.substringBeforeLast("-") + "-" + (baseBusId.substringAfterLast("-").toIntOrNull()?.plus(48) ?: "4100") else "${baseBusId}-B2"
            val bus3Id = if (baseBusId.contains("-")) baseBusId.substringBeforeLast("-") + "-" + (baseBusId.substringAfterLast("-").toIntOrNull()?.plus(170) ?: "4222") else "${baseBusId}-B3"

            var seg1 = 0; var prog1 = 0.6
            var seg2 = 0; var prog2 = 0.3
            var seg3 = 0; var prog3 = 0.05

            while (true) {
                kotlinx.coroutines.delay(1200)
                val waypoints = _routeWaypoints.value
                if (waypoints.size >= 2) {
                    val maxSeg = waypoints.size - 1

                    // Update Bus 1 (Primary / Closest)
                    prog1 += 0.25; if (prog1 >= 1.0) { prog1 = 0.0; seg1 = (seg1 + 1) % maxSeg }
                    val s1 = waypoints[seg1]; val e1 = waypoints[kotlin.math.min(seg1 + 1, maxSeg)]
                    val loc1 = LatLng(s1.latitude + (e1.latitude - s1.latitude) * prog1, s1.longitude + (e1.longitude - s1.longitude) * prog1)
                    val rem1 = (maxSeg - seg1).coerceAtLeast(1)

                    // Update Bus 2 (Following)
                    prog2 += 0.20; if (prog2 >= 1.0) { prog2 = 0.0; seg2 = (seg2 + 1) % maxSeg }
                    val s2 = waypoints[seg2]; val e2 = waypoints[kotlin.math.min(seg2 + 1, maxSeg)]
                    val loc2 = LatLng(s2.latitude + (e2.latitude - s2.latitude) * prog2, s2.longitude + (e2.longitude - s2.longitude) * prog2)
                    val rem2 = rem1 + 9

                    // Update Bus 3 (Queue)
                    prog3 += 0.18; if (prog3 >= 1.0) { prog3 = 0.0; seg3 = (seg3 + 1) % maxSeg }
                    val s3 = waypoints[seg3]; val e3 = waypoints[kotlin.math.min(seg3 + 1, maxSeg)]
                    val loc3 = LatLng(s3.latitude + (e3.latitude - s3.latitude) * prog3, s3.longitude + (e3.longitude - s3.longitude) * prog3)
                    val rem3 = rem2 + 11

                    val b1 = ActiveBus(baseBusId, loc1, rem1, kotlin.math.round((rem1 * 0.24) * 10) / 10.0, BusStatus.ON_TIME)
                    val b2 = ActiveBus(bus2Id, loc2, rem2, kotlin.math.round((rem2 * 0.24) * 10) / 10.0, BusStatus.DELAYED)
                    val b3 = ActiveBus(bus3Id, loc3, rem3, kotlin.math.round((rem3 * 0.24) * 10) / 10.0, BusStatus.ON_TIME)

                    val list = listOf(b1, b2, b3).sortedBy { it.etaMinutes }
                    _activeBuses.value = list
                    _busLocation.value = list.firstOrNull()?.location
                    _eta.value = list.firstOrNull()?.etaMinutes ?: rem1
                    _distance.value = list.firstOrNull()?.distanceKm ?: 1.2
                }
            }
        }
    }

    private fun fetchDirectionsForActiveRoute(route: com.example.livebus.data.TransitRoute) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val apiKey = com.example.livebus.BuildConfig.MAPS_API_KEY
                if (apiKey.isNotBlank() && !apiKey.contains("PASTE_YOUR")) {
                    val origin = "${route.initialBusLocation.latitude},${route.initialBusLocation.longitude}"
                    val dest = "${route.userStopLocation.latitude},${route.userStopLocation.longitude}"
                    val urlStr = "https://maps.googleapis.com/maps/api/directions/json?origin=$origin&destination=$dest&mode=driving&key=$apiKey"
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

    private fun subscribeToRouteTopic(topic: String) {
        routeDisposable?.dispose()
        routeDisposable = stompClient.topic(topic)
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
    }

    fun parseAndApplyMessage(payload: String) {
        try {
            val json = JSONObject(payload)
            val lat = json.getDouble("latitude")
            val lon = json.getDouble("longitude")
            val busId = json.optString("busId", transitRepository.getActiveRoute()?.busId ?: "TG-09-Z-4052")
            val eta = if (json.has("eta")) json.getInt("eta") else _eta.value
            val dist = if (json.has("distance")) json.getDouble("distance") else _distance.value
            val status = if (json.has("status")) BusStatus.valueOf(json.getString("status")) else BusStatus.ON_TIME

            val currentList = _activeBuses.value.toMutableList()
            val index = currentList.indexOfFirst { it.busId == busId }
            val newBus = ActiveBus(busId, LatLng(lat, lon), eta, dist, status)
            if (index >= 0) {
                currentList[index] = newBus
            } else {
                currentList.add(newBus)
            }
            val sortedList = currentList.sortedBy { it.etaMinutes }
            _activeBuses.value = sortedList
            _busLocation.value = sortedList.firstOrNull()?.location ?: LatLng(lat, lon)
            _eta.value = sortedList.firstOrNull()?.etaMinutes ?: eta
            _distance.value = sortedList.firstOrNull()?.distanceKm ?: dist
            _busStatus.value = sortedList.firstOrNull()?.status ?: status
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
