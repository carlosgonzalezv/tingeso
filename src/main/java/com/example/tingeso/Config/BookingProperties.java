package com.example.tingeso.Config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "booking.discount")
public class BookingProperties {

    private Group group = new Group();
    private Frequent frequent = new Frequent();
    private Multiple multiple = new Multiple();
    private Promo promo = new Promo();
    private int maxTotalPercentage = 20;

    @Data
    public static class Group {
        private int threshold = 4;
        private int percentage = 10;
    }

    @Data
    public static class Frequent {
        private int threshold = 3;
        private int percentage = 5;
    }

    @Data
    public static class Multiple {
        private int daysLimit = 30;
        private int percentage = 7;
    }

    @Data
    public static class Promo {
        private String startDate;
        private String endDate;
        private int percentage = 0;
    }
}