package com.fromzero.checkpoint.repository;

import com.fromzero.checkpoint.model.MarcacaoLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MarcacaoLogRepository extends MongoRepository<MarcacaoLog, String> {
}
