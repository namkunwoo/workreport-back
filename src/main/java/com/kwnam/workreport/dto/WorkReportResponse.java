package com.kwnam.workreport.dto;

import java.time.LocalDate;

import com.kwnam.workreport.entity.WorkReport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkReportResponse {
    private Long id;
    private String clientName;
    private String projectName;
    private String pjCode;
    private String workType;
    private int workHours;
    private boolean isOut;
    private String outLocation;
    private boolean isBackup;
    private String supportTeamMember;
    private String workDescription;
    private String supportProduct;
    private LocalDate workDate;
    
    public static WorkReportResponse fromEntity(WorkReport entity) {
        return WorkReportResponse.builder()
                .id(entity.getId())
                .clientName(entity.getClientName())
                .projectName(entity.getProjectName())
                .pjCode(entity.getPjCode())
                .workType(entity.getWorkType())
                .workHours(entity.getWorkHours())
                .isOut(entity.isOut())
                .outLocation(entity.getOutLocation())
                .workDescription(entity.getWorkDescription())
                .isBackup(entity.isBackup())
                .supportTeamMember(entity.getSupportTeamMember())
                .supportProduct(entity.getSupportProduct())
                .workDate(entity.getWorkDay() != null
                        ? entity.getWorkDay().getWorkDate()
                        : null)
                .build();
    }

}

