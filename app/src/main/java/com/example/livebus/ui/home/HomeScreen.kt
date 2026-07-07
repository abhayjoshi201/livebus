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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.CheckCircle
import com.example.livebus.data.TransitCity
import com.example.livebus.data.TransitRoute
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
    selectedCity: TransitCity = TransitCity("DDN", "Dehradun", "GEHU DDN", "GEHU Dehradun Campus Bus Service", com.example.livebus.ui.tracking.LatLng(30.2721, 78.0084), "GEHU Clement Town Campus"),
    allCities: List<TransitCity> = emptyList(),
    onCitySelect: (String) -> Unit = {},
    availableRoutes: List<TransitRoute> = emptyList(),
    onRouteClick: (String) -> Unit = {},
    onMapClick: () -> Unit = {},
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
    var showLiveCommuteWidget by remember { mutableStateOf(false) }
    var showCitySheet by remember { mutableStateOf(false) }

    if (showCitySheet) {
        ModalBottomSheet(
            onDismissRequest = { showCitySheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Select GEHU Campus",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Switching hubs updates live routes, stops, and map GPS boundaries.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                allCities.forEach { city ->
                    val isSelected = city.id == selectedCity.id
                    val bg = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(bg)
                            .clickable {
                                onCitySelect(city.id)
                                showCitySheet = false
                            }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "📍 ${city.name} • ${city.agencyName}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = contentColor
                            )
                            Text(
                                text = city.fullAgencyTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = contentColor.copy(alpha = 0.8f)
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    BottomSheetScaffold(
        scaffoldState = sheetState,
        sheetContent = {
            Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = { 
                    BottomNavigationBar(
                        selectedTabIndex = 0,
                        onHomeClick = {},
                        onMapClick = onMapClick,
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
                        .background(MaterialTheme.colorScheme.surface)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // City Selector Header Pill
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "National Transit Network",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                                    .clickable { showCitySheet = true }
                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📍 ${selectedCity.name} • ${selectedCity.agencyName}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Select City",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "⛰️ 3 Campuses",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

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

                    FavoriteRoutesSection(
                        routes = availableRoutes,
                        onRouteClick = onRouteClick,
                        onSeeAllClick = onSearchClick
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    PinnedStopsSection(
                        selectedCity = selectedCity,
                        routes = availableRoutes,
                        onStopClick = onRouteClick
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    NearestStopsSection(
                        selectedCity = selectedCity,
                        routes = availableRoutes,
                        onStopClick = onRouteClick
                    )
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
                    text = "Severe Delay on Route 219 • Tap for Advisories",
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
                    text = "Route 216W: 5 mins (On Time)",
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
fun FavoriteRoutesSection(
    routes: List<TransitRoute> = emptyList(),
    onRouteClick: (String) -> Unit = {},
    onSeeAllClick: () -> Unit = {}
) {
    val isDarkTheme = isSystemInDarkTheme()
    val onTimeColor = if (isDarkTheme) DarkGreen else LightGreen
    val minorDelayColor = if (isDarkTheme) DarkAmber else LightAmber

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { onSeeAllClick() }
                .padding(vertical = 6.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Suggested Routes",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "See All",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "See All Routes",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (routes.isEmpty()) {
            Text(
                text = "No routes available for this campus.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(end = 16.dp)
            ) {
                items(routes.size) { index ->
                    val route = routes[index]
                    RouteCard(
                        routeName = route.routeName,
                        destination = route.destination.take(16),
                        statusColor = if (index % 2 == 0) onTimeColor else minorDelayColor,
                        onClick = { onRouteClick(route.routeId) }
                    )
                }
            }
        }
    }
}

@Composable
fun RouteCard(routeName: String, destination: String, statusColor: Color, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = statusColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = routeName,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = destination,
                color = Color.White,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun PinnedStopsSection(
    selectedCity: TransitCity = TransitCity("DDN", "Dehradun", "GEHU DDN", "GEHU Dehradun Campus Bus Service", com.example.livebus.ui.tracking.LatLng(30.2721, 78.0084), "GEHU Clement Town Campus"),
    routes: List<TransitRoute> = emptyList(),
    onStopClick: (String) -> Unit = {}
) {
    val firstStop = routes.firstOrNull()?.stops?.firstOrNull()?.name ?: "${selectedCity.name} Central Depot"
    val firstRoute = routes.firstOrNull()?.routeId ?: "EXPRESS"
    val secondRoute = routes.getOrNull(1)?.routeId ?: "LOCAL"

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Pinned Stops (${selectedCity.name})",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onStopClick(firstRoute) },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("🚏 $firstStop", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("🚌 $firstRoute  Arriving in 5 min", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text("🚌 $secondRoute  Arriving in 12 min", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun NearestStopsSection(
    selectedCity: TransitCity = TransitCity("DDN", "Dehradun", "GEHU DDN", "GEHU Dehradun Campus Bus Service", com.example.livebus.ui.tracking.LatLng(30.2721, 78.0084), "GEHU Clement Town Campus"),
    routes: List<TransitRoute> = emptyList(),
    onStopClick: (String) -> Unit = {}
) {
    val secondStop = routes.firstOrNull()?.stops?.getOrNull(1)?.name ?: "${selectedCity.name} IT Junction"
    val firstRoute = routes.firstOrNull()?.routeId ?: "EXPRESS"

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Nearest Stops around ${selectedCity.defaultLocationName.take(20)}",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onStopClick(firstRoute) },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("📍 $secondStop (0.2 km)", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("🚌 $firstRoute   Arriving in 2 min", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}
