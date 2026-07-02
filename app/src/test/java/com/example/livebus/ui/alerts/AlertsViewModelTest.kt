package com.example.livebus.ui.alerts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AlertsViewModelTest {

    private lateinit var viewModel: AlertsViewModel

    @Before
    fun setup() {
        viewModel = AlertsViewModel()
    }

    @Test
    fun initialState_isCorrect() {
        assertEquals(3, viewModel.systemAlerts.value.size)
        assertEquals(3, viewModel.personalAlerts.value.size)
    }

    @Test
    fun dismissPersonalAlert_removesAlert() {
        val alertId = viewModel.personalAlerts.value[0].id
        viewModel.dismissPersonalAlert(alertId)

        assertEquals(2, viewModel.personalAlerts.value.size)
        assertTrue(viewModel.personalAlerts.value.none { it.id == alertId })
    }

    @Test
    fun clearAllPersonalAlerts_removesAll() {
        viewModel.clearAllPersonalAlerts()
        assertEquals(0, viewModel.personalAlerts.value.size)
    }

    @Test
    fun severityLabels_areCorrect() {
        assertEquals("🔴 SEVERE ALERT", AlertSeverity.SEVERE.label())
        assertEquals("🟠 MINOR DELAY", AlertSeverity.WARNING.label())
        assertEquals("🟢 RESOLVED", AlertSeverity.INFO_RESOLVED.label())
    }
}
