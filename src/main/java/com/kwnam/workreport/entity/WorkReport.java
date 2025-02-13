package com.kwnam.workreport.entity;

import jakarta.persistence.*;

@Entity
public class WorkReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 기존 필드
    private String clientName;  // 고객사명
    private String pjCode;      // PJ코드
    private String workIntensity;  // 업무 강도
    private int workHours;         // 업무 시간
    private boolean isOut;         // 외근 여부
    private String workDescription; // 업무 내용
    private String supportProduct;  // 지원 제품

    // 새로 추가된 필드
    private String outLocation;  // 외근 위치
    private boolean isBackup;  // 팀원 백업 여부
    private String supportTeamMember;  // 동반 지원 팀원 내용

    @ManyToOne
    @JoinColumn(name = "work_day_id")
    private WorkDay workDay;

    // 기본 생성자
    public WorkReport() {}

    // 모든 필드를 포함하는 생성자 (선택)
    public WorkReport(String clientName, String pjCode, String workIntensity, int workHours, boolean isOut,
                     String workDescription, String supportProduct, String outLocation, boolean isBackup,
                     String supportTeamMember) {
        this.clientName = clientName;
        this.pjCode = pjCode;
        this.workIntensity = workIntensity;
        this.workHours = workHours;
        this.isOut = isOut;
        this.workDescription = workDescription;
        this.supportProduct = supportProduct;
        this.outLocation = outLocation;
        this.isBackup = isBackup;
        this.supportTeamMember = supportTeamMember;
    }

    // Getters and Setters (생략 가능)
}
