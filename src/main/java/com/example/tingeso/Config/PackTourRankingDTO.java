package com.example.tingeso.Config;

import lombok.Data;

@Data
public class PackTourRankingDTO {
    private String packageName;
    private long totalReservas;
    private long totalPasajeros;
    private long montoGenerado;
}
