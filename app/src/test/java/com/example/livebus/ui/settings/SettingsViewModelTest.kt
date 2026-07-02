package com.example.livebus.ui.settings

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private lateinit var viewModel: SettingsViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = SettingsViewModel(SettingsRepository.createInMemoryForTesting()) // Test in-memory fallback without DataStore
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isCorrect() {
        val state = viewModel.uiState.value
        assertEquals(ThemeOption.SYSTEM, state.themeOption)
        assertTrue(state.arrivalAlertsEnabled)
        assertEquals("English", state.language)
        assertEquals("123 Main Street, Apt 4B", state.homeAddress)
        assertEquals("Central Tech Park, Building C", state.workAddress)
    }

    @Test
    fun updateTheme_changesThemeState() {
        viewModel.updateTheme(ThemeOption.DARK)
        assertEquals(ThemeOption.DARK, viewModel.uiState.value.themeOption)

        viewModel.updateTheme(ThemeOption.LIGHT)
        assertEquals(ThemeOption.LIGHT, viewModel.uiState.value.themeOption)
    }

    @Test
    fun toggleAlerts_changesAlertState() {
        assertTrue(viewModel.uiState.value.arrivalAlertsEnabled)
        viewModel.toggleAlerts(false)
        assertFalse(viewModel.uiState.value.arrivalAlertsEnabled)
    }

    @Test
    fun saveLocation_updatesAddresses() {
        viewModel.saveLocation("Home", "789 Pine Ave")
        assertEquals("789 Pine Ave", viewModel.uiState.value.homeAddress)

        viewModel.saveLocation("Work", "999 Tech Blvd")
        assertEquals("999 Tech Blvd", viewModel.uiState.value.workAddress)
    }

    @Test
    fun clearAppData_resetsStateToDefaults() {
        viewModel.updateTheme(ThemeOption.DARK)
        viewModel.saveLocation("Home", "Modified Address")
        viewModel.clearAppData()

        assertEquals(ThemeOption.SYSTEM, viewModel.uiState.value.themeOption)
        assertEquals("123 Main Street, Apt 4B", viewModel.uiState.value.homeAddress)
    }
}
