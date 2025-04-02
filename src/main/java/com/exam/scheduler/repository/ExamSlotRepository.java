package com.exam.scheduler.repository;

import com.exam.scheduler.model.ExamSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExamSlotRepository extends JpaRepository<ExamSlot, Long> {
    
    List<ExamSlot> findByExamDate(LocalDate examDate);
}
