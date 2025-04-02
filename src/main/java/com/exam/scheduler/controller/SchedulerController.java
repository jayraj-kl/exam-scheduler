package com.exam.scheduler.controller;


import com.exam.scheduler.model.ExamSchedule;
import com.exam.scheduler.service.SchedulerService;
import com.exam.scheduler.util.EmailService;
import com.exam.scheduler.util.ExcelExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/schedule")
public class SchedulerController {

    @Autowired
    private SchedulerService schedulerService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private ExcelExporter excelExporter;
    
    @PostMapping("/generate")
    public ResponseEntity<ExamSchedule> generateSchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String scheduleName) {
        
        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date cannot be before start date");
        }
        
        ExamSchedule schedule = schedulerService.generateExamSchedule(startDate, endDate, scheduleName);
        return ResponseEntity.ok(schedule);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ExamSchedule> getSchedule(@PathVariable Long id) {
        try {
            ExamSchedule schedule = schedulerService.getScheduleById(id);
            if (schedule == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found with id: " + id);
            }
            return ResponseEntity.ok(schedule);
        } catch (Exception e) {
            if (e instanceof ResponseStatusException) {
                throw e;
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving schedule: " + e.getMessage());
        }
    }
    
    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> exportSchedule(@PathVariable Long id) {
        try {
            // First check if schedule exists
            ExamSchedule schedule = schedulerService.getScheduleById(id);
            if (schedule == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found with id: " + id);
            }
            
            byte[] excelBytes = excelExporter.exportScheduleToExcel(id);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            String filename = "exam_schedule_" + schedule.getName().replaceAll("\\s+", "_") + ".xlsx";
            headers.setContentDispositionFormData("attachment", filename);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelBytes);
        } catch (Exception e) {
            if (e instanceof ResponseStatusException) {
                throw e;
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error exporting schedule: " + e.getMessage());
        }
    }
    
    @PostMapping("/{id}/email")
    public ResponseEntity<Map<String, Object>> emailSchedule(@PathVariable Long id) {
        try {
            // First check if schedule exists
            ExamSchedule schedule = schedulerService.getScheduleById(id);
            if (schedule == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found with id: " + id);
            }
            
            int emailsSent = emailService.sendScheduleEmails(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Schedule emails sent successfully");
            response.put("scheduleName", schedule.getName());
            response.put("emailsSent", emailsSent);
            response.put("scheduleId", id);
            response.put("timestamp", LocalDate.now());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            if (e instanceof ResponseStatusException) {
                throw e;
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error sending emails: " + e.getMessage());
        }
    }
}