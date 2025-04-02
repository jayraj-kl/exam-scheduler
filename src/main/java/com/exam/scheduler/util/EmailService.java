package com.exam.scheduler.util;


import com.exam.scheduler.model.ExamSchedule;
import com.exam.scheduler.model.ExamSlot;
import com.exam.scheduler.model.Faculty;
import com.exam.scheduler.repository.ExamScheduleRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    
    @Autowired
    private TemplateEngine templateEngine;
    
    @Autowired
    private ExamScheduleRepository scheduleRepository;
    
    public void sendScheduleEmails(Long scheduleId) {
        ExamSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        
        // Group slots by faculty
        Map<Faculty, List<ExamSlot>> facultySlots = new HashMap<>();
        
        for (ExamSlot slot : schedule.getExamSlots()) {
            // Add main faculty
            facultySlots.computeIfAbsent(slot.getFaculty(), k -> new ArrayList<>()).add(slot);
            
            // Add exam head
            facultySlots.computeIfAbsent(slot.getExamHead(), k -> new ArrayList<>()).add(slot);
            
            // Add invigilators
            for (Faculty invigilator : slot.getInvigilators()) {
                facultySlots.computeIfAbsent(invigilator, k -> new ArrayList<>()).add(slot);
            }
        }
        
        // Send email to each faculty
        for (Map.Entry<Faculty, List<ExamSlot>> entry : facultySlots.entrySet()) {
            Faculty faculty = entry.getKey();
            List<ExamSlot> slots = entry.getValue();
            
            sendEmailToFaculty(faculty, slots, schedule);
        }
    }
    
    private void sendEmailToFaculty(Faculty faculty, List<ExamSlot> slots, ExamSchedule schedule) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(faculty.getEmail());
            helper.setSubject("Exam Schedule: " + schedule.getName());
            
            Context context = new Context();
            context.setVariable("faculty", faculty);
            context.setVariable("slots", slots);
            context.setVariable("schedule", schedule);
            
            String emailContent = templateEngine.process("exam-schedule-template", context);
            helper.setText(emailContent, true);
            
            mailSender.send(message);
            
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email to " + faculty.getName(), e);
        }
    }
}