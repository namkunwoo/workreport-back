package com.kwnam.workreport.service;

import com.kwnam.workreport.entity.WorkDay;
import com.kwnam.workreport.repository.WorkDayRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkDayService {

    private final WorkDayRepository workDayRepository;

    public WorkDayService(WorkDayRepository workDayRepository) {
        this.workDayRepository = workDayRepository;
    }

    // 모든 WorkDay 목록 조회
    public List<WorkDay> getAllWorkDays() {
        return workDayRepository.findAll();
    }

    // WorkDay 생성
    public WorkDay createWorkDay(WorkDay workDay) {
        return workDayRepository.save(workDay);
    }
}
