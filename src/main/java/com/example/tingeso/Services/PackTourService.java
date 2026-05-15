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

    public List<PackTourEntity> getTourPack() {
        return packTourRepository.findAll().stream()
                .filter(p -> !"INACTIVO".equals(p.getStatus()))
                .collect(Collectors.toList());
    }

    //revisa si el pack cumple con multiples condiciones
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

    //de momento esta revisando que al hacer el paquete las fechas no se pongan mal
    public PackTourEntity saveTourPack (PackTourEntity packTour) {
        validateCriticalChanges(packTour);
        preparePack(packTour);
        return packTourRepository.save(packTour);
    }

    //funcion que reduce el slot de los paquetes con cada compra
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

    public void addSlot(Long id, int quantity) {
        PackTourEntity pack = packTourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paquete no encontrado"));

        pack.setAvailableSlots(pack.getAvailableSlots() + quantity);

        // Si el paquete vuelve a tener cupos, actualizamos su estado
        if (pack.getAvailableSlots() > 0 && "AGOTADO".equals(pack.getStatus())) {
            pack.setStatus("DISPONIBLE");
        }

        packTourRepository.save(pack);
    }

    //borra el paquete aunque en verdad solo lo deja inactivo para que no vuelva a aparecer
    public void deletePackTour(Long id) {
        PackTourEntity pack = packTourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Paquete no encontrado"));

        pack.setStatus("INACTIVO");//o dejalo como INACTIVO
        pack.setAvailableSlots(0);

        packTourRepository.save(pack);
    }

    //hace que cuando haya reservas en un paquetes, no se puedan cambiar los datos
    private void validateCriticalChanges(PackTourEntity newDetails) {
        if (newDetails.getId() == null) return;
        PackTourEntity currentPack = packTourRepository.findById(newDetails.getId())
                .orElseThrow(() -> new RuntimeException("Paquete no encontrado"));

        long activeBookingsCount = bookingRepository.countByPackTourId(newDetails.getId());
        if (activeBookingsCount > 0) {
            if (newDetails.getTotalSlots() < activeBookingsCount) {
                throw new IllegalArgumentException("No puedes reducir los cupos totales a " + newDetails.getTotalSlots() +
                        " porque ya existen " + activeBookingsCount + " reservas.");
            }
            if (!newDetails.getStartDate().equals(currentPack.getStartDate()) ||
                    !newDetails.getFinishDate().equals(currentPack.getFinishDate())) {
                throw new IllegalArgumentException("No se pueden modificar las fechas de un paquete con reservas activas.");
            }
        }
    }

}
