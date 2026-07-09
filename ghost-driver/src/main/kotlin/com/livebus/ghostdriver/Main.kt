package com.livebus.ghostdriver

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

data class LocationUpdate(
    val busId: String,
    val latitude: Double,
    val longitude: Double
)

val mockLocations = listOf(
    LocationUpdate("UA-07-TA-2024", 30.2872, 77.9984),     // ISBT Terminal (Stop 1)
    LocationUpdate("UA-07-TA-2024", 30.2868, 77.9998),
    LocationUpdate("UA-07-TA-2024", 30.2862, 78.0012),     // Turner Road Junction (Stop 2)
    LocationUpdate("UA-07-TA-2024", 30.2825, 78.0035),
    LocationUpdate("UA-07-TA-2024", 30.2785, 78.0055),     // Subhash Nagar Chowk (Stop 3)
    LocationUpdate("UA-07-TA-2024", 30.2740, 78.0070),
    LocationUpdate("UA-07-TA-2024", 30.2700, 78.0084)      // Clement Town Campus (Stop 4)
)

fun main() = runBlocking {
    val baseUrl = "http://localhost:8080"
    println("Initializing Ghost Driver REST Telemetry Publisher...")

    val client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .build()

    // 1. Login as Driver
    println("Logging in as driver...")
    val loginJson = """{"username": "driver", "password": "driver123"}"""
    val loginRequest = HttpRequest.newBuilder()
        .uri(URI.create("$baseUrl/api/auth/login"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(loginJson))
        .build()

    val loginResponse = client.send(loginRequest, HttpResponse.BodyHandlers.ofString())
    if (loginResponse.statusCode() != 200) {
        System.err.println("Login failed with status: ${loginResponse.statusCode()} - ${loginResponse.body()}")
        return@runBlocking
    }

    val cookieHeader = loginResponse.headers().allValues("Set-Cookie")
    val jsessionCookie = cookieHeader.firstOrNull { it.contains("JSESSIONID") }
        ?.split(";")?.firstOrNull()

    if (jsessionCookie == null) {
        System.err.println("Failed to retrieve session cookie from response!")
        return@runBlocking
    }
    println("Login successful. Session Cookie: $jsessionCookie")

    // 2. Start Trip for Route D-1 (d1111111-1111-1111-1111-111111111111)
    println("Starting active driver trip for Route D-1...")
    val startTripJson = """{
        "routeId": "d1111111-1111-1111-1111-111111111111",
        "busId": "11111111-1111-1111-1111-111111111111"
    }""".trimIndent()

    val startTripRequest = HttpRequest.newBuilder()
        .uri(URI.create("$baseUrl/api/driver/trips/start"))
        .header("Content-Type", "application/json")
        .header("Cookie", jsessionCookie)
        .POST(HttpRequest.BodyPublishers.ofString(startTripJson))
        .build()

    val startTripResponse = client.send(startTripRequest, HttpResponse.BodyHandlers.ofString())
    if (startTripResponse.statusCode() != 200 && !startTripResponse.body().contains("already has an active trip")) {
        System.err.println("Start trip failed with status: ${startTripResponse.statusCode()} - ${startTripResponse.body()}")
        return@runBlocking
    }
    println("Driver shift started successfully (or active trip reused).")

    // 3. Location Simulation Loop
    var index = 0
    while (true) {
        val update = mockLocations[index]
        val locationJson = """{
            "latitude": ${update.latitude},
            "longitude": ${update.longitude}
        }""".trimIndent()

        try {
            println("Streaming GPS update -> lat: ${update.latitude}, lon: ${update.longitude}")
            val locationRequest = HttpRequest.newBuilder()
                .uri(URI.create("$baseUrl/api/driver/trips/location"))
                .header("Content-Type", "application/json")
                .header("Cookie", jsessionCookie)
                .PUT(HttpRequest.BodyPublishers.ofString(locationJson))
                .build()

            val locationResponse = client.send(locationRequest, HttpResponse.BodyHandlers.ofString())
            if (locationResponse.statusCode() != 200) {
                System.err.println("Failed to update location. Status: ${locationResponse.statusCode()} - ${locationResponse.body()}")
            }
        } catch (e: Exception) {
            System.err.println("Network error sending GPS update: ${e.message}")
        }

        index = (index + 1) % mockLocations.size
        delay(3000)
    }
}
