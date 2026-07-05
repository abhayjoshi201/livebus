package com.example.livebus.ui.alerts

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.livebus.ui.theme.TransitColors
import java.util.UUID

enum class AlertSeverity {
    SEVERE,
    WARNING,
    INFO_RESOLVED
}

@Composable
fun AlertSeverity.statusColor(): Color {
    val colors = com.example.livebus.ui.theme.LiveBusTheme.transitColors
    return when (this) {
        AlertSeverity.SEVERE -> colors.severeDelay
        AlertSeverity.WARNING -> colors.delayed
        AlertSeverity.INFO_RESOLVED -> colors.onTime
    }
}

fun AlertSeverity.label(): String {
    return when (this) {
        AlertSeverity.SEVERE -> "🔴 SEVERE ALERT"
        AlertSeverity.WARNING -> "🟠 MINOR DELAY"
        AlertSeverity.INFO_RESOLVED -> "🟢 RESOLVED"
    }
}

data class SystemAlert(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val severity: AlertSeverity,
    val timestamp: String, // e.g., "Updated 2 mins ago"
    val affectedRoutes: List<String> = emptyList()
)

data class PersonalAlert(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val timestamp: String,
    val routeNumber: String? = null
)
