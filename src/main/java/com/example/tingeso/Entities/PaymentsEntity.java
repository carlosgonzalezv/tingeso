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
    private String paymentMethod;
    private int amount;
    private LocalDateTime paymentDate;
    private String state;

    @ManyToOne
    @JoinColumn(name= "bookingID", nullable = false)
    private BookingEntity bookingID;
}
