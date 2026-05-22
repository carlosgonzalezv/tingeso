package com.example.tingeso.Repositories;

import com.example.tingeso.Entities.PaymentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentsRepository extends JpaRepository<PaymentsEntity, Long> {
    //Check if the payment is associated with any reservation
    Optional<PaymentsEntity> findByBookingID_Id(Long bookingID);

    //Check the payment method that was used
    List<PaymentsEntity> findByPaymentMethod(String paymentMethod);
}
