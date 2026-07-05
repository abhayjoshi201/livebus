package livebus.driver.dto;

import java.util.UUID;

public record StartTripRequest(UUID routeId, UUID busId) {
}