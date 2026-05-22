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
     * No realiza validación externa, asumiendo éxito si las reglas de negocio se cumplen.
     */
    @Transactional
    public PaymentsEntity processPayment(PaymentsEntity payment) {
        // 1. ASOCIACIÓN OBLIGATORIA
        // Nota: Asegúrate que en PaymentsEntity el objeto se llame bookingID
        BookingEntity booking = bookingRepository.findById(payment.getBookingID().getId())
                .orElseThrow(() -> new RuntimeException("Error: No existe la reserva asociada."));

        // 2. REGLA DE MEDIO DE PAGO: Solo tarjeta de crédito simulada
        if (!"Tarjeta de Crédito".equalsIgnoreCase(payment.getPaymentMethod())) {
            throw new IllegalArgumentException("Error: Solo se permite 'Tarjeta de Crédito' simulada.");
        }

        // 3. REGLA DE UNICIDAD: Solo un pago por reserva
        // Nota: El método findByBookingID debe estar definido en tu PaymentsRepository
        if (paymentsRepository.findByBookingID(booking.getId()).isPresent()) {
            throw new IllegalStateException("Esta reserva ya cuenta con un pago registrado.");
        }

        // 4. VALIDACIÓN DE MONTO TOTAL Y POSITIVO
        if (payment.getAmount() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero.");
        }
        if (payment.getAmount() != booking.getTotalAmount()) {
            throw new IllegalArgumentException("El monto debe ser el total de la reserva: $" + booking.getTotalAmount());
        }

        // 5. REGLA DE ESTADO: No pagar reservas canceladas o expiradas
        if ("CANCELADO".equalsIgnoreCase(booking.getStatus()) || "EXPIRADA".equalsIgnoreCase(booking.getStatus())) {
            throw new IllegalStateException("No se puede pagar una reserva cancelada o vencida.");
        }

        // 6. ACTUALIZACIÓN AUTOMÁTICA A CONFIRMADA
        // Cumplimos con la regla de cambiar el estado tras el pago exitoso
        booking.setStatus("CONFIRMADA");
        bookingRepository.save(booking);

        // 7. PROCESAMIENTO DE DATOS SIMULADOS (Número, Expiración, CVV)
        // Seteamos la fecha actual de la transacción
        payment.setPaymentDate(LocalDateTime.now());
        payment.setState("EXITOSO");

        // Enmascaramiento de tarjeta (Simulación de seguridad real)
        // Esto permite que el usuario ingrese su tarjeta pero solo guardamos los últimos 4
        if (payment.getCardNumber() != null && payment.getCardNumber().length() > 4) {
            String fullCard = payment.getCardNumber();
            String lastFour = fullCard.substring(fullCard.length() - 4);
            payment.setCardNumber("XXXX-XXXX-XXXX-" + lastFour);
        }

        // Guardamos el registro del pago (incluyendo titular y expiración si están en tu Entity)
        return paymentsRepository.save(payment);
    }

    public PaymentsEntity getPaymentByBooking(Long bookingID) {
        return paymentsRepository.findByBookingID(bookingID).orElse(null);
    }


}