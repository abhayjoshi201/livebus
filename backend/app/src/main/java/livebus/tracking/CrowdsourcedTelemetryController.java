package livebus.tracking;

import livebus.admin.model.Route;
import livebus.admin.model.Stop;
import livebus.admin.repository.RouteRepository;
import livebus.admin.repository.StopRepository;
import livebus.driver.model.Trip;
import livebus.driver.repository.TripRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * CrowdsourcedTelemetryController:
 * REST endpoints accepting batch or real-time telemetry from citizen and conductor edge devices.
 * Supports store-and-forward batch uploads when devices exit mountain dead zones and reconnect to 4G/Wi-Fi.
 */
@RestController
@RequestMapping("/api/v1/tracking/crowdsource")
public class CrowdsourcedTelemetryController {

    private final CrowdsourceFusionEngine fusionEngine;
    private final TripRepository tripRepository;
    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public CrowdsourcedTelemetryController(CrowdsourceFusionEngine fusionEngine,
                                           TripRepository tripRepository,
                                           RouteRepository routeRepository,
                                           StopRepository stopRepository) {
        this.fusionEngine = fusionEngine;
        this.tripRepository = tripRepository;
        this.routeRepository = routeRepository;
        this.stopRepository = stopRepository;
    }

    /**
     * Batch Telemetry Upload Endpoint:
     * Receives queued location snapshots submitted by commuter/conductor devices.
     */
    @PostMapping("/batch")
    public ResponseEntity<String> submitBatchTelemetry(@RequestBody List<TelemetryPayload> payloads) {
        if (payloads == null || payloads.isEmpty()) {
            return ResponseEntity.badRequest().body("Empty telemetry payload list provided.");
        }

        // Group payloads by Trip ID so we run consensus independently per active trip
        payloads.stream()
                .filter(p -> p.tripId() != null)
                .collect(java.util.stream.Collectors.groupingBy(TelemetryPayload::tripId))
                .forEach((tripId, tripPayloads) -> {
                    tripRepository.findById(tripId).ifPresent(trip -> {
                        Route route = trip.getRoute();
                        UUID routeId = route != null ? route.getId() : null;

                        // Build route polyline from stops for cross-track drift filtering
                        List<Coordinate> routeWaypoints = new ArrayList<>();
                        if (routeId != null) {
                            List<Stop> stops = stopRepository.findByRouteIdOrderByStopSequenceAsc(routeId);
                            for (Stop stop : stops) {
                                if (stop.getLocation() != null) {
                                    routeWaypoints.add(new Coordinate(stop.getLocation().getX(), stop.getLocation().getY()));
                                }
                            }
                        }

                        // Run Crowdsource Consensus & STOMP Broadcast
                        Coordinate authoritativeCoord = fusionEngine.fuseAndBroadcast(
                                tripId,
                                routeId,
                                trip.getBus() != null ? trip.getBus().getLicensePlate() : "LIVE-BUS",
                                tripPayloads,
                                routeWaypoints
                        );

                        if (authoritativeCoord != null) {
                            Point newLocation = geometryFactory.createPoint(authoritativeCoord);
                            trip.setCurrentLocation(newLocation);
                            tripRepository.save(trip);
                        }
                    });
                });

        return ResponseEntity.ok("Successfully processed and fused " + payloads.size() + " crowdsourced beacons.");
    }
}
