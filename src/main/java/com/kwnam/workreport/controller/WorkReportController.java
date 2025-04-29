package com.kwnam.workreport.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kwnam.workreport.dto.WorkReportRequest;
import com.kwnam.workreport.dto.WorkReportResponse;
import com.kwnam.workreport.entity.WorkReport;
import com.kwnam.workreport.service.WorkReportService;

@RestController
@RequestMapping("/api/work-reports")
@CrossOrigin(origins = "http://localhost:3000")
public class WorkReportController {

    private final WorkReportService workReportService;

    public WorkReportController(WorkReportService workReportService) {
        this.workReportService = workReportService;
    }
    
    // 전체 업무 보고 조회
    @GetMapping
    public ResponseEntity<List<WorkReportResponse>> getAllReports() {
        List<WorkReportResponse> reports = workReportService.getAllReports();
        return ResponseEntity.ok(reports);
    }


    // 날짜별 업무 보고 조회
    @GetMapping("/by-date")
    public ResponseEntity<List<WorkReportResponse>> getReportsByDate(@RequestParam("date") String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        List<WorkReportResponse> reports = workReportService.getReportsByDate(date);
        return ResponseEntity.ok(reports);
    }
    
    @GetMapping("/dates-with-reports")
    public ResponseEntity<List<LocalDate>> getDatesWithReports() {
        List<LocalDate> dates = workReportService.getDatesWithReports();
        return ResponseEntity.ok(dates);
    }

    @PostMapping("/create-report")
    public ResponseEntity<?> createWorkReport(@RequestBody WorkReportRequest request) {
        WorkReport savedReport = workReportService.createWorkReport(request);
        return ResponseEntity.ok(savedReport.getId()); // 저장된 ID 반환
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> updateWorkReport(@PathVariable Long id, @RequestBody WorkReportRequest request) {
        WorkReport updatedReport = workReportService.updateWorkReport(id, request);
        return ResponseEntity.ok(updatedReport.getId()); // 수정된 ID 반환
    }

}
