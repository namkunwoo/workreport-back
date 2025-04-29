package com.kwnam.workreport.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate workDate;
    private boolean isHoliday;

    @OneToMany(mappedBy = "workDay", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkReport> workReports;
}
