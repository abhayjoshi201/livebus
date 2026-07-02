package com.example.livebus.ui.tickets

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TicketsViewModelTest {

    private lateinit var viewModel: TicketsViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = TicketsViewModel()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isCorrect() {
        assertEquals(1, viewModel.activeTickets.value.size)
        assertEquals(1, viewModel.expiredTickets.value.size)
        assertEquals(4, viewModel.ticketProducts.size)
    }

    @Test
    fun purchaseTicket_addsToActiveTickets() {
        val product = viewModel.ticketProducts[1] // 24-Hour Day Pass
        viewModel.purchaseTicket(product)

        assertEquals(2, viewModel.activeTickets.value.size)
        assertEquals("24-HOUR DAY PASS", viewModel.activeTickets.value[0].type)
        assertTrue(viewModel.activeTickets.value[0].isValid)
    }

    @Test
    fun renewExpiredTicket_movesFromExpiredToActive() {
        val expired = viewModel.expiredTickets.value[0]
        viewModel.renewExpiredTicket(expired)

        assertEquals(0, viewModel.expiredTickets.value.size)
        assertEquals(2, viewModel.activeTickets.value.size)
        assertEquals("Daily City Pass", viewModel.activeTickets.value[0].type)
        assertTrue(viewModel.activeTickets.value[0].isValid)
    }

    @Test
    fun qrCodeGenerator_doesNotThrowException() {
        val bitMatrix = QrCodeGenerator.encodeQrBitMatrix("sample-ticket-uuid-12345", 256)
        assertNotNull(bitMatrix)
        assertEquals(256, bitMatrix?.width)
    }
}
