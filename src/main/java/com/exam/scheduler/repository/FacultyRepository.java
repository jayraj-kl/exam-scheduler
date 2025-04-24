package com.exam.scheduler.repository;

import com.exam.scheduler.model.Faculty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    
    List<Faculty> findByIsExamHeadTrue();
    
    List<Faculty> findByIsInvigilatorTrue();
    
    @Query("SELECT f FROM Faculty f WHERE f.currentWorkload < f.workloadCapacity ORDER BY f.currentWorkload ASC")
    List<Faculty> findAvailableFacultiesOrderByWorkload();
    
    @Query("SELECT DISTINCT f FROM Faculty f " +
           "WHERE f.currentWorkload < f.workloadCapacity " +
           "AND (f.availabilitySlots IS EMPTY OR :slot MEMBER OF f.availabilitySlots " +
           "    OR NOT EXISTS (SELECT s FROM ExamSlot s WHERE s.faculty = f " +
           "                  AND s.examDate = :date " +
           "                  AND s.startTime = :startTime)) " +
           "ORDER BY f.currentWorkload ASC")
    List<Faculty> findAvailableFacultiesForSlot(
           @Param("slot") LocalDateTime slot,
           @Param("date") LocalDate date,
           @Param("startTime") LocalTime startTime);
    
    @Query("SELECT DISTINCT f FROM Faculty f " +
           "WHERE f.currentWorkload < f.workloadCapacity " +
           "AND NOT EXISTS (SELECT s FROM ExamSlot s " +
           "               WHERE s.faculty = f " +
           "               AND s.examDate = :date " +
           "               AND s.startTime >= :startTime " +
           "               AND s.startTime < :endTime) " +
           "ORDER BY f.currentWorkload ASC")
    List<Faculty> findAvailableFacultiesForTimeSlot(
           @Param("date") LocalDate date, 
           @Param("startTime") LocalTime startTime, 
           @Param("endTime") LocalTime endTime);
}