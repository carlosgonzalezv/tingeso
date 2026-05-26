package com.example.tingeso.Config;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PackTourRankingDTO {
    private String packageName;
    private long totalBookings;
    private long totalPassengers;
    private long generatedAmount;
}