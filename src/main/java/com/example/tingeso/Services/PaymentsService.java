package com.example.tingeso.Services;

import com.example.tingeso.Entities.BookingEntity;
import com.example.tingeso.Entities.PaymentsEntity;
import com.example.tingeso.Repositories.BookingRepository;
import com.example.tingeso.Repositories.PaymentsRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentsService {

    @Autowired
    private PaymentsRepository paymentsRepository;

    @Autowired
    private BookingRepository bookingRepository;

    public List<PaymentsEntity> getPayments() {
        return paymentsRepository.findAll();
    }

    /**
     * Proceso de pago simulado con ingreso de datos de tarjeta (CVV, Expiración).
     */
    @Transactional
    public PaymentsEntity processPayment(PaymentsEntity payment) {
        // 1. ASOCIACIÓN OBLIGATORIA
        BookingEntity booking = bookingRepository.findById(payment.getBookingID().getId())
                .orElseThrow(() -> new RuntimeException("Error: No existe la reserva asociada."));

        // 2. REGLA DE MEDIO DE PAGO
        if (!"Tarjeta de Crédito".equalsIgnoreCase(payment.getPaymentMethod())) {
            throw new IllegalArgumentException("Error: Solo se permite 'Tarjeta de Crédito' simulada.");
        }

        // 3. REGLA DE UNICIDAD
        // CORREGIDO: Se cambia findByBookingID_id por findByBookingID_Id (I mayúscula)
        if (paymentsRepository.findByBookingID_Id(booking.getId()).isPresent()) {
            throw new IllegalStateException("Esta reserva ya cuenta con un pago registrado.");
        }

        // 4. VALIDACIÓN DE MONTO TOTAL Y POSITIVO
        if (payment.getAmount() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero.");
        }
        if (payment.getAmount() != booking.getTotalAmount()) {
            throw new IllegalArgumentException("El monto debe ser el total de la reserva: $" + booking.getTotalAmount());
        }

        // 5. REGLA DE ESTADO
        // CORREGIDO: Cambiado 'CANCELADO' por 'CANCELADA' para calzar con los estados reales de Booking
        if ("CANCELADA".equalsIgnoreCase(booking.getStatus()) || "EXPIRADA".equalsIgnoreCase(booking.getStatus())) {
            throw new IllegalStateException("No se puede pagar una reserva cancelada o vencida.");
        }

        // 6. ACTUALIZACIÓN AUTOMÁTICA A CONFIRMADA
        booking.setStatus("CONFIRMADA");
        bookingRepository.save(booking);

        // 7. PROCESAMIENTO DE DATOS SIMULADOS
        payment.setPaymentDate(LocalDateTime.now());
        payment.setState("EXITOSO");

        if (payment.getCardNumber() != null && payment.getCardNumber().length() > 4) {
            String fullCard = payment.getCardNumber();
            String lastFour = fullCard.substring(fullCard.length() - 4);
            payment.setCardNumber("XXXX-XXXX-XXXX-" + lastFour);
        }

        return paymentsRepository.save(payment);
    }

    public PaymentsEntity getPaymentByBooking(Long bookingID) {
        // CORREGIDO: Se cambia findByBookingID_id por findByBookingID_Id
        return paymentsRepository.findByBookingID_Id(bookingID).orElse(null);
    }
}