package com.kwnam.workreport.service;


import java.util.List;

import org.springframework.stereotype.Service;

import com.kwnam.workreport.entity.WorkDay;
import com.kwnam.workreport.repository.WorkDayRepository;

@Service
public class WorkDayService {

    private final WorkDayRepository workDayRepository;

    public WorkDayService(WorkDayRepository workDayRepository) {
        this.workDayRepository = workDayRepository;
    }

    public List<WorkDay> getAllWorkDays() {
        return workDayRepository.findAll();
    }

    public WorkDay createWorkDay(WorkDay workDay) {
        return workDayRepository.save(workDay);
    }
}
