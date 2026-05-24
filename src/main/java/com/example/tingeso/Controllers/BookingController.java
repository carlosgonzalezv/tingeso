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

    // RULE: An administrator can query all bookings.
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

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingEntity>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUserId(userId));
    }

    // RULE: A client can only view their own bookings.
    @GetMapping("/my-bookings/{email}")
    public ResponseEntity<?> getByEmail(@PathVariable String email, @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado.");
        }

        // Extract real email from Keycloak token claims
        String tokenEmail = jwt.getClaimAsString("email");
        boolean isAdmin = checkIsAdminManual(jwt);

        // If path email does NOT match token email, and is not ADMIN, block access (403)
        if (!email.equals(tokenEmail) && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: No puedes consultar reservas de otro usuario.");
        }

        return ResponseEntity.ok(bookingService.getBookingsByEmail(email));
    }

    // NUEVA VERSIÓN: Retorna el DTO calculado en lugar de la Entidad cruda
    @GetMapping("/summary/{bookingId}")
    public ResponseEntity<?> getPaymentDetails(@PathVariable Long bookingId, @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado.");
        }

        try {
            // 1. Obtenemos el DTO calculado
            BookingResponseDTO details = bookingService.getBookingDetailsForDisplay(bookingId);

            // 2. Seguridad: Verificamos que el email del token coincida con el usuario de la reserva
            // (Asumiendo que BookingEntity tiene una relación con UserEntity)
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

    // RULE: Management and state changes are exclusive to administrators
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String newStatus, @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado.");
        }

        // Manual security block so only ADMIN can execute this PUT
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

    /**
     * Manual role validator reading Keycloak claims directly
     */
    private boolean checkIsAdminManual(Jwt jwt) {
        // Look into Keycloak structure (realm_access -> roles)
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            List<?> roles = (List<?>) realmAccess.get("roles");
            if (roles.contains("ADMIN")) {
                return true;
            }
        }

        // Alternative if flat in main claims
        List<String> directRoles = jwt.getClaimAsStringList("roles");
        return directRoles != null && directRoles.contains("ADMIN");
    }

    // AGENCY VISIBILITY: Get consolidated statistics for business dashboards
    @GetMapping("/dashboard/stats")
    public ResponseEntity<?> getDashboardStats(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado.");
        }

        // Security: Only admin accesses financial metrics
        if (!checkIsAdminManual(jwt)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Acceso denegado: Requiere rol de Administrador.");
        }

        List<BookingEntity> allBookings = bookingService.getAllBookings();

        // 1. Basic operational calculations
        long totalReservas = allBookings.size();
        long confirmadas = allBookings.stream().filter(b -> "CONFIRMADA".equalsIgnoreCase(b.getStatus())).count();
        long canceladas = allBookings.stream().filter(b -> "CANCELADA".equalsIgnoreCase(b.getStatus())).count();
        long completadas = allBookings.stream().filter(b -> "COMPLETADA".equalsIgnoreCase(b.getStatus())).count();

        // 2. Transform financial data: Total revenue (Sum of CONFIRMADA and COMPLETADA)
        int totalIngresos = allBookings.stream()
                .filter(b -> "CONFIRMADA".equalsIgnoreCase(b.getStatus()) || "COMPLETADA".equalsIgnoreCase(b.getStatus()))
                .mapToInt(BookingEntity::getTotalAmount)
                .sum();

        // 3. Build structured map useful for business analytics
        Map<String, Object> stats = Map.of(
                "totalBookings", totalReservas,
                "confirmedBookings", confirmadas,
                "canceledBookings", canceladas,
                "completedBookings", completadas,
                "totalRevenue", totalIngresos
        );

        return ResponseEntity.ok(stats);
    }

    // REPORTS: Get detailed chronological sales report by period
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

        // RULE VALIDATION: Start date cannot be after end date
        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().body("Error: La fecha de inicio no puede ser posterior a la fecha de término.");
        }

        return ResponseEntity.ok(bookingService.getSalesByPeriod(startDate, endDate));
    }

    // REPORTS: Get package demand ranking by period
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

        // RULE VALIDATION: Start date cannot be after end date
        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().body("Error: La fecha de inicio no puede ser posterior a la fecha de término.");
        }

        return ResponseEntity.ok(bookingService.getPackageRanking(startDate, endDate));
    }
}