package com.example.tingeso.Controllers;

import com.example.tingeso.Entities.BookingEntity;
import com.example.tingeso.Services.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/booking")
@CrossOrigin("*")
public class BookingController {
    @Autowired
    BookingService bookingService;

    //It retrieves all reservation records that exist in the system.
    @GetMapping("/")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ArrayList<BookingEntity>> listBookings() {
        return ResponseEntity.ok(bookingService.getBooking());
    }

    //It creates a new reserve, but with a security condition.
    @PostMapping("/")
    public ResponseEntity<BookingEntity> saveBooking(@RequestBody BookingEntity booking) {
        BookingEntity newBooking = bookingService.createBooking(booking);
        if (newBooking != null) {
            return ResponseEntity.ok(newBooking);
        } else {
            return ResponseEntity.badRequest().build();//This happens when there are no available slots
        }
    }

    //Filter the database to show only what belongs to a specific user.
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingEntity>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUserId(userId));
    }
}
