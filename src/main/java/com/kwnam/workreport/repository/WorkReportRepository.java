package com.kwnam.workreport.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.kwnam.workreport.entity.WorkReport;

public interface WorkReportRepository extends JpaRepository<WorkReport, Long> {
	List<WorkReport> findByWorkDay_WorkDate(LocalDate workDate);
	
	@Query("SELECT MAX(w.workDay.workDate) FROM WorkReport w")
    LocalDate findMaxWorkDate();
}
