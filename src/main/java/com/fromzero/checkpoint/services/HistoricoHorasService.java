package com.fromzero.checkpoint.services;

import com.fromzero.checkpoint.dto.*;
import com.fromzero.checkpoint.repositories.*;
import com.fromzero.checkpoint.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HistoricoHorasService {

    @Autowired
    private HorasExtrasRepository horasExtrasRepository;
    
    @Autowired
    private MarcacaoRepository marcacaoRepository;

    public HistoricoHorasResponse gerarHistoricoAnual(int ano, Long colaboradorId) {
        HistoricoHorasResponse response = new HistoricoHorasResponse();
        response.setAno(ano);
        
        // Gerar lista de meses com dias
        response.setMeses(gerarMesesDoAno(ano, colaboradorId));
        
        // Definir anos disponíveis (exemplo: últimos 3 anos)
        response.setAnosDisponiveis(Arrays.asList(ano-2, ano-1, ano));
        
        return response;
    }

    private List<MesHistorico> gerarMesesDoAno(int ano, Long colaboradorId) {
        List<MesHistorico> meses = new ArrayList<>();
        
        for (int mes = 1; mes <= 12; mes++) {
            MesHistorico mesHistorico = new MesHistorico();
            mesHistorico.setMes(Month.of(mes).getDisplayName(
                TextStyle.FULL, 
                new Locale("pt", "BR")
            ));
            
            // Obter dias do mês com registros
            mesHistorico.setDias(gerarDiasDoMes(ano, mes, colaboradorId));
            
            meses.add(mesHistorico);
        }
        
        return meses;
    }

    private List<DiaHistorico> gerarDiasDoMes(int ano, int mes, Long colaboradorId) {
        List<DiaHistorico> dias = new ArrayList<>();
        
        // Obter todos os dias do mês
        YearMonth yearMonth = YearMonth.of(ano, mes);
        int diasNoMes = yearMonth.lengthOfMonth();
        
        // Processar marcações normais (horas trabalhadas)
        processarMarcacoes(dias, ano, mes, diasNoMes, colaboradorId);
        
        // Processar horas extras
        processarHorasExtras(dias, ano, mes, colaboradorId);
        
        // Ordenar dias
        dias.sort(Comparator.comparingInt(DiaHistorico::getDia));
        
        return dias;
    }

    private void processarMarcacoes(List<DiaHistorico> dias, int ano, int mes, int diasNoMes, Long colaboradorId) {
        for (int dia = 1; dia <= diasNoMes; dia++) {
            LocalDate data = LocalDate.of(ano, mes, dia);
            
            // Verificar se é dia útil (não fim de semana)
            if (!data.getDayOfWeek().equals(DayOfWeek.SATURDAY) && 
                !data.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
                
                // Verificar se há marcação para este dia
                LocalDateTime inicioDia = LocalDateTime.of(ano, mes, dia, 0, 0);
                LocalDateTime fimDia = LocalDateTime.of(ano, mes, dia, 23, 59);
                
                List<Marcacao> marcacoes = marcacaoRepository
                    .findByColaboradorIdAndDataHoraBetween(colaboradorId, inicioDia, fimDia);
                
                // Se houver marcações, considerar como dia trabalhado
                if (!marcacoes.isEmpty()) {
                    DiaHistorico diaHistorico = new DiaHistorico();
                    diaHistorico.setDia(dia);
                    diaHistorico.setTipo("Horas Trabalhadas");
                    diaHistorico.setHoras(8); // Jornada padrão de 8h
                    
                    dias.add(diaHistorico);
                }
            }
        }
    }

    private void processarHorasExtras(List<DiaHistorico> dias, int ano, int mes, Long colaboradorId) {
        LocalDateTime inicioMes = LocalDateTime.of(ano, mes, 1, 0, 0);
        LocalDateTime fimMes = LocalDateTime.of(ano, mes, 
            YearMonth.of(ano, mes).lengthOfMonth(), 23, 59);
        
        List<HorasExtras> horasExtras = horasExtrasRepository
            .findByColaboradorIdAndCriadoEmBetween(colaboradorId, inicioMes, fimMes);
        
        for (HorasExtras he : horasExtras) {
            if (he.getStatus().equals(HorasExtras.Status.Aprovado)) {
                int dia = he.getCriadoEm().getDayOfMonth();
                
                // Converter saldo (ex: "2.5h") para horas
                double horas = Double.parseDouble(
                    he.getSaldo().replace("h", "").replace(",", ".")
                );
                
                // Arredondar para inteiro
                int horasInt = (int) Math.round(horas);
                
                // Adicionar ou atualizar dia
                Optional<DiaHistorico> existente = dias.stream()
                    .filter(d -> d.getDia() == dia && d.getTipo().equals("Horas Extras"))
                    .findFirst();
                
                if (existente.isPresent()) {
                    existente.get().setHoras(existente.get().getHoras() + horasInt);
                } else {
                    DiaHistorico novoDia = new DiaHistorico();
                    novoDia.setDia(dia);
                    novoDia.setTipo("Horas Extras");
                    novoDia.setHoras(horasInt);
                    dias.add(novoDia);
                }
            }
        }
    }
}