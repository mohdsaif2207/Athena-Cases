package com.athena.cases;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.athena.cases.config.DotEnvLoader;

@SpringBootApplication
public class CasesMtApplication {

	public static void main(String[] args) {
		DotEnvLoader.loadIfPresent();
		SpringApplication.run(CasesMtApplication.class, args);
	}

}
