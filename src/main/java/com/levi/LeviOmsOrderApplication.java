package com.levi;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LeviOmsOrderApplication {

	@Value("${PORT:8080}")
	private String port;

	public static void main(String[] args) {
		SpringApplication.run(LeviOmsOrderApplication.class, args);
	}


	@PostConstruct
	public void init() {
		System.out.println("Application running on port: " + port);
	}

}
