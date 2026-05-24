package com.example.tingeso.Config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor; // Añade este import
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor // Añade esta anotación
public class BookingCalculationResult {
    private int originalPrice;
    private int finalPrice;
    private List<String> appliedDiscounts;
    private int totalSavings;
}