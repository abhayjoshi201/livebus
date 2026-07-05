package livebus.passenger.repository;

import livebus.admin.model.Stop;
import livebus.passenger.dto.NearestStopProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PassengerStopQueryRepository extends JpaRepository<Stop, UUID> {

    @Query(value = """
        SELECT 
            CAST(s.id AS varchar) AS stopId,
            s.stop_name AS stopName,
            r.route_number AS routeName,
            ST_Y(CAST(s.location AS geometry)) AS latitude,
            ST_X(CAST(s.location AS geometry)) AS longitude,
            ST_DistanceSphere(CAST(s.location AS geometry), ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)) AS distanceMeters
        FROM stops s
        JOIN routes r ON s.route_id = r.id
        ORDER BY ST_DistanceSphere(CAST(s.location AS geometry), ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)) ASC
        LIMIT 5
    """, nativeQuery = true)
    List<NearestStopProjection> findNearestStops(@Param("latitude") double latitude, @Param("longitude") double longitude);
}