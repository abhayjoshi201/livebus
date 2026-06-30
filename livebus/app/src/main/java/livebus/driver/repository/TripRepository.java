package livebus.driver.repository;

import livebus.driver.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<Trip, UUID> {
    
    Optional<Trip> findByDriverIdAndStatus(UUID driverId, String status);
    List<Trip> findByRouteIdAndStatus(UUID routeId, String status);

    @Query(value = """
        SELECT 
            t.id AS "tripId", 
            b.license_plate AS "licensePlate", 
            ST_Y(CAST(t.current_location AS geometry)) AS "latitude", 
            ST_X(CAST(t.current_location AS geometry)) AS "longitude", 
            ST_DistanceSphere(CAST(t.current_location AS geometry), CAST(s.location AS geometry)) AS "distanceMeters" 
        FROM trip t 
        JOIN bus b ON t.bus_id = b.id 
        JOIN stop s ON s.id = CAST(:stopId AS uuid) 
        WHERE t.route_id = CAST(:routeId AS uuid) 
          AND t.status = 'IN_TRANSIT' 
          AND t.current_location IS NOT NULL
    """, nativeQuery = true)
    List<TripDistanceProjection> findActiveTripsWithDistance(
        @Param("routeId") UUID routeId, 
        @Param("stopId") UUID stopId
    );
}