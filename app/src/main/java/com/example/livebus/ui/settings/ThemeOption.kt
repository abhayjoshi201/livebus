package com.example.livebus.ui.settings

enum class ThemeOption {
    SYSTEM,
    LIGHT,
    DARK
}

data class SettingsUiState(
    val themeOption: ThemeOption = ThemeOption.SYSTEM,
    val arrivalAlertsEnabled: Boolean = true,
    val language: String = "English",
    val homeAddress: String = "123 Main Street, Apt 4B",
    val workAddress: String = "Central Tech Park, Building C"
)
