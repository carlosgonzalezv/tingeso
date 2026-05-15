package com.example.tingeso.Controllers;

import com.example.tingeso.Entities.PaymentsEntity;
import com.example.tingeso.Services.PaymentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/Payment")
@CrossOrigin("*")
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
}
