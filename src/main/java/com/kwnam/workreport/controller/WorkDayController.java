package com.kwnam.workreport.controller;


import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kwnam.workreport.entity.WorkDay;
import com.kwnam.workreport.service.WorkDayService;

@RestController
@RequestMapping("/api/work-days")
public class WorkDayController {

    private final WorkDayService workDayService;

    public WorkDayController(WorkDayService workDayService) {
        this.workDayService = workDayService;
    }

    @GetMapping
    public List<WorkDay> getAllWorkDays() {
        return workDayService.getAllWorkDays();
    }

    @PostMapping
    public WorkDay createWorkDay(@RequestBody WorkDay workDay) {
        return workDayService.createWorkDay(workDay);
    }
}
