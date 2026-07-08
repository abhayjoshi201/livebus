package com.example.livebus.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.livebus.ui.tracking.BusStatus

@Immutable
data class TransitColors(
    val onTime: Color,
    val delayed: Color,
    val severeDelay: Color
)

private val LocalTransitColors = staticCompositionLocalOf {
    TransitColors(
        onTime = OnTimeLight,
        delayed = DelayedLight,
        severeDelay = SevereDelayLight
    )
}

private val DarkColorPalette = darkColorScheme(
    primary = BrandGold,
    secondary = BrandOrange,
    tertiary = BrandNavy,
    background = BrandDarkBg,
    surface = BrandDarkSurface,
    onPrimary = BrandDarkBg,
    onSecondary = BrandDarkBg,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorPalette = lightColorScheme(
    primary = BrandNavy,
    secondary = BrandOrange,
    tertiary = BrandGold,
    background = BrandLightBg,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = BrandNavy,
    onBackground = BrandNavy,
    onSurface = BrandNavy
)

@Composable
fun LiveBusTheme(
    themeOption: com.example.livebus.ui.settings.ThemeOption = com.example.livebus.ui.settings.ThemeOption.SYSTEM,
    darkTheme: Boolean = when (themeOption) {
        com.example.livebus.ui.settings.ThemeOption.SYSTEM -> isSystemInDarkTheme()
        com.example.livebus.ui.settings.ThemeOption.DARK -> true
        com.example.livebus.ui.settings.ThemeOption.LIGHT -> false
    },
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    val transitColors = if (darkTheme) {
        TransitColors(
            onTime = OnTimeDark,
            delayed = DelayedDark,
            severeDelay = SevereDelayDark
        )
    } else {
        TransitColors(
            onTime = OnTimeLight,
            delayed = DelayedLight,
            severeDelay = SevereDelayLight
        )
    }

    CompositionLocalProvider(LocalTransitColors provides transitColors) {
        MaterialTheme(
            colorScheme = colors,
            typography = Typography,
            content = content
        )
    }
}

object LiveBusTheme {
    val transitColors: TransitColors
        @Composable
        @ReadOnlyComposable
        get() = LocalTransitColors.current
}

@Composable
@ReadOnlyComposable
fun BusStatus.statusColor(): Color {
    val colors = LiveBusTheme.transitColors
    return when (this) {
        BusStatus.ON_TIME -> colors.onTime
        BusStatus.DELAYED -> colors.delayed
        BusStatus.SEVERE_DELAY -> colors.severeDelay
    }
}
