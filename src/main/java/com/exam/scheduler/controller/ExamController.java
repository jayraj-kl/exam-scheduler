package com.exam.scheduler.controller;

import com.exam.scheduler.mapper.ExamSlotMapper;
import com.exam.scheduler.mapper.FacultyMapper;
import com.exam.scheduler.model.Exam;
import com.exam.scheduler.model.ExamSlot;
import com.exam.scheduler.model.Subject;
import com.exam.scheduler.service.ExamService;
import com.exam.scheduler.service.ExamSlotService;
import com.exam.scheduler.service.FacultyService;
import com.exam.scheduler.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static com.exam.scheduler.controller.ControllerErrors.withErrorPrefix;
import static com.exam.scheduler.controller.ControllerErrors.withErrorPrefixWrappingAll;

/**
 * Exam records (CRUD), subjects, and read-only views over scheduled exam slots.
 */
@RestController
@RequestMapping("/api/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;
    private final SubjectService subjectService;
    private final ExamSlotService examSlotService;
    private final FacultyService facultyService;

    // ---- Subjects

    @GetMapping("/subjects")
    public ResponseEntity<List<Subject>> getAllSubjects() {
        return ResponseEntity.ok(subjectService.getAllSubjects());
    }

    @GetMapping("/subjects/program/{programId}")
    public ResponseEntity<List<Subject>> getSubjectsByProgram(@PathVariable Long programId) {
        return ResponseEntity.ok(subjectService.getSubjectsByProgram(programId));
    }

    // ---- Exams

    @GetMapping
    public ResponseEntity<List<Exam>> getAllExams() {
        return ResponseEntity.ok(examService.getAllExams());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Exam> getExamById(@PathVariable Long id) {
        return ResponseEntity.ok(examService.getExamById(id));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<Exam>> getExamsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(examService.getExamsByDate(date));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Exam>> getExamsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(examService.getExamsByStatus(status));
    }

    @GetMapping("/subject")
    public ResponseEntity<List<Exam>> getExamsBySubject(@RequestParam String query) {
        return ResponseEntity.ok(examService.searchExamsBySubject(query));
    }

    @PostMapping
    public ResponseEntity<Exam> createExam(@RequestBody Exam exam) {
        return ResponseEntity.status(HttpStatus.CREATED).body(examService.createExam(exam));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Exam> updateExam(@PathVariable Long id, @RequestBody Exam examDetails) {
        return ResponseEntity.ok(examService.updateExam(id, examDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteExam(@PathVariable Long id) {
        examService.deleteExam(id);
        return ResponseEntity.ok(Map.of("deleted", Boolean.TRUE));
    }

    // ---- Scheduled exam slots

    /**
     * @param subject subject name or code
     */
    @GetMapping("/subject/{subject}")
    public ResponseEntity<List<ExamSlot>> getExamSlotsBySubject(@PathVariable String subject) {
        return withErrorPrefixWrappingAll("Error retrieving exams for subject: ",
            () -> ResponseEntity.ok(examSlotService.getSlotsBySubject(subject)));
    }

    /**
     * Exam slots scheduled from today through the next 7 days.
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<ExamSlot>> getUpcomingExams() {
        return withErrorPrefixWrappingAll("Error retrieving upcoming exams: ",
            () -> ResponseEntity.ok(examSlotService.getUpcomingSlots()));
    }

    /**
     * @param subject subject name or code
     */
    @GetMapping("/upcoming/subject/{subject}")
    public ResponseEntity<List<ExamSlot>> getUpcomingExamsBySubject(@PathVariable String subject) {
        return withErrorPrefixWrappingAll("Error retrieving upcoming exams for subject: ",
            () -> ResponseEntity.ok(examSlotService.getUpcomingSlotsBySubject(subject)));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getExamStatistics() {
        return withErrorPrefixWrappingAll("Error retrieving exam statistics: ",
            () -> ResponseEntity.ok(examSlotService.getStatistics()));
    }

    @GetMapping("/stats/range")
    public ResponseEntity<Map<String, Object>> getExamStatisticsForRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return withErrorPrefix("Error retrieving exam statistics for range: ", () -> {
            if (endDate.isBefore(startDate)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "End date cannot be before start date");
            }
            return ResponseEntity.ok(examSlotService.getStatisticsForRange(startDate, endDate));
        });
    }

    @GetMapping("/schedule/{scheduleId}")
    public ResponseEntity<List<ExamSlot>> getExamsBySchedule(@PathVariable Long scheduleId) {
        return withErrorPrefixWrappingAll("Error retrieving exams for schedule: ",
            () -> ResponseEntity.ok(examSlotService.getSlotsBySchedule(scheduleId)));
    }

    /**
     * Every exam slot with its room and assigned faculty, exam head and invigilators.
     */
    @GetMapping("/slots/faculty")
    public ResponseEntity<List<Map<String, Object>>> getExamSlotsWithFaculty() {
        return withErrorPrefixWrappingAll("Error retrieving exam slots with faculty: ",
            () -> ResponseEntity.ok(examSlotService.getAllSlots().stream()
                .map(ExamSlotMapper::toStaffAssignmentSummary)
                .toList()));
    }

    @GetMapping("/faculty/workload")
    public ResponseEntity<List<Map<String, Object>>> getFacultyWorkloadStats() {
        return withErrorPrefixWrappingAll("Error retrieving faculty workload statistics: ",
            () -> ResponseEntity.ok(facultyService.getAllFaculty().stream()
                .map(FacultyMapper::toWorkloadSummary)
                .toList()));
    }
}
