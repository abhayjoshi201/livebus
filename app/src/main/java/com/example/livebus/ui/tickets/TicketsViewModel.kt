package com.example.livebus.ui.tickets

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class Ticket(
    val id: String = UUID.randomUUID().toString(),
    val type: String, // e.g. "SINGLE JOURNEY", "DAILY CITY PASS"
    val routeOrZone: String, // e.g. "Route 216W (TGSRTC Express Fare)"
    val expirationTimestamp: Long, // epoch millis
    val isValid: Boolean = true
)

data class TicketProduct(
    val id: String,
    val name: String,
    val description: String,
    val price: String,
    val durationHours: Int
)

object QrCodeGenerator {
    fun encodeQrBitMatrix(content: String, sizePx: Int = 512): BitMatrix? {
        return try {
            MultiFormatWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                sizePx,
                sizePx
            )
        } catch (e: Exception) {
            null
        }
    }

    fun generateQrBitmap(content: String, sizePx: Int = 512): Bitmap? {
        return try {
            val bitMatrix: BitMatrix = encodeQrBitMatrix(content, sizePx) ?: return null
            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    // Solid black squares on solid white background
                    pixels[offset + x] = if (bitMatrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
                }
            }
            Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                setPixels(pixels, 0, width, 0, 0, width, height)
            }
        } catch (e: Exception) {
            println("Error generating QR code: ${e.message}")
            null
        }
    }
}

@HiltViewModel
class TicketsViewModel @Inject constructor() : ViewModel() {

    private val _activeTickets = MutableStateFlow<List<Ticket>>(
        listOf(
            Ticket(
                type = "SINGLE JOURNEY",
                routeOrZone = "Route 216W (TGSRTC Express Fare)",
                expirationTimestamp = System.currentTimeMillis() + 45 * 60 * 1000L, // 45 mins from now
                isValid = true
            )
        )
    )
    val activeTickets: StateFlow<List<Ticket>> = _activeTickets.asStateFlow()

    private val _expiredTickets = MutableStateFlow<List<Ticket>>(
        listOf(
            Ticket(
                type = "Daily City Pass",
                routeOrZone = "All City Zones • Unlimited",
                expirationTimestamp = System.currentTimeMillis() - 24 * 3600 * 1000L, // Yesterday
                isValid = false
            )
        )
    )
    val expiredTickets: StateFlow<List<Ticket>> = _expiredTickets.asStateFlow()

    val ticketProducts: List<TicketProduct> = listOf(
        TicketProduct(
            id = "p1",
            name = "Single Journey Pass",
            description = "Valid for 90 minutes on any single bus route with free transfers.",
            price = "$2.50",
            durationHours = 2
        ),
        TicketProduct(
            id = "p2",
            name = "24-Hour Day Pass",
            description = "Valid for 24 hours from activation on all city buses and metro lines.",
            price = "$7.00",
            durationHours = 24
        ),
        TicketProduct(
            id = "p3",
            name = "7-Day Weekly Commuter",
            description = "Unlimited travel across all zones for 7 full days from purchase.",
            price = "$28.00",
            durationHours = 168
        ),
        TicketProduct(
            id = "p4",
            name = "Monthly Express Pass",
            description = "Unlimited travel on standard and express transit lines for 30 days.",
            price = "$85.00",
            durationHours = 720
        )
    )

    init {
        startExpirationTimer()
    }

    private fun startExpirationTimer() {
        viewModelScope.launch {
            while (true) {
                delay(60_000L) // check every minute
                checkExpiredTickets()
            }
        }
    }

    fun checkExpiredTickets() {
        val now = System.currentTimeMillis()
        val currentActive = _activeTickets.value
        val newlyExpired = currentActive.filter { it.expirationTimestamp <= now }.map { it.copy(isValid = false) }
        if (newlyExpired.isNotEmpty()) {
            _activeTickets.value = currentActive.filter { it.expirationTimestamp > now }
            _expiredTickets.value = newlyExpired + _expiredTickets.value
        }
    }

    fun purchaseTicket(product: TicketProduct) {
        val newTicket = Ticket(
            type = product.name.uppercase(),
            routeOrZone = "All City Zones • Standard Fare",
            expirationTimestamp = System.currentTimeMillis() + product.durationHours * 3600 * 1000L,
            isValid = true
        )
        _activeTickets.value = listOf(newTicket) + _activeTickets.value
    }

    fun renewExpiredTicket(ticket: Ticket) {
        val renewed = ticket.copy(
            id = UUID.randomUUID().toString(),
            expirationTimestamp = System.currentTimeMillis() + 24 * 3600 * 1000L,
            isValid = true
        )
        _expiredTickets.value = _expiredTickets.value.filter { it.id != ticket.id }
        _activeTickets.value = listOf(renewed) + _activeTickets.value
    }
}

fun formatExpirationTime(timestamp: Long): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

fun formatExpirationDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
