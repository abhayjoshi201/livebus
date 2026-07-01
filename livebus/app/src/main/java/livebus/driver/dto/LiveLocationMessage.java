package livebus.driver.dto;

import java.util.UUID;

public record LiveLocationMessage(
        UUID tripId, 
        String licensePlate, 
        Double latitude, 
        Double longitude
) {}