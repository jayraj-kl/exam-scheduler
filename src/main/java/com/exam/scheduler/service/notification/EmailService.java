package com.exam.scheduler.service.notification;

import com.exam.scheduler.model.ExamSchedule;
import com.exam.scheduler.model.ExamSlot;
import com.exam.scheduler.model.Faculty;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Emails every faculty member involved in a schedule (as faculty, exam head or
 * invigilator) the slots they are assigned to, rendered from the
 * {@value #TEMPLATE_NAME} Thymeleaf template.
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final String TEMPLATE_NAME = "exam-schedule-template";

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    /**
     * @return the number of emails sent
     */
    public int sendScheduleEmails(ExamSchedule schedule) {
        Map<Faculty, List<ExamSlot>> facultySlots = new HashMap<>();

        for (ExamSlot slot : schedule.getExamSlots()) {
            facultySlots.computeIfAbsent(slot.getFaculty(), k -> new ArrayList<>()).add(slot);
            facultySlots.computeIfAbsent(slot.getExamHead(), k -> new ArrayList<>()).add(slot);
            for (Faculty invigilator : slot.getInvigilators()) {
                facultySlots.computeIfAbsent(invigilator, k -> new ArrayList<>()).add(slot);
            }
        }

        int emailsSent = 0;
        for (Map.Entry<Faculty, List<ExamSlot>> entry : facultySlots.entrySet()) {
            sendEmailToFaculty(entry.getKey(), entry.getValue(), schedule);
            emailsSent++;
        }

        return emailsSent;
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

            String emailContent = templateEngine.process(TEMPLATE_NAME, context);
            helper.setText(emailContent, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email to " + faculty.getName(), e);
        }
    }
}
