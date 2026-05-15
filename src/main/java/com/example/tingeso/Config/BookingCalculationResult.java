package com.example.tingeso.Config;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class BookingCalculationResult {
    private int originalPrice;
    private int finalPrice;
    private List<String> appliedDiscounts;
    private int totalSavings;
}