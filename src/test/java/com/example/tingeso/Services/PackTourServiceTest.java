package com.example.tingeso.Services;

import com.example.tingeso.Entities.PackTourEntity;
import com.example.tingeso.Repositories.BookingRepository;
import com.example.tingeso.Repositories.PackTourRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PackTourServiceTest {

    @Mock private PackTourRepository packTourRepository;
    @Mock private BookingRepository bookingRepository;
    @InjectMocks private PackTourService packTourService;

    @Test
    void getTourPack_Success() {
        PackTourEntity activePack = new PackTourEntity();
        activePack.setStatus("DISPONIBLE");
        when(packTourRepository.findAll()).thenReturn(List.of(activePack));

        List<PackTourEntity> result = packTourService.getTourPack();
        assertFalse(result.isEmpty());
    }

    @Test
    void preparePack_SetsAgotado() {
        PackTourEntity pack = new PackTourEntity();
        pack.setStartDate(LocalDateTime.now().plusDays(1));
        pack.setFinishDate(LocalDateTime.now().plusDays(5));
        pack.setTotalSlots(10);
        pack.setAvailableSlots(0);

        when(packTourRepository.save(any())).thenReturn(pack);

        packTourService.saveTourPack(pack);
        assertEquals("AGOTADO", pack.getStatus());
    }

    @Test
    void preparePack_SetsNoVigente() {
        PackTourEntity pack = new PackTourEntity();
        pack.setStartDate(LocalDateTime.now().minusDays(10));
        pack.setFinishDate(LocalDateTime.now().minusDays(1));
        pack.setTotalSlots(10);

        when(packTourRepository.save(any())).thenReturn(pack);

        packTourService.saveTourPack(pack);
        assertEquals("NO VIGENTE", pack.getStatus());
    }

    @Test
    void preparePack_SetsDisponible() {
        PackTourEntity pack = new PackTourEntity();
        pack.setStartDate(LocalDateTime.now().plusDays(1));
        pack.setFinishDate(LocalDateTime.now().plusDays(5));
        pack.setTotalSlots(10);
        pack.setAvailableSlots(5);

        when(packTourRepository.save(any())).thenReturn(pack);

        packTourService.saveTourPack(pack);
        assertEquals("DISPONIBLE", pack.getStatus());
    }

    @Test
    void validateCriticalChanges_ThrowsIfPassengersExist() {
        PackTourEntity pack = new PackTourEntity();
        pack.setId(1L);
        pack.setTotalSlots(5);

        when(packTourRepository.findById(1L)).thenReturn(Optional.of(pack));
        when(bookingRepository.countTotalPassengersByPackTourId(1L)).thenReturn(2L);

        PackTourEntity newDetails = new PackTourEntity();
        newDetails.setId(1L);
        newDetails.setTotalSlots(1);

        assertThrows(IllegalArgumentException.class, () -> packTourService.saveTourPack(newDetails));
    }

    @Test
    void preparePack_InvalidDates_ThrowsException() {
        PackTourEntity pack = new PackTourEntity();
        pack.setStartDate(LocalDateTime.now().plusDays(5));
        pack.setFinishDate(LocalDateTime.now().plusDays(1));

        assertThrows(IllegalArgumentException.class, () -> packTourService.saveTourPack(pack));
    }

    @Test
    void addSlot_ChangesAgotadoToDisponible() {
        PackTourEntity pack = new PackTourEntity();
        pack.setAvailableSlots(0);
        pack.setStatus("AGOTADO");
        when(packTourRepository.findById(1L)).thenReturn(Optional.of(pack));
        when(packTourRepository.save(any())).thenReturn(pack);

        packTourService.addSlot(1L, 5);

        assertEquals("DISPONIBLE", pack.getStatus());
        assertEquals(5, pack.getAvailableSlots());
    }

    @Test
    void addSlot_PackNotFound_ThrowsException() {
        when(packTourRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> packTourService.addSlot(1L, 5));
    }

    @Test
    void reduceSlot_Success() {
        PackTourEntity pack = new PackTourEntity();
        pack.setAvailableSlots(10);
        pack.setStatus("DISPONIBLE");
        when(packTourRepository.findById(1L)).thenReturn(Optional.of(pack));
        when(packTourRepository.save(any())).thenReturn(pack);

        packTourService.reduceSlot(1L, 3);

        assertEquals(7, pack.getAvailableSlots());
        assertEquals("DISPONIBLE", pack.getStatus());
    }

    @Test
    void reduceSlot_SetsAgotadoWhenZero() {
        PackTourEntity pack = new PackTourEntity();
        pack.setAvailableSlots(3);
        pack.setStatus("DISPONIBLE");
        when(packTourRepository.findById(1L)).thenReturn(Optional.of(pack));
        when(packTourRepository.save(any())).thenReturn(pack);

        packTourService.reduceSlot(1L, 3);

        assertEquals(0, pack.getAvailableSlots());
        assertEquals("AGOTADO", pack.getStatus());
    }

    @Test
    void reduceSlot_InsufficientSlots_ThrowsException() {
        PackTourEntity pack = new PackTourEntity();
        pack.setAvailableSlots(2);
        when(packTourRepository.findById(1L)).thenReturn(Optional.of(pack));

        assertThrows(IllegalArgumentException.class, () -> packTourService.reduceSlot(1L, 5));
    }

    @Test
    void reduceSlot_PackNotFound_ThrowsException() {
        when(packTourRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> packTourService.reduceSlot(1L, 2));
    }
}