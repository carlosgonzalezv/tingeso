package com.example.tingeso.Services;

import com.example.tingeso.Entities.BookingEntity;
import com.example.tingeso.Entities.CompanionEntity;
import com.example.tingeso.Entities.PackTourEntity;
import com.example.tingeso.Repositories.BookingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingCleanupServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PackTourService packTourService;

    @InjectMocks
    private BookingCleanupService cleanupService;

    @Test
    void releaseExpiredBookings_WithOneExpired_NoCompanions() {
        BookingEntity booking = new BookingEntity();
        booking.setId(1L);
        booking.setCompanions(new ArrayList<>()); // Sin acompañantes
        PackTourEntity pack = new PackTourEntity();
        pack.setId(10L);
        booking.setPackTour(pack);

        when(bookingRepository.findByStatusAndReservationBefore(eq("PENDIENTE"), any()))
                .thenReturn(List.of(booking));

        cleanupService.releaseExpiredBookings();

        verify(packTourService).addSlot(10L, 1); // Solo libera 1 cupo
        verify(bookingRepository).save(booking);
    }

    @Test
    void releaseExpiredBookings_WithExpired_WithCompanions() {
        BookingEntity booking = new BookingEntity();
        booking.setId(2L);
        booking.setCompanions(List.of(new CompanionEntity(), new CompanionEntity())); // 2 acompañantes
        PackTourEntity pack = new PackTourEntity();
        pack.setId(20L);
        booking.setPackTour(pack);

        when(bookingRepository.findByStatusAndReservationBefore(eq("PENDIENTE"), any()))
                .thenReturn(List.of(booking));

        cleanupService.releaseExpiredBookings();

        verify(packTourService).addSlot(20L, 3); // Libera 3 cupos (1 titular + 2 acompañantes)
    }

    @Test
    void releaseExpiredBookings_NoExpiredBookings_DoesNothing() {
        when(bookingRepository.findByStatusAndReservationBefore(eq("PENDIENTE"), any()))
                .thenReturn(new ArrayList<>());

        cleanupService.releaseExpiredBookings();

        verify(packTourService, never()).addSlot(anyLong(), anyInt());
        verify(bookingRepository, never()).save(any());
    }

    @Test
    void releaseExpiredBookings_MultipleBookings_ProcessesAll() {
        BookingEntity b1 = new BookingEntity();
        b1.setCompanions(new ArrayList<>());
        b1.setPackTour(new PackTourEntity());
        b1.getPackTour().setId(10L);

        BookingEntity b2 = new BookingEntity();
        b2.setCompanions(new ArrayList<>());
        b2.setPackTour(new PackTourEntity());
        b2.getPackTour().setId(20L);

        when(bookingRepository.findByStatusAndReservationBefore(eq("PENDIENTE"), any()))
                .thenReturn(List.of(b1, b2));

        cleanupService.releaseExpiredBookings();

        verify(packTourService, times(2)).addSlot(anyLong(), anyInt());
        verify(bookingRepository, times(2)).save(any(BookingEntity.class));
    }

    @Test
    void releaseExpiredBookings_UpdatesStatusToExpired() {
        BookingEntity booking = new BookingEntity();
        booking.setCompanions(new ArrayList<>());
        booking.setPackTour(new PackTourEntity());
        booking.getPackTour().setId(10L);

        when(bookingRepository.findByStatusAndReservationBefore(eq("PENDIENTE"), any()))
                .thenReturn(List.of(booking));

        cleanupService.releaseExpiredBookings();

        assert(booking.getStatus().equals("EXPIRADA"));
    }
}