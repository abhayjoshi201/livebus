package livebus.admin.repository;

import livebus.admin.model.Bus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface BusRepository extends JpaRepository<Bus, UUID> {
}