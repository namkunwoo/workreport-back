package com.kwnam.workreport.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class WorkReportRequest {
    private String client;
    private String projectName;
    private String pjCode;
    private String workType;
    private int workHours;
    private boolean out;
    private String location;
    private boolean backup;
    private String coWorkers;
    private String content;
    private String product; 
    private LocalDate workDate;
}
