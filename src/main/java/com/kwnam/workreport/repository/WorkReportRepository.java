package com.kwnam.workreport.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kwnam.workreport.entity.WorkReport;

public interface WorkReportRepository extends JpaRepository<WorkReport, Long> {

    // 기존: 날짜로 목록
    List<WorkReport> findByWorkDay_WorkDate(LocalDate workDate);

    // 추가: 날짜로 목록 (내림차순 정렬)
    List<WorkReport> findByWorkDay_WorkDateOrderByIdDesc(LocalDate workDate);

    // 추가: 기간 조회 + 정렬 (DTO 매핑은 Service에서 수행)
    @Query("""
      select w
      from WorkReport w
      join w.workDay d
      where d.workDate between :start and :end
      order by d.workDate desc, w.id desc
    """)
    List<WorkReport> findRange(@Param("start") LocalDate start,
                               @Param("end")   LocalDate end);

    // 기존: 최대 날짜
    @Query("SELECT MAX(w.workDay.workDate) FROM WorkReport w")
    LocalDate findMaxWorkDate();
    
}