package com.example.livebus

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// 1. ROUTE SELECTION SCREEN
@Composable
fun RouteSelectionScreen(onNavigateNext: (String) -> Unit) {
    val availableRoutes = listOf("101-A (Downtown Express)", "42-B (Uptown Local)", "77-C (Crosstown)")
    var selectedRoute by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Select Route", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 24.dp, bottom = 24.dp))
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(availableRoutes) { route ->
                ElevatedCard(
                    onClick = { selectedRoute = route },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (selectedRoute == route) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DirectionsBus, contentDescription = null)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(route, fontSize = 18.sp)
                    }
                }
            }
        }

        Button(
            onClick = { selectedRoute?.let { onNavigateNext(it) } },
            enabled = selectedRoute != null,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Confirm Route", fontSize = 18.sp)
        }
    }
}

// 2. BUS SELECTION SCREEN
@Composable
fun BusSelectionScreen(onNavigateNext: (String) -> Unit) {
    var busNumber by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.DirectionsBus, contentDescription = null, modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Assign Vehicle", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = busNumber,
            onValueChange = { busNumber = it },
            label = { Text("Vehicle ID (e.g., 4052)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { onNavigateNext(busNumber) },
            enabled = busNumber.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Next: Review Shift", fontSize = 18.sp)
        }
    }
}

// 3. SHIFT CONFIRMATION SCREEN
@Composable
fun ShiftConfirmationScreen(route: String, busId: String, onStartShift: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp), 
        horizontalAlignment = Alignment.CenterHorizontally, 
        verticalArrangement = Arrangement.Center
    ) {
        Text("Shift Summary", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(32.dp))
        
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Assigned Route", color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
                Text(route, fontSize = 20.sp, modifier = Modifier.padding(bottom = 16.dp))
                
                Text("Vehicle ID", color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
                Text("Bus #$busId", fontSize = 20.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onStartShift, 
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
        ) {
            Text("START BROADCASTING", fontSize = 18.sp)
        }
    }
}


// 4. ACTIVE SHIFT SCREEN (Broadcasting)
@Composable
fun ActiveShiftScreen(route: String, busId: String, onEndTrip: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp), 
        horizontalAlignment = Alignment.CenterHorizontally, 
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.DirectionsBus, 
            contentDescription = "Active", 
            modifier = Modifier.size(80.dp), 
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Status: LIVE", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        Text("Broadcasting Location for Bus #$busId", color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(bottom = 8.dp))
        Text("Route: $route", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        
        Spacer(modifier = Modifier.height(64.dp))
        
        Button(
            onClick = onEndTrip, 
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error) // Red button!
        ) {
            Text("END TRIP", fontSize = 18.sp)
        }
    }
}

// 5. TRIP END STATUS SCREEN
@Composable
fun TripEndScreen(onReturnHome: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp), 
        horizontalAlignment = Alignment.CenterHorizontally, 
        verticalArrangement = Arrangement.Center
    ) {
        Text("Shift Completed", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        Text("Telemetry broadcasting stopped.", color = MaterialTheme.colorScheme.secondary)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onReturnHome, 
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Start New Shift", fontSize = 18.sp)
        }
    }
}