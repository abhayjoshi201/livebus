package livebus.passenger.service;

import livebus.admin.model.Route;
import livebus.admin.model.Stop;
import livebus.admin.repository.RouteRepository;
import livebus.admin.repository.StopRepository;
import livebus.driver.model.Trip;
import livebus.driver.repository.TripRepository;
import livebus.passenger.dto.LiveLocationResponse;
import livebus.passenger.dto.RouteResponse;
import livebus.passenger.dto.StopResponse;
import livebus.passenger.dto.EtaResponse;
import livebus.driver.repository.TripDistanceProjection;
import livebus.passenger.dto.NearestStopProjection;
import livebus.passenger.repository.PassengerStopQueryRepository;
import livebus.driver.repository.TripRepository; 
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.HashSet;


@Service
public class PassengerService {

    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;
    private final TripRepository tripRepository;
    private final PassengerStopQueryRepository passengerStopQueryRepository;
  

    public PassengerService(RouteRepository routeRepository, StopRepository stopRepository, TripRepository tripRepository,
                            PassengerStopQueryRepository passengerStopQueryRepository
     ) {
        this.routeRepository = routeRepository;
        this.stopRepository = stopRepository;
        this.tripRepository = tripRepository;
        this.passengerStopQueryRepository = passengerStopQueryRepository;
    }

    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    public List<StopResponse> getStopsForRoute(UUID routeId) {
        List<Stop> stops = stopRepository.findByRouteIdOrderByStopSequenceAsc(routeId);
        
        return stops.stream().map(stop -> {
            double lat = 0.0;
            double lon = 0.0;
            
            
            if (stop.getLocation() != null) {
                lat = stop.getLocation().getY();
                lon = stop.getLocation().getX();
            }
            
            return new StopResponse(
                    stop.getId(), 
                    stop.getStopName(), 
                    lat, 
                    lon, 
                    stop.getStopSequence()
            );
        }).collect(Collectors.toList());
    }

    public List<LiveLocationResponse> getLiveBusesForRoute(UUID routeId) {
        
        List<Trip> activeTrips = tripRepository.findByRouteIdAndStatus(routeId, "IN_TRANSIT");
        
        return activeTrips.stream()
                .filter(trip -> trip.getCurrentLocation() != null)
                .map(trip -> new LiveLocationResponse(
                        trip.getCurrentLocation().getY(), 
                        trip.getCurrentLocation().getX(), 
                        trip.getBus().getLicensePlate()
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true) 
    public List<RouteResponse> searchRoutes(String keyword) {
        Set<Route> matchingRoutes = new HashSet<>();

        matchingRoutes.addAll(routeRepository.findByRouteNumberContainingIgnoreCase(keyword));

      
        List<Stop> matchingStops = stopRepository.findByStopNameContainingIgnoreCase(keyword);
        for (Stop stop : matchingStops) {
            matchingRoutes.add(stop.getRoute());
        }
        
        return matchingRoutes.stream().map(route -> 
            new RouteResponse(
                route.getId(), 
                route.getRouteNumber(), 
                route.getStartPoint(), 
                route.getEndPoint()
            )
        ).collect(Collectors.toList());
    }

    public List<EtaResponse> getEtaForStop(UUID routeId, UUID stopId) {
       
        List<TripDistanceProjection> projections = tripRepository.findActiveTripsWithDistance(routeId, stopId);

        
        return projections.stream().map(p -> {
            
            
            double distanceKm = (p.getDistanceMeters() * 1.3) / 1000.0;
            
            
            double speedKmh = 15.0; 
            
            
            double timeHours = distanceKm / speedKmh;
            int etaMinutes = (int) Math.ceil(timeHours * 60);

            
            double roundedDistance = Math.round(distanceKm * 10.0) / 10.0;

            return new EtaResponse(
                    p.getTripId(),
                    p.getLicensePlate(),
                    roundedDistance,
                    etaMinutes,
                    p.getLatitude(),
                    p.getLongitude()
            );
        }).collect(Collectors.toList());
    }


    public List<NearestStopProjection> getNearestStops(double latitude, double longitude) {
        return passengerStopQueryRepository.findNearestStops(latitude, longitude);
    }
}