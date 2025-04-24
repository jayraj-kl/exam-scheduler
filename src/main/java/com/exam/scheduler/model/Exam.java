package com.exam.scheduler.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Exam {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String examName;
    
    private String subject; // Kept for backward compatibility
    
    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subjectEntity; // New field to link to the Subject entity
    
    private LocalDate examDate;
    private int duration;
    private LocalTime startTime;
    private String status;
    private String description;
}