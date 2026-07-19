package livebus.tracking;

import java.util.UUID;

/**
 * TelemetryPayload:
 * Represents a single coordinate/speed beacon crowdsourced from either a verified
 * Conductor/Driver phone or an anonymous Citizen commuter riding on the bus.
 */
public record TelemetryPayload(
        UUID tripId,
        UUID routeId,
        String vehicleRegPlate, // e.g., "UK-07-PA-4052" or "PVT-SURESH-04"
        double latitude,
        double longitude,
        long timestamp,         // Epoch millis when recorded on edge device
        double speedKmh,
        boolean isVerifiedConductor, // True if submitted by logged-in Conductor/Driver
        float accuracyMeters
) {}
