package com.kwnam.workreport.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kwnam.workreport.entity.WorkReport;

public interface WorkReportRepository extends JpaRepository<WorkReport, Long> {
}
