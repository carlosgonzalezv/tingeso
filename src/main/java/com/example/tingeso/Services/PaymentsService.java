package com.example.tingeso.Services;

import com.example.tingeso.Entities.BookingEntity;
import com.example.tingeso.Entities.PaymentsEntity;
import com.example.tingeso.Repositories.BookingRepository;
import com.example.tingeso.Repositories.PaymentsRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class PaymentsService {
    @Autowired
    PaymentsRepository paymentsRepository;
    @Autowired
    BookingRepository bookingRepository;

    public ArrayList<PaymentsEntity> getPayment(){
        return(ArrayList<PaymentsEntity>) paymentsRepository.findAll();
    }

    //It's the payment process, but using @transactional ensures that if something goes wrong with the
    //reservation update, the payment won't be interrupted.
    @Transactional
    public PaymentsEntity proccesPayment(PaymentsEntity payments){
        BookingEntity booking= bookingRepository.findById(payments.getBookingID().getId()).orElse(null);
        if (booking!=null){
            booking.setStatus("Pagado");
            bookingRepository.save(booking);
            return paymentsRepository.save(payments);
        }
        return null;
    }

    public PaymentsEntity getPaymentByBooking(Long bookingID) {
        return paymentsRepository.findByBookingID(bookingID).orElse(null);
    }
}
