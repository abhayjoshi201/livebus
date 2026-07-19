package com.example.livebus.ui.tracking

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LiveTrackingScreen(
    onBackClick: () -> Unit = {},
    viewModel: LiveTrackingViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.startSimulating()
    }

    val activeBuses by viewModel.activeBuses.collectAsState()
    val busLocation by viewModel.busLocation.collectAsState()
    val userStopLocation by viewModel.userStopLocation.collectAsState()
    val routeWaypoints by viewModel.routeWaypoints.collectAsState()
    val routeDetails by viewModel.routeDetails.collectAsState()
    val eta by viewModel.eta.collectAsState()
    val distance by viewModel.distance.collectAsState()
    val status by viewModel.busStatus.collectAsState()
    val isAlertActive by viewModel.isAlertActive.collectAsState()

    // WIMT Offline Linear Schematic State (Option A / Pillar 1)
    val isWimtLinearMode by viewModel.isWimtLinearMode.collectAsState()
    val offlineRoute by viewModel.offlineRoute.collectAsState()
    val offlineStages by viewModel.offlineStages.collectAsState()
    val offlineDistanceMeters by viewModel.offlineDistanceMeters.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (isWimtLinearMode) {
            // Z-Index 0: WIMT Zero-Cost 1D Linear Track Progress UI (Room DB powered)
            WIMTLinearRouteSchematic(
                route = offlineRoute,
                stages = offlineStages,
                currentDistanceMeters = offlineDistanceMeters,
                currentSpeedKmh = 48,
                isOfflineTriangulated = true,
                activePassengersCount = 14,
                onSwitchToMapView = { viewModel.toggleTrackingViewMode() },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Z-Index 0: 2D Map Layer (Google / MapLibre)
            GoogleMapLayer(
                busLocation = busLocation,
                userStopLocation = userStopLocation,
                routeWaypoints = routeWaypoints,
                locationName = routeDetails.direction,
                activeBuses = activeBuses,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Z-Index 1: Top Navigation Layer
        TopNavigationLayer(
            onBackClick = onBackClick
        )

        // Z-Index 2: Dynamic Bottom Sheet Layer (Only in 2D Map Mode)
        if (!isWimtLinearMode) {
            BottomSheetLayer(
                routeDetails = routeDetails,
                eta = eta,
                distance = distance,
                status = status,
                isAlertActive = isAlertActive,
                activeBuses = activeBuses,
                onAlertClick = { viewModel.toggleAlert() },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

