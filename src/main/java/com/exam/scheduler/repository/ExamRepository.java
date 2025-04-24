package com.exam.scheduler.repository;

import com.exam.scheduler.model.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {
    
    List<Exam> findByExamDate(LocalDate examDate);
    List<Exam> findByStatus(String status);
    List<Exam> findBySubjectContaining(String subject);
}