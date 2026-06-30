package com.example.livebus

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusTrackingScreen(viewModel: BusViewModel) {
    val locationData by viewModel.locationData.collectAsState()
    val busId by viewModel.busId.collectAsState()
    val eta by viewModel.eta.collectAsState()
    val distance by viewModel.distance.collectAsState()

    val scaffoldState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Bus ID: ${busId ?: "N/A"}")
                Text(text = "ETA: ${eta ?: "N/A"}")
                Text(text = "Distance: ${distance ?: "N/A"}")
            }
        },
        sheetPeekHeight = 128.dp
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray)
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            locationData?.let {
                Text(text = "Lat: ${it.latitude}, Lon: ${it.longitude}")
            } ?: Text(text = "Location: N/A")
        }
    }
}
