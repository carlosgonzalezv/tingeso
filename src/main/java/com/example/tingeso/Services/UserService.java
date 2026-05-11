package com.example.tingeso.Services;

import com.example.tingeso.Entities.UserEntity;
import com.example.tingeso.Repositories.BookingRepository;
import com.example.tingeso.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    private BookingRepository bookingRepository;

    //It brings up all users registered in the database.
    public ArrayList<UserEntity> getUsers()  {
        return(ArrayList<UserEntity>) userRepository.findAll();
    }

    //Search for a specific person using their unique ID number.
    public UserEntity findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }//ve si se borra o no

    //Find a user using their email address.
    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    //Save a new user or update an existing one.
    public UserEntity saveUser(UserEntity user) {
        return userRepository.save(user);
    }

    //To delete someone, but first check if they have a booking history;
    //if they do, the account is marked as "INACTIVE".
    public void deleteUser(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        boolean hasBookings = bookingRepository.existsByUsers_Id(id);
        if (hasBookings) {
            user.setStatement("INACTIVO");
            userRepository.save(user);
        } else {
            userRepository.delete(user);//ACUERDATE DE CONECTAR ESTO AL HACER EL SISTEMA
        }
    }

    //Modify personal data without touching sensitive data.
    public UserEntity update(String keycloakId, UserEntity updatedData) {
        Optional<UserEntity> userOptional = userRepository.findByKeycloackID(keycloakId);
        if (userOptional.isPresent()) {
            UserEntity user = userOptional.get();
            user.setName(updatedData.getName());
            user.setCellphone(updatedData.getCellphone());
            user.setNationality(updatedData.getNationality());
            user.setIdDocument(updatedData.getIdDocument());
            return userRepository.save(user);
        }
        return null;
    }
}
