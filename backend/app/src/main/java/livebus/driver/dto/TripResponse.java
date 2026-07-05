package livebus.driver.dto;

import java.util.UUID;

public record TripResponse(
        UUID tripId, 
        String busLicensePlate, 
        String status
) {}