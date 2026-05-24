package com.example.tingeso.Controllers;

import com.example.tingeso.Entities.PaymentsEntity;
import com.example.tingeso.Services.PaymentsService;
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
    public ResponseEntity<PaymentsEntity> getPaymentByBooking(@PathVariable Long bookingId) { // Cambiado a bookingId
        PaymentsEntity payment = paymentsService.getPaymentByBooking(bookingId);
        if (payment != null) {
            return ResponseEntity.ok(payment);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/process")
    public ResponseEntity<?> process(@RequestBody PaymentRequestDTO request) {
        try {
            // 1. Convertimos el DTO a una Entidad temporal para el servicio
            PaymentsEntity payment = new PaymentsEntity();
            payment.setAmount(request.getAmount());
            payment.setPaymentMethod(request.getPaymentMethod());
            payment.setCardNumber(request.getCardNumber());
            payment.setCardHolder(request.getCardHolder());
            payment.setExpirationDate(request.getExpirationDate());

            // Creamos un "esqueleto" de BookingEntity solo con el ID para que el servicio lo busque
            BookingEntity booking = new BookingEntity();
            booking.setId(request.getBookingId());
            payment.setBookingID(booking);

            // 2. Llamamos al servicio con la entidad preparada
            PaymentsEntity confirmedPayment = paymentsService.processPayment(payment);

            // 3. Respuesta limpia (Map) para evitar recursión/profundidad excesiva
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("message", "¡Pago realizado con éxito!");
            response.put("transactionId", confirmedPayment.getId());
            response.put("amount", confirmedPayment.getAmount());
            response.put("date", confirmedPayment.getPaymentDate());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Devolvemos el mensaje de error de forma clara
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
