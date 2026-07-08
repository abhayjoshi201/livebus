package com.example.livebus.ui.search

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import com.example.livebus.data.TransitRepository

class SearchViewModelTest {

    private lateinit var viewModel: SearchViewModel

    @Before
    fun setup() {
        viewModel = SearchViewModel(TransitRepository())
    }

    @Test
    fun initialState_isCorrect() {
        assertEquals("", viewModel.searchQuery.value)
        assertEquals(SearchFilter.ALL, viewModel.selectedFilter.value)
        assertEquals(0, viewModel.recentSearches.value.size)
        assertEquals(2, viewModel.suggestedRoutes.size)
        assertEquals(3, viewModel.nearbyStops.size)
    }

    @Test
    fun filterResults_matchesRoutesAndStops() {
        val resultsAll = viewModel.filterResults("D-1", SearchFilter.ALL)
        assertEquals(1, resultsAll.size)
        assertTrue(resultsAll[0] is SearchResult.RouteResult)

        val resultsStops = viewModel.filterResults("ISBT Terminal", SearchFilter.ALL)
        assertEquals(1, resultsStops.size)
        assertTrue(resultsStops[0] is SearchResult.StopResult)

        // Filter by STOPS only ignores routes matching query
        val resultsFiltered = viewModel.filterResults("Route", SearchFilter.STOPS)
        assertEquals(0, resultsFiltered.size)
    }

    @Test
    fun addRecentSearch_movesToTopAndLimitsSize() {
        viewModel.addRecentSearch("ISBT Terminal")
        assertEquals("ISBT Terminal", viewModel.recentSearches.value[0])
        
        viewModel.addRecentSearch("New Search Query")
        assertEquals("New Search Query", viewModel.recentSearches.value[0])
        assertEquals(2, viewModel.recentSearches.value.size)

        // Adding duplicate moves it to front
        viewModel.addRecentSearch("ISBT Terminal")
        assertEquals("ISBT Terminal", viewModel.recentSearches.value[0])
        assertEquals(2, viewModel.recentSearches.value.size)
    }

    @Test
    fun removeRecentSearch_deletesItem() {
        viewModel.addRecentSearch("ISBT Terminal")
        assertEquals(1, viewModel.recentSearches.value.size)
        
        viewModel.removeRecentSearch("ISBT Terminal")
        assertEquals(0, viewModel.recentSearches.value.size)
        assertTrue(viewModel.recentSearches.value.none { it == "ISBT Terminal" })
    }
}
