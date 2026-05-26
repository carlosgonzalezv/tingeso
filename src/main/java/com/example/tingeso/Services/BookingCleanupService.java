package com.example.tingeso.Services;

import com.example.tingeso.Entities.BookingEntity;
import com.example.tingeso.Repositories.BookingRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingCleanupService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PackTourService packTourService;

    //It acts as an "automatic cleaner"
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void releaseExpiredBookings() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);
        List<BookingEntity> expiredBookings = bookingRepository.findByStatusAndReservationBefore("PENDIENTE", threshold);
        for (BookingEntity booking : expiredBookings) {
            booking.setStatus("EXPIRADA");
            int slotsToRelease = booking.getCompanions().size() + 1;
            packTourService.addSlot(booking.getPackTour().getId(), slotsToRelease);
            bookingRepository.save(booking);
            System.out.println("Booking ID " + booking.getId() + " has expired. Slots released: " + slotsToRelease);
        }
    }
}