package com.exam.scheduler.controller;


import com.exam.scheduler.model.ExamSchedule;
import com.exam.scheduler.service.SchedulerService;
import com.exam.scheduler.util.EmailService;
import com.exam.scheduler.util.ExcelExporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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
        
        ExamSchedule schedule = schedulerService.generateExamSchedule(startDate, endDate, scheduleName);
        return ResponseEntity.ok(schedule);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ExamSchedule> getSchedule(@PathVariable Long id) {
        // Implement method to get schedule by ID
        return ResponseEntity.ok(null);
    }
    
    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> exportSchedule(@PathVariable Long id) {
        // Implement export functionality
        byte[] excelBytes = excelExporter.exportScheduleToExcel(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "exam_schedule.xlsx");
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }
    
    @PostMapping("/{id}/email")
    public ResponseEntity<String> emailSchedule(@PathVariable Long id) {
        // Implement email sending functionality
        emailService.sendScheduleEmails(id);
        return ResponseEntity.ok("Schedule emails sent successfully");
    }
}