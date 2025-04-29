package com.kwnam.workreport.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kwnam.workreport.entity.WorkDay;

public interface WorkDayRepository extends JpaRepository<WorkDay, Long> {
	 Optional<WorkDay> findByWorkDate(LocalDate workDate); // ✅ 날짜로 조회
}
