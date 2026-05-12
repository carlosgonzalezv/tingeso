package com.example.tingeso.Entities;

import jakarta.persistence.*;

@Entity
public class CompanionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String fullName;
    private String rut; // O DNI según necesites

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private BookingEntity booking;
}