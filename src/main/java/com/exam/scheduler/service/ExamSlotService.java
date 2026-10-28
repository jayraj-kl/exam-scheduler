package com.exam.scheduler.service;

import com.exam.scheduler.model.ExamSlot;
import com.exam.scheduler.repository.ExamSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Read-side queries and statistics over scheduled exam slots.
 */
@Service
@RequiredArgsConstructor
public class ExamSlotService {

    /** "Upcoming" means scheduled from today through this many days ahead. */
    private static final int UPCOMING_WINDOW_DAYS = 7;

    private final ExamSlotRepository examSlotRepository;

    public List<ExamSlot> getAllSlots() {
        return examSlotRepository.findAll();
    }

    /**
     * @param subject subject name or code
     */
    public List<ExamSlot> getSlotsBySubject(String subject) {
        return examSlotRepository.findBySubject(subject);
    }

    public List<ExamSlot> getUpcomingSlots() {
        LocalDate today = LocalDate.now();
        return examSlotRepository.findByExamDateBetween(today, today.plusDays(UPCOMING_WINDOW_DAYS));
    }

    /**
     * @param subject subject name or code
     */
    public List<ExamSlot> getUpcomingSlotsBySubject(String subject) {
        LocalDate today = LocalDate.now();
        return examSlotRepository.findBySubjectAndDateRange(subject, today, today.plusDays(UPCOMING_WINDOW_DAYS));
    }

    public List<ExamSlot> getSlotsBySchedule(Long scheduleId) {
        return examSlotRepository.findByScheduleId(scheduleId);
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        LocalDate today = LocalDate.now();

        long totalExams = examSlotRepository.count();
        long upcomingExams = examSlotRepository.countByExamDateBetween(today, today.plusDays(UPCOMING_WINDOW_DAYS));
        // Approximation: every exam from today onwards counts as pending
        long pendingExams = examSlotRepository.countByExamDateBetween(today, LocalDate.MAX);

        stats.put("totalExams", totalExams);
        stats.put("upcomingExams", upcomingExams);
        stats.put("pendingExams", pendingExams);
        stats.put("timestamp", today.toString());

        return stats;
    }

    public Map<String, Object> getStatisticsForRange(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> stats = new HashMap<>();

        long examsInRange = examSlotRepository.countByExamDateBetween(startDate, endDate);
        List<ExamSlot> examList = examSlotRepository.findByExamDateBetween(startDate, endDate);

        stats.put("totalExamsInRange", examsInRange);
        stats.put("startDate", startDate.toString());
        stats.put("endDate", endDate.toString());
        stats.put("exams", examList);

        return stats;
    }
}
