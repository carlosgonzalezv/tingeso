package com.example.tingeso.Services;

import com.example.tingeso.Entities.BookingEntity;
import com.example.tingeso.Entities.PackTourEntity;
import com.example.tingeso.Entities.PaymentsEntity;
import com.example.tingeso.Repositories.BookingRepository;
import com.example.tingeso.Repositories.PaymentsRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentsServiceTest {

    @Mock private PaymentsRepository paymentsRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private PackTourService packTourService;

    @InjectMocks
    private PaymentsService paymentsService;

    @Test
    void getPayments_AndGetByBooking_Success() {
        when(paymentsRepository.findAll()).thenReturn(List.of(new PaymentsEntity()));
        when(paymentsRepository.findByBookingID_Id(1L)).thenReturn(Optional.of(new PaymentsEntity()));

        assertFalse(paymentsService.getPayments().isEmpty());
        assertNotNull(paymentsService.getPaymentByBooking(1L));
    }

    @Test
    void processPayment_InvalidBookingID_ThrowsException() {
        PaymentsEntity p1 = new PaymentsEntity();
        assertThrows(IllegalArgumentException.class, () -> paymentsService.processPayment(p1));

        p1.setBookingID(new BookingEntity());
        assertThrows(IllegalArgumentException.class, () -> paymentsService.processPayment(p1));
    }

    @Test
    void processPayment_BookingNotFound_ThrowsException() {
        PaymentsEntity payment = new PaymentsEntity();
        BookingEntity bRef = new BookingEntity(); bRef.setId(1L);
        payment.setBookingID(bRef);

        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> paymentsService.processPayment(payment));
    }

    @Test
    void processPayment_InvalidStatus_ThrowsException() {
        PaymentsEntity payment = new PaymentsEntity(); BookingEntity b = new BookingEntity(); b.setId(1L);
        payment.setBookingID(b);

        b.setStatus("CONFIRMADA");
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(b));
        assertThrows(IllegalStateException.class, () -> paymentsService.processPayment(payment));

        b.setStatus("CANCELADO");
        assertThrows(IllegalStateException.class, () -> paymentsService.processPayment(payment));
    }

    @Test
    void processPayment_InvalidAmounts_ThrowsException() {
        PaymentsEntity payment = new PaymentsEntity(); BookingEntity b = new BookingEntity(); b.setId(1L);
        b.setStatus("PENDIENTE"); b.setTotalAmount(500);
        payment.setBookingID(b);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(b));

        payment.setAmount(0);
        assertThrows(IllegalArgumentException.class, () -> paymentsService.processPayment(payment));

        payment.setAmount(499);
        assertThrows(IllegalArgumentException.class, () -> paymentsService.processPayment(payment));
    }

    @Test
    void processPayment_FullSuccess_WithCardMasking() {
        // 1. Preparamos el PackTour (esto es lo que faltaba)
        PackTourEntity pack = new PackTourEntity();
        pack.setId(100L); // Le damos un ID cualquiera

        BookingEntity b = new BookingEntity();
        b.setId(1L);
        b.setStatus("PENDIENTE");
        b.setTotalAmount(500);
        b.setPackTour(pack); // <--- ESTA LÍNEA ES LA CLAVE

        PaymentsEntity payment = new PaymentsEntity();
        payment.setAmount(500);
        payment.setCardNumber("1234567812345678");
        payment.setBookingID(b);

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(b));
        when(paymentsRepository.save(any())).thenReturn(payment);

        PaymentsEntity result = paymentsService.processPayment(payment);

        assertNotNull(result);
        assertEquals("EXITOSO", result.getState());
        assertEquals("XXXX-XXXX-XXXX-5678", result.getCardNumber());
        verify(packTourService, times(1)).reduceSlot(any(), anyInt());
    }
}