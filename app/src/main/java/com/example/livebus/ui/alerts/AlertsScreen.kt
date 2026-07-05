package com.example.livebus.ui.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.livebus.ui.common.BottomNavigationBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    onBackClick: () -> Unit = {},
    onViewMapClick: () -> Unit = {},
    onTicketsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    viewModel: AlertsViewModel = hiltViewModel()
) {
    val systemAlerts by viewModel.systemAlerts.collectAsState()
    val personalAlerts by viewModel.personalAlerts.collectAsState()

    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedTabIndex = 3,
                onHomeClick = onBackClick,
                onMapClick = onViewMapClick,
                onTicketsClick = onTicketsClick,
                onAlertsClick = {},
                onSettingsClick = onSettingsClick
            )
        },
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                TopAppBar(
                    title = {
                        Text(
                            text = "Alerts & Notifications",
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

                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = pagerState.currentPage == 0,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        },
                        text = {
                            Text(
                                text = "⚠️ SYSTEM ALERTS (${systemAlerts.size})",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                    Tab(
                        selected = pagerState.currentPage == 1,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        },
                        text = {
                            Text(
                                text = "🔔 MY ALERTS (${personalAlerts.size})",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) { page ->
            when (page) {
                0 -> {
                    SystemAlertsSection(
                        alerts = systemAlerts,
                        onRefresh = { viewModel.refreshSystemAlerts() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                1 -> {
                    PersonalAlertsSection(
                        alerts = personalAlerts,
                        onDismiss = { id -> viewModel.dismissPersonalAlert(id) },
                        onClearAll = { viewModel.clearAllPersonalAlerts() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
