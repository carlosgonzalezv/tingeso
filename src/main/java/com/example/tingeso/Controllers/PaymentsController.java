package com.example.tingeso.Controllers;

import com.example.tingeso.Entities.PaymentsEntity;
import com.example.tingeso.Services.PaymentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/Payment")
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
    public ResponseEntity<PaymentsEntity> getPaymentByBooking(@PathVariable Long bookingID) {
        PaymentsEntity payment = paymentsService.getPaymentByBooking(bookingID);
        if (payment != null) {
            return ResponseEntity.ok(payment);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/process")
    public ResponseEntity<?> process(@RequestBody PaymentsEntity payment) {
        try {
            PaymentsEntity confirmedPayment = paymentsService.processPayment(payment);

            // Usamos un Map para enviar la respuesta sin crear una clase nueva
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
