package com.company.office_admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OfficeAdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(OfficeAdminApplication.class, args);
	}

}
