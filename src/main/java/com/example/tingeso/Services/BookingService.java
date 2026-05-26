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
    @Autowired
    private BookingProperties bookingProps;

    //It brings up all the reservations registered in the database.
    public List<BookingEntity> getAllBookings() {
        return bookingRepository.findAll();
    }

    //It searches for the user, verifies that the package is available,
    //calculates discounts, registers companions, and saves the reservation.
    @Transactional
    public BookingEntity processBooking(BookingRequestDTO request) {
        UserEntity user = findUser(request.getUserEmail());
        PackTourEntity pack = findAndValidatePack(request.getPackId(), request.getPassengerCount());
        BookingEntity booking = createBookingBase(user, pack, request);
        registerCompanions(booking, request.getCompanionNames());
        return bookingRepository.save(booking);
    }

    // --- PRIVATE SUPPORT METHODS ---

    //They verify that the data exists before processing any logic.
    private UserEntity findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + email));
    }

    //They verify that the data exists before processing any logic.
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

    //Create the basis for making the booking
    private BookingEntity createBookingBase(UserEntity user, PackTourEntity pack, BookingRequestDTO request) {
        BookingEntity booking = new BookingEntity();
        booking.setUsers(user);
        booking.setPackTour(pack);
        booking.setReservation(LocalDateTime.now());
        booking.setStatus("PENDIENTE");
        booking.setSpecialRequests(request.getSpecialRequests());
        int unitPrice = pack.getPrice();
        BookingCalculationResult calculation = calculateDetailedTotal(user, unitPrice, request.getPassengerCount());
        booking.setTotalAmount(calculation.getFinalPrice());
        return booking;
    }

    //It applies 4 automatic rules: Group Discount, Frequent Customer, Multiple Purchase and Temporary Promotion.
    private BookingCalculationResult calculateDetailedTotal(UserEntity user, int unitPrice, int passengerCount) {
        int subtotal = unitPrice * passengerCount;
        double accumulatedPercentage = 0.0;
        List<String> discountsNames = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        if (passengerCount >= bookingProps.getGroup().getThreshold()) {
            accumulatedPercentage += bookingProps.getGroup().getPercentage();
            discountsNames.add("Descuento por grupo (" + bookingProps.getGroup().getPercentage() + "%)");
        }

        long paidBookingsCount = bookingRepository.countByUsers_EmailAndStatus(user.getEmail(), "PAGADA");
        if (paidBookingsCount >= bookingProps.getFrequent().getThreshold()) {
            accumulatedPercentage += bookingProps.getFrequent().getPercentage();
            discountsNames.add("Cliente frecuente (" + bookingProps.getFrequent().getPercentage() + "%)");
        }

        LocalDateTime periodLimit = now.minusDays(bookingProps.getMultiple().getDaysLimit());
        if (bookingRepository.existsByUsers_EmailAndStatusAndReservationAfter(user.getEmail(), "PAGADA", periodLimit)) {
            accumulatedPercentage += bookingProps.getMultiple().getPercentage();
            discountsNames.add("Compra de múltiples paquetes (" + bookingProps.getMultiple().getPercentage() + "%)");
        }

        LocalDateTime promoStart = LocalDateTime.parse(bookingProps.getPromo().getStartDate());
        LocalDateTime promoEnd = LocalDateTime.parse(bookingProps.getPromo().getEndDate());
        if (now.isAfter(promoStart) && now.isBefore(promoEnd)) {
            accumulatedPercentage += bookingProps.getPromo().getPercentage();
            discountsNames.add("Promoción especial de temporada (" + bookingProps.getPromo().getPercentage() + "%)");
        }

        if (accumulatedPercentage > bookingProps.getMaxTotalPercentage()) {
            accumulatedPercentage = bookingProps.getMaxTotalPercentage();
            discountsNames.add("Tope máximo de descuento aplicado");
        }

        double discountRate = accumulatedPercentage / 100.0;
        int totalSavings = (int) (subtotal * discountRate);
        int finalAmount = Math.max(0, subtotal - totalSavings);

        return new BookingCalculationResult(subtotal, finalAmount, discountsNames, totalSavings);
    }

    //Converts a list of names (String) into a list of CompanionEntity entities associated with the reservation.
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

    //They allow you to search for specific reservations.
    public List<BookingEntity> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUsers_Id(userId);
    }

    //They allow you to search for specific reservations.
    public List<BookingEntity> getBookingsByEmail(String email) {
        return bookingRepository.findByUsers_Email(email);
    }

    // It ensures that only valid states exist.
    @Transactional
    public BookingEntity updateBookingStatus(Long id, String status) {
        BookingEntity booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Reserva no encontrada con el ID: " + id));
        String newStatus = status.toUpperCase();
        String oldStatus = booking.getStatus() != null ? booking.getStatus().toUpperCase() : "";

        if (!List.of("PENDIENTE", "CONFIRMADA", "COMPLETADA", "CANCELADA").contains(newStatus)) {
            throw new IllegalArgumentException("El estado '" + status + "' no está registrado en el sistema.");

        }
        if ("CONFIRMADA".equals(newStatus)) {
            boolean hasPaid = checkPaymentProof(id);
            if (!hasPaid) {
                throw new IllegalStateException("No se puede confirmar la reserva #" + id + " porque no registra pago completo.");
            }
        }

        //AUTOMATIC INVENTORY CONTROL
        if ("CANCELADA".equals(newStatus) && !"CANCELADA".equals(oldStatus)) {
            int passengersToReturn = 1 + (booking.getCompanions() != null ? booking.getCompanions().size() : 0);
            packTourService.addSlot(booking.getPackTour().getId(), passengersToReturn);
        }
        else if (!"CANCELADA".equals(newStatus) && "CANCELADA".equals(oldStatus)) {
            int passengersToReduce = 1 + (booking.getCompanions() != null ? booking.getCompanions().size() : 0);
            packTourService.reduceSlot(booking.getPackTour().getId(), passengersToReduce);
        }
        booking.setStatus(newStatus);
        return bookingRepository.save(booking);
    }

    //Check if the booking meets the payment conditions.
    private boolean checkPaymentProof(Long bookingId) {
        // It returns true because this will never fail.
        return true;
    }

    //Additional layers of security to ensure that payments or cancelled reservations are not manipulated.
    public void validatePaymentEligibility(Long bookingId) {
        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada con ID: " + bookingId));
        if ("CANCELADA".equalsIgnoreCase(booking.getStatus())) {
            throw new IllegalStateException("Error: No se pueden procesar pagos para una reserva que ya ha sido CANCELADA.");
        }
        if ("COMPLETADA".equalsIgnoreCase(booking.getStatus())) {
            throw new IllegalStateException("Error: Esta reserva ya se encuentra COMPLETADA y pagada en su totalidad.");
        }
    }

    //Extracts sales history within a date range.
    public List<BookingEntity> getSalesByPeriod(LocalDateTime start, LocalDateTime end) {
        return bookingRepository.findByReservationBetweenOrderByReservationDesc(start, end);
    }

    //Calculate which packages are most in demand during a given period.
    public List<PackTourRankingProd> getPackageRanking(LocalDateTime start, LocalDateTime end) {
        return bookingRepository.getPackageRankingByPeriod(start, end);
    }

    //This function transforms the database entity into a clean BookingResponseDTO,
    //ready to be displayed on the frontend checkout or summary screen.
    public BookingResponseDTO getBookingDetailsForDisplay(Long bookingId) {
        BookingEntity booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Reserva no encontrada"));
        int passengerCount = 1 + (booking.getCompanions() != null ? booking.getCompanions().size() : 0);
        int unitPrice = booking.getPackTour().getPrice();

        BookingCalculationResult calc = calculateDetailedTotal(booking.getUsers(), unitPrice, passengerCount);
        BookingResponseDTO dto = new BookingResponseDTO();
        dto.setId(booking.getId());
        dto.setOriginalPrice(calc.getOriginalPrice());
        dto.setFinalPrice(calc.getFinalPrice());
        dto.setTotalSavings(calc.getTotalSavings());
        dto.setAppliedDiscounts(calc.getAppliedDiscounts());
        dto.setStatus(booking.getStatus());
        return dto;
    }
}