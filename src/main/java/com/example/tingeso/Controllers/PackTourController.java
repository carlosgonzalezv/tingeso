package com.example.tingeso.Controllers;


import com.example.tingeso.Entities.PackTourEntity;
import com.example.tingeso.Entities.UserEntity;
import com.example.tingeso.Services.PackTourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tourPack")
@CrossOrigin("*")
public class PackTourController {

    @Autowired
    private PackTourService packTourService;

    //Get all packages
    @GetMapping("/")
    public ResponseEntity<List<PackTourEntity>> listTourPack() {
        List<PackTourEntity> tourPacks = packTourService.getTourPack();
        if (tourPacks.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(tourPacks);
    }

    // Save or update a package
    @PostMapping("/")
    public ResponseEntity<PackTourEntity> saveTourPack(@RequestBody PackTourEntity packTour) {
        try {
            PackTourEntity newTourPack = packTourService.saveTourPack(packTour);
            return ResponseEntity.ok(newTourPack);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().header("Error-Message", e.getMessage()).build();
        }
    }

    // Logical "Delete": changes the state to "INACTIVO"
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTourPack(@PathVariable Long id) {
        try {
            packTourService.deletePackTour(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
