package com.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"com.library"})
public class ProjectGrp10Application {

	public static void main(String[] args) {
		SpringApplication.run(ProjectGrp10Application.class, args);
	}

}
