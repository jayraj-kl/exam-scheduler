package com.exam.scheduler.repository;

import com.exam.scheduler.model.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    
    List<Faculty> findByIsExamHeadTrue();
    
    List<Faculty> findByIsInvigilatorTrue();
    
    @Query("SELECT f FROM Faculty f WHERE f.currentWorkload < f.workloadCapacity ORDER BY f.currentWorkload ASC")
    List<Faculty> findAvailableFacultiesOrderByWorkload();
    
    @Query("SELECT f FROM Faculty f WHERE :slot MEMBER OF f.availabilitySlots AND f.currentWorkload < f.workloadCapacity ORDER BY f.currentWorkload ASC")
    List<Faculty> findAvailableFacultiesForSlot(LocalDateTime slot);
}