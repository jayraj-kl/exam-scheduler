package com.exam.scheduler.controller;


import com.exam.scheduler.model.ExamSchedule;
import com.exam.scheduler.model.ExamSlot;
import com.exam.scheduler.model.Faculty;
import com.exam.scheduler.repository.ExamScheduleRepository;
import com.exam.scheduler.repository.ExamSlotRepository;
import com.exam.scheduler.repository.FacultyRepository;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/schedule")
public class SchedulerController {

    @Autowired
    private SchedulerService schedulerService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private ExcelExporter excelExporter;
    
    @Autowired
    private ExamScheduleRepository examScheduleRepository;
    
    @Autowired
    private ExamSlotRepository examSlotRepository;
    
    @Autowired
    private FacultyRepository facultyRepository;
    
    @GetMapping
    public ResponseEntity<List<ExamSchedule>> getAllSchedules() {
        try {
            List<ExamSchedule> schedules = schedulerService.getAllSchedules();
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error retrieving schedules: " + e.getMessage());
        }
    }
    
    @PostMapping("/generate")
    public ResponseEntity<ExamSchedule> generateSchedule(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam String scheduleName,
            @RequestBody(required = false) Map<String, Object> requestBody) {
        
        if (endDate.isBefore(startDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date cannot be before start date");
        }
        
        List<String> programIds = requestBody != null ? (List<String>) requestBody.get("programIds") : null;
        Boolean includeWeekends = requestBody != null ? (Boolean) requestBody.get("includeWeekends") : false;
        
        ExamSchedule schedule = schedulerService.generateExamSchedule(startDate, endDate, scheduleName, programIds, includeWeekends);
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
    
    @GetMapping("/{id}/slots")
    public ResponseEntity<List<ExamSlot>> getExamSlotsBySchedule(@PathVariable Long id) {
        try {
            ExamSchedule schedule = schedulerService.getScheduleById(id);
            if (schedule == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found with id: " + id);
            }
            
            List<ExamSlot> examSlots = schedule.getExamSlots();
            return ResponseEntity.ok(examSlots);
        } catch (Exception e) {
            if (e instanceof ResponseStatusException) {
                throw e;
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error retrieving exam slots for schedule: " + e.getMessage());
        }
    }
    
    @PostMapping("/{id}/slots")
    public ResponseEntity<ExamSlot> addExamSlotToSchedule(
            @PathVariable Long id, 
            @RequestBody ExamSlot examSlot) {
        try {
            ExamSchedule schedule = schedulerService.getScheduleById(id);
            if (schedule == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found with id: " + id);
            }
            
            // Save the exam slot first
            examSlot = examSlotRepository.save(examSlot);
            
            // Add to schedule and save
            schedule.getExamSlots().add(examSlot);
            examScheduleRepository.save(schedule);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(examSlot);
        } catch (Exception e) {
            if (e instanceof ResponseStatusException) {
                throw e;
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error adding exam slot to schedule: " + e.getMessage());
        }
    }
    
    @PutMapping("/slots/{slotId}/faculty")
    public ResponseEntity<ExamSlot> assignFacultyToSlot(
            @PathVariable Long slotId,
            @RequestParam Long facultyId) {
        try {
            ExamSlot slot = examSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam slot not found with id: " + slotId));
            
            Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty not found with id: " + facultyId));
            
            slot.setFaculty(faculty);
            
            // Update faculty workload
            faculty.setCurrentWorkload(faculty.getCurrentWorkload() + 1);
            facultyRepository.save(faculty);
            
            examSlotRepository.save(slot);
            
            return ResponseEntity.ok(slot);
        } catch (Exception e) {
            if (e instanceof ResponseStatusException) {
                throw e;
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error assigning faculty to slot: " + e.getMessage());
        }
    }
    
    @PutMapping("/slots/{slotId}/examHead")
    public ResponseEntity<ExamSlot> assignExamHeadToSlot(
            @PathVariable Long slotId,
            @RequestParam Long facultyId) {
        try {
            ExamSlot slot = examSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam slot not found with id: " + slotId));
            
            Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty not found with id: " + facultyId));
            
            // Check if faculty is an exam head
            if (!faculty.isExamHead()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faculty is not an exam head");
            }
            
            slot.setExamHead(faculty);
            
            // Update faculty workload
            faculty.setCurrentWorkload(faculty.getCurrentWorkload() + 1);
            facultyRepository.save(faculty);
            
            examSlotRepository.save(slot);
            
            return ResponseEntity.ok(slot);
        } catch (Exception e) {
            if (e instanceof ResponseStatusException) {
                throw e;
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error assigning exam head to slot: " + e.getMessage());
        }
    }
    
    @PostMapping("/slots/{slotId}/invigilators")
    public ResponseEntity<ExamSlot> assignInvigilatorToSlot(
            @PathVariable Long slotId,
            @RequestParam Long facultyId) {
        try {
            ExamSlot slot = examSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam slot not found with id: " + slotId));
            
            Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty not found with id: " + facultyId));
            
            // Check if faculty is an invigilator
            if (!faculty.isInvigilator()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faculty is not an invigilator");
            }
            
            // Check if faculty is already assigned as an invigilator for this slot
            // First get the invigilators from database to ensure we have the latest data
            ExamSlot freshSlot = examSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam slot not found with id: " + slotId));
                
            boolean alreadyAssigned = freshSlot.getInvigilators().stream()
                .anyMatch(inv -> inv.getId().equals(faculty.getId()));
                
            if (alreadyAssigned) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT, 
                    "Faculty is already assigned as an invigilator for this slot"
                );
            }
            
            // Add invigilator to slot
            slot.getInvigilators().add(faculty);
            
            // Update faculty workload
            faculty.setCurrentWorkload(faculty.getCurrentWorkload() + 1);
            facultyRepository.save(faculty);
            
            examSlotRepository.save(slot);
            
            return ResponseEntity.ok(slot);
        } catch (Exception e) {
            if (e instanceof ResponseStatusException) {
                throw e;
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error assigning invigilator to slot: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/slots/{slotId}/invigilators/{facultyId}")
    public ResponseEntity<ExamSlot> removeInvigilatorFromSlot(
            @PathVariable Long slotId,
            @PathVariable Long facultyId) {
        try {
            ExamSlot slot = examSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam slot not found with id: " + slotId));
            
            Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty not found with id: " + facultyId));
            
            // Remove invigilator from slot
            boolean removed = slot.getInvigilators().removeIf(f -> f.getId().equals(facultyId));
            
            if (!removed) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faculty is not an invigilator for this slot");
            }
            
            // Update faculty workload
            faculty.setCurrentWorkload(Math.max(0, faculty.getCurrentWorkload() - 1));
            facultyRepository.save(faculty);
            
            examSlotRepository.save(slot);
            
            return ResponseEntity.ok(slot);
        } catch (Exception e) {
            if (e instanceof ResponseStatusException) {
                throw e;
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error removing invigilator from slot: " + e.getMessage());
        }
    }
    
    @GetMapping("/faculty/available")
    public ResponseEntity<List<Faculty>> getAvailableFaculty(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(pattern = "HH:mm:ss") LocalTime startTime,
            @RequestParam @DateTimeFormat(pattern = "HH:mm:ss") LocalTime endTime) {
        try {
            if (date == null || startTime == null || endTime == null) {
                throw new IllegalArgumentException("Date, start time, and end time are required");
            }

            if (date.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Cannot check availability for past dates");
            }

            if (endTime.isBefore(startTime)) {
                throw new IllegalArgumentException("End time cannot be before start time");
            }

            List<Faculty> availableFaculty = facultyRepository.findAvailableFacultiesForTimeSlot(date, startTime, endTime);
            
            if (availableFaculty.isEmpty()) {
                // Return 404 with informative message if no faculty is available
                throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, 
                    String.format("No faculty members available on %s between %s and %s", 
                        date, startTime, endTime)
                );
            }
            
            return ResponseEntity.ok(availableFaculty);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error retrieving available faculty: " + e.getMessage()
            );
        }
    }
    
    @PostMapping("/faculty/{facultyId}/availability")
    public ResponseEntity<Map<String, Object>> addFacultyAvailabilitySlot(
            @PathVariable Long facultyId,
            @RequestBody Map<String, Object> availabilityData) {
        try {
            Faculty faculty = facultyRepository.findById(facultyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty not found with id: " + facultyId));
            
            // Parse the date and time from request body
            LocalDate date = LocalDate.parse((String) availabilityData.get("date"));
            LocalTime startTime = LocalTime.parse((String) availabilityData.get("startTime"));
            LocalTime endTime = LocalTime.parse((String) availabilityData.get("endTime"));
            
            // Add availability to faculty
            schedulerService.addFacultyAvailability(faculty, date, startTime, endTime);
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", faculty.getId());
            response.put("faculty", faculty);
            response.put("date", date.toString());
            response.put("startTime", startTime.toString());
            response.put("endTime", endTime.toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            if (e instanceof ResponseStatusException) {
                throw e;
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error adding faculty availability slot: " + e.getMessage());
        }
    }
    
    @PutMapping("/slots/{slotId}/allocate")
    public ResponseEntity<ExamSlot> allocateResourcesForSlot(
            @PathVariable Long slotId,
            @RequestBody Map<String, Boolean> options) {
        try {
            ExamSlot examSlot = examSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam slot not found with id: " + slotId));
            
            // Get allocation options with defaults if not provided
            boolean assignFaculty = options.getOrDefault("assignFaculty", true);
            boolean assignRoom = options.getOrDefault("assignRoom", true);
            boolean assignInvigilators = options.getOrDefault("assignInvigilators", true);
            
            // Use the scheduler service to allocate resources based on options
            if (assignRoom) {
                schedulerService.allocateRoom(examSlot);
            }
            
            if (assignFaculty) {
                schedulerService.assignFaculty(examSlot);
            }
            
            if (assignInvigilators) {
                schedulerService.assignInvigilators(examSlot);
            }
            
            // Save the updated slot
            examSlot = examSlotRepository.save(examSlot);
            
            return ResponseEntity.ok(examSlot);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error allocating resources for exam slot: " + e.getMessage());
        }
    }
    
    @PostMapping("/{id}/allocate-all")
    public ResponseEntity<Map<String, Object>> allocateAllSlotsInSchedule(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> options) {
        try {
            ExamSchedule schedule = schedulerService.getScheduleById(id);
            if (schedule == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Schedule not found with id: " + id);
            }
            
            // Get allocation options with defaults if not provided
            boolean assignFaculty = options.getOrDefault("assignFaculty", true);
            boolean assignRoom = options.getOrDefault("assignRoom", true);
            boolean assignInvigilators = options.getOrDefault("assignInvigilators", true);
            
            int slotsUpdated = schedulerService.allocateResourcesForSchedule(
                id, assignRoom, assignFaculty, assignInvigilators);
            
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
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Error allocating resources for schedule: " + e.getMessage());
        }
    }
}