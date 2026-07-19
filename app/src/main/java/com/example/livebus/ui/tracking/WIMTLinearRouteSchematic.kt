package com.example.livebus.ui.tracking

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.livebus.R
import com.example.livebus.data.GovtRoute
import com.example.livebus.data.RouteStageSegment

/**
 * WIMTLinearRouteSchematic:
 * Redesigned to adopt the exact natural, spacious, pixel-perfect layout philosophy of
 * RouteItineraryScreen / VerticalTimeline, while providing zero-cost Room DB tracking & cellular triangulation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WIMTLinearRouteSchematic(
    route: GovtRoute?,
    stages: List<RouteStageSegment>,
    currentDistanceMeters: Double,
    currentSpeedKmh: Int = 48,
    isOfflineTriangulated: Boolean = true,
    activePassengersCount: Int = 14,
    onSwitchToMapView: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDarkTheme = isSystemInDarkTheme()
    val lineColor = if (isDarkTheme) Color(0xFF666666) else Color(0xFFCCCCCC)
    val passedColor = if (isDarkTheme) Color(0xFF888888) else Color(0xFF999999)
    val activeColor = Color(0xFF2E7D32) // Natural primary green theme

    var isExpressFare by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = route?.routeCode ?: "UK-DDO-01",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onSwitchToMapView) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Map"
                        )
                    }
                },
                actions = {
                    Surface(
                        color = if (route?.operatorId?.startsWith("GOVT") == true) Color(0xFF2563EB) else Color(0xFFD97706),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Text(
                            text = if (route?.operatorId?.startsWith("GOVT") == true) "GOVT UTC" else "PRIVATE COACH",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onSwitchToMapView,
                containerColor = activeColor,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = "Map View"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "VIEW LIVE ON 2D MAP",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            if (stages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("Loading offline schedule from Room DB...", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                // --- Natural Summary Header Card ---
                val totalRemainingKm = ((route?.totalDistanceMeters ?: 275000.0) - currentDistanceMeters) / 1000.0
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = activeColor.copy(alpha = 0.1f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "ETA to destination",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${route?.originHi ?: "देहरादून"} ──> ${route?.destinationHi ?: "हल्द्वानी"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = "${((totalRemainingKm / 35.0) * 60).toInt().coerceAtLeast(10)}",
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = activeColor
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "min",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = activeColor,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                }
                                Text(
                                    text = "${String.format("%.1f", totalRemainingKm.coerceAtLeast(0.0))} km remaining",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Offline / Crowdsourced Telemetry Pill
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF334155))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.SignalCellularAlt,
                                    contentDescription = "Tracking Mode",
                                    tint = Color(0xFF4ADE80),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isOfflineTriangulated) "📴 Cell-Tower Triangulation (0 KB Data)" else "🟢 Crowdsourced ($activePassengersCount citizens)",
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Speed, contentDescription = "Speed", tint = Color(0xFFFBBF24), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("$currentSpeedKmh km/h", fontSize = 12.sp, color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // --- Fare Class Switcher ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Route Milestones & Stage Fares", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (!isExpressFare) activeColor else Color.Transparent,
                            modifier = Modifier.clickable { isExpressFare = false }.padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Ordinary", fontSize = 12.sp, color = if (!isExpressFare) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(2.dp))
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isExpressFare) Color(0xFF2563EB) else Color.Transparent,
                            modifier = Modifier.clickable { isExpressFare = true }.padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Express/Coach", fontSize = 12.sp, color = if (isExpressFare) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(2.dp))
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))

                // --- Natural Vertical Timeline (Matches RouteItineraryScreen & VerticalTimeline exactly) ---
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    itemsIndexed(stages) { index, stage ->
                        val isPassed = stage.accumulatedDistanceMeters < currentDistanceMeters
                        val prevDistance = if (index == 0) 0.0 else stages[index - 1].accumulatedDistanceMeters
                        val isNextApproaching = !isPassed && currentDistanceMeters >= prevDistance && currentDistanceMeters <= stage.accumulatedDistanceMeters
                        val isFirst = index == 0
                        val isLast = index == stages.size - 1

                        // If this is the active target stop, insert BusIndicatorItem exactly before it!
                        if (isNextApproaching) {
                            WIMTBusIndicatorItem(activeColor = activeColor, speedKmh = currentSpeedKmh)
                        }

                        WIMTStopTimelineItem(
                            stage = stage,
                            isPassed = isPassed,
                            isTarget = isNextApproaching,
                            isFirst = isFirst,
                            isLast = isLast,
                            isExpressFare = isExpressFare,
                            currentDistanceMeters = currentDistanceMeters,
                            lineColor = lineColor,
                            passedColor = passedColor,
                            activeColor = activeColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WIMTBusIndicatorItem(activeColor: Color, speedKmh: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "busPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .drawBehind {
                val centerX = 28.dp.toPx()
                drawLine(color = activeColor, start = Offset(centerX, 0f), end = Offset(centerX, size.height), strokeWidth = 3.dp.toPx())
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.width(56.dp).height(24.dp), contentAlignment = Alignment.Center) {
            Box(modifier = Modifier.size(24.dp).scale(scale).clip(CircleShape).background(activeColor.copy(alpha = 0.3f)))
            Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(activeColor), contentAlignment = Alignment.Center) {
                Icon(painter = painterResource(id = R.drawable.ic_bus), contentDescription = "Live Bus", tint = Color.White, modifier = Modifier.size(12.dp))
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(activeColor.copy(alpha = 0.15f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text("<<<", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold, color = activeColor)
            Spacer(modifier = Modifier.width(8.dp))
            Text("LIVE BUS IS HERE", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = activeColor)
            Spacer(modifier = Modifier.width(6.dp))
            Text("• $speedKmh km/h", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = activeColor)
        }
    }
}

@Composable
fun WIMTStopTimelineItem(
    stage: RouteStageSegment,
    isPassed: Boolean,
    isTarget: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    isExpressFare: Boolean,
    currentDistanceMeters: Double,
    lineColor: Color,
    passedColor: Color,
    activeColor: Color
) {
    val nodeColor = when {
        isPassed -> passedColor
        isTarget -> activeColor
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val textColor = when {
        isPassed -> passedColor
        isTarget -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface
    }
    val surfaceColor = MaterialTheme.colorScheme.surface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .drawBehind {
                val centerX = 28.dp.toPx()
                val centerY = size.height / 2f

                if (!isFirst) {
                    val topColor = if (isPassed || isTarget) activeColor else lineColor
                    drawLine(color = topColor, start = Offset(centerX, 0f), end = Offset(centerX, centerY), strokeWidth = if (isPassed || isTarget) 3.dp.toPx() else 2.dp.toPx())
                }
                if (!isLast) {
                    val bottomColor = if (isPassed) activeColor else lineColor
                    drawLine(color = bottomColor, start = Offset(centerX, centerY), end = Offset(centerX, size.height), strokeWidth = if (isPassed) 3.dp.toPx() else 2.dp.toPx())
                }

                if (isPassed) {
                    drawCircle(color = passedColor, radius = 5.dp.toPx(), center = Offset(centerX, centerY))
                } else if (isTarget) {
                    drawCircle(color = activeColor, radius = 9.dp.toPx(), center = Offset(centerX, centerY))
                    drawCircle(color = Color.White, radius = 4.dp.toPx(), center = Offset(centerX, centerY))
                } else {
                    drawCircle(color = surfaceColor, radius = 6.dp.toPx(), center = Offset(centerX, centerY))
                    drawCircle(color = nodeColor, radius = 6.dp.toPx(), center = Offset(centerX, centerY), style = Stroke(width = 2.dp.toPx()))
                }
            },

        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(56.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = stage.nameHi, style = MaterialTheme.typography.bodyLarge, fontWeight = if (isTarget) FontWeight.ExtraBold else FontWeight.Medium, color = textColor)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = stage.nameEn, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f))
                Text(" • ", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                Text(
                    text = if (stage.isUphill) "⛰️ Uphill (${stage.uphillAvgSpeedKmh} km/h)" else "↘️ Downhill (${stage.downhillAvgSpeedKmh} km/h)",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (stage.isUphill) Color(0xFFD97706) else Color(0xFF059669)
                )
            }
        }

        // Fare Badge
        val currentStageFare = if (isExpressFare) stage.expressFareInr else stage.ordinaryFareInr
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isTarget) activeColor else if (isPassed) Color(0xFFE2E8F0) else Color(0xFFFEF3C7)
        ) {
            Text(
                text = if (currentStageFare == 0) "Origin" else "₹$currentStageFare",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isTarget) Color.White else if (isPassed) Color(0xFF64748B) else Color(0xFF92400E),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}
