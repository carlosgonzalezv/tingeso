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

    // REGLA: Un administrador puede consultar todas las reservas.
    @GetMapping("/")
    public ResponseEntity<?> listAll(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado.");
        }

        // Si no es administrador, bloqueamos el acceso a toda la lista
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

    // REGLA: Un cliente solo puede visualizar sus propias reservas.
    @GetMapping("/my-bookings/{email}")
    public ResponseEntity<?> getByEmail(@PathVariable String email, @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado.");
        }

        // Extraemos el email real del token cifrado de Keycloak
        String tokenEmail = jwt.getClaimAsString("email");
        boolean isAdmin = checkIsAdminManual(jwt);

        // Si el correo de la ruta NO coincide con el del token, y tampoco es ADMIN, bloqueamos (403)
        if (!email.equals(tokenEmail) && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Acceso denegado: No puedes consultar reservas de otro usuario.");
        }

        return ResponseEntity.ok(bookingService.getBookingsByEmail(email));
    }

    @GetMapping("/summary/{bookingId}")
    public ResponseEntity<BookingEntity> getPaymentSummary(@PathVariable Long bookingId) {
        return bookingRepository.findById(bookingId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // REGLA: Gestión y cambio de estados exclusivo para administradores
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String newStatus, @AuthenticationPrincipal Jwt jwt) {
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No autenticado.");
        }

        // Bloqueo de seguridad manual para que solo ADMIN ejecute este PUT
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
     * Validador manual de roles leyendo directo las claims de Keycloak
     */
    private boolean checkIsAdminManual(Jwt jwt) {
        // Busca en la estructura clásica de Keycloak (realm_access -> roles)
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            List<?> roles = (List<?>) realmAccess.get("roles");
            if (roles.contains("ADMIN")) {
                return true;
            }
        }

        // Alternativa si están planos en las claims principales
        List<String> directRoles = jwt.getClaimAsStringList("roles");
        return directRoles != null && directRoles.contains("ADMIN");
    }
}