package com.example.tingeso.Services;

import com.example.tingeso.Entities.BookingEntity;
import com.example.tingeso.Entities.PaymentsEntity;
import com.example.tingeso.Repositories.BookingRepository;
import com.example.tingeso.Repositories.PaymentsRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class PaymentsService {

    @Autowired
    private PaymentsRepository paymentsRepository;

    @Autowired
    private BookingRepository bookingRepository;

    /**
     * Simula el comportamiento de una pasarela de pago real (Transbank/Stripe style).
     * Garantiza pago total, éxito asumido y actualización de estado.
     */
    @Transactional
    public PaymentsEntity processPayment(PaymentsEntity payment) {
        // 1. ASOCIACIÓN OBLIGATORIA
        BookingEntity booking = bookingRepository.findById(payment.getBookingID().getId())
                .orElseThrow(() -> new RuntimeException("Error: No existe la reserva."));

        // 2. REGLA DE MEDIO DE PAGO DEFINIDO: Solo tarjeta de crédito simulada
        if (!"Tarjeta de Crédito".equalsIgnoreCase(payment.getPaymentMethod())) {
            throw new IllegalArgumentException("Error: El sistema solo acepta 'Tarjeta de Crédito' como medio de pago.");
        }

        // 3. REGLA DE UNICIDAD: Solo un pago por reserva
        if (paymentsRepository.findByBookingID(booking.getId()).isPresent()) {
            throw new IllegalStateException("Esta reserva ya cuenta con un pago registrado.");
        }

        // 4. VALIDACIÓN DE MONTO TOTAL Y POSITIVO (> 0)
        if (payment.getAmount() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero.");
        }
        if (payment.getAmount() != booking.getTotalAmount()) {
            throw new IllegalArgumentException("El monto no corresponde al total de la reserva ($" + booking.getTotalAmount() + ")");
        }

        // 5. REGLA DE ESTADO: No pagar reservas canceladas o expiradas
        if ("CANCELADO".equalsIgnoreCase(booking.getStatus()) || "EXPIRADA".equalsIgnoreCase(booking.getStatus())) {
            throw new IllegalStateException("No se puede pagar una reserva cancelada o vencida.");
        }

        // 6. ACTUALIZACIÓN AUTOMÁTICA A CONFIRMADA
        booking.setStatus("CONFIRMADA");
        bookingRepository.save(booking);

        // 7. CONSERVACIÓN DE METADATOS Y REGISTRO (Trazabilidad)
        // El ID se genera automáticamente gracias a @GeneratedValue en la entidad
        payment.setPaymentDate(LocalDateTime.now());
        payment.setState("EXITOSO");

        // Enmascaramiento de tarjeta para la base de datos
        if (payment.getCardNumber() != null && payment.getCardNumber().length() > 4) {
            String lastFour = payment.getCardNumber().substring(payment.getCardNumber().length() - 4);
            payment.setCardNumber("XXXX-XXXX-XXXX-" + lastFour);
        }

        return paymentsRepository.save(payment);
    }
}