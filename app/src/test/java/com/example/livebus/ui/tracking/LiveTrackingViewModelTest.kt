package com.example.livebus.ui.tracking

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LiveTrackingViewModelTest {

    private lateinit var viewModel: LiveTrackingViewModel

    @Before
    fun setup() {
        val repo = com.example.livebus.data.TransitRepository()
        repo.selectRoute("216W")
        viewModel = LiveTrackingViewModel(repo)
    }

    @Test
    fun initialState_isCorrect() {
        assertFalse(viewModel.isAlertActive.value)
        assertEquals(5, viewModel.eta.value)
        assertEquals(1.2, viewModel.distance.value, 0.001)
        assertEquals(BusStatus.ON_TIME, viewModel.busStatus.value)
    }

    @Test
    fun toggleAlert_changesAlertState() {
        assertFalse(viewModel.isAlertActive.value)
        viewModel.toggleAlert()
        assertTrue(viewModel.isAlertActive.value)
        viewModel.toggleAlert()
        assertFalse(viewModel.isAlertActive.value)
    }

    @Test
    fun parseAndApplyMessage_updatesStateCorrectly() {
        val jsonPayload = """
            {
                "latitude": 17.4000,
                "longitude": 78.4000,
                "eta": 12,
                "distance": 3.5,
                "status": "DELAYED"
            }
        """.trimIndent()

        viewModel.parseAndApplyMessage(jsonPayload)

        assertEquals(17.4000, viewModel.busLocation.value?.latitude ?: 0.0, 0.0001)
        assertEquals(78.4000, viewModel.busLocation.value?.longitude ?: 0.0, 0.0001)
        assertEquals(12, viewModel.eta.value)
        assertEquals(3.5, viewModel.distance.value, 0.001)
        assertEquals(BusStatus.DELAYED, viewModel.busStatus.value)
    }
}
