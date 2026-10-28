package com.exam.scheduler.service;

import com.exam.scheduler.dto.AllocationOptions;
import com.exam.scheduler.exception.ResourceNotFoundException;
import com.exam.scheduler.model.ExamSchedule;
import com.exam.scheduler.model.ExamSlot;
import com.exam.scheduler.model.Faculty;
import com.exam.scheduler.repository.ExamScheduleRepository;
import com.exam.scheduler.repository.ExamSlotRepository;
import com.exam.scheduler.service.scheduling.ResourceAllocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Reads exam schedules and manages the slots inside them, including manual staff
 * assignment. Automatic generation lives in
 * {@link com.exam.scheduler.service.scheduling.ScheduleGenerationService}.
 */
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ExamScheduleRepository examScheduleRepository;
    private final ExamSlotRepository examSlotRepository;
    private final FacultyService facultyService;
    private final ResourceAllocationService resourceAllocationService;

    public List<ExamSchedule> getAllSchedules() {
        return examScheduleRepository.findAll();
    }

    /**
     * Unlike other lookups, a missing schedule is a plain {@link RuntimeException},
     * which the schedule endpoints report as HTTP 500.
     */
    public ExamSchedule getScheduleById(Long id) {
        return examScheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found with id: " + id));
    }

    public ExamSlot addSlotToSchedule(Long scheduleId, ExamSlot examSlot) {
        ExamSchedule schedule = getScheduleById(scheduleId);

        ExamSlot savedSlot = examSlotRepository.save(examSlot);
        schedule.getExamSlots().add(savedSlot);
        examScheduleRepository.save(schedule);

        return savedSlot;
    }

    public ExamSlot getSlotById(Long slotId) {
        return examSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Exam slot", slotId));
    }

    public ExamSlot assignFacultyToSlot(Long slotId, Long facultyId) {
        ExamSlot slot = getSlotById(slotId);
        Faculty faculty = facultyService.getFacultyById(facultyId);

        slot.setFaculty(faculty);
        incrementWorkload(faculty);
        examSlotRepository.save(slot);

        return slot;
    }

    public ExamSlot assignExamHeadToSlot(Long slotId, Long facultyId) {
        ExamSlot slot = getSlotById(slotId);
        Faculty faculty = facultyService.getFacultyById(facultyId);

        if (!faculty.isExamHead()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faculty is not an exam head");
        }

        slot.setExamHead(faculty);
        incrementWorkload(faculty);
        examSlotRepository.save(slot);

        return slot;
    }

    public ExamSlot addInvigilatorToSlot(Long slotId, Long facultyId) {
        ExamSlot slot = getSlotById(slotId);
        Faculty faculty = facultyService.getFacultyById(facultyId);

        if (!faculty.isInvigilator()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faculty is not an invigilator");
        }

        boolean alreadyAssigned = slot.getInvigilators().stream()
            .anyMatch(inv -> inv.getId().equals(faculty.getId()));
        if (alreadyAssigned) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Faculty is already assigned as an invigilator for this slot"
            );
        }

        slot.getInvigilators().add(faculty);
        incrementWorkload(faculty);
        examSlotRepository.save(slot);

        return slot;
    }

    public ExamSlot removeInvigilatorFromSlot(Long slotId, Long facultyId) {
        ExamSlot slot = getSlotById(slotId);
        Faculty faculty = facultyService.getFacultyById(facultyId);

        boolean removed = slot.getInvigilators().removeIf(f -> f.getId().equals(facultyId));
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faculty is not an invigilator for this slot");
        }

        faculty.setCurrentWorkload(Math.max(0, faculty.getCurrentWorkload() - 1));
        facultyService.save(faculty);
        examSlotRepository.save(slot);

        return slot;
    }

    /**
     * (Re)allocates the selected resources for a single slot. Each allocation step
     * runs in its own transaction.
     */
    public ExamSlot allocateResourcesForSlot(ExamSlot examSlot, AllocationOptions options) {
        if (options.assignRoom()) {
            resourceAllocationService.allocateRoom(examSlot);
        }

        if (options.assignFaculty()) {
            resourceAllocationService.assignFaculty(examSlot);
        }

        if (options.assignInvigilators()) {
            resourceAllocationService.assignInvigilators(examSlot);
        }

        return examSlotRepository.save(examSlot);
    }

    private void incrementWorkload(Faculty faculty) {
        faculty.setCurrentWorkload(faculty.getCurrentWorkload() + 1);
        facultyService.save(faculty);
    }
}
