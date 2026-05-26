package com.example.tingeso.Controllers;

import com.example.tingeso.Entities.PaymentsEntity;
import com.example.tingeso.Services.PaymentsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.tingeso.Config.PaymentRequestDTO; // Asegúrate de importar el DTO
import com.example.tingeso.Entities.BookingEntity;

@RestController
@RequestMapping("/api/v1/payments")
@CrossOrigin("*")
public class PaymentsController {
    @Autowired
    PaymentsService paymentsService;

    //save the payment
    @PostMapping("/")
    public ResponseEntity<PaymentsEntity> savePayment(@RequestBody PaymentsEntity payment) {
        PaymentsEntity newPayment = paymentsService.processPayment(payment);
        if (newPayment != null) {
            return ResponseEntity.ok(newPayment);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    //It allows you to search if there is a payment record associated with a specific booking ID.
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<PaymentsEntity> getPaymentByBooking(@PathVariable Long bookingId) {
        PaymentsEntity payment = paymentsService.getPaymentByBooking(bookingId);
        if (payment != null) {
            return ResponseEntity.ok(payment);
        }
        return ResponseEntity.notFound().build();
    }

    //Main payment flow
    @PostMapping("/process")
    public ResponseEntity<?> process(@Valid @RequestBody PaymentRequestDTO request) {
        try {
            PaymentsEntity payment = new PaymentsEntity();
            payment.setAmount(request.getAmount());
            payment.setPaymentMethod(request.getPaymentMethod());
            payment.setCardNumber(request.getCardNumber());
            payment.setCardHolder(request.getCardHolder());
            payment.setExpirationDate(request.getExpirationDate());
            BookingEntity booking = new BookingEntity();
            booking.setId(request.getBookingId());
            payment.setBookingID(booking);
            PaymentsEntity confirmedPayment = paymentsService.processPayment(payment);
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("message", "¡Pago realizado con éxito!");
            response.put("transactionId", confirmedPayment.getId());
            response.put("amount", confirmedPayment.getAmount());
            response.put("date", confirmedPayment.getPaymentDate());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
