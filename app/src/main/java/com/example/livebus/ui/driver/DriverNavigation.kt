package com.example.livebus.ui.driver

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun LiveBusAppNavigation(
    onSwitchToPassenger: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val navController = rememberNavController()
    val shiftViewModel: ShiftViewModel = hiltViewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "login"

    // Map current destination to tab index for lower navbar
    val selectedTabIndex = when (currentRoute) {
        "route_selection", "bus_selection", "shift_confirmation" -> 0
        "active_shift" -> 1
        "operator_settings" -> 2
        else -> 0
    }

    val showBottomBar = currentRoute != "login" && currentRoute != "trip_end"
    val showTopBar = currentRoute != "login"

    val isBroadcasting by shiftViewModel.isBroadcasting.collectAsState()
    val selectedRoute by shiftViewModel.selectedRoute.collectAsState()
    val selectedBusId by shiftViewModel.selectedBus.collectAsState()
    val txCount by shiftViewModel.telemetryCount.collectAsState()

    val title = when (currentRoute) {
        "route_selection" -> "Step 1: Assign Itinerary"
        "bus_selection" -> "Step 2: Assign Fleet ID"
        "shift_confirmation" -> "Pre-Flight Summary"
        "active_shift" -> "Live Telemetry Console"
        "operator_settings" -> "Operator Configuration"
        "trip_end" -> "Shift Complete"
        else -> "Driver Portal"
    }

    // Determine if back button should be enabled
    val canGoBack = navController.previousBackStackEntry != null && 
                    currentRoute != "route_selection" && 
                    currentRoute != "active_shift" && 
                    currentRoute != "trip_end"

    Scaffold(
        topBar = {
            if (showTopBar) {
                DriverTopAppBar(
                    title = title,
                    isBroadcasting = isBroadcasting,
                    onNavigateBack = if (canGoBack) { { navController.popBackStack() } } else null
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                DriverBottomNavigationBar(
                    selectedTabIndex = selectedTabIndex,
                    onTabSelected = { tabIndex ->
                        when (tabIndex) {
                            0 -> navController.navigate("route_selection") {
                                popUpTo("route_selection") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            1 -> navController.navigate("active_shift") {
                                popUpTo("route_selection") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                            2 -> navController.navigate("operator_settings") {
                                popUpTo("route_selection") { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "route_selection",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("login") {
                LoginScreen(
                    onLoginSuccess = { navController.navigate("route_selection") },
                    onSwitchToPassenger = onSwitchToPassenger
                )
            }

            composable("route_selection") {
                val availableRoutes by shiftViewModel.availableRoutes.collectAsState()
                val selectedCityId by shiftViewModel.selectedCityId.collectAsState()

                RouteSelectionScreen(
                    availableRoutes = availableRoutes,
                    selectedRoute = selectedRoute,
                    onRouteSelected = { route -> shiftViewModel.setRoute(route) },
                    selectedCityId = selectedCityId,
                    allCities = shiftViewModel.allCities,
                    onCitySelected = { cityId -> shiftViewModel.selectCity(cityId) },
                    onNavigateNext = { navController.navigate("bus_selection") }
                )
            }

            composable("bus_selection") {
                BusSelectionScreen(onNavigateNext = { bus ->
                    shiftViewModel.setBus(bus)
                    navController.navigate("shift_confirmation")
                })
            }

            composable("shift_confirmation") {
                ShiftConfirmationScreen(
                    route = selectedRoute?.displayName ?: "",
                    busId = selectedBusId,
                    onStartShift = {
                        shiftViewModel.startBroadcasting()
                        navController.navigate("active_shift")
                    }
                )
            }

            composable("active_shift") {
                ActiveShiftScreen(
                    route = selectedRoute?.displayName ?: "",
                    busId = selectedBusId,
                    txCount = txCount,
                    onReportDelay = { shiftViewModel.reportTrafficDelay() },
                    onReportCrowd = { shiftViewModel.reportCrowd() },
                    onTriggerSos = { shiftViewModel.triggerSos() },
                    onEndTrip = {
                        shiftViewModel.stopBroadcasting()
                        navController.navigate("trip_end")
                    }
                )
            }

            composable("operator_settings") {
                OperatorSettingsScreen(
                    busId = selectedBusId,
                    route = selectedRoute?.displayName ?: "",
                    onSwitchToPassenger = onSwitchToPassenger
                )
            }

            composable("trip_end") {
                TripEndScreen(
                    route = selectedRoute?.displayName ?: "",
                    busId = selectedBusId,
                    txCount = txCount,
                    onReturnHome = {
                        navController.navigate("route_selection") {
                            popUpTo("route_selection") { inclusive = true }
                        }
                    },
                    onSwitchToPassenger = onSwitchToPassenger,
                    onLogout = onLogout
                )
            }
        }
    }
}