package livebus.admin.dto;

public record StopRequest(String stopName, double latitude, double longitude, int stopSequence) {
}