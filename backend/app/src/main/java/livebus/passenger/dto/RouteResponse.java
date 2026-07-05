package livebus.passenger.dto;

import java.util.UUID;

public record RouteResponse(UUID id, String routeNumber, String startPoint, String endPoint) {
}