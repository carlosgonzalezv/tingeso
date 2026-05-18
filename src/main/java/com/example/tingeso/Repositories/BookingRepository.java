package com.example.tingeso.Repositories;

import com.example.tingeso.Entities.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // REPORTE 1: Listado de ventas por período EXCLUYENDO las canceladas por pauta de negocio
    @Query("SELECT booking FROM BookingEntity booking " +
            "WHERE booking.reservation BETWEEN :start AND :end " +
            "AND UPPER(booking.status) != 'CANCELADA' " +
            "ORDER BY booking.reservation DESC")
    List<BookingEntity> findByReservationBetweenOrderByReservationDesc(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // REPORTE 2: Ranking de demanda de paquetes consolidado por período
    @Query("""
        SELECT b.packTour.name AS packageName, COUNT(b) AS totalBookings, SUM(b.totalAmount) AS generatedAmount, SUM(b.passengersCount) AS totalPassengers 
        FROM BookingEntity b WHERE b.reservation BETWEEN :start AND :end AND UPPER(b.status) != 'CANCELADA' GROUP BY b.packTour.name ORDER BY COUNT(b) DESC, SUM(b.totalAmount) DESC, b.packTour.name ASC
    """)
    List<PackTourRankingProd> getPackageRankingByPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}