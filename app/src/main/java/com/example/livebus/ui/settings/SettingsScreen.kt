package com.example.livebus.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.livebus.ui.theme.BottomNavigationBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    onViewMapClick: () -> Unit = {},
    onTicketsClick: () -> Unit = {},
    onAlertsClick: () -> Unit = {},
    onSwitchToDriver: () -> Unit = {},
    onThemeChanged: (ThemeOption) -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLinkToast by remember { mutableStateOf(false) }

    if (showLinkToast) {
        AlertDialog(
            onDismissRequest = { showLinkToast = false },
            title = { Text("Link Cloud Account", fontWeight = FontWeight.Bold) },
            text = { Text("Your account is currently stored locally for maximum privacy. Linking an email will sync your passes and favorite routes across devices.") },
            confirmButton = {
                TextButton(onClick = { showLinkToast = false }) {
                    Text("Got It", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedTabIndex = 4,
                onHomeClick = onBackClick,
                onMapClick = onViewMapClick,
                onTicketsClick = onTicketsClick,
                onAlertsClick = onAlertsClick,
                onSettingsClick = {}
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                ProfileSection(
                    onLinkAccount = { showLinkToast = true },
                    onSwitchToDriver = onSwitchToDriver
                )
            }

            item {
                SavedLocationsSection(
                    homeAddress = uiState.homeAddress,
                    workAddress = uiState.workAddress,
                    onSaveLocation = { type, address ->
                        viewModel.saveLocation(type, address)
                    }
                )
            }

            item {
                PreferencesSection(
                    currentTheme = uiState.themeOption,
                    onThemeSelect = { theme ->
                        viewModel.updateTheme(theme)
                        onThemeChanged(theme)
                    },
                    alertsEnabled = uiState.arrivalAlertsEnabled,
                    onToggleAlerts = { enabled ->
                        viewModel.toggleAlerts(enabled)
                    }
                )
            }

            item {
                AboutSupportSection(
                    onClearData = {
                        viewModel.clearAppData()
                        onThemeChanged(ThemeOption.SYSTEM)
                    }
                )
            }
        }
    }
}
