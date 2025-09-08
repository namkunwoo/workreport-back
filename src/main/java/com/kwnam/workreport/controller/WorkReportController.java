package com.kwnam.workreport.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.kwnam.workreport.dto.WorkReportRequest;
import com.kwnam.workreport.dto.WorkReportResponse;
import com.kwnam.workreport.entity.WorkReport;
import com.kwnam.workreport.service.WorkReportService;

@RestController
// 하이픈/비하이픈 모두 지원해서 프론트 경로 혼재를 수용
@RequestMapping(path = {"/api/work-reports"})
@CrossOrigin(origins = "http://localhost:3000")
public class WorkReportController {

    private final WorkReportService workReportService;

    public WorkReportController(WorkReportService workReportService) {
        this.workReportService = workReportService;
    }

    /** 전체: GET /api/work-reports */
    @GetMapping
    public ResponseEntity<List<WorkReportResponse>> getAllReports() {
        return ResponseEntity.ok(workReportService.getAllReports());
    }

    /** 날짜 조회(1): GET /api/work-reports?date=YYYY-MM-DD */
    @GetMapping(params = "date")
    public ResponseEntity<List<WorkReportResponse>> getReportsByDateParam(
            @RequestParam("date") @DateTimeFormat(iso = ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(workReportService.getReportsByDate(date));
    }

    /** 날짜 조회(2): GET /api/work-reports/by-date?date=YYYY-MM-DD  ← 프론트가 현재 쓰는 패턴 */
    @GetMapping("/by-date")
    public ResponseEntity<List<WorkReportResponse>> getReportsByDatePath(
            @RequestParam("date") @DateTimeFormat(iso = ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(workReportService.getReportsByDate(date));
    }

    /** 기간 조회: GET /api/work-reports?start=YYYY-MM-DD&end=YYYY-MM-DD */
    @GetMapping(params = {"start", "end"})
    public ResponseEntity<List<WorkReportResponse>> getReportsByRange(
            @RequestParam("start") @DateTimeFormat(iso = ISO.DATE) LocalDate start,
            @RequestParam("end")   @DateTimeFormat(iso = ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(workReportService.getReportsByRange(start, end));
    }

    /** 보고 날짜 목록: GET /api/work-reports/dates-with-reports */
    @GetMapping("/dates-with-reports")
    public ResponseEntity<List<LocalDate>> getDatesWithReports() {
        return ResponseEntity.ok(workReportService.getDatesWithReports());
    }

    /** 생성: POST /api/work-reports  또는  POST /api/work-reports/create-report */
    @PostMapping(path = {"", "/create-report"})
    public ResponseEntity<Long> createWorkReport(@RequestBody WorkReportRequest request) {
        WorkReport savedReport = workReportService.createWorkReport(request);
        return ResponseEntity.ok(savedReport.getId());
    }

    /** 수정: PUT /api/work-reports/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<Long> updateWorkReport(@PathVariable Long id, @RequestBody WorkReportRequest request) {
        WorkReport updatedReport = workReportService.updateWorkReport(id, request);
        return ResponseEntity.ok(updatedReport.getId());
    }
}
