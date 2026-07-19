package livebus.tracking;

import livebus.driver.dto.LiveLocationMessage;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * CrowdsourceFusionEngine (Option C / Pillar 3 Consensus Engine):
 * Aggregates multi-passenger and conductor telemetry beacons for public and private bus trips.
 * 
 * Key Features:
 * 1. Conductor Priority: Verified Conductor/Driver telemetry always takes precedence when available.
 * 2. Outlier Rejection: Filters out impossible mountain speeds (> 65 km/h) and cross-track drift (> 150m).
 * 3. Geometric Median Consensus: When the conductor is offline or absent, computes the spatial
 *    Geometric Median across all active citizens inside the bus, naturally filtering out passengers
 *    who disembarked at previous checkpoints.
 */
@Service
public class CrowdsourceFusionEngine {

    private static final double MAX_DEVIATION_METERS = 150.0; // Max allowed cross-track drift from road
    private static final double MAX_SPEED_KMH = 65.0;         // Max realistic mountain/corridor speed
    private static final long MAX_STALE_AGE_MS = 60000;       // Stale cutoff (60 seconds)

    private final SimpMessagingTemplate messagingTemplate;
    
    // In-memory sliding window of active telemetry beacons per trip
    private final Map<UUID, List<TelemetryPayload>> activeTripTelemetry = new ConcurrentHashMap<>();

    public CrowdsourceFusionEngine(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Fuses new incoming telemetry payloads with existing sliding window data for a trip,
     * computes the authoritative geometric center of mass, and broadcasts via STOMP WebSocket.
     */
    public Coordinate fuseAndBroadcast(UUID tripId,
                                       UUID routeId,
                                       String vehicleRegPlate,
                                       List<TelemetryPayload> incomingPayloads,
                                       List<Coordinate> routePolyline) {
        if (incomingPayloads == null || incomingPayloads.isEmpty()) return null;

        long now = System.currentTimeMillis();

        // 1. Merge and clean up stale sliding window data
        activeTripTelemetry.compute(tripId, (key, existing) -> {
            List<TelemetryPayload> merged = existing != null ? new ArrayList<>(existing) : new ArrayList<>();
            merged.addAll(incomingPayloads);
            // Retain only samples within last 60 seconds
            return merged.stream()
                    .filter(p -> (now - p.timestamp()) <= MAX_STALE_AGE_MS)
                    .collect(Collectors.toList());
        });

        List<TelemetryPayload> currentWindow = activeTripTelemetry.getOrDefault(tripId, Collections.emptyList());
        if (currentWindow.isEmpty()) return null;

        // 2. Check for Conductor Priority (Verified Conductor / Driver on this trip)
        Optional<TelemetryPayload> conductorPayload = currentWindow.stream()
                .filter(TelemetryPayload::isVerifiedConductor)
                .filter(p -> p.speedKmh() <= MAX_SPEED_KMH)
                .max(Comparator.comparingLong(TelemetryPayload::timestamp));

        Coordinate authoritativeCoord;
        if (conductorPayload.isPresent()) {
            TelemetryPayload cp = conductorPayload.get();
            authoritativeCoord = new Coordinate(cp.longitude(), cp.latitude());
            System.out.println("⭐ CONDUCTOR PRIORITY: Trip " + tripId + " using verified conductor location: (" 
                    + cp.latitude() + ", " + cp.longitude() + ")");
        } else {
            // 3. Citizen Crowdsourced Consensus (Geometric Median)
            List<Coordinate> validCoordinates = new ArrayList<>();
            for (TelemetryPayload p : currentWindow) {
                if (p.speedKmh() > MAX_SPEED_KMH) continue;

                // Check cross-track drift from known mountain road polyline
                double drift = GeoMath.distanceToPolyline(p.latitude(), p.longitude(), routePolyline);
                if (drift > MAX_DEVIATION_METERS) {
                    System.out.println("⚠️ OUTLIER REJECTED: Citizen drifted " + drift + "m off route polyline.");
                    continue;
                }
                validCoordinates.add(new Coordinate(p.longitude(), p.latitude()));
            }

            if (validCoordinates.isEmpty()) return null;

            // Compute Weiszfeld's Geometric Median (center of mass ignoring disembarked commuters)
            authoritativeCoord = GeoMath.calculateGeometricMedian(validCoordinates);
            System.out.println("🟢 CROWDSOURCE CONSENSUS (" + validCoordinates.size() + " citizens): Trip " 
                    + tripId + " resolved to Geometric Median: (" + authoritativeCoord.y + ", " + authoritativeCoord.x + ")");
        }

        // 4. Broadcast authoritative location via STOMP WebSocket topic (/topic/routes/{routeId})
        if (routeId != null && authoritativeCoord != null) {
            LiveLocationMessage liveMsg = new LiveLocationMessage(
                    tripId,
                    vehicleRegPlate != null ? vehicleRegPlate : "CROWD-LIVE",
                    authoritativeCoord.y, // latitude
                    authoritativeCoord.x  // longitude
            );
            messagingTemplate.convertAndSend("/topic/routes/" + routeId, liveMsg);
        }

        return authoritativeCoord;
    }
}
