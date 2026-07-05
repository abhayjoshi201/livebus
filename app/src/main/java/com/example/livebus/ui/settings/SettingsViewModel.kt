package com.example.livebus.ui.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "livebus_settings")

object SettingsKeys {
    val THEME = stringPreferencesKey("theme_option")
    val ALERTS_ENABLED = booleanPreferencesKey("arrival_alerts_enabled")
    val LANGUAGE = stringPreferencesKey("language")
    val HOME_ADDRESS = stringPreferencesKey("home_address")
    val WORK_ADDRESS = stringPreferencesKey("work_address")
}

@Singleton
open class SettingsRepository(
    private val context: Context?,
    private val isTest: Boolean
) {
    @Inject
    constructor(@ApplicationContext context: Context) : this(context, false)

    companion object {
        fun createInMemoryForTesting() = SettingsRepository(null, true)
    }

    private val _memoryState = MutableStateFlow(SettingsUiState())

    open val settingsState: Flow<SettingsUiState> = if (context != null) {
        context.dataStore.data.map { preferences ->
            val themeStr = preferences[SettingsKeys.THEME] ?: ThemeOption.SYSTEM.name
            val themeOption = try { ThemeOption.valueOf(themeStr) } catch (e: Exception) { ThemeOption.SYSTEM }
            SettingsUiState(
                themeOption = themeOption,
                arrivalAlertsEnabled = preferences[SettingsKeys.ALERTS_ENABLED] ?: true,
                language = preferences[SettingsKeys.LANGUAGE] ?: "English",
                homeAddress = preferences[SettingsKeys.HOME_ADDRESS] ?: "123 Main Street, Apt 4B",
                workAddress = preferences[SettingsKeys.WORK_ADDRESS] ?: "Central Tech Park, Building C"
            )
        }
    } else {
        _memoryState.asStateFlow()
    }

    open suspend fun updateTheme(theme: ThemeOption) {
        if (context != null) {
            context.dataStore.edit { preferences ->
                preferences[SettingsKeys.THEME] = theme.name
            }
        } else {
            _memoryState.value = _memoryState.value.copy(themeOption = theme)
        }
    }

    open suspend fun toggleAlerts(enabled: Boolean) {
        if (context != null) {
            context.dataStore.edit { preferences ->
                preferences[SettingsKeys.ALERTS_ENABLED] = enabled
            }
        } else {
            _memoryState.value = _memoryState.value.copy(arrivalAlertsEnabled = enabled)
        }
    }

    open suspend fun saveLocation(type: String, address: String) {
        if (context != null) {
            context.dataStore.edit { preferences ->
                if (type.equals("Home", ignoreCase = true)) {
                    preferences[SettingsKeys.HOME_ADDRESS] = address
                } else if (type.equals("Work", ignoreCase = true)) {
                    preferences[SettingsKeys.WORK_ADDRESS] = address
                }
            }
        } else {
            if (type.equals("Home", ignoreCase = true)) {
                _memoryState.value = _memoryState.value.copy(homeAddress = address)
            } else if (type.equals("Work", ignoreCase = true)) {
                _memoryState.value = _memoryState.value.copy(workAddress = address)
            }
        }
    }

    open suspend fun clearAppData() {
        if (context != null) {
            context.dataStore.edit { it.clear() }
        } else {
            _memoryState.value = SettingsUiState()
        }
    }
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repository.settingsState.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                // Fallback to initial state if error
            }
        }
    }

    fun updateTheme(theme: ThemeOption) {
        _uiState.value = _uiState.value.copy(themeOption = theme)
        viewModelScope.launch {
            try { repository.updateTheme(theme) } catch (e: Exception) {}
        }
    }

    fun toggleAlerts(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(arrivalAlertsEnabled = enabled)
        viewModelScope.launch {
            try { repository.toggleAlerts(enabled) } catch (e: Exception) {}
        }
    }

    fun saveLocation(type: String, address: String) {
        if (type.equals("Home", ignoreCase = true)) {
            _uiState.value = _uiState.value.copy(homeAddress = address)
        } else if (type.equals("Work", ignoreCase = true)) {
            _uiState.value = _uiState.value.copy(workAddress = address)
        }
        viewModelScope.launch {
            try { repository.saveLocation(type, address) } catch (e: Exception) {}
        }
    }

    fun clearAppData() {
        _uiState.value = SettingsUiState()
        viewModelScope.launch {
            try { repository.clearAppData() } catch (e: Exception) {}
        }
    }
}
