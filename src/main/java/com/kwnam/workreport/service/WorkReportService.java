package com.kwnam.workreport.service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.kwnam.workreport.dto.WorkReportRequest;
import com.kwnam.workreport.dto.WorkReportResponse;
import com.kwnam.workreport.entity.WorkDay;
import com.kwnam.workreport.entity.WorkReport;
import com.kwnam.workreport.repository.WorkDayRepository;
import com.kwnam.workreport.repository.WorkReportRepository;

@Service
public class WorkReportService {

    private final WorkReportRepository workReportRepository;
    private final WorkDayRepository workDayRepository;

    public WorkReportService(WorkReportRepository workReportRepository, WorkDayRepository workDayRepository) {
        this.workReportRepository = workReportRepository;
        this.workDayRepository = workDayRepository;
    }

    public List<WorkReportResponse> getAllReports() {
        List<WorkReport> reports = workReportRepository.findAll();
        return reports.stream()
                .map(WorkReportResponse::fromEntity)
                .toList();
    }

    
    public List<WorkReportResponse> getReportsByDate(LocalDate date) {
        List<WorkReport> reports = workReportRepository.findByWorkDay_WorkDate(date);
        return reports.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    private WorkReportResponse toResponseDto(WorkReport report) {
        return WorkReportResponse.builder()
                .id(report.getId())
                .clientName(report.getClientName())
                .projectName(report.getProjectName())
                .pjCode(report.getPjCode())
                .workType(report.getWorkType())
                .workHours(report.getWorkHours())
                .isOut(report.isOut())
                .outLocation(report.getOutLocation())
                .isBackup(report.isBackup())
                .supportTeamMember(report.getSupportTeamMember())
                .workDescription(report.getWorkDescription())
                .supportProduct(report.getSupportProduct())
                .workDate(report.getWorkDay() != null ? report.getWorkDay().getWorkDate() : null)
                .build();
    }
    
    public List<LocalDate> getDatesWithReports() {
        List<WorkReport> allReports = workReportRepository.findAll();
        return allReports.stream()
                .map(report -> report.getWorkDay().getWorkDate())
                .distinct()
                .collect(Collectors.toList());
    }

    public WorkReport createWorkReport(WorkReportRequest request) {
        LocalDate date = request.getWorkDate();

        // 해당 날짜의 WorkDay 조회 또는 새로 생성
        WorkDay workDay = workDayRepository.findByWorkDate(date)
                .orElseGet(() -> {
                    WorkDay newDay = new WorkDay();
                    newDay.setWorkDate(date);
                    newDay.setHoliday(false);
                    return workDayRepository.save(newDay);
                });

        // WorkReport 생성
        WorkReport report = WorkReport.builder()
                .clientName(request.getClient())
                .projectName(request.getProjectName())
                .pjCode(request.getPjCode())
                .workType(request.getWorkType())
                .workHours(request.getWorkHours())
                .isOut(request.isOut())
                .outLocation(request.getLocation())
                .isBackup(request.isBackup())
                .supportTeamMember(request.getCoWorkers())
                .workDescription(request.getContent())
                .supportProduct(request.getProduct())
                .workDay(workDay)
                .build();

        return workReportRepository.save(report);
    }
    
    public WorkReport updateWorkReport(Long id, WorkReportRequest request) {
        WorkReport report = workReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("업무를 찾을 수 없습니다: id=" + id));

        report.setClientName(request.getClient());
        report.setProjectName(request.getProjectName());
        report.setPjCode(request.getPjCode());
        report.setWorkType(request.getWorkType());
        report.setWorkHours(request.getWorkHours());
        report.setOut(request.isOut());
        report.setOutLocation(request.getLocation());
        report.setBackup(request.isBackup());
        report.setSupportTeamMember(request.getCoWorkers());
        report.setWorkDescription(request.getContent());
        report.setSupportProduct(request.getProduct());

        return workReportRepository.save(report);
    }

}


