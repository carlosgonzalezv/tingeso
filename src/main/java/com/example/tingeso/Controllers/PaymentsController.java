package com.example.tingeso.Controllers;

import com.example.tingeso.Entities.PaymentsEntity;
import com.example.tingeso.Services.PaymentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@CrossOrigin(origins = "http://localhost:5173")
public class PaymentsController {
    @Autowired
    PaymentsService paymentsService;

    @PostMapping("/")
    public ResponseEntity<PaymentsEntity> savePayment(@RequestBody PaymentsEntity payment) {
        PaymentsEntity newPayment = paymentsService.processPayment(payment);
        if (newPayment != null) {
            return ResponseEntity.ok(newPayment);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<PaymentsEntity> getPaymentByBooking(@PathVariable("bookingId") Long bookingId) {
        PaymentsEntity payment = paymentsService.getPaymentByBooking(bookingId);
        if (payment != null) {
            return ResponseEntity.ok(payment);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/process")
    public ResponseEntity<?> process(@RequestBody com.example.tingeso.Config.PaymentRequestDTO dto) {
        try {
            // Convertimos los datos del DTO a la entidad de persistencia
            PaymentsEntity paymentEntity = new PaymentsEntity();
            paymentEntity.setPaymentMethod(dto.getPaymentMethod());
            paymentEntity.setAmount(dto.getAmount());
            paymentEntity.setCardNumber(dto.getCardNumber());
            paymentEntity.setCardHolder(dto.getCardHolder());
            paymentEntity.setExpirationDate(dto.getExpirationDate());

            // Asociamos el ID de la reserva usando una entidad limpia con su ID asignado
            com.example.tingeso.Entities.BookingEntity bookingRef = new com.example.tingeso.Entities.BookingEntity();
            bookingRef.setId(dto.getBookingId());
            paymentEntity.setBookingID(bookingRef);

            // Procesamos a través del servicio con las reglas de negocio
            PaymentsEntity confirmedPayment = paymentsService.processPayment(paymentEntity);

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
