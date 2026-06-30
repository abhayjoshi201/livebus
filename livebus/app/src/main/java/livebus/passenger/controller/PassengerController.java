package livebus.passenger.controller;

import livebus.admin.model.Route;
import livebus.admin.model.Stop;
import livebus.passenger.dto.LiveLocationResponse;
import livebus.passenger.service.PassengerService;
import livebus.passenger.dto.RouteResponse;
import livebus.passenger.dto.EtaResponse;
import livebus.passenger.dto.StopResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/passenger")
public class PassengerController {

    private final PassengerService passengerService;

    public PassengerController(PassengerService passengerService) {
        this.passengerService = passengerService;
    }

    @GetMapping("/routes")
    public ResponseEntity<List<Route>> getRoutes() {
        return ResponseEntity.ok(passengerService.getAllRoutes());
    }

   @GetMapping("/routes/{routeId}/stops")
    public ResponseEntity<List<StopResponse>> getStops(@PathVariable UUID routeId) {
        return ResponseEntity.ok(passengerService.getStopsForRoute(routeId));
    }

    @GetMapping("/routes/{routeId}/live")
    public ResponseEntity<List<LiveLocationResponse>> getLiveLocations(@PathVariable UUID routeId) {
        return ResponseEntity.ok(passengerService.getLiveBusesForRoute(routeId));
    }

    @GetMapping("/routes/search")
    public ResponseEntity<List<RouteResponse>> searchRoutes(@RequestParam String keyword) {
        return ResponseEntity.ok(passengerService.searchRoutes(keyword));
    }

    @GetMapping("/routes/{routeId}/stops/{stopId}/eta")
    public ResponseEntity<List<EtaResponse>> getEta(
            @PathVariable UUID routeId, 
            @PathVariable UUID stopId) {
        return ResponseEntity.ok(passengerService.getEtaForStop(routeId, stopId));
    }
}