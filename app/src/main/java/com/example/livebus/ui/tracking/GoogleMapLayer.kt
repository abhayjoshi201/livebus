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
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()

    val stopLatLng = remember(userStopLocation) {
        com.google.android.gms.maps.model.LatLng(userStopLocation.latitude, userStopLocation.longitude)
    }
    val busLatLng = remember(busLocation) {
        busLocation?.let { com.google.android.gms.maps.model.LatLng(it.latitude, it.longitude) }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(busLatLng ?: stopLatLng, 14f)
    }

    // Automatically adjust camera bounds when vehicle coordinates update
    LaunchedEffect(busLatLng, stopLatLng) {
        if (busLatLng != null) {
            try {
                val bounds = LatLngBounds.builder()
                    .include(busLatLng)
                    .include(stopLatLng)
                    .build()
                cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(bounds, 150))
            } catch (e: Exception) {
                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(busLatLng, 14f))
            }
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
        // User Stop Marker
        Marker(
            state = MarkerState(position = stopLatLng),
            title = "User Stop: University Campus",
            snippet = "Assigned Boarding Location",
            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
        )

        // Live Bus Marker & Route Polyline
        if (busLatLng != null) {
            Marker(
                state = MarkerState(position = busLatLng),
                title = "Live Bus #BUS-4052",
                snippet = "Route 101-A • Active Telemetry",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
            )

            Polyline(
                points = listOf(busLatLng, stopLatLng),
                color = Color(0xFF2E7D32),
                width = 16f
            )
        }
    }
}
