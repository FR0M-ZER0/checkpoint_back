package com.fromzero.checkpoint.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.fromzero.checkpoint.entities.MarcacaoLog;

public interface MarcacaoLogRepository extends MongoRepository<MarcacaoLog, String> {
}
