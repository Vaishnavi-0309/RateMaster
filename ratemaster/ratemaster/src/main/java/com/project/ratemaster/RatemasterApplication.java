package com.project.ratemaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class RatemasterApplication {

	public static void main(String[] args) {
		SpringApplication.run(RatemasterApplication.class, args);
	}

}
