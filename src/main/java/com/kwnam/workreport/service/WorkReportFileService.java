package com.kwnam.workreport.service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.kwnam.workreport.entity.WorkDay;
import com.kwnam.workreport.entity.WorkReport;
import com.kwnam.workreport.repository.WorkDayRepository;
import com.kwnam.workreport.repository.WorkReportRepository;
import com.kwnam.workreport.util.Util;

@Service
public class WorkReportFileService {

    private final WorkReportRepository workReportRepository;
    private final WorkDayRepository workDayRepository;

    public WorkReportFileService(WorkReportRepository workReportRepository, WorkDayRepository workDayRepository) {
        this.workReportRepository = workReportRepository;
        this.workDayRepository = workDayRepository;
    }

    public Map<String, Object> importExcel(MultipartFile file, boolean reset) {
        Map<String, Object> result = new HashMap<>();
        List<WorkReport> reports = new ArrayList<>();
        int skippedFuture = 0;
        int skippedEmptyWork = 0;
        int inserted = 0;

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            boolean isFirstRow = true;

            if (reset) {
                workReportRepository.deleteAll();
                workDayRepository.deleteAll();
            }

            for (Row row : sheet) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }

                if (row.getCell(0) == null) continue;

                // 날짜 추출
                Cell dateCell = row.getCell(0);
                LocalDate workDate;
                if (dateCell.getCellType() == CellType.NUMERIC) {
                    workDate = dateCell.getLocalDateTimeCellValue().toLocalDate();
                } else {
                    workDate = Util.parseDate(Util.getCellValueAsString(dateCell));
                    if (workDate == null) continue;
                }

                if (workDate.isAfter(LocalDate.now())) {
                    skippedFuture++;
                    continue;
                }

                int workHours = Util.parseIntSafe(Util.getCellValueAsString(row.getCell(1)));

                WorkDay workDay = workDayRepository.findByWorkDate(workDate)
                        .orElseGet(() -> {
                            WorkDay newDay = new WorkDay();
                            newDay.setWorkDate(workDate);
                            return newDay;
                        });

                if (workHours == 0) {
                    workDay.setHoliday(true);
                    workDayRepository.save(workDay);
                    skippedEmptyWork++;
                    continue;
                } else {
                    workDay.setHoliday(false);
                    workDayRepository.save(workDay);
                }

                String outLocation = Util.getCellValueAsString(row.getCell(8));
                boolean isOut = !outLocation.isBlank();

                WorkReport report = WorkReport.builder()
                        .clientName(Util.getCellValueAsString(row.getCell(2)))
                        .projectName(Util.getCellValueAsString(row.getCell(3)))
                        .pjCode(Util.getCellValueAsString(row.getCell(4)))
                        .workType(Util.getCellValueAsString(row.getCell(5)))
                        .workHours(workHours)
                        .isBackup(Util.parseBoolean(Util.getCellValueAsString(row.getCell(6))))
                        .supportTeamMember(Util.getCellValueAsString(row.getCell(7)))
                        .isOut(isOut)
                        .outLocation(outLocation)
                        .workDescription(Util.getCellValueAsString(row.getCell(9)))
                        .supportProduct(Util.getCellValueAsString(row.getCell(10)))
                        .workDay(workDay)
                        .build();

                reports.add(report);
                inserted++;
            }

            workReportRepository.saveAll(reports);

            result.put("success", true);
            result.put("message", "✅ 엑셀 업로드 성공");
            result.put("inserted", inserted);
            result.put("skippedFuture", skippedFuture);
            result.put("skippedEmptyWork", skippedEmptyWork);
            result.put("totalRows", sheet.getLastRowNum());
            return result;

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "❌ 엑셀 업로드 실패: " + e.getMessage());
            return result;
        }
    }

	 // 전체 Export 메서드 추가
    public byte[] exportExcelAll() {
        LocalDate start = LocalDate.of(LocalDate.now().getYear() - 1, 12, 1); // 전년도 12월 1일
        LocalDate end = LocalDate.of(LocalDate.now().getYear(), 12, 31);      // 올해 12월 31일
        return exportExcelWithRange(start, end); // 스타일 모두 반영됨!
    }

    // 🔹 엑셀 Export - 날짜 범위 기반
    public byte[] exportExcelWithRange(LocalDate startDate, LocalDate endDate) {
        System.out.println("📤 [엑셀 Export] 시작일: " + startDate + ", 종료일: " + endDate);

        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("업무보고");

            // 기본 스타일
            XSSFCellStyle defaultStyle = workbook.createCellStyle();
            defaultStyle.setWrapText(true);
            defaultStyle.setAlignment(HorizontalAlignment.LEFT);
            defaultStyle.setVerticalAlignment(VerticalAlignment.TOP);
            defaultStyle.setBorderTop(BorderStyle.THIN);
            defaultStyle.setBorderBottom(BorderStyle.THIN);
            defaultStyle.setBorderLeft(BorderStyle.THIN);
            defaultStyle.setBorderRight(BorderStyle.THIN);

            // 주말 스타일 (오렌지색)
            XSSFCellStyle weekendStyle = workbook.createCellStyle();
            weekendStyle.cloneStyleFrom(defaultStyle);
            weekendStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(255, 192, 0), null));
            weekendStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // 헤더 스타일 (Bold만)
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);

            XSSFCellStyle headerStyle = workbook.createCellStyle();
            headerStyle.cloneStyleFrom(defaultStyle);
            headerStyle.setFont(headerFont);

            // 업무내용 주말/연차용 (Bold + 밑줄)
            XSSFFont boldUnderlineFont = workbook.createFont();
            boldUnderlineFont.setBold(true);
            boldUnderlineFont.setUnderline(Font.U_SINGLE);

            // 헤더 작성
            String[] headers = {"날짜", "업무시간", "고객사", "프로젝트명/시스템명", "PJ-CODE", "업무유형",
                    "팀원백업", "동반지원", "출장지역", "업무내용", "지원제품"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            LocalDate lastWorkDate = workReportRepository.findMaxWorkDate();
            int rowIdx = 1;

            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                boolean isWeekend = date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
                List<WorkReport> reports = workReportRepository.findByWorkDay_WorkDate(date);

                if (date.isAfter(lastWorkDate)) {
                    Row row = sheet.createRow(rowIdx++);
                    for (int i = 0; i < headers.length; i++) {
                        Cell cell = row.createCell(i);
                        cell.setCellStyle(isWeekend ? weekendStyle : defaultStyle);
                    }
                    row.getCell(0).setCellValue(date.toString());
                    if (isWeekend) {
                        Cell contentCell = row.getCell(9);
                        contentCell.setCellValue("주말");
                        XSSFCellStyle specialStyle = workbook.createCellStyle();
                        specialStyle.cloneStyleFrom(weekendStyle);
                        specialStyle.setFont(boldUnderlineFont);
                        contentCell.setCellStyle(specialStyle);
                    }
                    continue;
                }

                if (reports.isEmpty()) {
                    Row row = sheet.createRow(rowIdx++);
                    for (int i = 0; i < headers.length; i++) {
                        Cell cell = row.createCell(i);
                        cell.setCellStyle(isWeekend ? weekendStyle : defaultStyle);
                    }
                    row.getCell(0).setCellValue(date.toString());
                    Cell contentCell = row.getCell(9);
                    if (isWeekend) {
                        contentCell.setCellValue("주말");
                    } else {
                        contentCell.setCellValue("연차");
                    }
                    XSSFCellStyle specialStyle = workbook.createCellStyle();
                    specialStyle.cloneStyleFrom(isWeekend ? weekendStyle : defaultStyle);
                    specialStyle.setFont(boldUnderlineFont);
                    contentCell.setCellStyle(specialStyle);
                    continue;
                }

                for (WorkReport report : reports) {
                    Row row = sheet.createRow(rowIdx++);
                    boolean isHoliday = report.getWorkHours() == 0;
                    boolean useWeekendStyle = isWeekend;

                    String[] values = {
                            date.toString(),
                            isHoliday ? "" : String.valueOf(report.getWorkHours()),
                            report.getClientName(),
                            report.getProjectName(),
                            report.getPjCode(),
                            report.getWorkType(),
                            report.isBackup() ? "✅" : "",
                            report.getSupportTeamMember(),
                            report.getOutLocation(),
                            "", // 업무내용은 아래에서 따로 처리
                            report.getSupportProduct()
                    };

                    for (int i = 0; i < values.length; i++) {
                        Cell cell = row.createCell(i);
                        cell.setCellValue(values[i]);
                        cell.setCellStyle(useWeekendStyle ? weekendStyle : defaultStyle);
                    }

                    // 업무내용 처리
                    Cell contentCell = row.getCell(9);

                    if (isWeekend && (isHoliday || report.getWorkDescription() == null || report.getWorkDescription().isBlank())) {
                        contentCell.setCellValue("주말");
                        XSSFCellStyle specialStyle = workbook.createCellStyle();
                        specialStyle.cloneStyleFrom(weekendStyle);
                        specialStyle.setFont(boldUnderlineFont);
                        contentCell.setCellStyle(specialStyle);
                    } else if (isHoliday) {
                        contentCell.setCellValue("연차");
                        XSSFCellStyle specialStyle = workbook.createCellStyle();
                        specialStyle.cloneStyleFrom(defaultStyle);
                        specialStyle.setFont(boldUnderlineFont);
                        contentCell.setCellStyle(specialStyle);
                    } else {
                        contentCell.setCellValue(report.getWorkDescription());
                        // 👉 업무내용 있을 때는 그냥 기본 폰트 유지
                    }
                }
            }

            // 컬럼 자동 사이즈 조정
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            System.out.println("📄 엑셀 작성 완료. byte size: " + out.size());
            return out.toByteArray();

        } catch (Exception e) {
            System.err.println("❌ 엑셀 생성 실패: " + e.getMessage());
            throw new RuntimeException("❌ 엑셀 생성 실패: " + e.getMessage(), e);
        }
    }
	
}
