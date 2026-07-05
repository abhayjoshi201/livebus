package livebus.driver.service;

import livebus.admin.model.Bus;
import livebus.admin.model.Route;
import livebus.admin.repository.BusRepository;
import livebus.admin.repository.RouteRepository;
import livebus.admin.repository.StopRepository;
import livebus.driver.dto.LiveLocationMessage;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;

@Service
public class DriverService {

    private final TripRepository tripRepository;
    private final RouteRepository routeRepository;
    private final BusRepository busRepository;
    private final UserRepository userRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    private final SimpMessagingTemplate messagingTemplate;
    private final StopRepository stopRepository;
    private final int R = 6371000; 

    public DriverService(TripRepository tripRepository, RouteRepository routeRepository,
                         BusRepository busRepository, UserRepository userRepository,
                         SimpMessagingTemplate messagingTemplate, StopRepository stopRepository) {
        this.tripRepository = tripRepository;
        this.routeRepository = routeRepository;
        this.busRepository = busRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.stopRepository = stopRepository;
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
        
        System.out.println("🕵️ DEBUG: Bus is currently hunting for Stop Sequence: " + activeTrip.getNextStopSequence());

        stopRepository.findByRouteIdAndStopSequence(activeTrip.getRoute().getId(), activeTrip.getNextStopSequence())
            .ifPresentOrElse(nextStop -> {
                double stopLat = nextStop.getLocation().getY();
                double stopLon = nextStop.getLocation().getX();
                
                double distanceToStop = calculateDistanceInMeters(
                        request.latitude(), request.longitude(), 
                        stopLat, stopLon
                );

                System.out.println("🕵️ DEBUG: Distance to " + nextStop.getStopName() + " is exactly " + distanceToStop + " meters.");

                if (distanceToStop <= 50.0) {
                    activeTrip.setNextStopSequence(activeTrip.getNextStopSequence() + 1);
                    System.out.println("✅ GEOFENCE TRIGGERED: Bus arrived at Stop " + (activeTrip.getNextStopSequence() - 1) + ". Now heading to Stop " + activeTrip.getNextStopSequence());
                } else {
                    System.out.println("❌ DEBUG: Bus is too far away to trigger the geofence.");
                }
            }, () -> {
                System.out.println("⚠️ DEBUG: Stop Sequence " + activeTrip.getNextStopSequence() + " was NOT FOUND in the database!");
            });
        
        
        tripRepository.save(activeTrip);

        String destination = "/topic/routes/" + activeTrip.getRoute().getId();
        
        LiveLocationMessage liveData = new LiveLocationMessage(
                activeTrip.getId(),
                activeTrip.getBus().getLicensePlate(),
                request.latitude(),
                request.longitude()
        );

        messagingTemplate.convertAndSend(destination, liveData);
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


    private double calculateDistanceInMeters(double lat1, double lon1, double lat2, double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; 
    }
}