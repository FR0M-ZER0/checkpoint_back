package com.fromzero.checkpoint.services;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fromzero.checkpoint.entities.Colaborador;
import com.fromzero.checkpoint.entities.Falta;
import com.fromzero.checkpoint.entities.Jornada;
import com.fromzero.checkpoint.repositories.FaltaRepository;
import com.fromzero.checkpoint.repositories.JornadaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JornadaService {

    private final FaltaRepository faltaRepository;
    private final JornadaRepository jornadaRepository;

    @Scheduled(fixedRate = 60000) // Executa a cada 1 minuto
    @Transactional
    public void verificarAtraso() {
        LocalDateTime agora = LocalDateTime.now();
        System.out.println("🚨 Verificando atrasos em: " + agora);
        
        LocalDateTime inicioDia = agora.with(LocalTime.MIN);
        LocalDateTime fimDia = agora.with(LocalTime.MAX);
        
        List<Jornada> jornadasDoDia = jornadaRepository.findByDataBetween(inicioDia, fimDia);
        
        for (Jornada jornada : jornadasDoDia) {
            if (jornada.getInicioPrevisto() != null && jornada.getInicioPrevisto().isBefore(agora)) {
                long minutosAtraso = Duration.between(jornada.getInicioPrevisto(), agora).toMinutes();
                
                if (minutosAtraso > 10) {
                    Optional<Falta> faltaExistente = faltaRepository.findByJornadaAndTipo(
                        jornada, Falta.TipoFalta.ATRASO);
                    
                    if (faltaExistente.isEmpty()) {
                        criarFaltaPorAtraso(jornada, minutosAtraso, agora);
                    }
                }
            }
        }
    }

    private void criarFaltaPorAtraso(Jornada jornada, long minutosAtraso, LocalDateTime agora) {
        Colaborador colaborador = jornada.getColaborador();
        Falta falta = new Falta();
        falta.setColaborador(colaborador);
        falta.setJornada(jornada);
        falta.setTipo(Falta.TipoFalta.ATRASO);
        falta.setData(agora.toLocalDate());
        falta.setJustificativa("Atraso superior a 10 minutos");
        
        faltaRepository.save(falta);
        
        System.out.println("⚠️ Falta registrada para: " + 
            (colaborador != null ? colaborador.getNome() : "Colaborador desconhecido") + 
            " - Atraso: " + minutosAtraso + " minutos");
    }
}