package com.exam.scheduler.repository;

import com.exam.scheduler.model.ExamSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExamSlotRepository extends JpaRepository<ExamSlot, Long> {
    
    List<ExamSlot> findByExamDate(LocalDate examDate);
    
    // Find exams by subject name or code
    @Query("SELECT e FROM ExamSlot e WHERE e.subject.name = :subject OR e.subject.code = :subject")
    List<ExamSlot> findBySubject(@Param("subject") String subject);
    
    // Find upcoming exams
    List<ExamSlot> findByExamDateBetween(LocalDate startDate, LocalDate endDate);
    
    // Find exams by subject and date range
    @Query("SELECT e FROM ExamSlot e WHERE (e.subject.name = :subject OR e.subject.code = :subject) AND e.examDate BETWEEN :startDate AND :endDate")
    List<ExamSlot> findBySubjectAndDateRange(
            @Param("subject") String subject, 
            @Param("startDate") LocalDate startDate, 
            @Param("endDate") LocalDate endDate);
    
    // Count exams in date range
    long countByExamDateBetween(LocalDate startDate, LocalDate endDate);
    
    // Find exam slots by schedule ID
    @Query("SELECT e FROM ExamSlot e JOIN ExamSchedule s JOIN s.examSlots es WHERE s.id = :scheduleId AND e.id = es.id")
    List<ExamSlot> findByScheduleId(@Param("scheduleId") Long scheduleId);
}
