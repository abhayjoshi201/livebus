package com.example.livebus.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(val title: String, val icon: ImageVector)

@Composable
fun BottomNavigationBar(
    selectedTabIndex: Int = 0,
    onHomeClick: () -> Unit = {},
    onMapClick: () -> Unit = {},
    onTicketsClick: () -> Unit = {},
    onAlertsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    val items = listOf(
        BottomNavItem("Home", Icons.Default.Home),
        BottomNavItem("Map", Icons.Default.Map),
        BottomNavItem("Tickets", Icons.Default.ConfirmationNumber),
        BottomNavItem("Alerts", Icons.Default.Notifications),
        BottomNavItem("Settings", Icons.Default.Settings)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = selectedTabIndex == index,
                onClick = {
                    when (item.title) {
                        "Home" -> onHomeClick()
                        "Map" -> onMapClick()
                        "Tickets" -> onTicketsClick()
                        "Alerts" -> onAlertsClick()
                        "Settings" -> onSettingsClick()
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.surface,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
