package com.example.tingeso.Controllers;

import com.example.tingeso.Entities.BookingEntity;
import com.example.tingeso.Config.BookingRequestDTO;
import com.example.tingeso.Repositories.BookingRepository;
import com.example.tingeso.Services.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import com.example.tingeso.Config.BookingResponseDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/booking")
@CrossOrigin("*")
public class BookingController {

    @Autowired
    private BookingService bookingService;
    @Autowired
    private BookingRepository bookingRepository;

    //Returns the complete list of all existing reservations.
    @GetMapping("/")
    public ResponseEntity<?> listAll(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado.");
        }
        // If not admin, block access to the entire list
        if (!checkIsAdminManual(jwt)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: Se requieren permisos de administrador.");
        }
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    //Create a new reservation from the submitted data
    @PostMapping("/create")
    public ResponseEntity<?> createFromWeb(@RequestBody BookingRequestDTO request) {
        try {
            BookingEntity newBooking = bookingService.processBooking(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(newBooking);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    //Returns a list of reservations associated with a specific user ID.
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingEntity>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUserId(userId));
    }

    //Returns a user's reservations based on their email address.
    //Validates that the email belongs to the logged-in user or that the queryer is an admin.
    @GetMapping("/my-bookings/{email}")
    public ResponseEntity<?> getByEmail(@PathVariable String email, @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado.");
        }
        String tokenEmail = jwt.getClaimAsString("email");
        boolean isAdmin = checkIsAdminManual(jwt);
        if (!email.equals(tokenEmail) && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: No puedes consultar reservas de otro usuario.");
        }
        return ResponseEntity.ok(bookingService.getBookingsByEmail(email));
    }

    //Get the payment details for a specific booking.
    @GetMapping("/summary/{bookingId}")
    public ResponseEntity<?> getPaymentDetails(@PathVariable Long bookingId, @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado.");
        }
        try {
            BookingResponseDTO details = bookingService.getBookingDetailsForDisplay(bookingId);
            BookingEntity originalBooking = bookingRepository.findById(bookingId).orElseThrow();
            String tokenEmail = jwt.getClaimAsString("email");
            if (!originalBooking.getUsers().getEmail().equals(tokenEmail) && !checkIsAdminManual(jwt)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: No puedes ver esta reserva.");
            }
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + e.getMessage());
        }
    }

    //Change the status of a reservation
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String newStatus, @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado.");
        }
        if (!checkIsAdminManual(jwt)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: Solo la agencia puede cambiar estados.");
        }
        try {
            if (!List.of("PENDIENTE", "CONFIRMADA", "CANCELADA", "COMPLETADA").contains(newStatus.toUpperCase())) {
                return ResponseEntity.badRequest().body("Estado de reserva no válido.");
            }
            BookingEntity updatedBooking = bookingService.updateBookingStatus(id, newStatus.toUpperCase());
            return ResponseEntity.ok(updatedBooking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al actualizar estado: " + e.getMessage());
        }
    }

    //Extract the realm_access claims or roles from the JWT token.
    private boolean checkIsAdminManual(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            List<?> roles = (List<?>) realmAccess.get("roles");
            if (roles.contains("ADMIN")) {
                return true;
            }
        }
        List<String> directRoles = jwt.getClaimAsStringList("roles");
        return directRoles != null && directRoles.contains("ADMIN");
    }

    //Calculate quick metrics such as total bookings, number of bookings per state, and total cumulative revenue.
    @GetMapping("/dashboard/stats")
    public ResponseEntity<?> getDashboardStats(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado.");
        }
        if (!checkIsAdminManual(jwt)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: Requiere rol de Administrador.");
        }
        List<BookingEntity> allBookings = bookingService.getAllBookings();
        long totalReservas = allBookings.size();
        long confirmadas = allBookings.stream().filter(b -> "CONFIRMADA".equalsIgnoreCase(b.getStatus())).count();
        long canceladas = allBookings.stream().filter(b -> "CANCELADA".equalsIgnoreCase(b.getStatus())).count();
        long completadas = allBookings.stream().filter(b -> "COMPLETADA".equalsIgnoreCase(b.getStatus())).count();
        int totalIngresos = allBookings.stream()
                .filter(b -> "CONFIRMADA".equalsIgnoreCase(b.getStatus()) || "COMPLETADA".equalsIgnoreCase(b.getStatus()))
                .mapToInt(BookingEntity::getTotalAmount)
                .sum();
        Map<String, Object> stats = Map.of(
                "totalBookings", totalReservas,
                "confirmedBookings", confirmadas,
                "canceledBookings", canceladas,
                "completedBookings", completadas,
                "totalRevenue", totalIngresos
        );

        return ResponseEntity.ok(stats);
    }

    // It receives a date range and returns the detailed sales report for that period.
    @GetMapping("/reports/sales")
    public ResponseEntity<?> getSalesByPeriod(
            @RequestParam String start,
            @RequestParam String end,
            @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null || !checkIsAdminManual(jwt)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado.");
        }
        LocalDateTime startDate = LocalDateTime.parse(start);
        LocalDateTime endDate = LocalDateTime.parse(end);
        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().body("Error: La fecha de inicio no puede ser posterior a la fecha de término.");
        }
        return ResponseEntity.ok(bookingService.getSalesByPeriod(startDate, endDate));
    }

    // Analyze which tour packages have had the highest demand between two given dates.
    @GetMapping("/reports/ranking")
    public ResponseEntity<?> getRankingByPeriod(
            @RequestParam String start,
            @RequestParam String end,
            @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null || !checkIsAdminManual(jwt)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado.");
        }
        LocalDateTime startDate = LocalDateTime.parse(start);
        LocalDateTime endDate = LocalDateTime.parse(end);
        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().body("Error: La fecha de inicio no puede ser posterior a la fecha de término.");
        }
        return ResponseEntity.ok(bookingService.getPackageRanking(startDate, endDate));
    }
}