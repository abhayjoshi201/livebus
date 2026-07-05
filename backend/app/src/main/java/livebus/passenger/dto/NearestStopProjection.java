package livebus.passenger.dto;

public interface NearestStopProjection {
    String getStopId();
    String getStopName();
    String getRouteName();
    Double getLatitude();
    Double getLongitude();
    Double getDistanceMeters();
}