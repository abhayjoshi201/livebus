package livebus.admin.controller;

import java.util.List;
import livebus.admin.dto.RouteRequest;
import livebus.admin.dto.StopRequest;
import livebus.admin.model.Route;
import livebus.admin.model.Stop;
import livebus.admin.model.Bus;
import livebus.admin.dto.BusRequest;
import livebus.admin.service.AdminService;
import livebus.admin.dto.DriverRequest;
import livebus.security.model.User;
import livebus.admin.dto.StopResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/routes")
    public ResponseEntity<Route> createRoute(@RequestBody RouteRequest request) {
        Route savedRoute = adminService.createRoute(request);
        return ResponseEntity.ok(savedRoute);
    }

    @PostMapping("/routes/{routeId}/stops")
    public ResponseEntity<String> addStop(@PathVariable UUID routeId, @RequestBody StopRequest request) {
        adminService.addStopToRoute(routeId, request);
        return ResponseEntity.ok("Stop added successfully to route!");
    }

    @PostMapping("/buses")
    public ResponseEntity<Bus> createBus(@RequestBody BusRequest request) {
        Bus savedBus = adminService.createBus(request);
        return ResponseEntity.ok(savedBus);
    }

    @PostMapping("/drivers")
    public ResponseEntity<String> createDriver(@RequestBody DriverRequest request) {
        User savedDriver = adminService.createDriver(request);
        return ResponseEntity.ok("Driver account created successfully for: " + savedDriver.getUsername());
    }

    @GetMapping("/routes")
    public ResponseEntity<List<Route>> getAllRoutes() {
        return ResponseEntity.ok(adminService.getAllRoutes());
    }

    @GetMapping("/routes/{routeId}/stops")
    public ResponseEntity<List<StopResponse>> getStopsForRoute(@PathVariable UUID routeId) {
        List<Stop> stops = adminService.getStopsByRoute(routeId);
        
        List<StopResponse> safeResponse = stops.stream().map(stop -> {
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
        
        return ResponseEntity.ok(safeResponse);
    }

    @GetMapping("/buses")
    public ResponseEntity<List<Bus>> getAllBuses() {
        return ResponseEntity.ok(adminService.getAllBuses());
    }

    @GetMapping("/drivers")
    public ResponseEntity<List<User>> getAllDrivers() {
        return ResponseEntity.ok(adminService.getAllDrivers());
    }
}

