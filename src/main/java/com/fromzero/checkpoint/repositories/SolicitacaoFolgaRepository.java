package com.fromzero.checkpoint.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fromzero.checkpoint.entities.SolicitacaoFolga;

@Repository
public interface SolicitacaoFolgaRepository extends JpaRepository<SolicitacaoFolga, Long> {
	List<SolicitacaoFolga> findBySolFolStatus(SolicitacaoFolga.Status status);
}