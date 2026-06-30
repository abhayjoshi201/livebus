# 🚌 LiveBus — Real-Time Transit Tracking System

**LiveBus** is an end-to-end real-time bus GPS tracking and telemetry monitoring system. It demonstrates real-time communication between transit vehicles (simulated drivers), a central publish/subscribe broker, and end-user mobile clients using **STOMP over WebSocket**.

---

## 🏗️ System Architecture

LiveBus is organized into three distinct, decoupled modules:

```
  +-------------------+               +----------------------+               +-------------------+
  |   Ghost Driver    |               |  WebSocket Broker    |               |  Android Client   |
  |  (Bus Simulator)  | ------------> |    (Spring Boot)     | ------------> | (Jetpack Compose) |
  +-------------------+  PUBLISH GPS  +----------------------+  SUBSCRIBE    +-------------------+
       Kotlin/Gradle    /app/driver/update  Java 17/Maven      /topic/route/*     Kotlin/Android
```

### 1. `websocket-broker` (Backend Broker)
* **Tech Stack:** Java 17, Spring Boot, Maven, Spring WebSocket (STOMP).
* **Role:** Acts as the central WebSocket message broker.
* **Functionality:** Accepts incoming GPS telemetry packets from buses and instantly broadcasts them to all subscribing clients tracking that specific route.
* **Port:** `8080`

### 2. `ghost-driver` (Bus Telemetry Simulator)
* **Tech Stack:** Kotlin, Gradle, Coroutines, Spring STOMP Client.
* **Role:** Simulates an active bus driver navigating through scheduled route coordinates.
* **Functionality:** Connects asynchronously to the WebSocket broker and continuously publishes simulated GPS coordinates (Latitude, Longitude) for bus `101-A` every 3 seconds.

### 3. `app` (Mobile Tracking Client)
* **Tech Stack:** Kotlin, Android Jetpack Compose, RxJava, Naiksoftware STOMP Client.
* **Role:** Passenger-facing Android application for live bus tracking.
* **Functionality:** Establishes a STOMP connection to the broker, subscribes to route topics, and dynamically updates UI elements (live coordinates, ETA, distance) in real time without polling.

---

## 📋 Prerequisites

Before running the system, ensure you have the following installed:
* **JDK 17+** (Required for Spring Boot and Kotlin compilation)
* **Apache Maven** (For building the backend broker)
* **Gradle / Android Studio** (For building the Android app and ghost driver)
* **Android SDK** (API 24+)

---

## 🚀 Getting Started

To experience the full live tracking loop, start the components in the order listed below:

### Step 1: Start the Backend WebSocket Broker
Open a terminal, navigate to the `websocket-broker` directory, and start the Spring Boot application:

```bash
cd websocket-broker
mvn spring-boot:run
```
*The broker will initialize and listen for STOMP connections at `ws://localhost:8080/gs-guide-websocket`.*

### Step 2: Start the Ghost Driver Simulator
In a separate terminal window, launch the Kotlin bus simulator to start streaming GPS coordinates:

```bash
cd ghost-driver
../gradlew run
```
*You should see logs indicating successful connection to the broker and periodic GPS updates being sent for bus `101-A`.*

### Step 3: Launch the Android Client
1. Open the project root in **Android Studio**.
2. Sync Gradle dependencies.
3. Run the `app` configuration on an Android Emulator or physical device.

> [!IMPORTANT]
> **Android Emulator Networking Note:** By default, the Android app is configured to connect to `ws://localhost:8080/gs-guide-websocket`. When running inside an Android Emulator, `localhost` refers to the emulator's own loopback interface. To connect to the Spring Boot server running on your host machine, update the WebSocket connection URL in `BusViewModel.kt` to:
> `ws://10.0.2.2:8080/gs-guide-websocket`

---

## 📡 STOMP API Reference

| Endpoint Type | Path / Destination | Description |
| :--- | :--- | :--- |
| **Connection Endpoint** | `/gs-guide-websocket` | STOMP handshake over WebSocket (Supports allowed origin patterns `*`). |
| **Publish Destination** | `/app/driver/update` | Target destination for incoming driver telemetry payloads (`LocationUpdate`). |
| **Subscribe Topic** | `/topic/route/{busId}` | Broadcast channel where clients receive live updates (e.g., `/topic/route/101-A`). |

### Data Payload Format (`LocationUpdate`)
```json
{
  "busId": "101-A",
  "latitude": 34.0522,
  "longitude": -118.2437,
  "eta": "5 mins",
  "distance": "1.2 km"
}
```

---

## 📁 Project Structure

```
LiveBus/
├── app/                  # Android mobile app (Jetpack Compose, Kotlin)
│   ├── src/main/java/com/example/livebus/
│   │   ├── MainActivity.kt        # Application entry point
│   │   ├── BusTrackingScreen.kt   # Real-time tracking UI layout
│   │   ├── BusViewModel.kt        # State management & websocket subscription
│   │   └── WebSocketClient.kt     # RxJava STOMP client wrapper
├── ghost-driver/         # Bus GPS telemetry simulator (Kotlin, Gradle)
│   └── src/main/kotlin/com/livebus/ghostdriver/
│       └── Main.kt                # STOMP sender loop and coordinate generator
└── websocket-broker/     # Spring Boot STOMP relay backend (Java 17, Maven)
    └── src/main/java/com/livebus/websocket/
        ├── WebsocketBrokerApplication.java # Spring Boot entry point
        ├── WebSocketConfig.java            # STOMP broker configuration
        ├── DriverController.java           # Message mapping handler
        └── LocationUpdate.java             # Telemetry data model
```

---

## 📄 License

This project is open-source and available for educational and commercial transit telemetry demonstrations.
