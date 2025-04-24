package com.exam.scheduler.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String studentId;
    private String name;
    private String email;
    private String phone;
    
    @ManyToOne
    private Program program;
    
    private int semester;
    private String status;
    private LocalDate enrollmentDate;
    private String address;
}