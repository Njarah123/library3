package com.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.library"})
public class ProjectGrp10Application {

	public static void main(String[] args) {
		SpringApplication.run(ProjectGrp10Application.class, args);
	}

}
