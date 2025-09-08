package com.kwnam.workreport.service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /** 전체 조회 (기존 유지) */
    @Transactional(readOnly = true)
    public List<WorkReportResponse> getAllReports() {
        List<WorkReport> reports = workReportRepository.findAll();
        return reports.stream()
                .map(WorkReportResponse::fromEntity)
                .toList();
    }

    /** 단일 날짜 조회 (기존 유지) */
    @Transactional(readOnly = true)
    public List<WorkReportResponse> getReportsByDate(LocalDate date) {
        List<WorkReport> reports = workReportRepository.findByWorkDay_WorkDate(date);
        return reports.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    /** 기간 조회 (신규 추가) */
    @Transactional(readOnly = true)
    public List<WorkReportResponse> getReportsByRange(LocalDate start, LocalDate end) {
        List<WorkReport> reports = workReportRepository.findRange(start, end);
        return reports.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    /** 날짜가 있는 WorkDay를 보장: 없으면 생성 (신규 추가) */
    @Transactional
    public WorkDay getOrCreateWorkDay(LocalDate date) {
        return workDayRepository.findByWorkDate(date)
                .orElseGet(() -> {
                    WorkDay newDay = new WorkDay();
                    newDay.setWorkDate(date);
                    newDay.setHoliday(false);
                    return workDayRepository.save(newDay);
                });
    }

    /** 생성: WorkDay 보장 후 연결 (기존 로직을 안전화) */
    @Transactional
    public WorkReport createWorkReport(WorkReportRequest request) {
        LocalDate date = request.getWorkDate();
        WorkDay workDay = getOrCreateWorkDay(date);

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

    /** 업데이트: 날짜 바뀌면 WorkDay 재연결 (신규 보강) */
    @Transactional
    public WorkReport updateWorkReport(Long id, WorkReportRequest request) {
        WorkReport report = workReportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("업무를 찾을 수 없습니다: id=" + id));

        // 날짜 변경이 들어오면 WorkDay 재보장
        if (request.getWorkDate() != null) {
            WorkDay workDay = getOrCreateWorkDay(request.getWorkDate());
            report.setWorkDay(workDay);
        }

        // 나머지 필드 업데이트
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

    /** 내부 매핑 유틸 (기존 스타일 유지) */
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

    /** 보고가 있는 날짜 목록 (기존 유지) */
    @Transactional(readOnly = true)
    public List<LocalDate> getDatesWithReports() {
        List<WorkReport> allReports = workReportRepository.findAll();
        return allReports.stream()
                .map(report -> report.getWorkDay().getWorkDate())
                .distinct()
                .collect(Collectors.toList());
    }
}


