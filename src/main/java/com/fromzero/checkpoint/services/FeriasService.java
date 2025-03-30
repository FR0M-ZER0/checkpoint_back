package com.fromzero.checkpoint.services;

import com.fromzero.checkpoint.entities.Ferias;
import com.fromzero.checkpoint.repositories.FeriasRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeriasService {

    private final FeriasRepository feriasRepository;

    public Ferias solicitarFerias(Ferias ferias) {
        return feriasRepository.save(ferias);
    }

    public Ferias buscarPorId(Long id) {
        return feriasRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Férias não encontradas"));
    }

    public List<Ferias> listarPorColaborador(Long colaboradorId) {
        return feriasRepository.findByColaboradorId(colaboradorId);
    }

    public Ferias atualizar(Long id, Ferias ferias) {
        Ferias existente = buscarPorId(id);
        // Atualizar campos necessários
        existente.setDataInicio(ferias.getDataInicio());
        existente.setDataFim(ferias.getDataFim());
        existente.setStatus(ferias.getStatus());
        return feriasRepository.save(existente);
    }

    public void deletar(Long id) {
        feriasRepository.deleteById(id);
    }
}