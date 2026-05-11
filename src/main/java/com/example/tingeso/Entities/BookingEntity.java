package com.example.tingeso.Entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;

@Entity
@Table(name = "booking")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;
    private LocalDateTime reservation;
    private String status;
    private int totalAmount;

    //A user can have multiple reservations
    @ManyToOne
    @JoinColumn(name = "userID", nullable = false)
    private UserEntity users;

    //Many of the bookings may be for the same travel package.
    @ManyToOne
    @JoinColumn(name = "packageID", nullable = false)
    private PackTourEntity packTour;

    //public BookingEntity(){}
}
