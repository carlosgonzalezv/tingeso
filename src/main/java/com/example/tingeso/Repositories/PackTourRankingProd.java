package com.example.tingeso.Repositories;

public interface PackTourRankingProd {
    String getPackageName();
    Long getTotalBookings();
    Long getGeneratedAmount();
    Long getTotalPassengers(); // <-- AGREGADO
}