package com.example.livebus.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import javax.inject.Inject

enum class SearchFilter {
    ALL,
    ROUTES,
    STOPS
}

sealed interface SearchResult {
    data class RouteResult(
        val id: String,
        val routeNumber: String,
        val destination: String,
        val status: String = "ON TIME"
    ) : SearchResult

    data class StopResult(
        val id: String,
        val stopName: String,
        val distanceKm: Double,
        val arrivingRoutes: List<String> = listOf("101-A (5 min)", "204-B (12 min)")
    ) : SearchResult
}

@HiltViewModel
class SearchViewModel @Inject constructor() : ViewModel() {

    private val allRoutes = listOf(
        SearchResult.RouteResult("r1", "Route 101-A", "Towards City Center", "ON TIME"),
        SearchResult.RouteResult("r2", "Route 204-B", "Towards Uptown", "DELAYED"),
        SearchResult.RouteResult("r3", "Route 305-C", "Towards Northgate Mall", "ON TIME"),
        SearchResult.RouteResult("r4", "Route 42-C", "Towards Airport Terminal", "ON TIME"),
        SearchResult.RouteResult("r5", "Route 15-X", "Towards University Campus", "SEVERE DELAY")
    )

    private val allStops = listOf(
        SearchResult.StopResult("s1", "Central Station", 0.8, listOf("101-A (5 min)", "204-B (12 min)")),
        SearchResult.StopResult("s2", "Market Junction", 0.2, listOf("42-C (2 min)", "101-A (8 min)")),
        SearchResult.StopResult("s3", "Northgate Mall", 4.5, listOf("305-C (3 min)")),
        SearchResult.StopResult("s4", "State Library", 1.8, listOf("15-X (10 min)", "101-A (15 min)")),
        SearchResult.StopResult("s5", "University Campus", 3.1, listOf("15-X (4 min)", "204-B (9 min)"))
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(SearchFilter.ALL)
    val selectedFilter: StateFlow<SearchFilter> = _selectedFilter.asStateFlow()

    private val _recentSearches = MutableStateFlow(listOf("Central Station", "Route 101-A", "Market Junction"))
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    val suggestedRoutes: List<SearchResult.RouteResult> = allRoutes.take(2)
    val nearbyStops: List<SearchResult.StopResult> = allStops.take(2)

    @OptIn(FlowPreview::class)
    val searchResults: StateFlow<List<SearchResult>> = combine(
        _searchQuery.debounce(300),
        _selectedFilter
    ) { query, filter ->
        filterResults(query, filter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun filterResults(query: String, filter: SearchFilter): List<SearchResult> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            return emptyList()
        }
        val lower = trimmed.lowercase()
        val routes = if (filter == SearchFilter.ALL || filter == SearchFilter.ROUTES) {
            allRoutes.filter { 
                it.routeNumber.lowercase().contains(lower) || it.destination.lowercase().contains(lower) 
            }
        } else emptyList()

        val stops = if (filter == SearchFilter.ALL || filter == SearchFilter.STOPS) {
            allStops.filter { 
                it.stopName.lowercase().contains(lower) 
            }
        } else emptyList()

        return routes + stops
    }

    fun onQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onFilterSelect(filter: SearchFilter) {
        _selectedFilter.value = filter
    }

    fun removeRecentSearch(item: String) {
        _recentSearches.value = _recentSearches.value.filter { it != item }
    }

    fun addRecentSearch(item: String) {
        val current = _recentSearches.value.toMutableList()
        current.remove(item)
        current.add(0, item)
        _recentSearches.value = current.take(5)
    }
}
