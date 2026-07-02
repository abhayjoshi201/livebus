package com.example.livebus.ui.itinerary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.livebus.ui.tracking.BusStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import javax.inject.Inject

data class Stop(
    val id: String,
    val name: String,
    val distanceKm: Double,
    val estimatedMinutesFromStart: Int
)

@HiltViewModel
class RouteViewModel @Inject constructor() : ViewModel() {

    private val stompClient: StompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, "ws://10.0.2.2:8080/transit-ws")
    private val compositeDisposable = CompositeDisposable()

    private val defaultStops = listOf(
        Stop("1", "Northgate Mall", 0.0, 0),
        Stop("2", "Market Junction", 3.2, 8),
        Stop("3", "Central Station", 6.8, 15),
        Stop("4", "State Library", 9.5, 21),
        Stop("5", "University Campus", 12.0, 28)
    )

    private val _stops = MutableStateFlow(defaultStops)
    val stops: StateFlow<List<Stop>> = _stops.asStateFlow()

    private val _routeName = MutableStateFlow("ROUTE 101-A")
    val routeName: StateFlow<String> = _routeName.asStateFlow()

    private val _destinationName = MutableStateFlow("University Campus")
    val destinationName: StateFlow<String> = _destinationName.asStateFlow()

    private val _totalEtaMinutes = MutableStateFlow(25)
    val totalEtaMinutes: StateFlow<Int> = _totalEtaMinutes.asStateFlow()

    private val _remainingDistanceKm = MutableStateFlow(12.0)
    val remainingDistanceKm: StateFlow<Double> = _remainingDistanceKm.asStateFlow()

    // Index 2 is Central Station by default (target stop)
    private val _currentBusStopIndex = MutableStateFlow(2)
    val currentBusStopIndex: StateFlow<Int> = _currentBusStopIndex.asStateFlow()

    private val _nextStopEtaMinutes = MutableStateFlow(5)
    val nextStopEtaMinutes: StateFlow<Int> = _nextStopEtaMinutes.asStateFlow()

    private val _busStatus = MutableStateFlow(BusStatus.ON_TIME)
    val busStatus: StateFlow<BusStatus> = _busStatus.asStateFlow()

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
                println("Error subscribing to STOMP topic in RouteViewModel: ${error.message}")
            })
        compositeDisposable.add(disposable)
    }

    fun parseAndApplyMessage(payload: String) {
        try {
            val json = JSONObject(payload)
            if (json.has("eta")) {
                val eta = json.getInt("eta")
                _nextStopEtaMinutes.value = eta
                _totalEtaMinutes.value = eta + 20
            }
            if (json.has("distance")) {
                val dist = json.getDouble("distance")
                _remainingDistanceKm.value = dist + 8.5
            }
            if (json.has("stopIndex")) {
                val idx = json.getInt("stopIndex")
                if (idx in 0 until _stops.value.size) {
                    _currentBusStopIndex.value = idx
                }
            }
            if (json.has("status")) {
                _busStatus.value = BusStatus.valueOf(json.getString("status"))
            }
        } catch (e: Exception) {
            println("Error parsing STOMP payload in RouteViewModel: ${e.message}")
        }
    }

    fun updateCurrentStopIndex(newIndex: Int) {
        if (newIndex in 0 until _stops.value.size) {
            _currentBusStopIndex.value = newIndex
        }
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        stompClient.disconnect()
        super.onCleared()
    }
}
