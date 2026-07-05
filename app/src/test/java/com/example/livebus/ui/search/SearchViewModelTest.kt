package com.example.livebus.ui.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SearchViewModelTest {

    private lateinit var viewModel: SearchViewModel

    @Before
    fun setup() {
        viewModel = SearchViewModel()
    }

    @Test
    fun initialState_isCorrect() {
        assertEquals("", viewModel.searchQuery.value)
        assertEquals(SearchFilter.ALL, viewModel.selectedFilter.value)
        assertEquals(3, viewModel.recentSearches.value.size)
        assertEquals(2, viewModel.suggestedRoutes.size)
        assertEquals(2, viewModel.nearbyStops.size)
    }

    @Test
    fun filterResults_matchesRoutesAndStops() {
        val resultsAll = viewModel.filterResults("216", SearchFilter.ALL)
        assertEquals(1, resultsAll.size)
        assertTrue(resultsAll[0] is SearchResult.RouteResult)

        val resultsStops = viewModel.filterResults("Mehdipatnam", SearchFilter.ALL)
        assertEquals(1, resultsStops.size)
        assertTrue(resultsStops[0] is SearchResult.StopResult)

        // Filter by STOPS only ignores routes matching query
        val resultsFiltered = viewModel.filterResults("Route", SearchFilter.STOPS)
        assertEquals(0, resultsFiltered.size)
    }

    @Test
    fun addRecentSearch_movesToTopAndLimitsSize() {
        assertEquals("Mehdipatnam Bus Depot", viewModel.recentSearches.value[0])
        viewModel.addRecentSearch("New Search Query")
        assertEquals("New Search Query", viewModel.recentSearches.value[0])
        assertEquals(4, viewModel.recentSearches.value.size)

        // Adding duplicate moves it to front
        viewModel.addRecentSearch("Route 216W")
        assertEquals("Route 216W", viewModel.recentSearches.value[0])
        assertEquals(4, viewModel.recentSearches.value.size)
    }

    @Test
    fun removeRecentSearch_deletesItem() {
        viewModel.removeRecentSearch("Mehdipatnam Bus Depot")
        assertEquals(2, viewModel.recentSearches.value.size)
        assertTrue(viewModel.recentSearches.value.none { it == "Mehdipatnam Bus Depot" })
    }
}
