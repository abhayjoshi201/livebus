package livebus.admin.dto;

import java.util.UUID;

public record StopResponse(
        UUID id, 
        String stopName, 
        double latitude, 
        double longitude, 
        int stopSequence
) {}