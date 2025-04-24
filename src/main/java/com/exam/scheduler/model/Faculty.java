package com.exam.scheduler.model;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Faculty {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String department;
    private String email;
    
    @ElementCollection
    private List<LocalDateTime> availabilitySlots;
    
    private int workloadCapacity;
    private int currentWorkload;
    
    @OneToMany(mappedBy = "faculty")
    @JsonIgnore
    private List<ExamSlot> assignedSlots;
    
    private boolean isExamHead;
    private boolean isInvigilator;
}