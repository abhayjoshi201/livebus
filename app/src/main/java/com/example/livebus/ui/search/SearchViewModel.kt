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
        val arrivingRoutes: List<String>
    ) : SearchResult
}

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val transitRepository: com.example.livebus.data.TransitRepository
) : ViewModel() {

    private fun getAllRoutesList(): List<SearchResult.RouteResult> {
        return transitRepository.allRoutes.values.map { route ->
            SearchResult.RouteResult(
                id = route.routeId,
                routeNumber = route.displayName, // e.g. "Route D-1"
                destination = route.destination,
                status = "ON TIME"
            )
        }
    }

    private fun getAllStopsList(): List<SearchResult.StopResult> {
        return transitRepository.allRoutes.values.flatMap { route ->
            route.stops.map { stop ->
                SearchResult.StopResult(
                    id = stop.id,
                    stopName = stop.name,
                    distanceKm = stop.estimatedMinutesFromStart * 0.1,
                    arrivingRoutes = listOf("${route.displayName} (${stop.estimatedMinutesFromStart} min)")
                )
            }
        }.groupBy { it.stopName }
         .map { (name, group) ->
             SearchResult.StopResult(
                 id = group.first().id,
                 stopName = name,
                 distanceKm = group.first().distanceKm,
                 arrivingRoutes = group.flatMap { it.arrivingRoutes }.distinct()
             )
         }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(SearchFilter.ALL)
    val selectedFilter: StateFlow<SearchFilter> = _selectedFilter.asStateFlow()

    private val _recentSearches = MutableStateFlow(listOf<String>())
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    val suggestedRoutes: List<SearchResult.RouteResult>
        get() = transitRepository.getRoutesForCurrentCity().map { route ->
            SearchResult.RouteResult(
                id = route.routeId,
                routeNumber = route.displayName,
                destination = route.destination,
                status = "ON TIME"
            )
        }

    val nearbyStops: List<SearchResult.StopResult>
        get() = transitRepository.getRoutesForCurrentCity().flatMap { route ->
            route.stops.map { stop ->
                SearchResult.StopResult(
                    id = stop.id,
                    stopName = stop.name,
                    distanceKm = 0.5,
                    arrivingRoutes = listOf("${route.displayName} (${stop.estimatedMinutesFromStart} min)")
                )
            }
        }.groupBy { it.stopName }
         .map { (name, group) ->
             SearchResult.StopResult(
                 id = group.first().id,
                 stopName = name,
                 distanceKm = group.first().distanceKm,
                 arrivingRoutes = group.flatMap { it.arrivingRoutes }.distinct()
             )
         }.take(3)

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
            getAllRoutesList().filter { 
                it.routeNumber.lowercase().contains(lower) || it.destination.lowercase().contains(lower) 
            }
        } else emptyList()

        val stops = if (filter == SearchFilter.ALL || filter == SearchFilter.STOPS) {
            getAllStopsList().filter { 
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
