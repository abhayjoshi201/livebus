package livebus.driver.controller;

import livebus.driver.dto.LocationUpdateRequest;
import livebus.driver.dto.StartTripRequest;
import livebus.driver.model.Trip;
import livebus.driver.dto.TripResponse;
import livebus.driver.service.DriverService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/driver/trips")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping("/start")
    public ResponseEntity<TripResponse> startTrip(@RequestBody StartTripRequest request, Principal principal) {
        Trip trip = driverService.startTrip(principal.getName(), request);
        TripResponse response = new TripResponse(
                trip.getId(), 
                trip.getBus().getLicensePlate(), 
                trip.getStatus()
        );
        
        return ResponseEntity.ok(response);
    }

    @PutMapping("/location")
    public ResponseEntity<String> updateLocation(@RequestBody LocationUpdateRequest request, Principal principal) {
        driverService.updateLocation(principal.getName(), request);
        return ResponseEntity.ok("Location updated");
    }

    @PostMapping("/end")
    public ResponseEntity<String> endTrip(Principal principal) {
        driverService.endTrip(principal.getName());
        return ResponseEntity.ok("Trip completed successfully");
    }
}