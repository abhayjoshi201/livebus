# GEHU LiveBus: College Transit Tracking System

**GEHU LiveBus** is an end-to-end real-time bus GPS tracking and telemetry monitoring system customized for the students, faculty, and shuttle drivers of **Graphic Era Hill University (GEHU)**.

The application serves three active campuses located in **Dehradun**, **Bhimtal**, and **Haldwani**, helping commuters track shuttles, get dynamic arrival estimates, and helping administrators dispatch alerts during delays.

---

## ⚠️ Problem Statement

University shuttle scheduling faces critical operational challenges in regional environments like Dehradun, Bhimtal, and Haldwani:
* **Hilly Terrains & Weather Inclemencies**: Hilly corridors are highly susceptible to heavy rain, seasonal fog, landslides, and road constructions. This causes severe, unpredictable delays that traditional static timetables cannot account for.
* **Blind Commuting**: Students and faculty wait blindly at shuttle checkpoints, having no real-time visibility into whether their assigned bus is ahead of schedule, running late, or already full.
* **Disconnected Operations**: Shuttle drivers lack direct channels to report traffic delays, crowding alerts, or critical emergency distress signals back to central dispatch administrators and commuters.

---

## ✨ Core Features

### 1. Passenger Commuter Portal (Android & Web)
* **Real-time Map Visualizer**: Interactive map (using Leaflet on Web and Google/MapLibre on Android) displaying the live movements of campus buses.
* **Campus Hub Switcher**: Dropdown selector to dynamically swap between **Dehradun**, **Bhimtal**, and **Haldwani** campuses. Selecting a campus instantly updates the corresponding route catalog, stops, and coordinates.
* **Suggested Routes Grid**: Quick-access cards representing different campus routes, showing their destination and active transit status (e.g. `"On Time"`, `"Delayed"`).
* **Itinerary Stop Timeline**: Displays the list of stops along the route corridor (identifying transit checkpoints and final alighting depots).
* **Live Arrival Estimator (ETA)**: Calculates the remaining distance (in km) and estimated minutes until arrival.
* **Mock Ticketing & Alerts**: Allows passengers to purchase/view boarding passes and toggling arrival alert notifications.

### 2. Driver & Operator Portal (Android App)
* **Duty Setup Console**: Allows drivers to choose their assigned campus route and register their specific bus vehicle number (e.g., `UA-07-TA-2024`).
* **Telemetry Broadcast Engine**: Once the shift starts, automatically sends live JSON GPS coordinates payloads to the WebSocket broker every 3 seconds to update the passenger maps.
* **Dispatch Alerts**: Quick-reporting buttons to broadcast delays (e.g., `🟠 Traffic Delay +5m`) or high passenger density (`🔵 Crowd alerts`) to the dispatch center.
* **Emergency Distress (SOS)**: A single-tap SOS button that publishes an emergency broadcast signal to the fleet control console.

### 3. Administrator Console (Angular Web Portal)
* **Route Registry Manager**: Tab to add new campus routes (inputting ID, name, destination, description, card color theme, and durations) or decommission obsolete ones.
* **Fleet Catalog**: List all registered campus shuttle vehicles.
* **Service Advisory Broadcaster**: An alert publisher to broadcast real-time weather alerts or traffic delays to the main passenger home screens.

### 4. Enterprise Backend Services (Spring Boot Server)
* **SockJS & STOMP Message Broker**: Relays coordinate messages from active driver devices to passenger browsers and mobile clients in sub-second latency.
* **PostgreSQL Persistence**: Saves and updates configurations for buses, routes, and coordinates.
* **Spring Security Configurations**: Locks down administrative routes (`/api/admin/**`) and driver shifts (`/api/driver/**`) using role checks and secure password encryption.

---

## 🏛️ System Architecture

The ecosystem consists of three main components:
1. **Spring Boot Backend**: Serves REST configuration endpoints, handles authentication, and acts as the SockJS/STOMP WebSocket message broker for real-time GPS coordinates.
2. **Angular Web Portal**: A web interface featuring Leaflet Maps for live bus routing and a tabbed **Admin Control Panel** to register new routes, manage fleet vehicles, and broadcast advisory alerts.
3. **Android Mobile Application**: A multi-role Compose application:
   * **Passenger Mode**: A dashboard with interactive maps, estimated arrival times (ETA), nearest stops lists, and local alert settings.
   * **Driver Mode**: A shift dashboard for selecting routes and vehicles, broadcasting real-time coordinates, and reporting delays or emergency SOS signals.

```mermaid
graph TD
    subgraph Publisher [Telemetry Producer]
        Driver[Driver Android App]
    end

    subgraph Broker [Spring Boot Backend Server]
        WS[STOMP WebSocket Broker]
        REST[REST Configuration APIs]
        DB[(PostgreSQL Database)]
    end

    subgraph Subscribers [Commuter Clients]
        Passenger[Passenger Android App]
        WebPortal[Angular Web Portal / Admin Panel]
    end

    %% Flows
    Driver -->|Publishes Telemetry via STOMP| WS
    WS -->|Streams live GPS via STOMP| Passenger
    WS -->|Streams live GPS via STOMP| WebPortal
    WebPortal -->|Updates Routes/Buses via REST| REST
    REST -->|Persists configurations| DB
```

---

## 📂 Project Structure

* **`app/`**: Native Android mobile application (Jetpack Compose + Kotlin + Hilt).
  * `ui/home/`: Commuter homepage with campus selectors and suggested routes.
  * `ui/tracking/`: Map layer and WebSocket live tracking engine.
  * `ui/driver/`: Driver shift setup, GPS transmitter, and incident logger.
  * `ui/theme/`: Customized GEHU brand palette (Navy, Gold, Orange).
* **`backend/`**: Spring Boot application (Java 17 + Gradle).
  * `livebus/admin/`: Controllers and database repositories for Route, Stop, and Bus configurations.
  * `livebus/driver/`: Trip controllers mapping active driver shifts.
  * `livebus/security/`: Security configs and user auth controllers.
* **`angular-app/`**: Angular Web application.
  * `app/app.ts`: State managers, Leaflet map renderer, and simulated fallback loops.
  * `app/app.html`: Layout container for sliding panel views, map overlays, and admin tabs.

---

## 🗄️ Database & Schema Specifications

The backend connects to a **PostgreSQL** database running on port **`5433`** (`jdbc:postgresql://localhost:5433/livebus`).

```
  +--------------+        +---------------+        +-------------+
  |    ROUTE     | 1    N |     STOP      | N    1 |     BUS     |
  |  (id, name,  |--------| (id, name,    |--------|  (id, plate,|
  |   dest, dir) |        |  lat, lon)    |        |   capacity) |
  +--------------+        +---------------+        +-------------+
```

Hibernate Automatically generates the following schemas:
* **`Route` Table**: Stores itinerary details.
  * `id` (VARCHAR, Primary Key) - e.g., `"D-1"`
  * `routeName` (VARCHAR) - e.g., `"ROUTE D-1"`
  * `destination` (VARCHAR) - e.g., `"GEHU Clement Town Campus"`
  * `direction` (VARCHAR) - e.g., `"Clement Town Bus Service"`
* **`Stop` Table**: Stores geographical coordinates of boarding checkpoints.
  * `id` (BIGINT, Primary Key)
  * `name` (VARCHAR) - e.g., `"ISBT Terminal"`
  * `latitude` (DOUBLE) - e.g., `30.2872`
  * `longitude` (DOUBLE) - e.g., `77.9984`
* **`Bus` Table**: Identifies shuttle inventory.
  * `id` (VARCHAR, Primary Key) - e.g., `"UA-07-TA-2024"`
  * `capacity` (INT)

---

## 📡 STOMP WebSocket API Reference

### Channels & Endpoints
* **Handshake Endpoint**: `ws://localhost:8080/ws-livebus` (SockJS fallback enabled)
* **Driver Publish Channel**: `/app/driver/update`
* **Passenger Subscription Channel**: `/topic/route/{routeId}` (e.g. `/topic/route/D-1`)

### LocationUpdate Payload (JSON format)
```json
{
  "busId": "UA-07-TA-2024",
  "latitude": 30.2721,
  "longitude": 78.0084,
  "eta": "5 mins",
  "distance": "1.8 km"
}
```

---

## 🔐 Spring Security & Access Control

The backend implements role-based security configurations using **Spring Security** in [SecurityConfig.java](file:///usr/local/google/home/abhayjoshi/AndroidStudioProjects/LiveBus/backend/app/src/main/java/livebus/security/config/SecurityConfig.java):

* **Authorization Rules**:
  * **`ROLE_ADMIN`**: Required to call administration configuration REST endpoints (`/api/admin/**`).
  * **`ROLE_DRIVER`**: Required to execute driver-specific updates (`/api/driver/**`).
  * **Permit All**: Public assets (`.js`, `.css`, `index.html`), authentication endpoint (`/api/auth/login`), passenger data queries (`/api/passenger/**`), and the WebSocket handler (`/ws-livebus/**`) are configured to bypass security.
* **Cryptography**: Password hashing and verification are performed using the **BCrypt** hashing algorithm.

---

## 🛠️ How to Build and Run

### 1. Database Setup
Ensure PostgreSQL is running locally on port `5433`:
```bash
# Check if PostgreSQL port is ready
pg_isready -h localhost -p 5433
```

### 2. Spring Boot Backend Server
To build and start the backend service (served on port `8080`):
```bash
cd backend
./gradlew :app:bootRun
```
The **Web Portal** will automatically be served at:
* **[http://localhost:8080/index.html](http://localhost:8080/index.html)**

---

### 3. Angular Web Application

#### Live Local Development
To run the live Angular development server locally (served on port `4200` by default):
```bash
cd angular-app
npm install
npm run start
```

#### Compile and Sync Assets with Backend
To compile the static production assets and automatically synchronize/copy them to the Spring Boot resources directory:
```bash
cd angular-app
npm run build:sync
```

---

### 4. Android Application

#### Step A: Launch the Android Virtual Device (AVD)
Find your virtual device identifier and start it in the background:
```bash
# List available emulator device names
~/Android/Sdk/emulator/emulator -list-avds

# Start the selected emulator (replace Pixel_10_Pro with your device name)
~/Android/Sdk/emulator/emulator -avd Pixel_10_Pro &
```

#### Step B: Install and Launch the Android App
Build the debug APK, install it on the active emulator, and start the app:
```bash
./gradlew installDebug && ~/Android/Sdk/platform-tools/adb shell am start -n com.example.livebus/.MainActivity
```

---

## 🔧 Troubleshooting Guide

### 1. Driver Location Mismatches
If the map displays "Standby" and passenger devices are not receiving updates, verify:
* The driver application has started location broadcasting (indicated by a green blinking status light in the console).
* The **Stomp Client** URL path matches on both sides:
  * Backend: `WebSocketConfig.java` listens to `/ws-livebus`.
  * Mobile client: `LiveTrackingViewModel.kt` connects to `ws://10.0.2.2:8080/ws-livebus`.

### 2. Android Emulator Loopback
When testing the Android app inside the emulator, **`localhost`** refers to the emulator's internal network. 
* To connect to the Spring Boot backend running on your developer host machine, make sure you configure the WebSocket connection to use the loopback IP **`10.0.2.2`** instead of `localhost`.
