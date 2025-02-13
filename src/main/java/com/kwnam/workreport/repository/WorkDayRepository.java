package com.kwnam.workreport.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kwnam.workreport.entity.WorkDay;

public interface WorkDayRepository extends JpaRepository<WorkDay, Long> {
}
