package com.example.tingeso.Services;

import com.example.tingeso.Entities.BookingEntity;
import com.example.tingeso.Entities.PackTourEntity;
import com.example.tingeso.Repositories.BookingRepository;
import com.example.tingeso.Repositories.PackTourRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookingService {
    @Autowired
    BookingRepository bookingRepository;
    @Autowired
    PackTourRepository packTourRepository;


    public ArrayList<BookingEntity> getBooking() {
        return(ArrayList<BookingEntity>) bookingRepository.findAll();
    }

    //Check if the travel package still has available slots to book
    public BookingEntity createBooking(BookingEntity booking) {
        if (booking.getPackTour() == null || booking.getPackTour().getId() == null) {
            throw new IllegalArgumentException("La reserva debe estar asociada a un paquete válido.");
        }
        PackTourEntity pack = packTourRepository.findById(booking.getPackTour().getId())
                .orElseThrow(() -> new RuntimeException("El paquete solicitado no existe."));

        if (!"DISPONIBLE".equals(pack.getStatus())) {
            throw new IllegalStateException("No se pueden realizar reservas: El paquete está " + pack.getStatus());
        }
        if (pack.getFinishDate().isBefore(LocalDateTime.now())) {
            pack.setStatus("NO VIGENTE");
            packTourRepository.save(pack);
            throw new IllegalStateException("No se pueden realizar reservas: El paquete ya no está vigente.");
        }
        booking.setPackTour(pack);
        return bookingRepository.save(booking);
    }

    public List<BookingEntity> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUsers_Id(userId);
    }
}
