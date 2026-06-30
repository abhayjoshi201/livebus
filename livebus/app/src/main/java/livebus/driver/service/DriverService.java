package livebus.driver.service;

import livebus.admin.model.Bus;
import livebus.admin.model.Route;
import livebus.admin.repository.BusRepository;
import livebus.admin.repository.RouteRepository;
import livebus.driver.dto.LocationUpdateRequest;
import livebus.driver.dto.StartTripRequest;
import livebus.driver.model.Trip;
import livebus.driver.repository.TripRepository;
import livebus.security.model.User;
import livebus.security.repository.UserRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class DriverService {

    private final TripRepository tripRepository;
    private final RouteRepository routeRepository;
    private final BusRepository busRepository;
    private final UserRepository userRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public DriverService(TripRepository tripRepository, RouteRepository routeRepository,
                         BusRepository busRepository, UserRepository userRepository) {
        this.tripRepository = tripRepository;
        this.routeRepository = routeRepository;
        this.busRepository = busRepository;
        this.userRepository = userRepository;
    }

    public Trip startTrip(String username, StartTripRequest request) {
        User driver = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

       
        if (tripRepository.findByDriverIdAndStatus(driver.getId(), "IN_TRANSIT").isPresent()) {
            throw new RuntimeException("Driver already has an active trip!");
        }

        Route route = routeRepository.findById(request.routeId())
                .orElseThrow(() -> new RuntimeException("Route not found"));
        Bus bus = busRepository.findById(request.busId())
                .orElseThrow(() -> new RuntimeException("Bus not found"));

        Trip trip = new Trip();
        trip.setDriver(driver);
        trip.setRoute(route);
        trip.setBus(bus);
        trip.setStatus("IN_TRANSIT");
        trip.setStartTime(LocalDateTime.now());

        return tripRepository.save(trip);
    }

    public void updateLocation(String username, LocationUpdateRequest request) {
        User driver = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        
        Trip activeTrip = tripRepository.findByDriverIdAndStatus(driver.getId(), "IN_TRANSIT")
                .orElseThrow(() -> new RuntimeException("No active trip found for this driver!"));

        
        Point newLocation = geometryFactory.createPoint(new Coordinate(request.longitude(), request.latitude()));
        activeTrip.setCurrentLocation(newLocation);

        tripRepository.save(activeTrip);
    }

    public void endTrip(String username) {
        User driver = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

        Trip activeTrip = tripRepository.findByDriverIdAndStatus(driver.getId(), "IN_TRANSIT")
                .orElseThrow(() -> new RuntimeException("No active trip found to end!"));

        activeTrip.setStatus("COMPLETED");
        activeTrip.setEndTime(LocalDateTime.now());

        tripRepository.save(activeTrip);
    }
}