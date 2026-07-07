package com.example.livebus.ui.itinerary

import com.example.livebus.ui.tracking.BusStatus
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RouteViewModelTest {

    private lateinit var viewModel: RouteViewModel

    @Before
    fun setup() {
        val repo = com.example.livebus.data.TransitRepository()
        repo.selectRoute("D-1")
        viewModel = RouteViewModel(repo)
    }

    @Test
    fun initialState_isCorrect() {
        assertEquals(4, viewModel.stops.value.size)
        assertEquals(2, viewModel.currentBusStopIndex.value)
        assertEquals("ROUTE D-1", viewModel.routeName.value)
        assertEquals(25, viewModel.totalEtaMinutes.value)
    }

    @Test
    fun updateCurrentStopIndex_updatesWhenWithinBounds() {
        assertEquals(2, viewModel.currentBusStopIndex.value)
        viewModel.updateCurrentStopIndex(3)
        assertEquals(3, viewModel.currentBusStopIndex.value)
        // Ignored out of bounds
        viewModel.updateCurrentStopIndex(4)
        assertEquals(3, viewModel.currentBusStopIndex.value)
    }

    @Test
    fun parseAndApplyMessage_updatesItineraryState() {
        val jsonPayload = """
            {
                "eta": 8,
                "distance": 4.2,
                "stopIndex": 3,
                "status": "DELAYED"
            }
        """.trimIndent()

        viewModel.parseAndApplyMessage(jsonPayload)

        assertEquals(8, viewModel.nextStopEtaMinutes.value)
        assertEquals(28, viewModel.totalEtaMinutes.value) // 8 + 20
        assertEquals(12.7, viewModel.remainingDistanceKm.value, 0.001) // 4.2 + 8.5
        assertEquals(3, viewModel.currentBusStopIndex.value)
        assertEquals(BusStatus.DELAYED, viewModel.busStatus.value)
    }
}
