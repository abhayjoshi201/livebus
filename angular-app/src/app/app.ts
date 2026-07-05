import { Component, signal, computed, AfterViewInit, OnDestroy, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

declare const L: any;

export interface TransitRoute {
  id: string;
  name: string;
  dest: string;
  dir: string;
  color: string;
  busId: string;
  status: string;
  totalMin: number;
  totalDist: number;
  waypoints: number[][];
  stops: string[];
}

export interface TransitHub {
  name: string;
  center: number[];
  zoom: number;
  station: string;
  nearest: string;
  alert: string;
  routes: TransitRoute[];
}

export interface QueueItem {
  icon: string;
  busId: string;
  eta: number;
  isPrimary: boolean;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements AfterViewInit, OnDestroy {
  @ViewChild('searchInput') searchInputRef!: ElementRef<HTMLInputElement>;

  // --- ALL-INDIA TRANSIT DATABASE WITH CURVED ROADS ---
  readonly hubs: Record<string, TransitHub> = {
    "HYD": {
      name: "Hyderabad",
      center: [17.4250, 78.3900],
      zoom: 13,
      station: "🚏 Mehdipatnam Central Depot",
      nearest: "📍 IIIT Junction Gachibowli",
      alert: "Severe Delay on Route 219 • Patancheru Corridor Highway construction",
      routes: [
        { id: "216W", name: "ROUTE 216W", dest: "IIIT Gachibowli Campus", dir: "Westbound towards IT Corridor", color: "bg-green", busId: "TG-09-Z-4052", status: "On Time", totalMin: 28, totalDist: 12.0, waypoints: [[17.3916, 78.4356], [17.3945, 78.4280], [17.3980, 78.4190], [17.4018, 78.4111], [17.4040, 78.4010], [17.4065, 78.3912], [17.4140, 78.3860], [17.4242, 78.3816], [17.4320, 78.3710], [17.4401, 78.3611], [17.4430, 78.3550], [17.4455, 78.3489]], stops: ["Mehdipatnam Depot", "Toli Chowki Junction", "Shaikpet Flyover", "Gachibowli Stadium", "IIIT Campus"] },
        { id: "219", name: "ROUTE 219", dest: "Patancheru Terminal", dir: "Northwest towards Patancheru", color: "bg-amber", busId: "TG-11-Z-8821", status: "Delayed", totalMin: 35, totalDist: 15.2, waypoints: [[17.4875, 78.3881], [17.4910, 78.3890], [17.4950, 78.3895], [17.4982, 78.3891], [17.5020, 78.3700], [17.5050, 78.3490], [17.5090, 78.3360], [17.5144, 78.3242], [17.5210, 78.2950], [17.5287, 78.2667]], stops: ["Kukatpally Housing Board", "JNTU College", "Miyapur Metro Depot", "BHEL Junction", "Patancheru Terminal"] },
        { id: "10H", name: "ROUTE 10H", dest: "Secunderabad Station", dir: "Eastbound towards Secunderabad", color: "bg-blue", busId: "TG-03-Z-1109", status: "On Time", totalMin: 36, totalDist: 14.8, waypoints: [[17.4483, 78.3725], [17.4440, 78.3850], [17.4380, 78.3960], [17.4325, 78.4072], [17.4340, 78.4250], [17.4375, 78.4483], [17.4395, 78.4610], [17.4418, 78.4735], [17.4410, 78.4860], [17.4399, 78.4983]], stops: ["Kondapur Junction", "Hitech City Metro", "Jubilee Hills Checkpost", "Panjagutta Flyover", "Secunderabad East Station"] }
      ]
    },
    "BLR": {
      name: "Bengaluru",
      center: [12.9550, 77.6600],
      zoom: 12,
      station: "🚏 Silk Board Junction TTMC",
      nearest: "📍 Indiranagar 100ft Road Stop",
      alert: "Heavy Waterlogging Alert on ORR • Route 500-D experiencing minor delays",
      routes: [
        { id: "500-D", name: "ROUTE 500-D", dest: "Hebbal Bridge Terminal", dir: "Northbound Outer Ring Road Express", color: "bg-green", busId: "KA-57-F-2099", status: "On Time", totalMin: 52, totalDist: 22.0, waypoints: [[12.9172, 77.6228], [12.9195, 77.6380], [12.9230, 77.6520], [12.9260, 77.6650], [12.9305, 77.6800], [12.9400, 77.6920], [12.9520, 77.6990], [12.9650, 77.7010], [12.9780, 77.6980], [12.9900, 77.6880], [13.0020, 77.6750], [13.0120, 77.6550], [13.0200, 77.6350], [13.0280, 77.6150], [13.0354, 77.5988]], stops: ["Central Silk Board", "Agara Lake", "Marathahalli Bridge", "KR Puram Railway Station", "Hebbal Bridge Terminal"] },
        { id: "KIA-8", name: "ROUTE KIA-8", dest: "Kempegowda Airport T1", dir: "Airport Vayu Vajra AC Service", color: "bg-amber", busId: "KA-57-A-8001", status: "Delayed", totalMin: 60, totalDist: 34.5, waypoints: [[12.9698, 77.7499], [12.9820, 77.7520], [12.9982, 77.7564], [13.0150, 77.7500], [13.0320, 77.7350], [13.0450, 77.7120], [13.0550, 77.6880], [13.0650, 77.6550], [13.0850, 77.6200], [13.1150, 77.6050], [13.1450, 77.6100], [13.1750, 77.6400], [13.1986, 77.7066]], stops: ["Whitefield TTMC", "ITPL Main Gate", "Hope Farm Junction", "Hebbal Toll Plaza", "Kempegowda Airport T1"] }
      ]
    },
    "MUM": {
      name: "Mumbai",
      center: [18.9320, 72.8300],
      zoom: 14,
      station: "🚏 CST Station Terminus",
      nearest: "📍 Churchgate Station South",
      alert: "Marine Drive Coastal Road Diversion • Route A-115 diverted via Oval Maidan",
      routes: [
        { id: "A-115", name: "ROUTE A-115", dest: "Nariman Point / NCPA", dir: "Southbound Marine Drive Express", color: "bg-blue", busId: "MH-01-DR-1150", status: "On Time", totalMin: 18, totalDist: 4.5, waypoints: [[18.9398, 72.8354], [18.9370, 72.8330], [18.9350, 72.8310], [18.9322, 72.8264], [18.9350, 72.8235], [18.9380, 72.8210], [18.9450, 72.8180], [18.9500, 72.8150], [18.9420, 72.8150], [18.9320, 72.8180], [18.9270, 72.8200], [18.9250, 72.8220]], stops: ["CST Terminus", "Hutatma Chowk", "Churchgate Station", "Marine Drive", "Nariman Point NCPA"] },
        { id: "333", name: "ROUTE 333", dest: "Mahakali Caves Terminal", dir: "Eastbound Andheri Connector", color: "bg-green", busId: "MH-02-EE-3330", status: "On Time", totalMin: 22, totalDist: 4.8, waypoints: [[19.1197, 72.8464], [19.1205, 72.8475], [19.1215, 72.8495], [19.1225, 72.8520], [19.1240, 72.8550], [19.1255, 72.8575], [19.1270, 72.8605], [19.1290, 72.8640], [19.1300, 72.8660], [19.1310, 72.8680]], stops: ["Andheri East Station", "Chakala Metro", "SEEPZ Gate 1", "MIDC Central", "Mahakali Caves Terminal"] }
      ]
    },
    "DEL": {
      name: "Delhi-NCR",
      center: [28.5800, 77.2400],
      zoom: 12,
      station: "🚏 Anand Vihar ISBT Terminal",
      nearest: "📍 AIIMS Hospital Gate 1 Stop",
      alert: "VIP Movement at India Gate • Ring Road Route 534 rerouted via Lodhi Road",
      routes: [
        { id: "534", name: "ROUTE 534", dest: "Mehrauli Terminal", dir: "Southbound Ring Road Service", color: "bg-green", busId: "DL-1PC-5340", status: "On Time", totalMin: 55, totalDist: 24.8, waypoints: [[28.6502, 77.3152], [28.6400, 77.3090], [28.6300, 77.3020], [28.6200, 77.2940], [28.6100, 77.2850], [28.6000, 77.2750], [28.5900, 77.2650], [28.5800, 77.2500], [28.5700, 77.2380], [28.5600, 77.2280], [28.5500, 77.2200], [28.5380, 77.2050], [28.5280, 77.1950], [28.5245, 77.1855]], stops: ["Anand Vihar ISBT", "Ashram Chowk", "Lajpat Nagar Metro", "AIIMS Hospital", "Mehrauli Terminal"] },
        { id: "419", name: "ROUTE 419", dest: "Old Delhi Railway Station", dir: "Northbound Central Corridor", color: "bg-amber", busId: "DL-1PB-4190", status: "Delayed", totalMin: 45, totalDist: 19.5, waypoints: [[28.5150, 77.2300], [28.5250, 77.2290], [28.5350, 77.2280], [28.5450, 77.2260], [28.5550, 77.2250], [28.5680, 77.2270], [28.5800, 77.2300], [28.5920, 77.2320], [28.6050, 77.2350], [28.6150, 77.2370], [28.6250, 77.2400], [28.6350, 77.2390], [28.6450, 77.2380], [28.6600, 77.2300]], stops: ["Ambedkar Nagar", "South Extension", "ITO Junction", "Red Fort", "Old Delhi Railway Station"] }
      ]
    },
    "PUNE": {
      name: "Pune",
      center: [18.5600, 73.7900],
      zoom: 13,
      station: "🚏 Shivaji Nagar Central Stand",
      nearest: "📍 Wakad Bridge Highway Stop",
      alert: "Expressway Maintenance • Route 322 running on 10-minute headway queue",
      routes: [
        { id: "322", name: "ROUTE 322", dest: "Hinjewadi IT Park Phase 3", dir: "Westbound IT Fast Connector", color: "bg-green", busId: "MH-12-RN-3220", status: "On Time", totalMin: 42, totalDist: 18.2, waypoints: [[18.5308, 73.8475], [18.5340, 73.8410], [18.5380, 73.8350], [18.5410, 73.8280], [18.5450, 73.8200], [18.5480, 73.8120], [18.5520, 73.8050], [18.5570, 73.7980], [18.5620, 73.7900], [18.5670, 73.7820], [18.5720, 73.7750], [18.5760, 73.7680], [18.5800, 73.7620], [18.5850, 73.7500], [18.5913, 73.7389]], stops: ["Shivaji Nagar Stand", "Aundh Parihar Chowk", "Wakad Bridge", "Hinjewadi Phase 1", "Hinjewadi Phase 3 Circle"] }
      ]
    }
  };

  // --- SIGNALS FOR STATE MANAGEMENT ---
  readonly currentCity = signal<string>("HYD");
  readonly isTrackingView = signal<boolean>(false);
  readonly searchQuery = signal<string>("");
  readonly activeRouteId = signal<string>("216W");

  // Computed state
  readonly activeHub = computed(() => this.hubs[this.currentCity()]);
  readonly activeRoute = computed(() => {
    const hub = this.activeHub();
    return hub.routes.find(r => r.id === this.activeRouteId()) || hub.routes[0];
  });

  readonly filteredRoutes = computed(() => {
    const q = this.searchQuery().toLowerCase().trim();
    const routes = this.activeHub().routes;
    if (!q) return routes;
    return routes.filter(r => 
      r.name.toLowerCase().includes(q) || 
      r.dest.toLowerCase().includes(q) || 
      r.dir.toLowerCase().includes(q)
    );
  });

  // Telemetry signals
  readonly etaValue = signal<number>(5);
  readonly distValue = signal<number>(1.8);
  readonly queueList = signal<QueueItem[]>([]);

  // Leaflet references
  private map: any = null;
  private routePolyline: any = null;
  private stopMarker: any = null;
  private busMarkers: any[] = [];
  private simInterval: any = null;

  ngAfterViewInit() {
    this.initMap();
  }

  ngOnDestroy() {
    if (this.simInterval) clearInterval(this.simInterval);
    if (this.map) this.map.remove();
  }

  private initMap() {
    const hub = this.activeHub();
    this.map = L.map('map', { zoomControl: false }).setView(hub.center, hub.zoom);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '© OpenStreetMap contributors • LiveBus National Portal'
    }).addTo(this.map);

    L.control.zoom({ position: 'topright' }).addTo(this.map);
  }

  onCityChange(event: any) {
    const cityId = event.target.value;
    this.currentCity.set(cityId);
    const hub = this.hubs[cityId];
    this.activeRouteId.set(hub.routes[0].id);
    this.map.setView(hub.center, hub.zoom);
    this.showHomeView();
  }

  selectRoute(route: TransitRoute) {
    this.activeRouteId.set(route.id);
    this.showTrackingView();
    this.startSimulation();
  }

  selectRouteByIndex(idx: number) {
    const hub = this.activeHub();
    const route = hub.routes[idx] || hub.routes[0];
    this.selectRoute(route);
  }

  showHomeView() {
    this.isTrackingView.set(false);
    const hub = this.activeHub();
    this.map.setView(hub.center, hub.zoom);
    this.clearMapLayers();
    if (this.simInterval) clearInterval(this.simInterval);
  }

  showTrackingView() {
    this.isTrackingView.set(true);
  }

  focusSearchInput() {
    if (this.searchInputRef?.nativeElement) {
      this.searchInputRef.nativeElement.focus();
      this.searchInputRef.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }
  }

  onSearchInput(event: any) {
    this.searchQuery.set(event.target.value);
  }

  showAdvisory() {
    alert(`🚨 LIVE TRANSIT ADVISORY:\n${this.activeHub().alert}\n\nOur intelligent routing engine has automatically adjusted headway spacing to minimize commuter delays.`);
  }

  quickNav(type: string) {
    if (type === 'Home') {
      alert(`🏠 Navigating to saved Home commute shortcut:\nSelecting fastest route from ${this.activeHub().name} IT corridor to Residential zone.`);
      this.selectRouteByIndex(0);
    } else if (type === 'Work') {
      alert(`🏢 Navigating to saved Work commute shortcut:\nTracking morning express shuttles to Business District.`);
      this.selectRouteByIndex(1 % this.activeHub().routes.length);
    } else {
      alert(`+ Add Shortcut:\nYou can save your custom daily boarding and alighting stops in the mobile Android application settings!`);
    }
  }

  triggerAlert() {
    const r = this.activeRoute();
    alert(`🔔 Arrival Alert Activated for ${r.name}!\nYou will receive a browser notification when Bus #${r.busId} is 2 minutes away from your station.`);
  }

  private clearMapLayers() {
    if (this.routePolyline) this.map.removeLayer(this.routePolyline);
    if (this.stopMarker) this.map.removeLayer(this.stopMarker);
    this.busMarkers.forEach(m => this.map.removeLayer(m));
    this.busMarkers = [];
  }

  private startSimulation() {
    if (this.simInterval) clearInterval(this.simInterval);
    this.clearMapLayers();

    const route = this.activeRoute();
    this.routePolyline = L.polyline(route.waypoints, { color: '#3b82f6', weight: 6, opacity: 0.85 }).addTo(this.map);
    
    const destCoord = route.waypoints[route.waypoints.length - 1];
    this.stopMarker = L.marker(destCoord, {
      icon: L.divIcon({ className: 'custom-bus-marker', html: '📍', iconSize: [32, 32] })
    }).addTo(this.map).bindPopup(`<b>Boarding Stop: ${route.dest}</b>`);

    this.map.fitBounds(this.routePolyline.getBounds(), { padding: [80, 80] });

    let prog1 = 0.65;
    let prog2 = 0.35;
    let prog3 = 0.08;

    const baseBusId = route.busId;
    const bus2Id = baseBusId.slice(0, -4) + (parseInt(baseBusId.slice(-4)) + 48 || "4100");
    const bus3Id = baseBusId.slice(0, -4) + (parseInt(baseBusId.slice(-4)) + 170 || "4222");

    const m1 = L.marker(route.waypoints[0], { icon: L.divIcon({ className: 'custom-bus-marker', html: '🟢', iconSize: [28, 28] }) }).addTo(this.map);
    const m2 = L.marker(route.waypoints[0], { icon: L.divIcon({ className: 'custom-bus-marker', html: '🟡', iconSize: [24, 24] }) }).addTo(this.map);
    const m3 = L.marker(route.waypoints[0], { icon: L.divIcon({ className: 'custom-bus-marker', html: '🟡', iconSize: [24, 24] }) }).addTo(this.map);
    this.busMarkers = [m1, m2, m3];

    const updateStep = () => {
      const wps = route.waypoints;
      const maxSeg = wps.length - 1;

      prog1 += 0.04; if (prog1 >= 1.0) prog1 = 0.0;
      prog2 += 0.035; if (prog2 >= 1.0) prog2 = 0.0;
      prog3 += 0.03; if (prog3 >= 1.0) prog3 = 0.0;

      const getCoordAndETA = (progress: number) => {
        const segIdx = Math.floor(progress * maxSeg);
        const remRatio = Math.max(0.04, 1.0 - progress);
        const s = wps[segIdx];
        const e = wps[Math.min(segIdx + 1, maxSeg)];
        const subProg = (progress * maxSeg) % 1;
        const lat = s[0] + (e[0] - s[0]) * subProg;
        const lng = s[1] + (e[1] - s[1]) * subProg;
        const eta = Math.max(1, Math.round(remRatio * route.totalMin));
        const dist = Math.round((remRatio * route.totalDist) * 10) / 10;
        return { lat, lng, eta, dist };
      };

      const b1 = getCoordAndETA(prog1);
      const b2 = getCoordAndETA(prog2);
      const b3 = getCoordAndETA(prog3);

      m1.setLatLng([b1.lat, b1.lng]).bindPopup(`<b>Next Bus #${baseBusId}</b><br>ETA: ${b1.eta} min`);
      m2.setLatLng([b2.lat, b2.lng]).bindPopup(`<b>Following Bus #${bus2Id}</b><br>ETA: ${b2.eta} min`);
      m3.setLatLng([b3.lat, b3.lng]).bindPopup(`<b>Queue Bus #${bus3Id}</b><br>ETA: ${b3.eta} min`);

      this.etaValue.set(b1.eta);
      this.distValue.set(b1.dist);

      this.queueList.set([
        { icon: '🟢', busId: `#${baseBusId}`, eta: b1.eta, isPrimary: true },
        { icon: '🟡', busId: `#${bus2Id}`, eta: b2.eta, isPrimary: false },
        { icon: '🟡', busId: `#${bus3Id}`, eta: b3.eta, isPrimary: false }
      ]);
    };

    updateStep();
    this.simInterval = setInterval(updateStep, 1000);
  }
}
