package com.example.livebus.ui.tracking

import com.example.livebus.data.RouteStageSegment
import kotlin.math.*

/**
 * ProjectedPoint holds the orthogonally snapped coordinate along with the fractional
 * distance along that specific segment.
 */
data class ProjectedPoint(
    val snappedLatLng: LatLng,
    val segmentFraction: Double,
    val distanceToSegmentMeters: Double
)

/**
 * TransitPhysicsEngine:
 * Local zero-cost spatial and physics engine that replaces Google Directions / Distance Matrix APIs.
 * 
 * Key Capabilities:
 * 1. Polyline Decoding: Decodes compressed ASCII polylines (_p~iF~ps|U_ulLnnqC...) offline.
 * 2. Vector Snap-to-Polyline: Orthogonally projects raw/noisy coordinates onto road vectors.
 * 3. Terrain-Aware ETA Engine: Computes dynamic ETAs based on uphill vs. downhill mountain speed profiles.
 */
object TransitPhysicsEngine {

    private const val EARTH_RADIUS_METERS = 6371000.0

    /**
     * Computes great-circle distance between two coordinates in meters using the Haversine formula.
     */
    fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val radLat1 = Math.toRadians(lat1)
        val radLat2 = Math.toRadians(lat2)

        val a = sin(dLat / 2).pow(2.0) + cos(radLat1) * cos(radLat2) * sin(dLon / 2).pow(2.0)
        val c = 2.0 * atan2(sqrt(a), sqrt(1.0 - a))
        return EARTH_RADIUS_METERS * c
    }

    /**
     * Orthogonally projects point [p] onto the line segment between [a] and [b].
     * Returns the snapped coordinate, parameter t in [0, 1], and the lateral drift distance.
     */
    fun projectOntoSegment(p: LatLng, a: LatLng, b: LatLng): ProjectedPoint {
        // Convert to approximate Euclidean plane in meters around segment origin (a)
        val cosLat = cos(Math.toRadians(a.latitude))
        val dx = (b.longitude - a.longitude) * Math.toRadians(1.0) * EARTH_RADIUS_METERS * cosLat
        val dy = (b.latitude - a.latitude) * Math.toRadians(1.0) * EARTH_RADIUS_METERS

        val px = (p.longitude - a.longitude) * Math.toRadians(1.0) * EARTH_RADIUS_METERS * cosLat
        val py = (p.latitude - a.latitude) * Math.toRadians(1.0) * EARTH_RADIUS_METERS

        val segmentLenSq = dx * dx + dy * dy
        if (segmentLenSq == 0.0) {
            // Segment has zero length (a == b)
            val dist = haversine(p.latitude, p.longitude, a.latitude, a.longitude)
            return ProjectedPoint(a, 0.0, dist)
        }

        // Project vector (px, py) onto segment vector (dx, dy)
        var t = (px * dx + py * dy) / segmentLenSq
        t = t.coerceIn(0.0, 1.0)

        val snappedLat = a.latitude + t * (b.latitude - a.latitude)
        val snappedLon = a.longitude + t * (b.longitude - a.longitude)
        val snapped = LatLng(snappedLat, snappedLon)

        val driftMeters = haversine(p.latitude, p.longitude, snappedLat, snappedLon)
        return ProjectedPoint(snapped, t, driftMeters)
    }

    /**
     * Snaps a raw coordinate [point] onto a polyline path [waypoints].
     * Returns the exact 1D accumulated distance from the polyline origin (in meters).
     */
    fun snapToRoutePolyline(point: LatLng, waypoints: List<LatLng>): Double {
        if (waypoints.isEmpty()) return 0.0
        if (waypoints.size == 1) return 0.0

        var bestAccumulatedDistance = 0.0
        var minDriftMeters = Double.MAX_VALUE
        var currentRunningDistance = 0.0

        for (i in 0 until waypoints.size - 1) {
            val a = waypoints[i]
            val b = waypoints[i + 1]
            val segmentLen = haversine(a.latitude, a.longitude, b.latitude, b.longitude)

            val projected = projectOntoSegment(point, a, b)
            if (projected.distanceToSegmentMeters < minDriftMeters) {
                minDriftMeters = projected.distanceToSegmentMeters
                bestAccumulatedDistance = currentRunningDistance + (projected.segmentFraction * segmentLen)
            }
            currentRunningDistance += segmentLen
        }

        return bestAccumulatedDistance
    }

    /**
     * Terrain-Aware ETA Engine (Pillar 4):
     * Distance along mountain roads does not linearly equal time.
     * Uphill climbs (e.g., Dehradun -> Mussoorie) average 22 km/h, while downhill slopes average 38 km/h.
     */
    fun calculateETA(currentDistanceMeters: Double, targetStopMeters: Double, segment: RouteStageSegment): Int {
        val distanceRemainingKm = ((targetStopMeters - currentDistanceMeters) / 1000.0).coerceAtLeast(0.0)
        if (distanceRemainingKm <= 0.0) return 0

        // Select historical speed specific to the direction of this hilly segment
        val effectiveSpeedKmh = if (segment.isUphill) segment.uphillAvgSpeedKmh else segment.downhillAvgSpeedKmh
        val timeHours = distanceRemainingKm / effectiveSpeedKmh.toDouble()

        return (timeHours * 60.0).roundToInt().coerceAtLeast(1)
    }

    /**
     * Decodes a Google Encoded Polyline ASCII string into a list of LatLng coordinates.
     * Works 100% offline without needing third-party libraries.
     */
    fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                if (index >= len) break
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if ((result and 1) != 0) -(result shr 1) else (result shr 1)
            lat += dlat

            shift = 0
            result = 0
            do {
                if (index >= len) break
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if ((result and 1) != 0) -(result shr 1) else (result shr 1)
            lng += dlng

            poly.add(LatLng((lat / 1E5), (lng / 1E5)))
        }
        return poly
    }
}
