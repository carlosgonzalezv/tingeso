package com.example.tingeso.Config;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PaymentRequestDTO {
    private Long bookingId;
    private String paymentMethod;
    private Integer amount;

    @Size(min = 16, max = 16, message = "El número de tarjeta debe tener 16 dígitos")
    @Pattern(regexp = "^\\d{16}$", message = "El número de tarjeta debe contener solo números")
    private String cardNumber;
    private String expirationDate;
    private String cvv;
    private String cardHolder;
}