package com.example.livebus.ui.itinerary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.livebus.ui.theme.statusColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteItineraryScreen(
    onBackClick: () -> Unit = {},
    onViewMapClick: () -> Unit = {},
    viewModel: RouteViewModel = hiltViewModel()
) {
    val stops by viewModel.stops.collectAsState()
    val routeName by viewModel.routeName.collectAsState()
    val destinationName by viewModel.destinationName.collectAsState()
    val totalEtaMinutes by viewModel.totalEtaMinutes.collectAsState()
    val remainingDistanceKm by viewModel.remainingDistanceKm.collectAsState()
    val currentBusStopIndex by viewModel.currentBusStopIndex.collectAsState()
    val nextStopEta by viewModel.nextStopEtaMinutes.collectAsState()
    val preferredStopId by viewModel.preferredStopId.collectAsState()
    val status by viewModel.busStatus.collectAsState()

    val statusColor = status.statusColor()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = routeName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onViewMapClick,
                containerColor = statusColor,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = "Map View"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "VIEW LIVE ON MAP",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            if (stops.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "📍 Your Location: $destinationName",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No route is currently selected.\nChoose a route or destination from 'Plan Trip' to view stop itineraries and ETA countdowns!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                // Summary Header Section
                SummaryHeaderSection(
                    totalEtaMinutes = totalEtaMinutes,
                    destinationName = destinationName,
                    remainingDistanceKm = remainingDistanceKm,
                    statusColor = statusColor
                )

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
                    thickness = 1.dp
                )

                // Vertical Timeline Section
                VerticalTimeline(
                    stops = stops,
                    currentBusStopIndex = currentBusStopIndex,
                    nextStopEta = nextStopEta,
                    status = status,
                    preferredStopId = preferredStopId,
                    onStopClick = { stop ->
                        viewModel.selectPreferredStop(stop.id)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun SummaryHeaderSection(
    totalEtaMinutes: Int,
    destinationName: String,
    remainingDistanceKm: Double,
    statusColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = statusColor.copy(alpha = 0.1f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ETA to destination",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "($destinationName)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$totalEtaMinutes",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "min",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Text(
                    text = "${String.format("%.1f", remainingDistanceKm)} km remaining",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
