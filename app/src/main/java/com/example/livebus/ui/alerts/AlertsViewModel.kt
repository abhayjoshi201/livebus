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
                title = "Route 216W Suspended",
                description = "Due to heavy waterlogging at Tolichowki X Roads, all inbound buses are halted. Expect 45m delays.",
                severity = AlertSeverity.SEVERE,
                timestamp = "Updated 2 mins ago",
                affectedRoutes = listOf("216W")
            ),
            SystemAlert(
                id = "sys-2",
                title = "General Weather Warning",
                description = "Heavy monsoon rain is causing 10-15 min delays across all IT Corridor / Gachibowli routes.",
                severity = AlertSeverity.WARNING,
                timestamp = "Updated 1 hr ago",
                affectedRoutes = listOf("All Cyberabad Lines")
            ),
            SystemAlert(
                id = "sys-3",
                title = "Route 219 Resumed",
                description = "Standard schedule has resumed. No ongoing delays reported towards Patancheru.",
                severity = AlertSeverity.INFO_RESOLVED,
                timestamp = "Updated 3 hrs ago",
                affectedRoutes = listOf("219")
            )
        )
    )
    val systemAlerts: StateFlow<List<SystemAlert>> = _systemAlerts.asStateFlow()

    private val _personalAlerts = MutableStateFlow<List<PersonalAlert>>(
        listOf(
            PersonalAlert(
                id = "pers-1",
                title = "Arrival Reminder",
                message = "Your bus on Route 216W is arriving at Mehdipatnam Depot in 5 minutes.",
                timestamp = "Just now",
                routeNumber = "216W"
            ),
            PersonalAlert(
                id = "pers-2",
                title = "Service Change Notice",
                message = "Your favorited stop Tolichowki X Roads has moved 50 meters north due to flyover construction.",
                timestamp = "Yesterday",
                routeNumber = null
            ),
            PersonalAlert(
                id = "pers-3",
                title = "Live Commute Alert",
                message = "Express bus 204-B departed Uptown 3 minutes ahead of schedule.",
                timestamp = "2 days ago",
                routeNumber = "204-B"
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
