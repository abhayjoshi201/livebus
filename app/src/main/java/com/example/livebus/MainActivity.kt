package com.example.livebus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.livebus.ui.alerts.AlertsScreen
import com.example.livebus.ui.itinerary.RouteItineraryScreen
import com.example.livebus.ui.search.SearchScreen
import com.example.livebus.ui.settings.SettingsScreen
import com.example.livebus.ui.settings.SettingsViewModel
import com.example.livebus.ui.settings.ThemeOption
import com.example.livebus.ui.tickets.TicketsScreen
import com.example.livebus.ui.home.HomeScreen
import com.example.livebus.ui.driver.LiveBusAppNavigation
import com.example.livebus.ui.theme.LiveBusTheme
import com.example.livebus.ui.tracking.LiveTrackingScreen
import com.example.livebus.data.TransitRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var transitRepository: TransitRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settingsState by settingsViewModel.uiState.collectAsState()

            LiveBusTheme(themeOption = settingsState.themeOption) {
                var appMode by remember { mutableStateOf("passenger") }
                var currentScreen by remember { mutableStateOf("home") }

                if (appMode == "driver") {
                    LiveBusAppNavigation(
                        onSwitchToPassenger = { appMode = "passenger" }
                    )
                } else {
                    when (currentScreen) {
                        "settings" -> SettingsScreen(
                            onBackClick = { currentScreen = "home" },
                            onViewMapClick = { currentScreen = "tracking" },
                            onTicketsClick = { currentScreen = "tickets" },
                            onAlertsClick = { currentScreen = "alerts" },
                            onSwitchToDriver = { appMode = "driver" },
                            onThemeChanged = { theme ->
                                settingsViewModel.updateTheme(theme)
                            }
                        )
                        "alerts" -> AlertsScreen(
                            onBackClick = { currentScreen = "home" },
                            onViewMapClick = { currentScreen = "tracking" },
                            onTicketsClick = { currentScreen = "tickets" },
                            onSettingsClick = { currentScreen = "settings" }
                        )
                        "tickets" -> TicketsScreen(
                            onBackClick = { currentScreen = "home" },
                            onViewMapClick = { currentScreen = "tracking" },
                            onAlertsClick = { currentScreen = "alerts" },
                            onSettingsClick = { currentScreen = "settings" }
                        )
                        "search" -> SearchScreen(
                            onBackClick = { currentScreen = "home" },
                            onRouteSelect = { routeId ->
                                transitRepository.selectRoute(routeId)
                                currentScreen = "itinerary"
                            }
                        )
                        "itinerary" -> RouteItineraryScreen(
                            onBackClick = { currentScreen = "home" },
                            onViewMapClick = { currentScreen = "tracking" }
                        )
                        "tracking" -> LiveTrackingScreen(
                            onBackClick = { currentScreen = "itinerary" }
                        )
                        else -> HomeScreen(
                            onRouteClick = { routeId ->
                                transitRepository.selectRoute(routeId)
                                currentScreen = "itinerary"
                            },
                            onMapClick = { currentScreen = "tracking" },
                            onSearchClick = { currentScreen = "search" },
                            onTicketsClick = { currentScreen = "tickets" },
                            onAlertsClick = { currentScreen = "alerts" },
                            onSettingsClick = { currentScreen = "settings" }
                        )
                    }
                }
            }
        }
    }
}
