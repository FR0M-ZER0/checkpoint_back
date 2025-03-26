package com.fromzero.checkpoint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.fromzero.checkpoint.repository")
public class CheckpointApplication {

	public static void main(String[] args) {
		SpringApplication.run(CheckpointApplication.class, args);
	}

}
