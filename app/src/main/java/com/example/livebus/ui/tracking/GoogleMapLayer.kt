package com.example.livebus.ui.tracking

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*

@Composable
fun GoogleMapLayer(
    busLocation: LatLng?,
    userStopLocation: LatLng,
    routeWaypoints: List<LatLng> = emptyList(),
    locationName: String = "Hyderabad IT Corridor",
    activeBuses: List<ActiveBus> = emptyList(),
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()

    val stopLatLng = remember(userStopLocation) {
        com.google.android.gms.maps.model.LatLng(userStopLocation.latitude, userStopLocation.longitude)
    }
    val busLatLng = remember(busLocation) {
        busLocation?.let { com.google.android.gms.maps.model.LatLng(it.latitude, it.longitude) }
    }
    val waypointLatLngs = remember(routeWaypoints) {
        routeWaypoints.map { com.google.android.gms.maps.model.LatLng(it.latitude, it.longitude) }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(busLatLng ?: stopLatLng, 14f)
    }

    // Automatically adjust camera bounds to frame street corridor and vehicles
    LaunchedEffect(busLatLng, stopLatLng, waypointLatLngs, activeBuses) {
        if (activeBuses.isNotEmpty() || busLatLng != null) {
            try {
                val boundsBuilder = LatLngBounds.builder()
                boundsBuilder.include(stopLatLng)
                activeBuses.forEach {
                    boundsBuilder.include(com.google.android.gms.maps.model.LatLng(it.location.latitude, it.location.longitude))
                }
                if (busLatLng != null) boundsBuilder.include(busLatLng)
                waypointLatLngs.forEach { boundsBuilder.include(it) }
                val bounds = boundsBuilder.build()
                cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 150))
            } catch (e: Exception) {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(busLatLng ?: stopLatLng, 14f))
            }
        } else {
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(stopLatLng, 14f))
        }
    }

    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = true,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false
        )
    }
    val properties = remember {
        MapProperties(
            isTrafficEnabled = true,
            mapType = MapType.NORMAL
        )
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        uiSettings = uiSettings,
        properties = properties
    ) {
        // User Location Marker
        Marker(
            state = MarkerState(position = stopLatLng),
            title = if (activeBuses.isNotEmpty() || busLatLng != null) "User Stop: $locationName" else "Your Location: $locationName",
            snippet = if (activeBuses.isNotEmpty() || busLatLng != null) "Assigned Boarding Location" else "Select a route from Plan Trip to view buses",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
        )

        // Live Bus Markers & Road Polyline Corridor
        if (activeBuses.isNotEmpty()) {
            activeBuses.forEachIndexed { index, bus ->
                val bLatLng = remember(bus.location) {
                    com.google.android.gms.maps.model.LatLng(bus.location.latitude, bus.location.longitude)
                }
                Marker(
                    state = MarkerState(position = bLatLng),
                    title = if (index == 0) "🟢 Next Bus #${bus.busId}" else "🟡 Following #${bus.busId}",
                    snippet = "ETA: ${bus.etaMinutes} min • ${bus.status.name.replace("_", " ")}",
                    icon = BitmapDescriptorFactory.defaultMarker(
                        if (index == 0) BitmapDescriptorFactory.HUE_GREEN else BitmapDescriptorFactory.HUE_YELLOW
                    )
                )
            }

            Polyline(
                points = if (waypointLatLngs.isNotEmpty()) waypointLatLngs else listOf(com.google.android.gms.maps.model.LatLng(activeBuses[0].location.latitude, activeBuses[0].location.longitude), stopLatLng),
                color = Color(0xFF2E7D32),
                width = 16f
            )
        } else if (busLatLng != null) {
            Marker(
                state = MarkerState(position = busLatLng),
                title = "Live Bus #TG-09-Z-4052",
                snippet = "Active Telemetry",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            )

            Polyline(
                points = if (waypointLatLngs.isNotEmpty()) waypointLatLngs else listOf(busLatLng, stopLatLng),
                color = Color(0xFF2E7D32),
                width = 16f
            )
        }
    }
}
