package com.example.tingeso.Services;

import com.example.tingeso.Config.BookingCalculationResult;
import com.example.tingeso.Config.BookingRequestDTO;
import com.example.tingeso.Config.BookingProperties;
import com.example.tingeso.Config.BookingResponseDTO;
import com.example.tingeso.Entities.*;
import com.example.tingeso.Repositories.BookingRepository;
import com.example.tingeso.Repositories.PackTourRankingProd;
import com.example.tingeso.Repositories.PackTourRepository;
import com.example.tingeso.Repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    // Configuración centralizada de reglas de negocio
    @Autowired
    private BookingProperties bookingProps;

    public List<BookingEntity> getAllBookings() {
        return bookingRepository.findAll();
    }

    @Transactional
    public BookingEntity processBooking(BookingRequestDTO request) {
        // 1. Validación y obtención de entidades
        UserEntity user = findUser(request.getUserEmail());
        PackTourEntity pack = findAndValidatePack(request.getPackId(), request.getPassengerCount());

        // 2. Lógica operativa: reducir cupos
       // packTourService.reduceSlot(pack.getId(), request.getPassengerCount());

        // 3. Creación de la reserva con cálculos financieros
        BookingEntity booking = createBookingBase(user, pack, request);

        // 4. Registro de acompañantes
        registerCompanions(booking, request.getCompanionNames());

        return bookingRepository.save(booking);
    }

    // --- MÉTODOS DE APOYO PRIVADOS ---

    private UserEntity findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
    }

    private PackTourEntity findAndValidatePack(Long packId, int passengerCount) {
        PackTourEntity pack = packTourRepository.findById(packId)
                .orElseThrow(() -> new RuntimeException("Paquete no encontrado"));

        if ("NO VIGENTE".equals(pack.getStatus()) || "CANCELADO".equals(pack.getStatus())) {
            throw new IllegalStateException("El paquete no está disponible para reserva.");
        }

        if ("AGOTADO".equals(pack.getStatus()) || pack.getAvailableSlots() < passengerCount) {
            throw new IllegalStateException("No hay cupos suficientes.");
        }

        return pack;
    }

    private BookingEntity createBookingBase(UserEntity user, PackTourEntity pack, BookingRequestDTO request) {
        BookingEntity booking = new BookingEntity();
        booking.setUsers(user);
        booking.setPackTour(pack);
        booking.setReservation(LocalDateTime.now());
        booking.setStatus("PENDIENTE");
        booking.setSpecialRequests(request.getSpecialRequests());

        // Cálculo financiero detallado
        int unitPrice = pack.getPrice();

        // Aquí recibimos la "caja" con todo el desglose
        BookingCalculationResult calculation = calculateDetailedTotal(user, unitPrice, request.getPassengerCount());

        // Guardamos solo el monto final en la entidad (DB)
        booking.setTotalAmount(calculation.getFinalPrice());

        return booking;
    }

    /**
     * Motor de Cálculo: Aplica reglas de negocio y registra el origen de los descuentos.
     * Cumple con la regla de Transparencia al generar 'discountsNames'.
     */
    private BookingCalculationResult calculateDetailedTotal(UserEntity user, int unitPrice, int passengerCount) {
        int subtotal = unitPrice * passengerCount;
        double accumulatedPercentage = 0.0;
        List<String> discountsNames = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // REGLA 1: Descuento por Grupo
        if (passengerCount >= bookingProps.getGroup().getThreshold()) {
            accumulatedPercentage += bookingProps.getGroup().getPercentage();
            discountsNames.add("Descuento por grupo (" + bookingProps.getGroup().getPercentage() + "%)");
        }

        // REGLA 2: Cliente Frecuente
        long paidBookingsCount = bookingRepository.countByUsers_EmailAndStatus(user.getEmail(), "PAGADA");
        if (paidBookingsCount >= bookingProps.getFrequent().getThreshold()) {
            accumulatedPercentage += bookingProps.getFrequent().getPercentage();
            discountsNames.add("Cliente frecuente (" + bookingProps.getFrequent().getPercentage() + "%)");
        }

        // REGLA 3: Compra Múltiple (Paquetes recientes)
        LocalDateTime periodLimit = now.minusDays(bookingProps.getMultiple().getDaysLimit());
        if (bookingRepository.existsByUsers_EmailAndStatusAndReservationAfter(user.getEmail(), "PAGADA", periodLimit)) {
            accumulatedPercentage += bookingProps.getMultiple().getPercentage();
            discountsNames.add("Compra de múltiples paquetes (" + bookingProps.getMultiple().getPercentage() + "%)");
        }

        // REGLA 4: Promoción Temporal
        LocalDateTime promoStart = LocalDateTime.parse(bookingProps.getPromo().getStartDate());
        LocalDateTime promoEnd = LocalDateTime.parse(bookingProps.getPromo().getEndDate());
        if (now.isAfter(promoStart) && now.isBefore(promoEnd)) {
            accumulatedPercentage += bookingProps.getPromo().getPercentage();
            discountsNames.add("Promoción especial de temporada (" + bookingProps.getPromo().getPercentage() + "%)");
        }

        // Límite máximo (CAP)
        if (accumulatedPercentage > bookingProps.getMaxTotalPercentage()) {
            accumulatedPercentage = bookingProps.getMaxTotalPercentage();
            discountsNames.add("Tope máximo de descuento aplicado");
        }

        // Cálculos finales
        double discountRate = accumulatedPercentage / 100.0;
        int totalSavings = (int) (subtotal * discountRate);
        int finalAmount = Math.max(0, subtotal - totalSavings);

        // Retornamos el resultado completo
        return new BookingCalculationResult(subtotal, finalAmount, discountsNames, totalSavings);
    }

    private void registerCompanions(BookingEntity booking, List<String> names) {
        if (names != null && !names.isEmpty()) {
            List<CompanionEntity> companions = names.stream()
                    .map(name -> {
                        CompanionEntity companion = new CompanionEntity();
                        companion.setFullName(name);
                        companion.setBooking(booking);
                        return companion;
                    }).toList();

            booking.setCompanions(companions);
        }
    }

    public List<BookingEntity> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUsers_Id(userId);
    }

    // 1. VISIBILIDAD CLIENTE: Obtener reservas filtradas por el email de Keycloak
    public List<BookingEntity> getBookingsByEmail(String email) {
        return bookingRepository.findByUsers_Email(email);
    }

    // 2. GESTIÓN DE ESTADOS BLINDADA CON REGLAS DE NEGOCIO (Agencia / Sistema)
    @Transactional
    public BookingEntity updateBookingStatus(Long id, String status) {
        BookingEntity booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Reserva no encontrada con el ID: " + id));

        String newStatus = status.toUpperCase();
        String oldStatus = booking.getStatus() != null ? booking.getStatus().toUpperCase() : "";

        // REGLA 1: Toda reserva debe tener un estado válido y controlado por el sistema.
        if (!List.of("PENDIENTE", "CONFIRMADA", "COMPLETADA", "CANCELADA").contains(newStatus)) {
            throw new IllegalArgumentException("El estado '" + status + "' no está registrado en el sistema.");

        }

        // REGLA 2: Una reserva solo puede marcarse como confirmada si cumple las condiciones (Pago Completo).
        if ("CONFIRMADA".equals(newStatus)) {
            boolean hasPaid = checkPaymentProof(id);
            if (!hasPaid) {
                throw new IllegalStateException("No se puede confirmar la reserva #" + id + " porque no registra pago completo.");
            }
        }

        // CONTROL AUTOMÁTICO DE INVENTARIO:
        // Si la reserva pasa a CANCELADA y antes no lo estaba, devolvemos los cupos a la base de datos
        if ("CANCELADA".equals(newStatus) && !"CANCELADA".equals(oldStatus)) {
            int passengersToReturn = 1 + (booking.getCompanions() != null ? booking.getCompanions().size() : 0);
            packTourService.addSlot(booking.getPackTour().getId(), passengersToReturn);
        }
        // Reversa: Si por alguna razón una reserva cancelada vuelve a activarse, restamos los cupos nuevamente
        else if (!"CANCELADA".equals(newStatus) && "CANCELADA".equals(oldStatus)) {
            int passengersToReduce = 1 + (booking.getCompanions() != null ? booking.getCompanions().size() : 0);
            packTourService.reduceSlot(booking.getPackTour().getId(), passengersToReduce);
        }

        booking.setStatus(newStatus);
        return bookingRepository.save(booking);
    }

    /**
     * Validador auxiliar de reglas: Comprueba si la reserva cumple las condiciones de pago.
     */
    private boolean checkPaymentProof(Long bookingId) {
        // Por ahora retorna true para tus pruebas de flujo generales en local.
        // Si tienes una entidad o tabla de transacciones de pago/boletas, aquí debes validar
        // que el monto pagado coincida con el totalAmount de la reserva.
        return true;
    }

    public void validatePaymentEligibility(Long bookingId) {
        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada con ID: " + bookingId));

        // REGLA DE NEGOCIO: Una reserva cancelada no debe aceptar nuevos pagos.
        if ("CANCELADA".equalsIgnoreCase(booking.getStatus())) {
            throw new IllegalStateException("Error: No se pueden procesar pagos para una reserva que ya ha sido CANCELADA.");
        }

        // Opcional: También podrías bloquear si ya está COMPLETADA
        if ("COMPLETADA".equalsIgnoreCase(booking.getStatus())) {
            throw new IllegalStateException("Error: Esta reserva ya se encuentra COMPLETADA y pagada en su totalidad.");
        }
    }
    public List<BookingEntity> getSalesByPeriod(LocalDateTime start, LocalDateTime end) {
        return bookingRepository.findByReservationBetweenOrderByReservationDesc(start, end);
    }

    public List<PackTourRankingProd> getPackageRanking(LocalDateTime start, LocalDateTime end) {
        return bookingRepository.getPackageRankingByPeriod(start, end);
    }

    public BookingResponseDTO getBookingDetailsForDisplay(Long bookingId) {
        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));

        // Calcular cuántos pasajeros (1 titular + acompañantes)
        int passengerCount = 1 + (booking.getCompanions() != null ? booking.getCompanions().size() : 0);
        int unitPrice = booking.getPackTour().getPrice();

        // Re-calculamos con tu motor de reglas
        BookingCalculationResult calc = calculateDetailedTotal(booking.getUsers(), unitPrice, passengerCount);

        // Mapeamos al DTO
        BookingResponseDTO dto = new BookingResponseDTO();
        dto.setId(booking.getId());

        // USAMOS LOS GETTERS QUE LOMBOK CREÓ PARA TI:
        dto.setOriginalPrice(calc.getOriginalPrice()); // <--- Corregido
        dto.setFinalPrice(calc.getFinalPrice());
        dto.setTotalSavings(calc.getTotalSavings());
        dto.setAppliedDiscounts(calc.getAppliedDiscounts()); // <--- Corregido
        dto.setStatus(booking.getStatus());

        return dto;
    }
}