package com.example.tingeso.Services;

import com.example.tingeso.Entities.UserEntity;
import com.example.tingeso.Repositories.BookingRepository;
import com.example.tingeso.Repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void deleteUser_WithBookings_SetsInactive() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.existsByUsers_Id(1L)).thenReturn(true);

        userService.deleteUser(1L);

        assertEquals("INACTIVO", user.getStatement());
        verify(userRepository, times(1)).save(user);
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUser_WithoutBookings_DeletesUser() {
        UserEntity user = new UserEntity();
        user.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(bookingRepository.existsByUsers_Id(2L)).thenReturn(false);

        userService.deleteUser(2L);

        verify(userRepository, times(1)).delete(user);
        verify(userRepository, never()).save(any());
    }

    @Test
    void update_UserExists_UpdatesData() {
        UserEntity existingUser = new UserEntity();
        existingUser.setName("Shirou Emiya");

        UserEntity updatedData = new UserEntity();
        updatedData.setName("Archer");
        updatedData.setNationality("Chilena");
        updatedData.setIdDocument("11111111-1");

        when(userRepository.findByKeycloackID("key-123")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(UserEntity.class))).thenReturn(existingUser);

        UserEntity result = userService.update("key-123", updatedData);

        assertNotNull(result);
        assertEquals("Archer", result.getName());
        assertEquals("Chilena", result.getNationality());
    }

    @Test
    void update_UserDoesNotExist_ReturnsNull() {
        when(userRepository.findByKeycloackID("key-404")).thenReturn(Optional.empty());
        UserEntity result = userService.update("key-404", new UserEntity());
        assertNull(result);
    }

    @Test
    void findByEmail_ReturnsUserOptional() {
        UserEntity user = new UserEntity();
        user.setEmail("estudiante@usach.cl");
        when(userRepository.findByEmail("estudiante@usach.cl")).thenReturn(Optional.of(user));

        Optional<UserEntity> result = userService.findByEmail("estudiante@usach.cl");

        assertTrue(result.isPresent());
        assertEquals("estudiante@usach.cl", result.get().getEmail());
    }
}