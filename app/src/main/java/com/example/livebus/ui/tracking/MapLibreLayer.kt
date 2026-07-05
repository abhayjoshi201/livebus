package com.example.livebus.ui.tracking

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.animation.LinearInterpolator
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.livebus.R
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng as MapboxLatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.Style

@Composable
fun MapLibreLayer(
    busLocation: LatLng?,
    userStopLocation: LatLng,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isDarkTheme = isSystemInDarkTheme()

    // Initialize MapLibre/Mapbox before creating MapView
    LaunchedEffect(Unit) {
        try {
            Mapbox.getInstance(context)
        } catch (e: Exception) {
            println("Mapbox init error: ${e.message}")
        }
    }

    val mapView: MapView? = remember {
        try {
            Mapbox.getInstance(context)
            MapView(context)
        } catch (e: Exception) {
            null
        }
    }

    var mapboxMap by remember { mutableStateOf<MapboxMap?>(null) }
    var isStyleLoaded by remember { mutableStateOf(false) }
    var busMarker by remember { mutableStateOf<Marker?>(null) }
    var stopMarker by remember { mutableStateOf<Marker?>(null) }
    var routePolyline by remember { mutableStateOf<com.mapbox.mapboxsdk.annotations.Polyline?>(null) }

    // CartoDB public XYZ tiles (no API keys required)
    val styleJson = remember(isDarkTheme) {
        val themePrefix = if (isDarkTheme) "dark_all" else "light_all"
        """
        {
          "version": 8,
          "sources": {
            "carto-raster": {
              "type": "raster",
              "tiles": [
                "https://a.basemaps.cartocdn.com/$themePrefix/{z}/{x}/{y}.png",
                "https://b.basemaps.cartocdn.com/$themePrefix/{z}/{x}/{y}.png",
                "https://c.basemaps.cartocdn.com/$themePrefix/{z}/{x}/{y}.png",
                "https://d.basemaps.cartocdn.com/$themePrefix/{z}/{x}/{y}.png"
              ],
              "tileSize": 256,
              "attribution": "© OpenStreetMap © CARTO"
            }
          },
          "layers": [
            {
              "id": "carto-raster-layer",
              "type": "raster",
              "source": "carto-raster",
              "minzoom": 0,
              "maxzoom": 22
            }
          ]
        }
        """.trimIndent()
    }

    // Lifecycle observer for MapView
    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            mapView?.let { mv ->
                when (event) {
                    Lifecycle.Event.ON_CREATE -> mv.onCreate(null)
                    Lifecycle.Event.ON_START -> mv.onStart()
                    Lifecycle.Event.ON_RESUME -> mv.onResume()
                    Lifecycle.Event.ON_PAUSE -> mv.onPause()
                    Lifecycle.Event.ON_STOP -> mv.onStop()
                    Lifecycle.Event.ON_DESTROY -> mv.onDestroy()
                    else -> {}
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        mapView?.onCreate(null)
        mapView?.onStart()
        mapView?.onResume()

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView?.onPause()
            mapView?.onStop()
            mapView?.onDestroy()
        }
    }

    // Setup map once created
    LaunchedEffect(mapView, styleJson) {
        mapView?.getMapAsync { map ->
            mapboxMap = map
            map.setStyle(Style.Builder().fromJson(styleJson)) { style ->
                isStyleLoaded = true
            }
        }
    }

    // Update markers and animate bus movement
    LaunchedEffect(mapboxMap, isStyleLoaded, busLocation, userStopLocation) {
        val map = mapboxMap ?: return@LaunchedEffect
        if (!isStyleLoaded) return@LaunchedEffect

        // Setup stop marker
        val currentStopMarker = stopMarker
        if (currentStopMarker == null) {
            val stopIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.ic_user_stop)
            val stopIcon = stopIconBitmap?.let { IconFactory.getInstance(context).fromBitmap(it) }
            val options = MarkerOptions()
                .position(MapboxLatLng(userStopLocation.latitude, userStopLocation.longitude))
                .title("User Stop")
            if (stopIcon != null) {
                options.icon(stopIcon)
            }
            stopMarker = map.addMarker(options)
        } else {
            currentStopMarker.position = MapboxLatLng(userStopLocation.latitude, userStopLocation.longitude)
        }

        // Setup bus marker & animation
        if (busLocation != null) {
            val newPos = MapboxLatLng(busLocation.latitude, busLocation.longitude)
            val stopPos = MapboxLatLng(userStopLocation.latitude, userStopLocation.longitude)

            // Update route polyline
            val currentPolyline = routePolyline
            if (currentPolyline == null) {
                val polyOptions = com.mapbox.mapboxsdk.annotations.PolylineOptions()
                    .add(newPos, stopPos)
                    .color(android.graphics.Color.parseColor("#2E7D32"))
                    .width(7f)
                routePolyline = map.addPolyline(polyOptions)
            } else {
                currentPolyline.points = listOf(newPos, stopPos)
            }

            val currentBusMarker = busMarker
            if (currentBusMarker == null) {
                val busIconBitmap = getBitmapFromVectorDrawable(context, R.drawable.ic_bus)
                val busIcon = busIconBitmap?.let { IconFactory.getInstance(context).fromBitmap(it) }
                val options = MarkerOptions()
                    .position(newPos)
                    .title("Live Bus")
                if (busIcon != null) {
                    options.icon(busIcon)
                }
                busMarker = map.addMarker(options)

                try {
                    val bounds = com.mapbox.mapboxsdk.geometry.LatLngBounds.Builder()
                        .include(newPos)
                        .include(stopPos)
                        .build()
                    map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100, 150, 100, 850))
                } catch (e: Exception) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(newPos, 14.0))
                }
            } else {
                val startPos = currentBusMarker.position
                animateMarker(currentBusMarker, startPos, newPos)
            }
        }
    }

    if (mapView != null) {
        AndroidView(
            factory = { mapView },
            modifier = modifier
        )
    }
}

private fun animateMarker(marker: Marker, start: MapboxLatLng, end: MapboxLatLng) {
    val animator = ValueAnimator.ofFloat(0f, 1f)
    animator.duration = 1000
    animator.interpolator = LinearInterpolator()
    animator.addUpdateListener { valueAnimator ->
        val fraction = valueAnimator.animatedFraction
        val lat = (end.latitude - start.latitude) * fraction + start.latitude
        val lng = (end.longitude - start.longitude) * fraction + start.longitude
        marker.position = MapboxLatLng(lat, lng)
    }
    animator.start()
}

private fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap? {
    val drawable = ContextCompat.getDrawable(context, drawableId) ?: return null
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth.takeIf { it > 0 } ?: 64,
        drawable.intrinsicHeight.takeIf { it > 0 } ?: 64,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}
