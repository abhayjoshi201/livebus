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

    val busLocation by viewModel.busLocation.collectAsState()
    val userStopLocation by viewModel.userStopLocation.collectAsState()
    val routeDetails by viewModel.routeDetails.collectAsState()
    val eta by viewModel.eta.collectAsState()
    val distance by viewModel.distance.collectAsState()
    val status by viewModel.busStatus.collectAsState()
    val isAlertActive by viewModel.isAlertActive.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Z-Index 0: Map Layer
        MapLibreLayer(
            busLocation = busLocation,
            userStopLocation = userStopLocation,
            modifier = Modifier.fillMaxSize()
        )

        // Z-Index 1: Top Navigation Layer
        TopNavigationLayer(
            onBackClick = onBackClick
        )

        // Z-Index 2: Dynamic Bottom Sheet Layer
        BottomSheetLayer(
            routeDetails = routeDetails,
            eta = eta,
            distance = distance,
            status = status,
            isAlertActive = isAlertActive,
            onAlertClick = { viewModel.toggleAlert() },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
