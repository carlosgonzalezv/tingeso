package com.example.tingeso.Config;

import lombok.Data;
import java.util.List;

@Data
public class BookingResponseDTO {
    private Long id;
    private int originalPrice;
    private int finalPrice;
    private int totalSavings;
    private List<String> appliedDiscounts;
    private String status;
}
