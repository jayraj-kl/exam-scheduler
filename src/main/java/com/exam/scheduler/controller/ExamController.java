package com.exam.scheduler.controller;

import com.exam.scheduler.model.Exam;
import com.exam.scheduler.model.Faculty;
import com.exam.scheduler.model.Subject;
import com.exam.scheduler.model.ExamSlot;
import com.exam.scheduler.repository.ExamRepository;
import com.exam.scheduler.repository.FacultyRepository;
import com.exam.scheduler.repository.SubjectRepository;
import com.exam.scheduler.repository.ExamSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/exams")
public class ExamController {

    @Autowired
    private ExamRepository examRepository;
    
    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ExamSlotRepository examSlotRepository;
    
    @Autowired
    private FacultyRepository facultyRepository;

    @GetMapping("/subjects")
    public ResponseEntity<List<Subject>> getAllSubjects() {
        return ResponseEntity.ok(subjectRepository.findAll());
    }
    
    @GetMapping("/subjects/program/{programId}")
    public ResponseEntity<List<Subject>> getSubjectsByProgram(@PathVariable Long programId) {
        return ResponseEntity.ok(subjectRepository.findByProgramId(programId));
    }
    
    @GetMapping
    public ResponseEntity<List<Exam>> getAllExams() {
        return ResponseEntity.ok(examRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Exam> getExamById(@PathVariable Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam not found with id: " + id));
        return ResponseEntity.ok(exam);
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<Exam>> getExamsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(examRepository.findByExamDate(date));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Exam>> getExamsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(examRepository.findByStatus(status));
    }

    @GetMapping("/subject")
    public ResponseEntity<List<Exam>> getExamsBySubject(@RequestParam String query) {
        return ResponseEntity.ok(examRepository.findBySubjectContaining(query));
    }

    @PostMapping
    public ResponseEntity<Exam> createExam(@RequestBody Exam exam) {
        // If subjectEntity ID is provided but not fully loaded, fetch the complete subject
        if (exam.getSubjectEntity() != null && exam.getSubjectEntity().getId() != null) {
            Subject subject = subjectRepository.findById(exam.getSubjectEntity().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Subject not found with id: " + exam.getSubjectEntity().getId()));
            exam.setSubjectEntity(subject);
            
            // Update the subject string field for backward compatibility
            if (exam.getSubject() == null || exam.getSubject().isEmpty()) {
                exam.setSubject(subject.getName());
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(examRepository.save(exam));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Exam> updateExam(@PathVariable Long id, @RequestBody Exam examDetails) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam not found with id: " + id));
        
        exam.setExamName(examDetails.getExamName());
        exam.setSubject(examDetails.getSubject());
        
        // Update subject entity if provided
        if (examDetails.getSubjectEntity() != null && examDetails.getSubjectEntity().getId() != null) {
            Subject subject = subjectRepository.findById(examDetails.getSubjectEntity().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Subject not found with id: " + examDetails.getSubjectEntity().getId()));
            exam.setSubjectEntity(subject);
            
            // Update the subject string field for backward compatibility if it wasn't explicitly set
            if (examDetails.getSubject() == null || examDetails.getSubject().isEmpty()) {
                exam.setSubject(subject.getName());
            }
        } else {
            exam.setSubjectEntity(null);
        }
        
        exam.setExamDate(examDetails.getExamDate());
        exam.setDuration(examDetails.getDuration());
        exam.setStartTime(examDetails.getStartTime());
        exam.setStatus(examDetails.getStatus());
        exam.setDescription(examDetails.getDescription());
        
        return ResponseEntity.ok(examRepository.save(exam));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteExam(@PathVariable Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam not found with id: " + id));
        
        examRepository.delete(exam);
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all exams for a specific subject
     * @param subject The subject name or code
     * @return List of exam slots for the specified subject
     */
    @GetMapping("/subject/{subject}")
    public ResponseEntity<List<ExamSlot>> getExamSlotsBySubject(@PathVariable String subject) {
        try {
            List<ExamSlot> examSlots = examSlotRepository.findBySubject(subject);
            return ResponseEntity.ok(examSlots);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error retrieving exams for subject: " + e.getMessage());
        }
    }
    
    /**
     * Get upcoming exams (next 7 days)
     * @return List of exam slots scheduled for the next 7 days
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<ExamSlot>> getUpcomingExams() {
        try {
            LocalDate today = LocalDate.now();
            LocalDate nextWeek = today.plusDays(7);
            
            List<ExamSlot> upcomingExams = examSlotRepository.findByExamDateBetween(today, nextWeek);
            return ResponseEntity.ok(upcomingExams);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error retrieving upcoming exams: " + e.getMessage());
        }
    }
    
    /**
     * Get upcoming exams for a specific subject
     * @param subject The subject name or code
     * @return List of upcoming exam slots for the specified subject
     */
    @GetMapping("/upcoming/subject/{subject}")
    public ResponseEntity<List<ExamSlot>> getUpcomingExamsBySubject(@PathVariable String subject) {
        try {
            LocalDate today = LocalDate.now();
            LocalDate nextWeek = today.plusDays(7);
            
            List<ExamSlot> upcomingExams = examSlotRepository.findBySubjectAndDateRange(
                subject, today, nextWeek);
            return ResponseEntity.ok(upcomingExams);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error retrieving upcoming exams for subject: " + e.getMessage());
        }
    }
    
    /**
     * Get exam statistics - total count, upcoming count, etc.
     * @return Map containing various exam statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getExamStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            LocalDate today = LocalDate.now();
            LocalDate nextWeek = today.plusDays(7);
            
            long totalExams = examSlotRepository.count();
            long upcomingExams = examSlotRepository.countByExamDateBetween(today, nextWeek);
            
            // You may need to add a custom query for pending exams if there's a status field
            // This is an approximation assuming future exams are "pending"
            long pendingExams = examSlotRepository.countByExamDateBetween(today, LocalDate.MAX);
            
            stats.put("totalExams", totalExams);
            stats.put("upcomingExams", upcomingExams);
            stats.put("pendingExams", pendingExams);
            stats.put("timestamp", today.toString());
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error retrieving exam statistics: " + e.getMessage());
        }
    }
    
    /**
     * Get exam statistics for a specific date range
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @return Map containing exam statistics for the specified date range
     */
    @GetMapping("/stats/range")
    public ResponseEntity<Map<String, Object>> getExamStatisticsForRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            if (endDate.isBefore(startDate)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "End date cannot be before start date");
            }
            
            Map<String, Object> stats = new HashMap<>();
            
            long examsInRange = examSlotRepository.countByExamDateBetween(startDate, endDate);
            List<ExamSlot> examList = examSlotRepository.findByExamDateBetween(startDate, endDate);
            
            stats.put("totalExamsInRange", examsInRange);
            stats.put("startDate", startDate.toString());
            stats.put("endDate", endDate.toString());
            stats.put("exams", examList);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            if (e instanceof ResponseStatusException) {
                throw e;
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error retrieving exam statistics for range: " + e.getMessage());
        }
    }
    
    /**
     * Get all exams in a specific schedule
     * @param scheduleId The schedule ID
     * @return List of exam slots in the specified schedule
     */
    @GetMapping("/schedule/{scheduleId}")
    public ResponseEntity<List<ExamSlot>> getExamsBySchedule(@PathVariable Long scheduleId) {
        try {
            // Using the repository method from ExamScheduleRepository
            List<ExamSlot> examSlots = examSlotRepository.findByScheduleId(scheduleId);
            return ResponseEntity.ok(examSlots);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error retrieving exams for schedule: " + e.getMessage());
        }
    }
    
    /**
     * Get all exam slots with their assigned faculty
     * @return List of exam slots with faculty information
     */
    @GetMapping("/slots/faculty")
    public ResponseEntity<List<Map<String, Object>>> getExamSlotsWithFaculty() {
        try {
            List<ExamSlot> slots = examSlotRepository.findAll();
            List<Map<String, Object>> result = new ArrayList<>();
            
            for (ExamSlot slot : slots) {
                Map<String, Object> slotInfo = new HashMap<>();
                slotInfo.put("id", slot.getId());
                slotInfo.put("examDate", slot.getExamDate());
                slotInfo.put("startTime", slot.getStartTime());
                slotInfo.put("endTime", slot.getEndTime());
                slotInfo.put("subject", slot.getSubject().getName());
                slotInfo.put("room", slot.getRoom() != null ? slot.getRoom().getRoomNumber() : "Not assigned");
                
                Map<String, Object> facultyInfo = new HashMap<>();
                if (slot.getFaculty() != null) {
                    facultyInfo.put("id", slot.getFaculty().getId());
                    facultyInfo.put("name", slot.getFaculty().getName());
                    facultyInfo.put("department", slot.getFaculty().getDepartment());
                    facultyInfo.put("workload", slot.getFaculty().getCurrentWorkload());
                } else {
                    facultyInfo = null;
                }
                slotInfo.put("faculty", facultyInfo);
                
                Map<String, Object> examHeadInfo = new HashMap<>();
                if (slot.getExamHead() != null) {
                    examHeadInfo.put("id", slot.getExamHead().getId());
                    examHeadInfo.put("name", slot.getExamHead().getName());
                    examHeadInfo.put("department", slot.getExamHead().getDepartment());
                } else {
                    examHeadInfo = null;
                }
                slotInfo.put("examHead", examHeadInfo);
                
                List<Map<String, Object>> invigilatorsInfo = new ArrayList<>();
                if (slot.getInvigilators() != null && !slot.getInvigilators().isEmpty()) {
                    for (Faculty invigilator : slot.getInvigilators()) {
                        Map<String, Object> invigilatorInfo = new HashMap<>();
                        invigilatorInfo.put("id", invigilator.getId());
                        invigilatorInfo.put("name", invigilator.getName());
                        invigilatorInfo.put("department", invigilator.getDepartment());
                        invigilatorsInfo.add(invigilatorInfo);
                    }
                }
                slotInfo.put("invigilators", invigilatorsInfo);
                
                result.add(slotInfo);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error retrieving exam slots with faculty: " + e.getMessage());
        }
    }
    
    /**
     * Get faculty workload statistics
     * @return Map containing faculty workload statistics
     */
    @GetMapping("/faculty/workload")
    public ResponseEntity<List<Map<String, Object>>> getFacultyWorkloadStats() {
        try {
            List<Faculty> allFaculty = facultyRepository.findAll();
            List<Map<String, Object>> result = new ArrayList<>();
            
            for (Faculty faculty : allFaculty) {
                Map<String, Object> facultyStats = new HashMap<>();
                facultyStats.put("id", faculty.getId());
                facultyStats.put("name", faculty.getName());
                facultyStats.put("department", faculty.getDepartment());
                facultyStats.put("currentWorkload", faculty.getCurrentWorkload());
                facultyStats.put("workloadCapacity", faculty.getWorkloadCapacity());
                facultyStats.put("usagePercentage", 
                    faculty.getWorkloadCapacity() > 0 ? 
                        (double) faculty.getCurrentWorkload() / faculty.getWorkloadCapacity() * 100 : 0);
                facultyStats.put("availabilitySlots", faculty.getAvailabilitySlots());
                facultyStats.put("isExamHead", faculty.isExamHead());
                facultyStats.put("isInvigilator", faculty.isInvigilator());
                
                result.add(facultyStats);
            }
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error retrieving faculty workload statistics: " + e.getMessage());
        }
    }
}