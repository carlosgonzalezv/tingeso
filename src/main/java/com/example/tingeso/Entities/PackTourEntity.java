package com.example.tingeso.Entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Tourist_Package")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PackTourEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;
    private String name;
    private String destination;
    private String description;

    @Min(value = 1, message = "el precio es mayor a 0")
    private Integer price;

    @Min(value = 1, message = "Los cupos totales deben ser al menos 1")
    private Integer totalSlots;
    @Min(value = 0, message = "Los cupos disponibles no pueden ser negativos")
    @Column(name = "available_slots")
    private Integer availableSlots;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDate;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime finishDate;
    private String status;
}
