package com.example.livebus.ui.driver

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ShiftViewModel : ViewModel() {
    private val _selectedRoute = MutableStateFlow("")
    val selectedRoute: StateFlow<String> = _selectedRoute.asStateFlow()

    private val _selectedBus = MutableStateFlow("")
    val selectedBus: StateFlow<String> = _selectedBus.asStateFlow()

    fun setRoute(route: String) {
        _selectedRoute.value = route
    }

    fun setBus(busId: String) {
        _selectedBus.value = busId
    }
}