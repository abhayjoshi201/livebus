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
        val arrivingRoutes: List<String> = listOf("216W (5 min)", "219 (12 min)")
    ) : SearchResult
}

@HiltViewModel
class SearchViewModel @Inject constructor() : ViewModel() {

    private val allRoutes = listOf(
        SearchResult.RouteResult("r1", "Route 216W", "Towards IIIT Gachibowli", "ON TIME"),
        SearchResult.RouteResult("r2", "Route 219", "Towards Patancheru", "DELAYED"),
        SearchResult.RouteResult("r3", "Route 10H", "Towards Secunderabad Station", "ON TIME"),
        SearchResult.RouteResult("r4", "Route 47L", "Towards RGIA Shamshabad Airport", "ON TIME"),
        SearchResult.RouteResult("r5", "Route 222A", "Towards Lingampally", "SEVERE DELAY")
    )

    private val allStops = listOf(
        SearchResult.StopResult("s1", "Mehdipatnam Bus Depot", 0.8, listOf("216W (5 min)", "219 (12 min)")),
        SearchResult.StopResult("s2", "Tolichowki X Roads", 0.2, listOf("47L (2 min)", "216W (8 min)")),
        SearchResult.StopResult("s3", "Secunderabad Station", 4.5, listOf("10H (3 min)")),
        SearchResult.StopResult("s4", "Raidurg Bio-Diversity", 1.8, listOf("222A (10 min)", "216W (15 min)")),
        SearchResult.StopResult("s5", "IIIT Gachibowli Campus", 3.1, listOf("222A (4 min)", "219 (9 min)"))
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(SearchFilter.ALL)
    val selectedFilter: StateFlow<SearchFilter> = _selectedFilter.asStateFlow()

    private val _recentSearches = MutableStateFlow(listOf("Mehdipatnam Bus Depot", "Route 216W", "Tolichowki X Roads"))
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
