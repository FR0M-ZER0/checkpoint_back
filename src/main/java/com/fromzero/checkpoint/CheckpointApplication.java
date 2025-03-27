package com.fromzero.checkpoint;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@EnableMongoRepositories(basePackages = "com.fromzero.checkpoint.repository")
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
