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
            id = "HYD",
            name = "Hyderabad",
            agencyName = "TGSRTC",
            fullAgencyTitle = "Telangana State Road Transport Corporation",
            centerLatLng = LatLng(17.4455, 78.3489),
            defaultLocationName = "Hyderabad IT Corridor (Gachibowli)"
        ),
        TransitCity(
            id = "BLR",
            name = "Bengaluru",
            agencyName = "BMTC",
            fullAgencyTitle = "Bangalore Metropolitan Transport Corporation",
            centerLatLng = LatLng(12.9172, 77.6228),
            defaultLocationName = "Silk Board IT Corridor"
        ),
        TransitCity(
            id = "MUM",
            name = "Mumbai",
            agencyName = "BEST",
            fullAgencyTitle = "Brihanmumbai Electric Supply & Transport",
            centerLatLng = LatLng(18.9322, 72.8264),
            defaultLocationName = "Nariman Point / Marine Drive"
        ),
        TransitCity(
            id = "DEL",
            name = "Delhi-NCR",
            agencyName = "DTC / NCR",
            fullAgencyTitle = "Delhi Transport Corporation & NCR Metro",
            centerLatLng = LatLng(28.5245, 77.1855),
            defaultLocationName = "Mehrauli / Cyber Hub Corridor"
        ),
        TransitCity(
            id = "PUNE",
            name = "Pune",
            agencyName = "PMPML",
            fullAgencyTitle = "Pune Mahanagar Parivahan Mahamandal Ltd",
            centerLatLng = LatLng(18.5913, 73.7389),
            defaultLocationName = "Hinjewadi IT Park Phase 3"
        )
    )

    private val _selectedCityId = MutableStateFlow("HYD")
    val selectedCityId: StateFlow<String> = _selectedCityId.asStateFlow()

    fun getSelectedCity(): TransitCity {
        return allCities.find { it.id == _selectedCityId.value } ?: allCities[0]
    }

    fun selectCity(cityId: String) {
        if (allCities.any { it.id == cityId }) {
            _selectedCityId.value = cityId
            _activeRouteId.value = null // Reset route selection when switching national metro hubs
        }
    }

    val allRoutes: Map<String, TransitRoute> = mapOf(
        // HYDERABAD (HYD)
        "216W" to TransitRoute(
            routeId = "216W",
            cityId = "HYD",
            routeName = "ROUTE 216W",
            displayName = "Route 216W",
            destination = "IIIT Gachibowli Campus",
            direction = "Westbound towards IT Corridor",
            busId = "TG-09-Z-4052",
            stompTopic = "/topic/route/216W",
            initialBusLocation = LatLng(17.3916, 78.4356), // Mehdipatnam
            userStopLocation = LatLng(17.4455, 78.3489),   // IIIT Gachibowli Campus
            waypoints = listOf(
                LatLng(17.3916, 78.4356),
                LatLng(17.4018, 78.4111),
                LatLng(17.4065, 78.3912),
                LatLng(17.4242, 78.3816),
                LatLng(17.4401, 78.3611),
                LatLng(17.4455, 78.3489)
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
            cityId = "HYD",
            routeName = "ROUTE 219",
            displayName = "Route 219",
            destination = "Patancheru Terminal",
            direction = "Northwest towards Patancheru",
            busId = "TG-11-Z-8821",
            stompTopic = "/topic/route/219",
            initialBusLocation = LatLng(17.4875, 78.3881), // KPHB Colony
            userStopLocation = LatLng(17.5287, 78.2667),   // Patancheru Bus Station
            waypoints = listOf(
                LatLng(17.4875, 78.3881),
                LatLng(17.4982, 78.3891),
                LatLng(17.5050, 78.3490),
                LatLng(17.5144, 78.3242),
                LatLng(17.5287, 78.2667)
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
            cityId = "HYD",
            routeName = "ROUTE 10H",
            displayName = "Route 10H",
            destination = "Secunderabad Station",
            direction = "Eastbound towards Secunderabad",
            busId = "TG-03-Z-1109",
            stompTopic = "/topic/route/10H",
            initialBusLocation = LatLng(17.4483, 78.3725), // Madhapur / Cyber Towers
            userStopLocation = LatLng(17.4399, 78.4983),   // Secunderabad Station
            waypoints = listOf(
                LatLng(17.4483, 78.3725),
                LatLng(17.4325, 78.4072),
                LatLng(17.4375, 78.4483),
                LatLng(17.4418, 78.4735),
                LatLng(17.4399, 78.4983)
            ),
            stops = listOf(
                Stop("201", "Cyber Towers Madhapur", 0.0, 0),
                Stop("202", "Jubilee Hills Check Post", 3.8, 10),
                Stop("203", "Ameerpet X Roads", 7.5, 18),
                Stop("204", "Begumpet Police Lines", 11.2, 26),
                Stop("205", "Secunderabad Station", 14.8, 36)
            )
        ),
        // BENGALURU (BLR)
        "500-D" to TransitRoute(
            routeId = "500-D",
            cityId = "BLR",
            routeName = "ROUTE 500-D",
            displayName = "Route 500-D",
            destination = "Hebbal Bridge",
            direction = "Northbound Outer Ring Road Express",
            busId = "KA-57-F-2099",
            stompTopic = "/topic/route/500-D",
            initialBusLocation = LatLng(12.9172, 77.6228), // Central Silk Board
            userStopLocation = LatLng(13.0354, 77.5988),   // Hebbal Bridge
            waypoints = listOf(
                LatLng(12.9172, 77.6228), // Silk Board
                LatLng(12.9260, 77.6762), // Bellandur
                LatLng(12.9569, 77.7011), // Marathahalli Bridge
                LatLng(12.9935, 77.6611), // Tin Factory
                LatLng(13.0354, 77.5988)  // Hebbal Bridge
            ),
            stops = listOf(
                Stop("301", "Central Silk Board", 0.0, 0),
                Stop("302", "Bellandur Gate", 5.2, 14),
                Stop("303", "Marathahalli Bridge", 10.4, 26),
                Stop("304", "Tin Factory KR Puram", 16.1, 38),
                Stop("305", "Hebbal Bridge Terminal", 22.0, 52)
            )
        ),
        "KIA-8" to TransitRoute(
            routeId = "KIA-8",
            cityId = "BLR",
            routeName = "ROUTE KIA-8",
            displayName = "Route KIA-8",
            destination = "Kempegowda Airport T1",
            direction = "Airport Vayu Vajra AC Service",
            busId = "KA-57-A-8001",
            stompTopic = "/topic/route/KIA-8",
            initialBusLocation = LatLng(12.9698, 77.7499), // Whitefield TTMC
            userStopLocation = LatLng(13.1986, 77.7066),   // Bangalore Airport
            waypoints = listOf(
                LatLng(12.9698, 77.7499), // Whitefield
                LatLng(12.9982, 77.7564), // Hope Farm
                LatLng(13.0520, 77.7590), // Budigere X Roads
                LatLng(13.1986, 77.7066)  // Airport T1
            ),
            stops = listOf(
                Stop("311", "Whitefield TTMC", 0.0, 0),
                Stop("312", "Hope Farm Junction", 3.4, 10),
                Stop("313", "Budigere X Roads", 11.2, 28),
                Stop("314", "Kempegowda Airport T1", 34.5, 60)
            )
        ),
        // MUMBAI (MUM)
        "A-115" to TransitRoute(
            routeId = "A-115",
            cityId = "MUM",
            routeName = "ROUTE A-115",
            displayName = "Route A-115",
            destination = "Nariman Point / NCPA",
            direction = "Southbound Marine Drive Express",
            busId = "MH-01-DR-1150",
            stompTopic = "/topic/route/A-115",
            initialBusLocation = LatLng(18.9398, 72.8354), // CST Station
            userStopLocation = LatLng(18.9250, 72.8220),   // NCPA Nariman Point
            waypoints = listOf(
                LatLng(18.9398, 72.8354), // CST Station
                LatLng(18.9322, 72.8264), // Churchgate Station
                LatLng(18.9250, 72.8220)  // Nariman Point
            ),
            stops = listOf(
                Stop("401", "CST Station Terminus", 0.0, 0),
                Stop("402", "Churchgate Station", 2.1, 8),
                Stop("403", "Marine Drive Promenade", 3.2, 12),
                Stop("404", "NCPA Nariman Point", 4.5, 18)
            )
        ),
        "333" to TransitRoute(
            routeId = "333",
            cityId = "MUM",
            routeName = "ROUTE 333",
            displayName = "Route 333",
            destination = "Mahakali Caves",
            direction = "Eastbound Andheri Connector",
            busId = "MH-02-EE-3330",
            stompTopic = "/topic/route/333",
            initialBusLocation = LatLng(19.1197, 72.8464), // Andheri Station East
            userStopLocation = LatLng(19.1310, 72.8680),   // Mahakali Caves
            waypoints = listOf(
                LatLng(19.1197, 72.8464),
                LatLng(19.1240, 72.8550),
                LatLng(19.1310, 72.8680)
            ),
            stops = listOf(
                Stop("411", "Andheri Station East", 0.0, 0),
                Stop("412", "Chakala Metro X Roads", 2.2, 10),
                Stop("413", "Mahakali Caves Terminal", 4.8, 22)
            )
        ),
        // DELHI-NCR (DEL)
        "534" to TransitRoute(
            routeId = "534",
            cityId = "DEL",
            routeName = "ROUTE 534",
            displayName = "Route 534",
            destination = "Mehrauli Terminal",
            direction = "Southbound Ring Road Service",
            busId = "DL-1PC-5340",
            stompTopic = "/topic/route/534",
            initialBusLocation = LatLng(28.6502, 77.3152), // Anand Vihar ISBT
            userStopLocation = LatLng(28.5245, 77.1855),   // Mehrauli
            waypoints = listOf(
                LatLng(28.6502, 77.3152),
                LatLng(28.5800, 77.2500), // Ashram X Roads
                LatLng(28.5500, 77.2200), // AIIMS Crossing
                LatLng(28.5245, 77.1855)  // Mehrauli
            ),
            stops = listOf(
                Stop("501", "Anand Vihar ISBT", 0.0, 0),
                Stop("502", "Ashram X Roads", 10.5, 24),
                Stop("503", "AIIMS Crossing", 16.2, 38),
                Stop("504", "Mehrauli Terminal", 24.8, 55)
            )
        ),
        "419" to TransitRoute(
            routeId = "419",
            cityId = "DEL",
            routeName = "ROUTE 419",
            displayName = "Route 419",
            destination = "Old Delhi Railway Station",
            direction = "Northbound Central Corridor",
            busId = "DL-1PB-4190",
            stompTopic = "/topic/route/419",
            initialBusLocation = LatLng(28.5150, 77.2300), // Ambedkar Nagar
            userStopLocation = LatLng(28.6600, 77.2300),   // Old Delhi Station
            waypoints = listOf(
                LatLng(28.5150, 77.2300),
                LatLng(28.5800, 77.2300), // ITO
                LatLng(28.6600, 77.2300)  // Old Delhi Station
            ),
            stops = listOf(
                Stop("511", "Ambedkar Nagar Terminal", 0.0, 0),
                Stop("512", "Lajpat Nagar Crossing", 6.8, 16),
                Stop("513", "ITO X Roads", 14.2, 32),
                Stop("514", "Old Delhi Railway Station", 19.5, 45)
            )
        ),
        // PUNE (PUNE)
        "322" to TransitRoute(
            routeId = "322",
            cityId = "PUNE",
            routeName = "ROUTE 322",
            displayName = "Route 322",
            destination = "Hinjewadi IT Park Phase 3",
            direction = "Westbound IT Fast Connector",
            busId = "MH-12-RN-3220",
            stompTopic = "/topic/route/322",
            initialBusLocation = LatLng(18.5308, 73.8475), // Shivaji Nagar
            userStopLocation = LatLng(18.5913, 73.7389),   // Hinjewadi Phase 3
            waypoints = listOf(
                LatLng(18.5308, 73.8475), // Shivaji Nagar
                LatLng(18.5520, 73.8050), // Aundh
                LatLng(18.5850, 73.7500), // Wakad Bridge
                LatLng(18.5913, 73.7389)  // Hinjewadi Phase 3
            ),
            stops = listOf(
                Stop("601", "Shivaji Nagar Bus Stand", 0.0, 0),
                Stop("602", "University Circle Aundh", 4.2, 12),
                Stop("603", "Wakad Bridge Highway", 12.5, 28),
                Stop("604", "Hinjewadi IT Park Phase 3", 18.2, 42)
            )
        )
    )

    private val _activeRouteId = MutableStateFlow<String?>(null)
    val activeRouteId: StateFlow<String?> = _activeRouteId.asStateFlow()

    fun selectRoute(routeIdOrName: String) {
        val cleanedId = when {
            routeIdOrName.contains("219") -> "219"
            routeIdOrName.contains("10H") -> "10H"
            routeIdOrName.contains("500-D") -> "500-D"
            routeIdOrName.contains("KIA-8") -> "KIA-8"
            routeIdOrName.contains("A-115") -> "A-115"
            routeIdOrName.contains("333") -> "333"
            routeIdOrName.contains("534") -> "534"
            routeIdOrName.contains("419") -> "419"
            routeIdOrName.contains("322") -> "322"
            else -> "216W"
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
