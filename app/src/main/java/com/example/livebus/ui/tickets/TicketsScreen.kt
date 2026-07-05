package com.example.livebus.ui.tickets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ReceiptLong
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
fun TicketsScreen(
    onBackClick: () -> Unit = {},
    onViewMapClick: () -> Unit = {},
    onAlertsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    viewModel: TicketsViewModel = hiltViewModel()
) {
    val activeTickets by viewModel.activeTickets.collectAsState()
    val expiredTickets by viewModel.expiredTickets.collectAsState()
    val ticketProducts = viewModel.ticketProducts

    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    var showReceiptsToast by remember { mutableStateOf(false) }

    if (showReceiptsToast) {
        AlertDialog(
            onDismissRequest = { showReceiptsToast = false },
            title = { Text("Receipts & History", fontWeight = FontWeight.Bold) },
            text = { Text("Showing fare inspection receipts and payment history for user wallet.") },
            confirmButton = {
                TextButton(onClick = { showReceiptsToast = false }) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                selectedTabIndex = 2,
                onHomeClick = onBackClick,
                onMapClick = onViewMapClick,
                onTicketsClick = {},
                onAlertsClick = onAlertsClick,
                onSettingsClick = onSettingsClick
            )
        },
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                TopAppBar(
                    title = {
                        Text(
                            text = "My Tickets",
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
                    actions = {
                        IconButton(onClick = { showReceiptsToast = true }) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = "Receipts and History",
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
                                text = "ACTIVE (${activeTickets.size})",
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
                                text = "BUY NEW PASS",
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
                    ActiveTicketsSection(
                        activeTickets = activeTickets,
                        expiredTickets = expiredTickets,
                        onRenewClick = { ticket ->
                            viewModel.renewExpiredTicket(ticket)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                1 -> {
                    BuyTicketsSection(
                        products = ticketProducts,
                        onPurchaseConfirm = { product ->
                            viewModel.purchaseTicket(product)
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
