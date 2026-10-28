package com.exam.scheduler.controller;

import com.exam.scheduler.dto.AllocationOptions;
import com.exam.scheduler.model.ExamSlot;
import com.exam.scheduler.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.exam.scheduler.controller.ControllerErrors.withErrorPrefix;
import static com.exam.scheduler.controller.ControllerErrors.withErrorPrefixWrappingAll;

/**
 * Staff assignment and resource allocation for individual exam slots.
 */
@RestController
@RequestMapping("/api/schedule/slots")
@RequiredArgsConstructor
public class ExamSlotController {

    private final ScheduleService scheduleService;

    @PutMapping("/{slotId}/faculty")
    public ResponseEntity<ExamSlot> assignFacultyToSlot(
            @PathVariable Long slotId,
            @RequestParam Long facultyId) {
        return withErrorPrefix("Error assigning faculty to slot: ",
            () -> ResponseEntity.ok(scheduleService.assignFacultyToSlot(slotId, facultyId)));
    }

    @PutMapping("/{slotId}/examHead")
    public ResponseEntity<ExamSlot> assignExamHeadToSlot(
            @PathVariable Long slotId,
            @RequestParam Long facultyId) {
        return withErrorPrefix("Error assigning exam head to slot: ",
            () -> ResponseEntity.ok(scheduleService.assignExamHeadToSlot(slotId, facultyId)));
    }

    @PostMapping("/{slotId}/invigilators")
    public ResponseEntity<ExamSlot> assignInvigilatorToSlot(
            @PathVariable Long slotId,
            @RequestParam Long facultyId) {
        return withErrorPrefix("Error assigning invigilator to slot: ",
            () -> ResponseEntity.ok(scheduleService.addInvigilatorToSlot(slotId, facultyId)));
    }

    @DeleteMapping("/{slotId}/invigilators/{facultyId}")
    public ResponseEntity<ExamSlot> removeInvigilatorFromSlot(
            @PathVariable Long slotId,
            @PathVariable Long facultyId) {
        return withErrorPrefix("Error removing invigilator from slot: ",
            () -> ResponseEntity.ok(scheduleService.removeInvigilatorFromSlot(slotId, facultyId)));
    }

    /**
     * Body: {@code {"assignRoom": true, "assignFaculty": true, "assignInvigilators": true}},
     * each option defaulting to {@code true}.
     */
    @PutMapping("/{slotId}/allocate")
    public ResponseEntity<ExamSlot> allocateResourcesForSlot(
            @PathVariable Long slotId,
            @RequestBody Map<String, Boolean> options) {
        return withErrorPrefixWrappingAll("Error allocating resources for exam slot: ", () -> {
            ExamSlot examSlot = scheduleService.getSlotById(slotId);
            return ResponseEntity.ok(
                scheduleService.allocateResourcesForSlot(examSlot, AllocationOptions.from(options)));
        });
    }
}
