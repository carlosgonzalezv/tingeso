package com.example.tingeso.Entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDateTime;
import java.util.List;

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
    @Column(name = "totalamount", nullable = false)
    private int totalAmount;


    //A user can have multiple reservations
    @ManyToOne
    @JoinColumn(name = "userID", nullable = false)
    private UserEntity users;

    //Many of the bookings may be for the same travel package.
    @ManyToOne
    @JoinColumn(name = "packageID", nullable = false)
    private PackTourEntity packTour;

    // En tu BookingEntity actual
    @JsonIgnore
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<CompanionEntity> companions;
    private String specialRequests; // Para las "solicitudes especiales"

    //public BookingEntity(){}
}
