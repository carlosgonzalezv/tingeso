package com.example.tingeso.Entities;
import lombok.Data;
import java.util.List;

@Data
public class BookingRequestDTO {
    private Long packId;
    private String userEmail;
    private int passengerCount;
    private String specialRequests;
    private List<String> companionNames;
}