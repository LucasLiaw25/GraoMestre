package com.liaw.dev.GraoMestre;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.util.TimeZone;

@EnableScheduling
@SpringBootApplication
public class GraoMestreApplication {

	public static void main(String[] args) {
		SpringApplication.run(GraoMestreApplication.class, args);
	}

	@PostConstruct
	public void init() {
		TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
	}

}
