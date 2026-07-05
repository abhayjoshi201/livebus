package livebus.passenger.dto;

import java.util.UUID;

public record EtaResponse(
        UUID tripId, 
        String licensePlate, 
        double distanceKm, 
        int etaMinutes, 
        double latitude, 
        double longitude
) {}