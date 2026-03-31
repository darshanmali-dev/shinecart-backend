package com.college.shinecart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ShinecartApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShinecartApplication.class, args);
	}

}
