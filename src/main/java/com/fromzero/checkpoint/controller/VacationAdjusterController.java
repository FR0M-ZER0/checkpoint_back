package com.fromzero.checkpoint.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VacationAdjusterController {
	
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
		
		

		return new VacationAdjuster(addedDays);

	}
	
	public static class VacationRequest {
	    private LocalDate startDate;
	    private int duration;
	    public LocalDate getStartDate() { return startDate; }
	    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
	    public int getDuration() { return duration; }
	    public void setDuration(int duration) { this.duration = duration; }
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
