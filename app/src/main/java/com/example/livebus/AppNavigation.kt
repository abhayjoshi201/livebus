package com.example.livebus

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun LiveBusAppNavigation(
    onSwitchToPassenger: () -> Unit = {}
) {
    val navController = rememberNavController()
    val shiftViewModel: ShiftViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login") {
        
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("route_selection") },
                onSwitchToPassenger = onSwitchToPassenger
            )
        }
        
        composable("route_selection") {
            RouteSelectionScreen(onNavigateNext = { route -> 
                shiftViewModel.setRoute(route)
                navController.navigate("bus_selection") 
            })
        }
        
        composable("bus_selection") {
            BusSelectionScreen(onNavigateNext = { bus -> 
                shiftViewModel.setBus(bus)
                navController.navigate("shift_confirmation") 
            })
        }
        
        composable("shift_confirmation") {
            val route by shiftViewModel.selectedRoute.collectAsState()
            val bus by shiftViewModel.selectedBus.collectAsState()
            
            ShiftConfirmationScreen(
                route = route,
                busId = bus,
                // Navigate to the active shift screen!
                onStartShift = { navController.navigate("active_shift") } 
            )
        }

        composable("active_shift") {
            val route by shiftViewModel.selectedRoute.collectAsState()
            val bus by shiftViewModel.selectedBus.collectAsState()

            ActiveShiftScreen(
                route = route,
                busId = bus,
                // Navigate to the trip end screen!
                onEndTrip = { navController.navigate("trip_end") }
            )
        }

        composable("trip_end") {
            TripEndScreen(
                // Clear the backstack and go back to route selection for a new shift
                onReturnHome = { 
                    navController.navigate("route_selection") {
                        popUpTo("route_selection") { inclusive = true }
                    }
                }
            )
        }
    }
}