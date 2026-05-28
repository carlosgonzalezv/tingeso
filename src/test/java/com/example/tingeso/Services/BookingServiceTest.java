package com.example.tingeso.Services;

import com.example.tingeso.Config.*;
import com.example.tingeso.Entities.*;
import com.example.tingeso.Repositories.BookingRepository;
import com.example.tingeso.Repositories.PackTourRankingProd;
import com.example.tingeso.Repositories.PackTourRepository;
import com.example.tingeso.Repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private PackTourRepository packTourRepository;
    @Mock private UserRepository userRepository;
    @Mock private PackTourService packTourService;
    @Mock private BookingProperties bookingProps;

    @InjectMocks
    private BookingService bookingService;

    private BookingProperties.Group group;
    private BookingProperties.Frequent frequent;
    private BookingProperties.Multiple multiple;
    private BookingProperties.Promo promo;

    @BeforeEach
    void setUp() {
        group = new BookingProperties.Group();
        frequent = new BookingProperties.Frequent();
        multiple = new BookingProperties.Multiple();
        promo = new BookingProperties.Promo();

        promo.setStartDate("2020-01-01T00:00:00");
        promo.setEndDate("2030-01-01T00:00:00");
    }

    @Test
    void getAllBookings_Success() {
        when(bookingRepository.findAll()).thenReturn(List.of(new BookingEntity()));
        assertFalse(bookingService.getAllBookings().isEmpty());
    }

    @Test
    void processBooking_UserNotFound_ThrowsException() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setUserEmail("error@mail.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> bookingService.processBooking(request));
    }

    @Test
    void processBooking_PackNotFound_ThrowsException() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setUserEmail("test@mail.com");
        request.setPackId(1L);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new UserEntity()));
        when(packTourRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> bookingService.processBooking(request));
    }

    @Test
    void processBooking_PackNoVigente_ThrowsException() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setUserEmail("test@mail.com"); request.setPackId(1L);
        PackTourEntity pack = new PackTourEntity(); pack.setStatus("NO VIGENTE");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new UserEntity()));
        when(packTourRepository.findById(1L)).thenReturn(Optional.of(pack));
        assertThrows(IllegalStateException.class, () -> bookingService.processBooking(request));
    }

    @Test
    void processBooking_PackAgotado_ThrowsException() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setUserEmail("test@mail.com"); request.setPackId(1L); request.setPassengerCount(5);
        PackTourEntity pack = new PackTourEntity(); pack.setStatus("AGOTADO"); pack.setAvailableSlots(2);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new UserEntity()));
        when(packTourRepository.findById(1L)).thenReturn(Optional.of(pack));
        assertThrows(IllegalStateException.class, () -> bookingService.processBooking(request));
    }

    @Test
    void processBooking_AllDiscountsAndMaxCapHit_Success() {
        BookingRequestDTO request = new BookingRequestDTO();
        request.setUserEmail("test@test.com"); request.setPackId(1L); request.setPassengerCount(10);
        request.setCompanionNames(List.of("Acompañante 1"));

        UserEntity user = new UserEntity(); user.setEmail("test@test.com");
        PackTourEntity pack = new PackTourEntity(); pack.setStatus("DISPONIBLE"); pack.setPrice(100); pack.setAvailableSlots(20);

        group.setThreshold(4); group.setPercentage(15);
        frequent.setThreshold(2); frequent.setPercentage(10);
        multiple.setDaysLimit(30); multiple.setPercentage(10);
        promo.setPercentage(10);

        when(bookingProps.getGroup()).thenReturn(group);
        when(bookingProps.getFrequent()).thenReturn(frequent);
        when(bookingProps.getMultiple()).thenReturn(multiple);
        when(bookingProps.getPromo()).thenReturn(promo);
        when(bookingProps.getMaxTotalPercentage()).thenReturn(20);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(packTourRepository.findById(anyLong())).thenReturn(Optional.of(pack));
        when(bookingRepository.countByUsers_EmailAndStatus(anyString(), anyString())).thenReturn(5L);
        when(bookingRepository.existsByUsers_EmailAndStatusAndReservationAfter(anyString(), anyString(), any())).thenReturn(true);
        when(bookingRepository.save(any(BookingEntity.class))).thenReturn(new BookingEntity());

        BookingEntity result = bookingService.processBooking(request);
        assertNotNull(result);
    }

    @Test
    void getBookingsByUserId_AndEmail_Success() {
        when(bookingRepository.findByUsers_Id(1L)).thenReturn(new ArrayList<>());
        when(bookingRepository.findByUsers_Email("a@a.com")).thenReturn(new ArrayList<>());
        assertNotNull(bookingService.getBookingsByUserId(1L));
        assertNotNull(bookingService.getBookingsByEmail("a@a.com"));
    }

    @Test
    void updateBookingStatus_NotFound_ThrowsException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> bookingService.updateBookingStatus(1L, "PENDIENTE"));
    }

    @Test
    void updateBookingStatus_InvalidStatus_ThrowsException() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(new BookingEntity()));
        assertThrows(IllegalArgumentException.class, () -> bookingService.updateBookingStatus(1L, "ESTADO_FALSO"));
    }

    @Test
    void updateBookingStatus_CancelToPending_ReducesSlot() {
        BookingEntity booking = new BookingEntity(); booking.setStatus("CANCELADA");
        PackTourEntity pack = new PackTourEntity(); pack.setId(5L); booking.setPackTour(pack);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);

        bookingService.updateBookingStatus(1L, "PENDIENTE");
        verify(packTourService).reduceSlot(eq(5L), anyInt());
    }

    @Test
    void updateBookingStatus_PendingToCancel_AddsSlot() {
        BookingEntity booking = new BookingEntity(); booking.setStatus("PENDIENTE");
        PackTourEntity pack = new PackTourEntity(); pack.setId(5L); booking.setPackTour(pack);
        booking.setCompanions(List.of(new CompanionEntity()));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any())).thenReturn(booking);

        bookingService.updateBookingStatus(1L, "CANCELADA");
        verify(packTourService).addSlot(eq(5L), eq(2));
    }

    @Test
    void validatePaymentEligibility_Scenarios() {
        BookingEntity cancelada = new BookingEntity(); cancelada.setStatus("CANCELADA");
        BookingEntity completada = new BookingEntity(); completada.setStatus("COMPLETADA");
        BookingEntity pendiente = new BookingEntity(); pendiente.setStatus("PENDIENTE");

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(cancelada));
        when(bookingRepository.findById(2L)).thenReturn(Optional.of(completada));
        when(bookingRepository.findById(3L)).thenReturn(Optional.of(pendiente));
        when(bookingRepository.findById(4L)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> bookingService.validatePaymentEligibility(1L));
        assertThrows(IllegalStateException.class, () -> bookingService.validatePaymentEligibility(2L));
        assertThrows(RuntimeException.class, () -> bookingService.validatePaymentEligibility(4L));
        assertDoesNotThrow(() -> bookingService.validatePaymentEligibility(3L));
    }

    @Test
    void getSalesAndRanking_Success() {
        LocalDateTime now = LocalDateTime.now();
        when(bookingRepository.findByReservationBetweenOrderByReservationDesc(any(), any())).thenReturn(new ArrayList<>());
        when(bookingRepository.getPackageRankingByPeriod(any(), any())).thenReturn(new ArrayList<>());

        assertNotNull(bookingService.getSalesByPeriod(now, now));
        assertNotNull(bookingService.getPackageRanking(now, now));
    }

    @Test
    void getBookingDetailsForDisplay_Success() {
        BookingEntity booking = new BookingEntity(); booking.setId(10L); booking.setStatus("PENDIENTE");
        UserEntity user = new UserEntity(); user.setEmail("a@a.com"); booking.setUsers(user);
        PackTourEntity pack = new PackTourEntity(); pack.setPrice(100); booking.setPackTour(pack);

        when(bookingProps.getGroup()).thenReturn(group);
        when(bookingProps.getFrequent()).thenReturn(frequent);
        when(bookingProps.getMultiple()).thenReturn(multiple);
        when(bookingProps.getPromo()).thenReturn(promo);

        when(bookingRepository.findById(10L)).thenReturn(Optional.of(booking));

        BookingResponseDTO dto = bookingService.getBookingDetailsForDisplay(10L);
        assertNotNull(dto);
        assertEquals(10L, dto.getId());
    }
}