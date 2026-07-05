package com.example.livebus.data

import com.example.livebus.ui.itinerary.Stop
import com.example.livebus.ui.tracking.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class TransitRoute(
    val routeId: String,            // e.g. "216W"
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

    val allRoutes: Map<String, TransitRoute> = mapOf(
        "216W" to TransitRoute(
            routeId = "216W",
            routeName = "ROUTE 216W",
            displayName = "Route 216W",
            destination = "IIIT Gachibowli Campus",
            direction = "Westbound towards IT Corridor",
            busId = "TG-09-Z-4052",
            stompTopic = "/topic/route/216W",
            initialBusLocation = LatLng(17.3916, 78.4356), // Mehdipatnam
            userStopLocation = LatLng(17.4455, 78.3489),   // IIIT Gachibowli Campus
            waypoints = listOf(
                LatLng(17.3916, 78.4356), // Mehdipatnam Bus Depot
                LatLng(17.4018, 78.4111), // Tolichowki X Roads
                LatLng(17.4065, 78.3912), // Shaikpet Dargah
                LatLng(17.4242, 78.3816), // Raidurg Bio-Diversity X Roads
                LatLng(17.4401, 78.3611), // Gachibowli Stadium X Roads
                LatLng(17.4455, 78.3489)  // IIIT Hyderabad Campus
            ),
            stops = listOf(
                Stop("1", "Mehdipatnam Bus Depot", 0.0, 0),
                Stop("2", "Tolichowki X Roads", 3.2, 8),
                Stop("3", "Shaikpet Dargah", 6.8, 15),
                Stop("4", "Raidurg Bio-Diversity", 9.5, 21),
                Stop("5", "IIIT Gachibowli Campus", 12.0, 28)
            )
        ),
        "219" to TransitRoute(
            routeId = "219",
            routeName = "ROUTE 219",
            displayName = "Route 219",
            destination = "Patancheru Terminal",
            direction = "Northwest towards Patancheru",
            busId = "TG-11-Z-8821",
            stompTopic = "/topic/route/219",
            initialBusLocation = LatLng(17.4875, 78.3881), // KPHB Colony
            userStopLocation = LatLng(17.5287, 78.2667),   // Patancheru Bus Station
            waypoints = listOf(
                LatLng(17.4875, 78.3881), // KPHB Colony
                LatLng(17.4982, 78.3891), // JNTU College X Roads
                LatLng(17.5050, 78.3490), // Miyapur X Roads
                LatLng(17.5144, 78.3242), // BHEL Circle
                LatLng(17.5287, 78.2667)  // Patancheru Bus Station
            ),
            stops = listOf(
                Stop("101", "KPHB Colony Bus Stop", 0.0, 0),
                Stop("102", "JNTU College X Roads", 2.4, 6),
                Stop("103", "Miyapur X Roads", 6.1, 14),
                Stop("104", "BHEL Circle", 10.5, 22),
                Stop("105", "Patancheru Terminal", 15.2, 35)
            )
        ),
        "10H" to TransitRoute(
            routeId = "10H",
            routeName = "ROUTE 10H",
            displayName = "Route 10H",
            destination = "Secunderabad Station",
            direction = "Eastbound towards Secunderabad",
            busId = "TG-03-Z-1109",
            stompTopic = "/topic/route/10H",
            initialBusLocation = LatLng(17.4483, 78.3725), // Madhapur / Cyber Towers
            userStopLocation = LatLng(17.4399, 78.4983),   // Secunderabad Station
            waypoints = listOf(
                LatLng(17.4483, 78.3725), // Cyber Towers / Madhapur
                LatLng(17.4325, 78.4072), // Jubilee Hills Check Post
                LatLng(17.4375, 78.4483), // Ameerpet X Roads
                LatLng(17.4418, 78.4735), // Begumpet / Rasoolpura
                LatLng(17.4399, 78.4983)  // Secunderabad Station
            ),
            stops = listOf(
                Stop("201", "Cyber Towers Madhapur", 0.0, 0),
                Stop("202", "Jubilee Hills Check Post", 3.8, 10),
                Stop("203", "Ameerpet X Roads", 7.5, 18),
                Stop("204", "Begumpet Police Lines", 11.2, 26),
                Stop("205", "Secunderabad Station", 14.8, 36)
            )
        )
    )

    private val _activeRouteId = MutableStateFlow<String?>(null)
    val activeRouteId: StateFlow<String?> = _activeRouteId.asStateFlow()

    fun selectRoute(routeIdOrName: String) {
        val cleanedId = when {
            routeIdOrName.contains("219") -> "219"
            routeIdOrName.contains("10H") -> "10H"
            else -> "216W"
        }
        if (allRoutes.containsKey(cleanedId)) {
            _activeRouteId.value = cleanedId
        }
    }

    fun getActiveRoute(): TransitRoute? {
        return _activeRouteId.value?.let { allRoutes[it] }
    }
}
