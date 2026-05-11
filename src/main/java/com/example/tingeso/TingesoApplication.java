package com.example.tingeso;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication

public class TingesoApplication {
	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("America/Santiago"));
	}
	public static void main(String[] args) {
		SpringApplication.run(TingesoApplication.class, args);
	}
}