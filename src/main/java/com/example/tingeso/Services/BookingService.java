package com.example.tingeso.Services;

import com.example.tingeso.Entities.BookingRequestDTO;
import com.example.tingeso.Entities.BookingEntity;
import com.example.tingeso.Entities.PackTourEntity;
import com.example.tingeso.Entities.UserEntity;
import com.example.tingeso.Repositories.BookingRepository;
import com.example.tingeso.Repositories.PackTourRepository;
import com.example.tingeso.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PackTourRepository packTourRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PackTourService packTourService;

    /**
     * Retrieves all booking records from the database.
     * @return A list of all existing bookings.
     */
    public List<BookingEntity> getAllBookings() {
        return bookingRepository.findAll();
    }

    /**
     * Processes a complete booking request from the web platform.
     * It validates user existence, package availability, and expiration dates.
     * It also calculates the total amount based on the passenger count and updates stock.
     * * @param request DTO containing packId, userEmail, passengerCount, and additional info.
     * @return The saved BookingEntity if successful.
     * @throws IllegalStateException if the package is expired or if there are not enough slots.
     */
    public BookingEntity processBooking(BookingRequestDTO request) {
        // Find the user by email to associate with the booking
        UserEntity user = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getUserEmail()));

        // Retrieve the travel package details
        PackTourEntity pack = packTourRepository.findById(request.getPackId())
                .orElseThrow(() -> new RuntimeException("Travel package not found with ID: " + request.getPackId()));

        // Validation: Check if the package's end date has already passed
        if (pack.getFinishDate().isBefore(LocalDateTime.now())) {
            pack.setStatus("NO VIGENTE");
            packTourRepository.save(pack);
            throw new IllegalStateException("Cannot book: The travel package has expired.");
        }

        // Validation: Real-time slot availability check
        if (pack.getAvailableSlots() < request.getPassengerCount()) {
            throw new IllegalStateException("Insufficient slots available. Requested: "
                    + request.getPassengerCount() + ", Available: " + pack.getAvailableSlots());
        }

        // Business Logic: Reduce the number of available slots in the package
        packTourService.reduceSlot(pack.getId(), request.getPassengerCount());

        // Entity Mapping: Construct the booking record
        BookingEntity booking = new BookingEntity();
        booking.setUsers(user);
        booking.setPackTour(pack);
        booking.setReservation(LocalDateTime.now());
        booking.setStatus("PENDIENTE");

        // Additional info: Optional requests from the client
        // booking.setSpecialRequests(request.getSpecialRequests()); // Ensure this field exists in your Entity

        // Financial Calculation: Multiply unit price by the number of passengers
        int unitPrice = Integer.parseInt(pack.getPrice());
        booking.setTotalAmount(unitPrice * request.getPassengerCount());

        return bookingRepository.save(booking);
    }

    /**
     * Retrieves all bookings associated with a specific user ID.
     * @param userId The ID of the user.
     * @return A list of bookings filtered by the user.
     */
    public List<BookingEntity> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUsers_Id(userId);
    }
}