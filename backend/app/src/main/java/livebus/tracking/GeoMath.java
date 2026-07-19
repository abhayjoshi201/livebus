package livebus.tracking;

import org.locationtech.jts.geom.Coordinate;
import java.util.List;

/**
 * GeoMath:
 * Spatial calculations and spatial clustering algorithms for crowdsourced consensus.
 * Replaces simple arithmetic averages with robust spatial geometric medians.
 */
public final class GeoMath {

    private static final double EARTH_RADIUS_METERS = 6371000.0;

    private GeoMath() {}

    /**
     * Haversine great-circle distance in meters.
     */
    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }

    /**
     * Computes the cross-track orthogonal distance (drift in meters) from point (pLat, pLon)
     * to a line segment defined by (aLat, aLon) and (bLat, bLon).
     */
    public static double distanceToSegment(double pLat, double pLon,
                                           double aLat, double aLon,
                                           double bLat, double bLon) {
        double cosLat = Math.cos(Math.toRadians(aLat));
        double dx = (bLon - aLon) * Math.toRadians(1.0) * EARTH_RADIUS_METERS * cosLat;
        double dy = (bLat - aLat) * Math.toRadians(1.0) * EARTH_RADIUS_METERS;

        double px = (pLon - aLon) * Math.toRadians(1.0) * EARTH_RADIUS_METERS * cosLat;
        double py = (pLat - aLat) * Math.toRadians(1.0) * EARTH_RADIUS_METERS;

        double segLenSq = dx * dx + dy * dy;
        if (segLenSq == 0.0) {
            return haversine(pLat, pLon, aLat, aLon);
        }

        double t = (px * dx + py * dy) / segLenSq;
        t = Math.max(0.0, Math.min(1.0, t));

        double snappedLat = aLat + t * (bLat - aLat);
        double snappedLon = aLon + t * (bLon - aLon);
        return haversine(pLat, pLon, snappedLat, snappedLon);
    }

    /**
     * Finds the minimum cross-track distance in meters from point to any segment along a polyline.
     */
    public static double distanceToPolyline(double lat, double lon, List<Coordinate> waypoints) {
        if (waypoints == null || waypoints.isEmpty()) return 0.0;
        if (waypoints.size() == 1) return haversine(lat, lon, waypoints.get(0).y, waypoints.get(0).x);

        double minDist = Double.MAX_VALUE;
        for (int i = 0; i < waypoints.size() - 1; i++) {
            Coordinate a = waypoints.get(i);
            Coordinate b = waypoints.get(i + 1);
            double dist = distanceToSegment(lat, lon, a.y, a.x, b.y, b.x);
            if (dist < minDist) {
                minDist = dist;
            }
        }
        return minDist;
    }

    /**
     * Calculates the Spatial Geometric Median of a list of coordinates using Weiszfeld's algorithm.
     * Unlike arithmetic mean, Geometric Median naturally resists spatial anomalies and outliers (up to 50% breakdown).
     * If 10 commuters are on the bus and 3 commuters disembarked and walked away, this locates the exact bus.
     */
    public static Coordinate calculateGeometricMedian(List<Coordinate> points) {
        if (points == null || points.isEmpty()) return null;
        if (points.size() == 1) return points.get(0);
        if (points.size() == 2) {
            return new Coordinate(
                    (points.get(0).x + points.get(1).x) / 2.0,
                    (points.get(0).y + points.get(1).y) / 2.0
            );
        }

        // Start with centroid approximation as initial guess
        double sumX = 0.0;
        double sumY = 0.0;
        for (Coordinate p : points) {
            sumX += p.x;
            sumY += p.y;
        }
        double currentX = sumX / points.size();
        double currentY = sumY / points.size();

        // Weiszfeld iterative convergence (max 20 iterations or epsilon 1e-6)
        for (int iter = 0; iter < 20; iter++) {
            double numX = 0.0;
            double numY = 0.0;
            double denom = 0.0;

            for (Coordinate p : points) {
                double dist = haversine(currentY, currentX, p.y, p.x);
                if (dist < 1e-4) dist = 1e-4; // Prevent division by zero if guess coincides with a point
                double weight = 1.0 / dist;
                numX += p.x * weight;
                numY += p.y * weight;
                denom += weight;
            }

            if (denom == 0.0) break;
            double nextX = numX / denom;
            double nextY = numY / denom;

            if (Math.abs(nextX - currentX) < 1e-7 && Math.abs(nextY - currentY) < 1e-7) {
                currentX = nextX;
                currentY = nextY;
                break;
            }
            currentX = nextX;
            currentY = nextY;
        }

        return new Coordinate(currentX, currentY);
    }
}
