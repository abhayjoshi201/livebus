package livebus.admin.service;

import java.util.List;
import livebus.admin.dto.RouteRequest;
import livebus.admin.dto.StopRequest;
import livebus.admin.model.Route;
import livebus.admin.model.Stop;
import livebus.admin.repository.RouteRepository;
import livebus.admin.repository.StopRepository;
import livebus.admin.model.Bus;
import livebus.admin.dto.BusRequest;
import livebus.admin.dto.DriverRequest;
import livebus.admin.repository.BusRepository;
import livebus.security.model.Role;
import livebus.security.model.User;
import livebus.security.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AdminService {

    private final RouteRepository routeRepository;
    private final StopRepository stopRepository;
    private final BusRepository busRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    public AdminService(RouteRepository routeRepository, 
                        StopRepository stopRepository, 
                        BusRepository busRepository,
                        UserRepository userRepository,
                        PasswordEncoder passwordEncoder) {
        this.routeRepository = routeRepository;
        this.stopRepository = stopRepository;
        this.busRepository = busRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Route createRoute(RouteRequest request) {
        Route route = new Route(request.routeNumber(), request.startPoint(), request.endPoint());
        return routeRepository.save(route);
    }

    public Stop addStopToRoute(UUID routeId, StopRequest request) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new RuntimeException("Route not found!"));

        Stop stop = new Stop();
        stop.setRoute(route);
        stop.setStopName(request.stopName());
        stop.setStopSequence(request.stopSequence());

       
        Point location = geometryFactory.createPoint(new Coordinate(request.longitude(), request.latitude()));
        stop.setLocation(location);

        return stopRepository.save(stop);
    }

    public Bus createBus(BusRequest request) {
        Bus bus = new Bus(request.licensePlate(), request.capacity(), request.busType());
        return busRepository.save(bus);
    }

    public User createDriver(DriverRequest request) {
       
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new RuntimeException("Username already exists!");
        }

        User driver = new User(
                request.username(),
                passwordEncoder.encode(request.rawPassword()), 
                Role.DRIVER 
        );
        
        return userRepository.save(driver);
    }

    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    public List<Stop> getStopsByRoute(UUID routeId) {
        return stopRepository.findByRouteIdOrderByStopSequenceAsc(routeId);
    }

    public List<Bus> getAllBuses() {
        return busRepository.findAll();
    }

    public List<User> getAllDrivers() {
        return userRepository.findByRole(Role.DRIVER);
    }
}