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

    /**
     * Executes every hour to release slots from unpaid bookings.
     * Cron expression: second, minute, hour, day, month, day-of-week.
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void releaseExpiredBookings() {
        // Define the expiration threshold (e.g., 24 hours)
        LocalDateTime threshold = LocalDateTime.now().minusHours(24);

        // Find bookings with "PENDIENTE" status created before the threshold
        List<BookingEntity> expiredBookings = bookingRepository.findByStatusAndReservationBefore("PENDIENTE", threshold);

        for (BookingEntity booking : expiredBookings) {
            // 1. Update the booking status to "EXPIRADA"
            booking.setStatus("EXPIRADA");

            // 2. OPERATIONAL RULE: Release the slots back to the package
            // Logic: Total passengers = Lead passenger (1) + Companions count
            int slotsToRelease = booking.getCompanions().size() + 1;

            packTourService.addSlot(booking.getPackTour().getId(), slotsToRelease);

            // 3. Persist the status change
            bookingRepository.save(booking);

            System.out.println("Booking ID " + booking.getId() + " has expired. Slots released: " + slotsToRelease);
        }
    }
}