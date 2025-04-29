package com.kwnam.workreport.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.kwnam.workreport.service.WorkReportFileService;

@RestController
@RequestMapping("/api/work-reports/file")
@CrossOrigin(origins = "http://localhost:3000")
public class WorkReportFileController {

    private final WorkReportFileService workReportFileService;

    public WorkReportFileController(WorkReportFileService workReportFileService) {
        this.workReportFileService = workReportFileService;
    }

    // 🔹 엑셀 Import
    @PostMapping("/import")
    public ResponseEntity<?> importExcel(@RequestParam("file") MultipartFile file,
                                         @RequestParam(name = "mode", defaultValue = "append") String mode) {
        boolean reset = "replace".equalsIgnoreCase(mode);
        Map<String, Object> result = workReportFileService.importExcel(file, reset);

        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    // 🔹 전체 Export
    @GetMapping("/export-all")
    public ResponseEntity<byte[]> exportAllExcel() {
        byte[] excelData = workReportFileService.exportExcelAll();
        String rawFilename = "전체_업무보고.xlsx";
        String encodedFilename = URLEncoder.encode(rawFilename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFilename)
            .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            .header("Access-Control-Expose-Headers", "Content-Disposition")
            .body(excelData);
    }


    
    // 🔹 날짜 범위 기반 Export
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcelWithRange(
            @RequestParam("start") String startDateStr,
            @RequestParam("end") String endDateStr) {

        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);

        byte[] excelData = workReportFileService.exportExcelWithRange(startDate, endDate);
        String rawFilename = String.format("%s_%s_업무보고.xlsx", startDate, endDate);
        String encodedFilename = URLEncoder.encode(rawFilename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");

        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFilename)
            .header("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            .header("Access-Control-Expose-Headers", "Content-Disposition")
            .body(excelData);
    }

}
