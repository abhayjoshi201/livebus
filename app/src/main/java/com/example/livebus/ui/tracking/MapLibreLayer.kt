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
    var busMarker by remember { mutableStateOf<Marker?>(null) }
    var stopMarker by remember { mutableStateOf<Marker?>(null) }

    // MapTiler styles
    val mapStyleUrl = if (isDarkTheme) {
        "https://api.maptiler.com/maps/darkmatter/style.json?key=get_your_own_OpIi9ZULNHzrESv6T2vL"
    } else {
        "https://api.maptiler.com/maps/streets/style.json?key=get_your_own_OpIi9ZULNHzrESv6T2vL"
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
    LaunchedEffect(mapView, mapStyleUrl) {
        mapView?.getMapAsync { map ->
            mapboxMap = map
            map.setStyle(Style.Builder().fromUri(mapStyleUrl)) { style ->
                // Style loaded
            }
        }
    }

    // Update markers and animate bus movement
    LaunchedEffect(mapboxMap, busLocation, userStopLocation) {
        val map = mapboxMap ?: return@LaunchedEffect

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
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(newPos, 14.0))
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
