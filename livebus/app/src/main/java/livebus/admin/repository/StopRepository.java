package livebus.admin.repository;

import livebus.admin.model.Stop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StopRepository extends JpaRepository<Stop, UUID> {
    
    List<Stop> findByRouteIdOrderByStopSequenceAsc(UUID routeId);
    List<Stop> findByStopNameContainingIgnoreCase(String keyword);
    Optional<Stop> findByRouteIdAndStopSequence(UUID routeId, Integer stopSequence);
}