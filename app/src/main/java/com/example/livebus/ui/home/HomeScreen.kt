package com.example.livebus.ui.home

import com.example.livebus.R
import com.example.livebus.ui.common.BottomNavigationBar
import com.example.livebus.ui.theme.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToTracking: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onTicketsClick: () -> Unit = {},
    onAlertsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val sheetState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.Expanded
        )
    )
    var showLiveCommuteWidget by remember { mutableStateOf(false) } // Placeholder for live commute logic

    BottomSheetScaffold(
        scaffoldState = sheetState,
        sheetContent = {
            Scaffold(
                bottomBar = { 
                    BottomNavigationBar(
                        selectedTabIndex = 0,
                        onHomeClick = {},
                        onMapClick = onNavigateToTracking,
                        onTicketsClick = onTicketsClick,
                        onAlertsClick = onAlertsClick,
                        onSettingsClick = onSettingsClick
                    ) 
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                        .background(MaterialTheme.colorScheme.surface),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SearchBarSection(onSearchClick = onSearchClick)
                    Spacer(modifier = Modifier.height(16.dp))

                    ServiceAlertsBanner(onClick = onAlertsClick)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (showLiveCommuteWidget) {
                        LiveCommuteWidget()
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    QuickCommuteButtonsSection()
                    Spacer(modifier = Modifier.height(24.dp))

                    FavoriteRoutesSection()
                    Spacer(modifier = Modifier.height(24.dp))

                    PinnedStopsSection()
                    Spacer(modifier = Modifier.height(24.dp))

                    NearestStopsSection()
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        },
        sheetPeekHeight = 400.dp, // Optimized for thumb-zone
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetContainerColor = MaterialTheme.colorScheme.surface
    ) { paddingValues ->
        val isDarkTheme = isSystemInDarkTheme()
        val mapStyleUrl = if (isDarkTheme) {
            "https://api.maptiler.com/maps/darkmatter/style.json?key=YOUR_KEY"
        } else {
            "https://api.maptiler.com/maps/streets/style.json?key=YOUR_KEY"
        }
        // Map Placeholder
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Map Placeholder", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Map Style URL: $mapStyleUrl", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun SkeletonLoader(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(4.dp),
    color: Color = LightGray
) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeletonLoader")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha"
    )
    Box(
        modifier = modifier
            .background(color.copy(alpha = alpha), shape)
    )
}

@Composable
fun ServiceAlertsBanner(onClick: () -> Unit = {}) {
    var isDismissed by remember { mutableStateOf(false) }
    val isDarkTheme = isSystemInDarkTheme()
    val alertColor = if (isDarkTheme) DarkRed else LightRed

    if (!isDismissed) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable { onClick() },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = alertColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Severe Delay on Route 202-B • Tap for Advisories",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
                IconButton(onClick = { isDismissed = true }) {
                    Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun LiveCommuteWidget() {
    var isLoading by remember { mutableStateOf(true) } // Placeholder for loading state
    if (isLoading) {
        SkeletonLoader(modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = 16.dp))
    } else {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MutedBlue)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Live Commute to Work",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Route 101-A: 5 mins (On Time)",
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun SearchBarSection(onSearchClick: () -> Unit = {}) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .clickable { onSearchClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(12.dp))
            Text("Search routes or stops...", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            Icon(Icons.Default.Map, contentDescription = "Map/Filter", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun QuickCommuteButtonsSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        AssistChip(
            onClick = { /* TODO: Navigate to Home */ },
            label = { Text("🏠 Home") }
        )
        AssistChip(
            onClick = { /* TODO: Navigate to Work */ },
            label = { Text("🏢 Work") }
        )
        AssistChip(
            onClick = { /* TODO: Add Shortcut */ },
            label = { Text("+ Add Shortcut") }
        )
    }
}

@Composable
fun FavoriteRoutesSection(onRouteClick: () -> Unit = {}) {
    val isDarkTheme = isSystemInDarkTheme()
    val onTimeColor = if (isDarkTheme) DarkGreen else LightGreen
    val minorDelayColor = if (isDarkTheme) DarkAmber else LightAmber

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Favorite Routes",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(Icons.Default.ChevronRight, contentDescription = "See All", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            items(2) { index ->
                RouteCard(
                    routeName = "ROUTE 101-A",
                    destination = "City Center",
                    statusColor = onTimeColor,
                    onClick = onRouteClick
                )
            }
            item {
                RouteCard(
                    routeName = "ROUTE 202-B",
                    destination = "Uptown",
                    statusColor = minorDelayColor,
                    onClick = onRouteClick
                )
            }
        }
    }
}

@Composable
fun RouteCard(routeName: String, destination: String, statusColor: Color, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = statusColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = routeName,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = destination,
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun PinnedStopsSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Pinned Stops",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🚏 Central Station", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("🚌 101-A  Arriving in 5 min", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text("🚌 101-B  Arriving in 12 min", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun NearestStopsSection() {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Nearest Stops",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📍 Market Junction (0.2 km)", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("🚌 42-C   Arriving in 2 min", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun HomeScreenPreview() {
    // Wrap in a theme if you have one
    HomeScreen()
}
