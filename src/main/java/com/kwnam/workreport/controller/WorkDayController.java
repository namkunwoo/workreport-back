package com.kwnam.workreport.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kwnam.workreport.entity.WorkDay;
import com.kwnam.workreport.service.WorkDayService;

@RestController
@RequestMapping("/api/work-days")
@CrossOrigin(origins = "http://localhost:3000")
public class WorkDayController {

    private final WorkDayService workDayService;

    public WorkDayController(WorkDayService workDayService) {
        this.workDayService = workDayService;
    }

    // 모든 WorkDay 조회
    @GetMapping
    public ResponseEntity<List<WorkDay>> getAllWorkDays() {
        List<WorkDay> workDays = workDayService.getAllWorkDays();
        return ResponseEntity.ok(workDays);
    }

    // 새로운 WorkDay 생성
    @PostMapping
    public ResponseEntity<WorkDay> createWorkDay(@RequestBody WorkDay workDay) {
        WorkDay savedWorkDay = workDayService.createWorkDay(workDay);
        return ResponseEntity.ok(savedWorkDay);
    }
}
