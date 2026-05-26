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
    @Autowired
    private PackTourService packTourService;


    public List<PaymentsEntity> getPayments() {
        return paymentsRepository.findAll();
    }

    //This is where we confirm the reservation and reduce the inventory (slots).
    @Transactional
    public PaymentsEntity processPayment(PaymentsEntity payment) {
        if (payment.getBookingID() == null || payment.getBookingID().getId() == null) {
            throw new IllegalArgumentException("Error: El pago debe tener una reserva asociada.");
        }
        BookingEntity booking = bookingRepository.findById(payment.getBookingID().getId())
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada: " + payment.getBookingID().getId()));
        if ("CONFIRMADA".equalsIgnoreCase(booking.getStatus())) {
            throw new IllegalStateException("Esta reserva ya ha sido pagada y confirmada.");
        }
        if ("CANCELADO".equalsIgnoreCase(booking.getStatus())) {
            throw new IllegalStateException("No se puede pagar una reserva cancelada.");
        }
        if (payment.getAmount() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero.");
        }
        if (payment.getAmount() != booking.getTotalAmount()) {
            throw new IllegalArgumentException("El monto no coincide con el total de la reserva.");
        }
        int passengerCount = 1 + (booking.getCompanions() != null ? booking.getCompanions().size() : 0);
        packTourService.reduceSlot(booking.getPackTour().getId(), passengerCount);
        booking.setStatus("CONFIRMADA");
        bookingRepository.save(booking);
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
        return paymentsRepository.findByBookingID_Id(bookingID).orElse(null);
    }
}