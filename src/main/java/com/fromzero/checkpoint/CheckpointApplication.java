package com.fromzero.checkpoint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.fromzero.checkpoint.repositories")
@EnableMongoAuditing
@EnableScheduling
public class CheckpointApplication {
	static {
		Dotenv dotenv = Dotenv.load();
		dotenv.entries().forEach(entry ->
			System.setProperty(entry.getKey(), entry.getValue())
		);
	}
	
	public static void main(String[] args) {
		SpringApplication.run(CheckpointApplication.class, args);
	}

}
