package com.example.livebus.ui.itinerary

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.livebus.R
import com.example.livebus.ui.theme.statusColor
import com.example.livebus.ui.tracking.BusStatus

@Composable
fun VerticalTimeline(
    stops: List<Stop>,
    currentBusStopIndex: Int,
    nextStopEta: Int,
    status: BusStatus,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    val isDarkTheme = isSystemInDarkTheme()
    val lineColor = if (isDarkTheme) Color(0xFF666666) else Color(0xFFCCCCCC)
    val passedColor = if (isDarkTheme) Color(0xFF888888) else Color(0xFF999999)
    val activeColor = status.statusColor()

    // Automatically animate scroll to center the bus indicator when currentBusStopIndex changes
    LaunchedEffect(currentBusStopIndex) {
        val targetScrollIndex = (currentBusStopIndex - 1).coerceAtLeast(0)
        try {
            listState.animateScrollToItem(targetScrollIndex)
        } catch (e: Exception) {
            // Ignore scroll animation errors if item not ready
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Iterate through stops and dynamically insert the bus indicator before the target stop
        itemsIndexed(stops) { index, stop ->
            val isPassed = index < currentBusStopIndex
            val isTarget = index == currentBusStopIndex
            val isUpcoming = index > currentBusStopIndex
            val isFirst = index == 0
            val isLast = index == stops.size - 1

            // If this is the target stop, render the Live Bus Indicator RIGHT BEFORE it
            if (isTarget) {
                BusIndicatorItem(
                    status = status,
                    lineColor = lineColor
                )
            }

            StopTimelineItem(
                stop = stop,
                isPassed = isPassed,
                isTarget = isTarget,
                isUpcoming = isUpcoming,
                isFirst = isFirst,
                isLast = isLast,
                etaMinutes = if (isTarget) nextStopEta else null,
                lineColor = lineColor,
                passedColor = passedColor,
                activeColor = activeColor
            )
        }
    }
}

@Composable
fun BusIndicatorItem(
    status: BusStatus,
    lineColor: Color
) {
    val activeColor = status.statusColor()
    val infiniteTransition = rememberInfiniteTransition(label = "busPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .drawBehind {
                // Draw continuous vertical line through the bus indicator
                val centerX = 28.dp.toPx()
                drawLine(
                    color = activeColor,
                    start = Offset(centerX, 0f),
                    end = Offset(centerX, size.height),
                    strokeWidth = 3.dp.toPx()
                )
            }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Center icon at 28.dp (12.dp padding + 16.dp half-width)
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(24.dp),
            contentAlignment = Alignment.Center
        ) {
            // Pulsing background aura
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(activeColor.copy(alpha = 0.3f))
            )
            // Solid bus icon background badge
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(activeColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_bus),
                    contentDescription = "Live Bus Here",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(20.dp))

        // Dynamic Bus Indicator Banner
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(activeColor.copy(alpha = 0.15f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "<<<",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = activeColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "BUS 101 IS HERE",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = activeColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "• LIVE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = activeColor
            )
        }
    }
}

@Composable
fun StopTimelineItem(
    stop: Stop,
    isPassed: Boolean,
    isTarget: Boolean,
    isUpcoming: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    etaMinutes: Int?,
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

    val fontWeight = when {
        isTarget -> FontWeight.ExtraBold
        isUpcoming -> FontWeight.Medium
        else -> FontWeight.Normal
    }

    val surfaceColor = MaterialTheme.colorScheme.surface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .drawBehind {
                val centerX = 28.dp.toPx()
                val centerY = size.height / 2f

                // Draw line from top to center if not first
                if (!isFirst) {
                    val topColor = if (isPassed || isTarget) activeColor else lineColor
                    drawLine(
                        color = topColor,
                        start = Offset(centerX, 0f),
                        end = Offset(centerX, centerY),
                        strokeWidth = if (isPassed || isTarget) 3.dp.toPx() else 2.dp.toPx()
                    )
                }

                // Draw line from center to bottom if not last
                if (!isLast) {
                    val bottomColor = if (isPassed) activeColor else lineColor
                    drawLine(
                        color = bottomColor,
                        start = Offset(centerX, centerY),
                        end = Offset(centerX, size.height),
                        strokeWidth = if (isPassed) 3.dp.toPx() else 2.dp.toPx()
                    )
                }

                // Draw Circle Node
                if (isPassed) {
                    // Small solid gray/active circle
                    drawCircle(
                        color = passedColor,
                        radius = 5.dp.toPx(),
                        center = Offset(centerX, centerY)
                    )
                } else if (isTarget) {
                    // Larger outlined circle in primary theme color
                    drawCircle(
                        color = activeColor,
                        radius = 9.dp.toPx(),
                        center = Offset(centerX, centerY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4.dp.toPx(),
                        center = Offset(centerX, centerY)
                    )
                } else {
                    // Standard hollow circle for upcoming stops
                    drawCircle(
                        color = surfaceColor,
                        radius = 6.dp.toPx(),
                        center = Offset(centerX, centerY)
                    )
                    drawCircle(
                        color = nodeColor,
                        radius = 6.dp.toPx(),
                        center = Offset(centerX, centerY),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Space reserved for the timeline column (28.dp center + 16.dp right margin)
        Spacer(modifier = Modifier.width(44.dp))

        // Stop Name and details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stop.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = fontWeight,
                color = textColor
            )
            if (isTarget) {
                Text(
                    text = "Next Stop • ${stop.distanceKm} km from start",
                    style = MaterialTheme.typography.bodySmall,
                    color = activeColor
                )
            } else {
                Text(
                    text = "${stop.distanceKm} km",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }

        // ETA Badge for Target Stop
        if (isTarget && etaMinutes != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = activeColor,
                shadowElevation = 2.dp
            ) {
                Text(
                    text = "$etaMinutes MIN",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}
