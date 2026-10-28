package com.exam.scheduler.controller;

import com.exam.scheduler.dto.AllocationOptions;
import com.exam.scheduler.model.ExamSchedule;
import com.exam.scheduler.model.ExamSlot;
import com.exam.scheduler.service.ScheduleService;
import com.exam.scheduler.service.export.ExcelExporter;
import com.exam.scheduler.service.notification.EmailService;
import com.exam.scheduler.service.scheduling.ResourceAllocationService;
import com.exam.scheduler.service.scheduling.ScheduleGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.exam.scheduler.controller.ControllerErrors.withErrorPrefix;
import static com.exam.scheduler.controller.ControllerErrors.withErrorPrefixWrappingAll;

/**
 * Exam schedules: generation, retrieval, export, notification and bulk allocation.
 * Slot-level and faculty endpoints under {@code /api/schedule} live in
 * {@link ExamSlotController} and {@link FacultyController}.
 */
@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class SchedulerController {

    private final ScheduleService scheduleService;
    private final ScheduleGenerationService scheduleGenerationService;
    private final ResourceAllocationService resourceAllocationService;
    private final EmailService emailService;
    private final ExcelExporter excelExporter;

    @GetMapping
    public ResponseEntity<List<ExamSchedule>> getAllSchedules() {
        return withErrorPrefixWrappingAll("Error retrieving schedules: ",
            () -> ResponseEntity.ok(scheduleService.getAllSchedules()));
    }

    /**
     * Optional body: {@code {"programIds": ["cs", "2"], "includeWeekends": false}}.
     */
    @PostMapping("/generate")
    public ResponseEntity<ExamSchedule> generateSchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String scheduleName,
            @RequestBody(required = false) Map<String, Object> requestBody) {

        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date cannot be before start date");
        }

        @SuppressWarnings("unchecked")
        List<String> programIds = requestBody != null ? (List<String>) requestBody.get("programIds") : null;
        boolean includeWeekends = requestBody != null ? (Boolean) requestBody.get("includeWeekends") : false;

        ExamSchedule schedule = scheduleGenerationService.generateExamSchedule(
            startDate, endDate, scheduleName, programIds, includeWeekends);
        return ResponseEntity.ok(schedule);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamSchedule> getSchedule(@PathVariable Long id) {
        return withErrorPrefix("Error retrieving schedule: ",
            () -> ResponseEntity.ok(scheduleService.getScheduleById(id)));
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> exportSchedule(@PathVariable Long id) {
        return withErrorPrefix("Error exporting schedule: ", () -> {
            ExamSchedule schedule = scheduleService.getScheduleById(id);
            byte[] excelBytes = excelExporter.exportScheduleToExcel(schedule);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            String filename = "exam_schedule_" + schedule.getName().replaceAll("\\s+", "_") + ".xlsx";
            headers.setContentDispositionFormData("attachment", filename);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelBytes);
        });
    }

    @PostMapping("/{id}/email")
    public ResponseEntity<Map<String, Object>> emailSchedule(@PathVariable Long id) {
        return withErrorPrefix("Error sending emails: ", () -> {
            ExamSchedule schedule = scheduleService.getScheduleById(id);
            int emailsSent = emailService.sendScheduleEmails(schedule);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Schedule emails sent successfully");
            response.put("scheduleName", schedule.getName());
            response.put("emailsSent", emailsSent);
            response.put("scheduleId", id);
            response.put("timestamp", LocalDate.now());

            return ResponseEntity.ok(response);
        });
    }

    @GetMapping("/{id}/slots")
    public ResponseEntity<List<ExamSlot>> getExamSlotsBySchedule(@PathVariable Long id) {
        return withErrorPrefix("Error retrieving exam slots for schedule: ",
            () -> ResponseEntity.ok(scheduleService.getScheduleById(id).getExamSlots()));
    }

    @PostMapping("/{id}/slots")
    public ResponseEntity<ExamSlot> addExamSlotToSchedule(
            @PathVariable Long id,
            @RequestBody ExamSlot examSlot) {
        return withErrorPrefix("Error adding exam slot to schedule: ",
            () -> ResponseEntity.status(HttpStatus.CREATED).body(scheduleService.addSlotToSchedule(id, examSlot)));
    }

    /**
     * Body: {@code {"assignRoom": true, "assignFaculty": true, "assignInvigilators": true}},
     * each option defaulting to {@code true}. Only resources a slot is missing are allocated.
     */
    @PostMapping("/{id}/allocate-all")
    public ResponseEntity<Map<String, Object>> allocateAllSlotsInSchedule(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> options) {
        return withErrorPrefixWrappingAll("Error allocating resources for schedule: ", () -> {
            ExamSchedule schedule = scheduleService.getScheduleById(id);

            int slotsUpdated = resourceAllocationService.allocateResourcesForSchedule(
                schedule, AllocationOptions.from(options));

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Resources allocated successfully");
            response.put("scheduleName", schedule.getName());
            response.put("slotsUpdated", slotsUpdated);
            response.put("totalSlots", schedule.getExamSlots().size());
            response.put("slots", schedule.getExamSlots());
            response.put("successfulAllocations", slotsUpdated);
            response.put("failedAllocations", schedule.getExamSlots().size() - slotsUpdated);

            return ResponseEntity.ok(response);
        });
    }
}
