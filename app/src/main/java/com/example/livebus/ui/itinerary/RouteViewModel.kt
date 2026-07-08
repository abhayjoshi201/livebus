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

import com.example.livebus.BuildConfig
import com.example.livebus.data.TransitRepository
import io.reactivex.disposables.Disposable

data class Stop(
    val id: String,
    val name: String,
    val distanceKm: Double,
    val estimatedMinutesFromStart: Int
)

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val transitRepository: TransitRepository
) : ViewModel() {

    private val stompClient: StompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, BuildConfig.WEBSOCKET_URL)
    private val compositeDisposable = CompositeDisposable()
    private var routeDisposable: Disposable? = null

    private val _stops = MutableStateFlow(transitRepository.getActiveRoute()?.stops ?: emptyList())
    val stops: StateFlow<List<Stop>> = _stops.asStateFlow()

    private val _routeName = MutableStateFlow(transitRepository.getActiveRoute()?.routeName ?: "No Route Selected")
    val routeName: StateFlow<String> = _routeName.asStateFlow()

    private val _destinationName = MutableStateFlow(transitRepository.getActiveRoute()?.destination ?: "Select a route from Plan Trip")
    val destinationName: StateFlow<String> = _destinationName.asStateFlow()

    private val _totalEtaMinutes = MutableStateFlow(25)
    val totalEtaMinutes: StateFlow<Int> = _totalEtaMinutes.asStateFlow()

    private val _remainingDistanceKm = MutableStateFlow(12.0)
    val remainingDistanceKm: StateFlow<Double> = _remainingDistanceKm.asStateFlow()

    private val _currentBusStopIndex = MutableStateFlow(2)
    val currentBusStopIndex: StateFlow<Int> = _currentBusStopIndex.asStateFlow()

    private val _nextStopEtaMinutes = MutableStateFlow(5)
    val nextStopEtaMinutes: StateFlow<Int> = _nextStopEtaMinutes.asStateFlow()

    private val _busStatus = MutableStateFlow(BusStatus.ON_TIME)
    val busStatus: StateFlow<BusStatus> = _busStatus.asStateFlow()

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
                    _stops.value = route.stops
                    _routeName.value = route.routeName
                    _destinationName.value = route.destination
                    _currentBusStopIndex.value = kotlin.math.min(2, route.stops.size - 1)
                    subscribeToRouteTopic(route.stompTopic)
                } else {
                    _stops.value = emptyList()
                    _routeName.value = "No Route Selected"
                    _destinationName.value = "${city.name} (${city.defaultLocationName})"
                    routeDisposable?.dispose()
                }
            }
        }
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
                println("Error subscribing to STOMP topic in RouteViewModel: ${error.message}")
            })
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
