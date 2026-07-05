package livebus.driver.repository;

import java.util.UUID;

public interface TripDistanceProjection {
    UUID getTripId();
    String getLicensePlate();
    Double getLatitude();
    Double getLongitude();
    Double getDistanceMeters();
}