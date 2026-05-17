package com.example.tingeso.Repositories;

import com.example.tingeso.Entities.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {
    List<BookingEntity> findByUsers_Id(Long Id);
    List<BookingEntity> findByStatus(String status);
    List<BookingEntity> findByPackTourId(Long packTourId);
    boolean existsByUsers_Id(Long Id);
    long countByPackTourId(Long packTourId);
    List<BookingEntity> findByStatusAndReservationBefore(String status, LocalDateTime date);
    // Counts how many "PAGADA" bookings a specific user has
    long countByUsers_EmailAndStatus(String email, String status);
    boolean existsByUsers_EmailAndStatusAndReservationAfter(String email, String status, LocalDateTime date);
    List<BookingEntity> findByUserEmail(String userEmail);

}


