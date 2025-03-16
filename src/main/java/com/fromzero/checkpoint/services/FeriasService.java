package com.fromzero.checkpoint;

import com.fromzero.checkpoint.models.Ferias;
import com.fromzero.checkpoint.repositories.FeriasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.Year;
import java.util.List;

@Service
public class FeriasService {

    @Autowired
    private FeriasRepository feriasRepository;

    public List<Ferias> listarFerias(Long colaboradorId) {
        return feriasRepository.findByColaboradorId(colaboradorId);
    }

    public boolean podeSolicitarFerias(Long colaboradorId) {
        int anoAtual = Year.now().getValue();
        long totalFeriasAno = listarFerias(colaboradorId).stream()
            .filter(f -> f.getDataInicio().getYear() == anoAtual)
            .count();
        return totalFeriasAno < 3;
    }

    public Ferias solicitarFerias(Ferias ferias) {
        if (!podeSolicitarFerias(ferias.getColaborador().getId())) {
            throw new RuntimeException("Máximo de 3 períodos de férias por ano atingido!");
        }
        return feriasRepository.save(ferias);
    }
}