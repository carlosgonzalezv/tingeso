package com.example.tingeso.Config;

import lombok.Data;

@Data
public class PackTourRankingDTO {
    private String packageName;
    private long totalBookings;
    private long totalPassengers;
    private long generatedAmount;
}
