package com.example.tingeso.Services;

import com.example.tingeso.Config.BookingCalculationResult;
import com.example.tingeso.Config.BookingRequestDTO;
import com.example.tingeso.Config.BookingProperties;
import com.example.tingeso.Entities.*;
import com.example.tingeso.Repositories.BookingRepository;
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
        packTourService.reduceSlot(pack.getId(), request.getPassengerCount());

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
        int unitPrice = Integer.parseInt(pack.getPrice());

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
}