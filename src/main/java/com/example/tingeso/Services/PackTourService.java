package com.example.tingeso.Services;

import com.example.tingeso.Entities.PackTourEntity;
import com.example.tingeso.Repositories.BookingRepository;
import com.example.tingeso.Repositories.PackTourRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
//import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PackTourService {
    @Autowired
    PackTourRepository packTourRepository;
    @Autowired
    private BookingRepository bookingRepository;

    //Returns a list of all packages, but with a filter: it excludes all those marked as "INACTIVO"
    public List<PackTourEntity> getTourPack() {
        return packTourRepository.findAll().stream()
                .filter(p -> !"INACTIVO".equals(p.getStatus()))
                .collect(Collectors.toList());
    }

    //Check if the pack meets multiple conditions
    private void preparePack (PackTourEntity packTour){
        if (packTour.getStartDate() == null || packTour.getFinishDate()==null){
            throw new IllegalArgumentException("la fecha de inicio y termino son obligatorias");
        }
        if (!packTour.getFinishDate().isAfter(packTour.getStartDate())){
            throw new IllegalArgumentException("la fecha de termino debe ser despues de la de inicio");
        }
        if (packTour.getFinishDate().isBefore(LocalDateTime.now())) {
            packTour.setStatus("NO VIGENTE");
            return;
        }
        if ("CANCELADO".equals(packTour.getStatus())) {
            packTour.setAvailableSlots(0);
            return;
        }
        if (packTour.getTotalSlots() ==null ||packTour.getTotalSlots() <=0){
            throw new IllegalArgumentException("debe al menos haber un cupo en el paquete");
        }
        if (packTour.getAvailableSlots()==null){
            packTour.setAvailableSlots(packTour.getTotalSlots());
        }
        if (packTour.getAvailableSlots()==0){
            packTour.setAvailableSlots(0);
            packTour.setStatus("AGOTADO");
        }else {
            packTour.setStatus("DISPONIBLE");
        }
    }

    //Save the package to the database, but not before validating twice:
    //First, that the changes are legal (validateCriticalChanges) and then that the state is correct (preparePack).
    public PackTourEntity saveTourPack (PackTourEntity packTour) {
        validateCriticalChanges(packTour);
        preparePack(packTour);
        return packTourRepository.save(packTour);
    }

    //The number of available slots decreases when someone makes a purchase.
    public void reduceSlot(Long Id, int quantity){
        PackTourEntity packTour=packTourRepository.findById(Id).orElseThrow();
        int newSlot=packTour.getAvailableSlots()-quantity;
        if (newSlot<0){
            throw new IllegalArgumentException("no hay suficientes cupos disponibles");
        }
        packTour.setAvailableSlots(newSlot);
        if (newSlot==0){
            packTour.setStatus("AGOTADO");
        }
        packTourRepository.save(packTour);
    }

    //Returns slots to inventory. Primarily used when a reservation is canceled or expires.
    public void addSlot(Long id, int quantity) {
        PackTourEntity pack = packTourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paquete no encontrado"));
        pack.setAvailableSlots(pack.getAvailableSlots() + quantity);
        if (pack.getAvailableSlots() > 0 && "AGOTADO".equals(pack.getStatus())) {
            pack.setStatus("DISPONIBLE");
        }
        packTourRepository.save(pack);
    }

    //Perform a "logical delete". Instead of deleting the record from the database,
    //simply change its status to "INACTIVE" and reset the quotas to zero.
    public void deletePackTour(Long id) {
        PackTourEntity pack = packTourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paquete no encontrado"));
        pack.setStatus("INACTIVO");
        pack.setAvailableSlots(0);
        packTourRepository.save(pack);
    }

    //This means that when there are reservations in a package, the details cannot be changed.
    private void validateCriticalChanges(PackTourEntity newDetails) {
        if (newDetails.getId() == null) return;
        PackTourEntity currentPack = packTourRepository.findById(newDetails.getId())
                .orElseThrow(() -> new RuntimeException("Paquete no encontrado"));
        long totalPassengersCount = bookingRepository.countTotalPassengersByPackTourId(newDetails.getId());
        if (totalPassengersCount > 0) {
            if (newDetails.getTotalSlots() < totalPassengersCount) {
                throw new IllegalArgumentException("No puedes reducir los cupos totales a " + newDetails.getTotalSlots() +
                        " porque ya existen " + totalPassengersCount + " cupos reales ocupados.");
            }
            if (!newDetails.getStartDate().equals(currentPack.getStartDate()) ||
                    !newDetails.getFinishDate().equals(currentPack.getFinishDate())) {
                throw new IllegalArgumentException("No se pueden modificar las fechas de un paquete con reservas activas.");
            }
        }
    }
}
