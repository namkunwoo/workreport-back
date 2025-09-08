package com.kwnam.workreport.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "work_day",
    uniqueConstraints = {
        // DB의 실제 컬럼명이 work_date인 것이 확인되었으므로 그대로 사용
        @UniqueConstraint(name = "uk_workday_date", columnNames = {"work_date"})
    },
    indexes = {
        @Index(name = "ix_workday_date", columnList = "work_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // DB 컬럼명과 정확히 일치시키기
    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "is_holiday", nullable = false)
    private boolean isHoliday;

    // ⚠️ 무한 중첩/과다 직렬화 방지를 위해 컬렉션 제거
    // @OneToMany(mappedBy = "workDay", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<WorkReport> workReports;
}
