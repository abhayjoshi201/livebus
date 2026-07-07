package com.example.livebus.data

import com.example.livebus.ui.itinerary.Stop
import com.example.livebus.ui.tracking.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class TransitCity(
    val id: String,                  // e.g., "HYD", "BLR", "MUM", "DEL", "PUNE"
    val name: String,                // e.g., "Hyderabad"
    val agencyName: String,          // e.g., "TGSRTC"
    val fullAgencyTitle: String,     // e.g., "Telangana State Road Transport Corporation"
    val centerLatLng: LatLng,        // Default camera center when no route is selected
    val defaultLocationName: String  // e.g., "Hyderabad IT Corridor"
)

data class TransitRoute(
    val routeId: String,            // e.g. "216W"
    val cityId: String,             // e.g. "HYD"
    val routeName: String,          // e.g. "ROUTE 216W"
    val displayName: String,        // e.g. "Route 216W"
    val destination: String,        // e.g. "IIIT Gachibowli Campus"
    val direction: String,          // e.g. "Westbound towards IT Corridor"
    val busId: String,              // e.g. "TG-09-Z-4052"
    val stompTopic: String,         // e.g. "/topic/route/216W"
    val initialBusLocation: LatLng, // e.g. Mehdipatnam
    val userStopLocation: LatLng,   // e.g. IIIT Campus
    val waypoints: List<LatLng>,    // Road intersection waypoints along corridor
    val stops: List<Stop>           // Bus stops
)

@Singleton
class TransitRepository @Inject constructor() {

    val allCities: List<TransitCity> = listOf(
        TransitCity(
            id = "DDN",
            name = "Dehradun",
            agencyName = "GEHU DDN",
            fullAgencyTitle = "GEHU Dehradun Campus Bus Service",
            centerLatLng = LatLng(30.2721, 78.0084),
            defaultLocationName = "GEHU Clement Town Campus"
        ),
        TransitCity(
            id = "BHT",
            name = "Bhimtal",
            agencyName = "GEHU BHT",
            fullAgencyTitle = "GEHU Bhimtal Campus Bus Service",
            centerLatLng = LatLng(29.3479, 79.5463),
            defaultLocationName = "GEHU Bhimtal Campus"
        ),
        TransitCity(
            id = "HLD",
            name = "Haldwani",
            agencyName = "GEHU HLD",
            fullAgencyTitle = "GEHU Haldwani Campus Bus Service",
            centerLatLng = LatLng(29.2181, 79.5130),
            defaultLocationName = "GEHU Haldwani Campus"
        )
    )

    private val _selectedCityId = MutableStateFlow("DDN")
    val selectedCityId: StateFlow<String> = _selectedCityId.asStateFlow()

    fun getSelectedCity(): TransitCity {
        return allCities.find { it.id == _selectedCityId.value } ?: allCities[0]
    }

    fun selectCity(cityId: String) {
        if (allCities.any { it.id == cityId }) {
            _selectedCityId.value = cityId
            _activeRouteId.value = null // Reset route selection when switching campuses
        }
    }

    val allRoutes: Map<String, TransitRoute> = mapOf(
        // DEHRADUN (DDN)
        "D-1" to TransitRoute(
            routeId = "D-1",
            cityId = "DDN",
            routeName = "ROUTE D-1",
            displayName = "Route D-1",
            destination = "GEHU Clement Town Campus",
            direction = "Clement Town Bus Service",
            busId = "UA-07-TA-2024",
            stompTopic = "/topic/route/D-1",
            initialBusLocation = LatLng(30.2872, 77.9984), // ISBT
            userStopLocation = LatLng(30.2700, 78.0075),   // GEHU Campus
            waypoints = DehradunRoutes.d1Waypoints,
            stops = listOf(
                Stop("1", "ISBT Terminal", 0.0, 0),
                Stop("2", "Turner Road Junction", 2.2, 5),
                Stop("3", "Subhash Nagar Chowk", 4.1, 10),
                Stop("4", "GEHU Clement Town Campus", 5.5, 15)
            )
        ),
        "D-2" to TransitRoute(
            routeId = "D-2",
            cityId = "DDN",
            routeName = "ROUTE D-2",
            displayName = "Route D-2",
            destination = "GEHU Clement Town Campus",
            direction = "Dehradun City Service",
            busId = "UA-07-TA-4050",
            stompTopic = "/topic/route/D-2",
            initialBusLocation = LatLng(30.3244, 78.0411), // Clock Tower
            userStopLocation = LatLng(30.2700, 78.0075),   // GEHU Campus
            waypoints = DehradunRoutes.d2Waypoints,
            stops = listOf(
                Stop("11", "Clock Tower", 0.0, 0),
                Stop("12", "Prince Chowk", 1.8, 6),
                Stop("13", "Saharanpur Road Crossing", 3.2, 10),
                Stop("14", "ISBT Dehradun", 5.8, 18),
                Stop("15", "GEHU Clement Town Campus", 8.2, 25)
            )
        ),
        // BHIMTAL (BHT)
        "B-1" to TransitRoute(
            routeId = "B-1",
            cityId = "BHT",
            routeName = "ROUTE B-1",
            displayName = "Route B-1",
            destination = "GEHU Bhimtal Campus",
            direction = "Haldwani-Bhimtal Hill Service",
            busId = "UA-04-TC-8821",
            stompTopic = "/topic/route/B-1",
            initialBusLocation = LatLng(29.2170, 79.5180), // Haldwani
            userStopLocation = LatLng(29.3510, 79.5480),   // Bhimtal Campus
            waypoints = BhimtalRoutes.b1Waypoints,
            stops = listOf(
                Stop("21", "Haldwani Bus Station", 0.0, 0),
                Stop("22", "Kathgodam Rly Station", 6.2, 14),
                Stop("23", "Ranibagh Toll Plaza", 10.1, 22),
                Stop("24", "Bhimtal Lake Crossing", 20.2, 40),
                Stop("25", "GEHU Bhimtal Campus", 22.0, 45)
            )
        ),
        // HALDWANI (HLD)
        "H-1" to TransitRoute(
            routeId = "H-1",
            cityId = "HLD",
            routeName = "ROUTE H-1",
            displayName = "Route H-1",
            destination = "GEHU Haldwani Campus",
            direction = "Lalkuan-Haldwani Connector",
            busId = "UA-04-TB-5340",
            stompTopic = "/topic/route/H-1",
            initialBusLocation = LatLng(29.0770, 79.5090), // Lalkuan
            userStopLocation = LatLng(29.2220, 79.5110),   // Haldwani Campus
            waypoints = HaldwaniRoutes.h1Waypoints,
            stops = listOf(
                Stop("31", "Lalkuan Junction", 0.0, 0),
                Stop("32", "Motahaldu Stop", 5.5, 10),
                Stop("33", "Bareilly Road Crossing", 9.1, 17),
                Stop("34", "GEHU Haldwani Campus", 10.5, 20)
            )
        )
    )

    private val _activeRouteId = MutableStateFlow<String?>(null)
    val activeRouteId: StateFlow<String?> = _activeRouteId.asStateFlow()

    fun selectRoute(routeIdOrName: String) {
        val cleanedId = when {
            routeIdOrName.contains("D-2") -> "D-2"
            routeIdOrName.contains("B-1") -> "B-1"
            routeIdOrName.contains("H-1") -> "H-1"
            else -> "D-1"
        }
        if (allRoutes.containsKey(cleanedId)) {
            _activeRouteId.value = cleanedId
            // Ensure city matches selected route
            val targetRoute = allRoutes[cleanedId]
            if (targetRoute != null && targetRoute.cityId != _selectedCityId.value) {
                _selectedCityId.value = targetRoute.cityId
            }
        }
    }

    fun getActiveRoute(): TransitRoute? {
        return _activeRouteId.value?.let { allRoutes[it] }
    }

    fun getRoutesForCurrentCity(): List<TransitRoute> {
        return allRoutes.values.filter { it.cityId == _selectedCityId.value }
    }
}
