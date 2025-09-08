package com.kwnam.workreport.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ 업무 필드
    @Column(length = 500)
    private String clientName;         // 고객사명
    @Column(length = 500)
    private String projectName;  // 프로젝트명/시스템명
    @Column(length = 300)
    private String pjCode;             // 프로젝트 코드
    private String workType;      		// 업무 유형
    private int workHours;             // 업무 시간
    private boolean isOut;             // 외근 여부
    @Column(length = 2000)
    private String workDescription;    // 업무 내용
    @Column(length = 500)
    private String supportProduct;     // 지원 제품
    @Column(length = 500)
    private String outLocation;        // 외근 위치
    private boolean isBackup;          // 팀원 백업 여부
    @Column(length = 1000)
    private String supportTeamMember;  // 동반 지원 팀원

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_day_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_report_workday"))
    private WorkDay workDay;
}
