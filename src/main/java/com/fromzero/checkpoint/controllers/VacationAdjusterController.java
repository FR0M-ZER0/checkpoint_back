package com.fromzero.checkpoint.controllers;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fromzero.checkpoint.entities.Colaborador;
import com.fromzero.checkpoint.entities.Notificacao.NotificacaoTipo;
import com.fromzero.checkpoint.repositories.ColaboradorRepository;
import com.fromzero.checkpoint.services.NotificacaoService;

@RestController
public class VacationAdjusterController {
	@Autowired
    private NotificacaoService notificacaoService;
	@Autowired
    private ColaboradorRepository colaboradorRepository;
	
	@PostMapping("/adjust-vacation")
	public VacationAdjuster adjustVacation(@RequestBody VacationRequest request) {
		LocalDate startDate = request.startDate;
		int durationDays = request.duration;

		LocalDate endDate = startDate.plusDays(durationDays);
		LocalDate dateCheck = endDate;

		int addedDays = 0;
		boolean N = true;
		while (N) {
		    DayOfWeek dayCheck = dateCheck.getDayOfWeek();
		    if (dayCheck == DayOfWeek.SATURDAY || dayCheck == DayOfWeek.SUNDAY) {
		        dateCheck = dateCheck.plusDays(1);
		        addedDays++;
		    } else {
		        N = false;
		    }
		}
		
		if (addedDays > 0) {
			Colaborador colaborador = colaboradorRepository.findById(request.getColaboradorId())
		            .orElseThrow(() -> new RuntimeException("Colaborador not found"));

		        notificacaoService.criaNotificacao(
		            "Férias ajustadas", 
		            NotificacaoTipo.ferias, 
		            colaborador
		        );
		}

		return new VacationAdjuster(addedDays);

	}
	
	public static class VacationRequest {
	    private LocalDate startDate;
	    private int duration;
	    private Long colaboradorId;
	    public LocalDate getStartDate() { return startDate; }
	    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
	    public int getDuration() { return duration; }
	    public void setDuration(int duration) { this.duration = duration; }
	    public Long getColaboradorId() { return colaboradorId; }
	    public void setColaboradorId(Long colaboradorId) { this.colaboradorId = colaboradorId; }
	}
	
	public static class VacationAdjuster {
	    private int addedDays;

	    public VacationAdjuster(int addedDays) {
	        this.addedDays = addedDays;
	    }
	    public int getAddedDays() {
	        return addedDays;
	    }
	}
}
