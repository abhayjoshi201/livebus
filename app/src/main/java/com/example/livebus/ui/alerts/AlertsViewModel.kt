package com.example.livebus.ui.alerts

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AlertsViewModel @Inject constructor() : ViewModel() {

    private val _systemAlerts = MutableStateFlow<List<SystemAlert>>(
        listOf(
            SystemAlert(
                id = "sys-1",
                title = "Route B-1 Suspended",
                description = "Due to heavy fog and minor landslide near Bhimtal Lake route, all inbound campus shuttle services are temporarily suspended. Expect major delays.",
                severity = AlertSeverity.SEVERE,
                timestamp = "Updated 2 mins ago",
                affectedRoutes = listOf("B-1")
            ),
            SystemAlert(
                id = "sys-2",
                title = "Weather Delay Advisory",
                description = "Heavy rain in Haldwani region is causing 10-15 minute delays across all campus shuttle routes.",
                severity = AlertSeverity.WARNING,
                timestamp = "Updated 1 hr ago",
                affectedRoutes = listOf("All Haldwani Lines")
            ),
            SystemAlert(
                id = "sys-3",
                title = "Route D-1 Resumed",
                description = "Standard schedule has resumed for the Clement Town shuttle. All transit lines are clear.",
                severity = AlertSeverity.INFO_RESOLVED,
                timestamp = "Updated 3 hrs ago",
                affectedRoutes = listOf("D-1")
            )
        )
    )
    val systemAlerts: StateFlow<List<SystemAlert>> = _systemAlerts.asStateFlow()

    private val _personalAlerts = MutableStateFlow<List<PersonalAlert>>(
        listOf(
            PersonalAlert(
                id = "pers-1",
                title = "Arrival Reminder",
                message = "Your Clement Town shuttle (Route D-1) is arriving at the Main Gate Stop in 5 minutes.",
                timestamp = "Just now",
                routeNumber = "D-1"
            ),
            PersonalAlert(
                id = "pers-2",
                title = "Service Change Notice",
                message = "The Bhimtal Campus pick-up point has moved 20 meters west due to parking lane maintenance.",
                timestamp = "Yesterday",
                routeNumber = null
            ),
            PersonalAlert(
                id = "pers-3",
                title = "Live Commute Alert",
                message = "Campus Express Shuttle D-2 has departed the City Center stop.",
                timestamp = "2 days ago",
                routeNumber = "D-2"
            )
        )
    )
    val personalAlerts: StateFlow<List<PersonalAlert>> = _personalAlerts.asStateFlow()

    fun dismissPersonalAlert(alertId: String) {
        _personalAlerts.value = _personalAlerts.value.filter { it.id != alertId }
    }

    fun clearAllPersonalAlerts() {
        _personalAlerts.value = emptyList()
    }

    fun refreshSystemAlerts() {
        // Simulates REST endpoint reload GET /api/v1/alerts
        val current = _systemAlerts.value
        _systemAlerts.value = current
    }
}
