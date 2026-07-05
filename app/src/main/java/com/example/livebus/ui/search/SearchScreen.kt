package com.example.livebus.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBackClick: () -> Unit = {},
    onRouteSelect: (String) -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    var selectedStopForSheet by remember { mutableStateOf<SearchResult.StopResult?>(null) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (e: Exception) {
            // Ignore focus request error if view not attached yet
        }
    }

    if (selectedStopForSheet != null) {
        StopArrivalsBottomSheet(
            stop = selectedStopForSheet!!,
            onDismiss = { selectedStopForSheet = null },
            onRouteClick = { routeStr ->
                selectedStopForSheet = null
                val routeNum = routeStr.substringBefore(" (")
                viewModel.addRecentSearch(routeNum)
                onRouteSelect(routeNum)
            }
        )
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onQueryChange(it) },
                        placeholder = { 
                            Text("Search routes, stops, places...", color = MaterialTheme.colorScheme.onSurfaceVariant) 
                        },
                        leadingIcon = { 
                            Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) 
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onQueryChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(50),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Filter Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedFilter == SearchFilter.ALL,
                            onClick = { viewModel.onFilterSelect(SearchFilter.ALL) },
                            label = { Text("All", fontWeight = FontWeight.Bold) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == SearchFilter.ROUTES,
                            onClick = { viewModel.onFilterSelect(SearchFilter.ROUTES) },
                            label = { Text("Routes", fontWeight = FontWeight.Bold) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == SearchFilter.STOPS,
                            onClick = { viewModel.onFilterSelect(SearchFilter.STOPS) },
                            label = { Text("Stops", fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            if (searchQuery.isEmpty()) {
                // RECENT SEARCHES
                if (recentSearches.isNotEmpty()) {
                    item {
                        SectionHeader("RECENT SEARCHES")
                    }
                    items(recentSearches) { item ->
                        RecentSearchItem(
                            text = item,
                            onSelect = {
                                viewModel.onQueryChange(item)
                            },
                            onDelete = {
                                viewModel.removeRecentSearch(item)
                            }
                        )
                    }
                }

                // SUGGESTED ROUTES
                item {
                    SectionHeader("SUGGESTED ROUTES")
                }
                items(viewModel.suggestedRoutes) { route ->
                    RouteResultItem(
                        route = route,
                        onClick = {
                            viewModel.addRecentSearch(route.routeNumber)
                            onRouteSelect(route.routeNumber)
                        }
                    )
                }

                // NEARBY STOPS
                item {
                    SectionHeader("NEARBY STOPS")
                }
                items(viewModel.nearbyStops) { stop ->
                    StopResultItem(
                        stop = stop,
                        onClick = {
                            viewModel.addRecentSearch(stop.stopName)
                            selectedStopForSheet = stop
                        }
                    )
                }
            } else {
                // SEARCH RESULTS
                if (searchResults.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No results found for \"$searchQuery\"",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(searchResults) { result ->
                        when (result) {
                            is SearchResult.RouteResult -> {
                                RouteResultItem(
                                    route = result,
                                    onClick = {
                                        viewModel.addRecentSearch(result.routeNumber)
                                        onRouteSelect(result.routeNumber)
                                    }
                                )
                            }
                            is SearchResult.StopResult -> {
                                StopResultItem(
                                    stop = result,
                                    onClick = {
                                        viewModel.addRecentSearch(result.stopName)
                                        selectedStopForSheet = result
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}
