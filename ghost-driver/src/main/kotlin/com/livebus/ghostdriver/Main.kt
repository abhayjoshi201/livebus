package com.livebus.ghostdriver

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import java.util.concurrent.TimeUnit

/**
 * POJO / Data class representing the location update payload.
 */
data class LocationUpdate(
    val busId: String,
    val latitude: Double,
    val longitude: Double
)

/**
 * Hardcoded list of 5 mock LocationUpdate objects simulating the bus route.
 */
val mockLocations = listOf(
    LocationUpdate("101-A", 34.0522, -118.2437),
    LocationUpdate("101-A", 34.0532, -118.2447),
    LocationUpdate("101-A", 34.0542, -118.2457),
    LocationUpdate("101-A", 34.0552, -118.2467),
    LocationUpdate("101-A", 34.0562, -118.2477)
)

fun main() = runBlocking {
    val url = "ws://localhost:8080/gs-guide-websocket"
    println("Initializing Ghost Driver STOMP WebSocket Client...")

    // Set up standard WebSocket client and STOMP messaging wrapper
    val webSocketClient = StandardWebSocketClient()
    val stompClient = WebSocketStompClient(webSocketClient)

    // Register Kotlin module with Jackson so STOMP can serialize data classes automatically
    val objectMapper = ObjectMapper().registerKotlinModule()
    val messageConverter = MappingJackson2MessageConverter()
    messageConverter.objectMapper = objectMapper
    stompClient.messageConverter = messageConverter

    val sessionHandler = object : StompSessionHandlerAdapter() {
        override fun afterConnected(session: StompSession, connectedHeaders: StompHeaders) {
            println("Connected successfully to STOMP broker! Session ID: ${session.sessionId}")
        }

        override fun handleException(
            session: StompSession,
            command: StompCommand?,
            headers: StompHeaders,
            payload: ByteArray,
            exception: Throwable
        ) {
            System.err.println("STOMP Exception occurred: ${exception.message}")
        }

        override fun handleTransportError(session: StompSession, exception: Throwable) {
            System.err.println("Transport error: ${exception.message}")
        }
    }

    var stompSession: StompSession? = null
    try {
        println("Connecting to $url...")
        // Connect asynchronously on IO dispatcher with a 10-second timeout
        stompSession = withContext(Dispatchers.IO) {
            stompClient.connectAsync(url, sessionHandler).get(10, TimeUnit.SECONDS)
        }
    } catch (e: Exception) {
        System.err.println("Failed to connect to WebSocket server at $url: ${e.message}")
        println("Please ensure the Spring Boot server is running on localhost:8080.")
        return@runBlocking
    }

    // Launch coroutine for the infinite data sending loop
    val senderJob = launch {
        var index = 0
        while (true) {
            val update = mockLocations[index]
            try {
                println("Sending GPS Update -> busId: ${update.busId}, lat: ${update.latitude}, lon: ${update.longitude}")
                stompSession?.send("/app/driver/update", update)
            } catch (e: Exception) {
                System.err.println("Error sending update: ${e.message}")
            }

            // Move to next coordinate circularly
            index = (index + 1) % mockLocations.size
            delay(3000)
        }
    }

    senderJob.join()
}
