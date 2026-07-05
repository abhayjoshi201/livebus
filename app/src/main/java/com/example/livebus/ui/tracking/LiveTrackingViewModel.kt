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
    val routeName: String = "Route 101-A",
    val destination: String = "City Center",
    val direction: String = "Northbound"
)

@HiltViewModel
class LiveTrackingViewModel @Inject constructor() : ViewModel() {

    private val stompClient: StompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://10.0.2.2:8080/transit-ws")
    private val compositeDisposable = CompositeDisposable()

    // Default initial coordinates for realistic map startup
    private val _busLocation = MutableStateFlow<LatLng?>(LatLng(37.7749, -122.4194))
    val busLocation: StateFlow<LatLng?> = _busLocation.asStateFlow()

    private val _userStopLocation = MutableStateFlow(LatLng(37.7833, -122.4167))
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

    private fun connectStomp() {
        stompClient.connect()

        val disposable = stompClient.topic("/topic/route/101-A")
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
