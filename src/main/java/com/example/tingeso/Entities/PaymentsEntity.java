package com.example.tingeso.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    private String paymentMethod; // Ejemplo: "Tarjeta de Crédito", "Débito"
    private Integer amount;
    private LocalDateTime paymentDate;

    // NUEVOS CAMPOS PARA LA SIMULACIÓN
    private String cardNumber;    // Guardaremos algo como "XXXX-XXXX-XXXX-1234"
    private String cardHolder;    // Nombre del titular ingresado
    private String expirationDate;
    private String state;         // "APROBADO", "RECHAZADO"

    @ManyToOne
    @JoinColumn(name= "bookingID", nullable = false)
    private BookingEntity bookingID;
}