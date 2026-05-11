package com.example.tingeso.Controllers;

import com.example.tingeso.Entities.UserEntity;
import com.example.tingeso.Repositories.UserRepository;
import com.example.tingeso.Services.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/user")
@CrossOrigin("*")
public class UserController {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    //Each time a user logs in through Keycloak, their information is correctly
    //reflected in your local PostgreSQL database.
    @PostMapping("/sync")
    public ResponseEntity<UserEntity> syncUser(@RequestBody UserEntity user) {
        return userRepository.findByEmail(user.getEmail())
                .map(existingUser -> ResponseEntity.ok(userService.update(String.valueOf(existingUser.getId()), user)))
                .orElseGet(() -> ResponseEntity.ok(userService.saveUser(user)));
    }

    //Display the list of all users registered in the database.
    @GetMapping("/users")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public ResponseEntity<?> ListUsers(){
        try {
            return ResponseEntity.ok(userService.getUsers());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    //Create a new user record in your Postgres database.
    @PostMapping("/")
    public ResponseEntity<UserEntity> saveUser(@RequestBody UserEntity user){
        UserEntity userNew= userService.saveUser(user);
        return ResponseEntity.ok(userNew);
    }

    //Search for information about a specific person using their email address.
    @GetMapping("/{email}")
    public ResponseEntity<UserEntity> getUserByEmail(@PathVariable String email) {
        UserEntity user = userService.findByEmail(email).orElse(null);
        return ResponseEntity.ok(user);
    }

    //It allows editing data, but with strict "owner" rules.
    @PutMapping("/update/{keycloakId}")
    @Transactional
    public ResponseEntity<?> updateMyUser(@PathVariable String keycloakId, @RequestBody UserEntity updatedData, Authentication authentication) {
        UserEntity userInDb = userRepository.findByKeycloackID(keycloakId).orElse(null);
        if (userInDb == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado en la DB");

        }
        String subFromToken = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ADMIN")
                        || a.getAuthority().equalsIgnoreCase("ROLE_ADMIN"));
        boolean isOwner = subFromToken.trim().equals(keycloakId.trim());

        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("No tienes permisos para editar este perfil.");
        }
        return ResponseEntity.ok(userService.update(keycloakId, updatedData));
    }

    //Review of account statement
    @GetMapping("/perfil")
    public ResponseEntity<?> getMyProfile(Authentication authentication) {
        String email = authentication.getName();
        UserEntity user = userService.findByEmail(email).orElse(null);
        if (user != null && "INACTIVO".equals(user.getStatement())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Tu cuenta está desactivada. Contacta al administrador.");
        }
        return ResponseEntity.ok(user);
    }
}
