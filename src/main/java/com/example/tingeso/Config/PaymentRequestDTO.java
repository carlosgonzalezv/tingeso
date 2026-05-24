package com.example.tingeso.Config;

import lombok.Data;

@Data
public class PaymentRequestDTO {
    private Long bookingId;
    private String paymentMethod;
    private Integer amount;

    // Datos de tarjeta simulados (Requerimiento específico)
    private String cardNumber;
    private String expirationDate;
    private String cvv;
    private String cardHolder;
}