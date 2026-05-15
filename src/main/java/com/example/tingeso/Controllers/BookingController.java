package com.example.tingeso.Controllers;

import com.example.tingeso.Entities.BookingEntity;
import com.example.tingeso.Config.BookingRequestDTO; // Ajusta el import si lo dejaste ahí
import com.example.tingeso.Services.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/booking")
@CrossOrigin("*")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    // Standard list for admin or general view
    @GetMapping("/")
    public ResponseEntity<List<BookingEntity>> listAll() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    /**
     * Unified endpoint to process bookings from the web.
     * It handles passengers, slots, and business logic.
     */
    @PostMapping("/create")
    public ResponseEntity<?> createFromWeb(@RequestBody BookingRequestDTO request) {
        try {
            // This replaces saveBooking and createBookingFromWeb
            BookingEntity newBooking = bookingService.processBooking(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(newBooking);
        } catch (IllegalStateException e) {
            // Catching business logic errors (no slots, expired, etc.)
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingEntity>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUserId(userId));
    }
}