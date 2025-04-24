package com.exam.scheduler.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExamSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private LocalDate examDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalTime reportingTime;
    
    @Enumerated(EnumType.STRING)
    private SlotType slotType;
    
    @ManyToOne
    private Subject subject;
    
    @ManyToOne
    private Room room;
    
    @ManyToOne
    private Faculty faculty;
    
    @ManyToOne
    private Faculty examHead;
    
    @ManyToMany
    @JoinTable(
        name = "exam_slot_invigilators",
        joinColumns = @JoinColumn(name = "exam_slot_id"),
        inverseJoinColumns = @JoinColumn(name = "invigilators_id"),
        uniqueConstraints = @UniqueConstraint(
            columnNames = {"exam_slot_id", "invigilators_id"}
        )
    )
    @JsonIgnore
    private List<Faculty> invigilators;
    
    private int studentCount;
    private int regularStudentCount;
    private int backlogStudentCount;
    
    private boolean isMorningSlot;

    public void setIsMorningSlot(boolean isMorningSlot) {
        this.isMorningSlot = isMorningSlot;
    }

    public enum SlotType {
        REGULAR, BACKLOG, MIXED
    }
}
