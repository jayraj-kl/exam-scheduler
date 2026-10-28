package com.exam.scheduler.service.scheduling;

import com.exam.scheduler.model.ExamSchedule;
import com.exam.scheduler.model.ExamSlot;
import com.exam.scheduler.model.Program;
import com.exam.scheduler.model.Subject;
import com.exam.scheduler.repository.ExamScheduleRepository;
import com.exam.scheduler.repository.ExamSlotRepository;
import com.exam.scheduler.repository.ProgramRepository;
import com.exam.scheduler.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.exam.scheduler.service.scheduling.SchedulingRules.AFTERNOON_SLOT_START;
import static com.exam.scheduler.service.scheduling.SchedulingRules.EXAM_DURATION_HOURS;
import static com.exam.scheduler.service.scheduling.SchedulingRules.MAX_STUDENTS_PER_SLOT;
import static com.exam.scheduler.service.scheduling.SchedulingRules.MORNING_SLOT_START;
import static com.exam.scheduler.service.scheduling.SchedulingRules.REPORTING_TIME_MINUTES;

/**
 * Generates an exam schedule: splits each subject's students into slots, places
 * the slots on morning/afternoon sessions across the date range and allocates
 * resources to every slot.
 */
@Service
@RequiredArgsConstructor
public class ScheduleGenerationService {

    private final SubjectRepository subjectRepository;
    private final ProgramRepository programRepository;
    private final ExamSlotRepository examSlotRepository;
    private final ExamScheduleRepository examScheduleRepository;
    private final ResourceAllocationService resourceAllocationService;

    /**
     * @param programIds numeric program ids or program codes (e.g. "cs") to restrict
     *                   the schedule to; {@code null} or empty means all subjects
     */
    @Transactional
    public ExamSchedule generateExamSchedule(LocalDate startDate, LocalDate endDate, String scheduleName,
                                            List<String> programIds, boolean includeWeekends) {
        ExamSchedule schedule = new ExamSchedule();
        schedule.setName(scheduleName);
        schedule.setStartDate(startDate);
        schedule.setEndDate(endDate);
        schedule.setExamSlots(new ArrayList<>());

        List<Subject> subjects = findSubjectsToSchedule(programIds);

        // Sort subjects by total students (descending) to prioritize larger groups
        subjects.sort(Comparator.comparing(Subject::getTotalStudents).reversed());

        LocalDate currentDate = startDate;
        boolean isMorningSlot = true;

        for (Subject subject : subjects) {
            int totalStudents = subject.getTotalStudents();
            int requiredSlots = calculateRequiredSlots(totalStudents);

            for (int i = 0; i < requiredSlots; i++) {
                // Skip weekends if not included
                if (!includeWeekends) {
                    while (currentDate.getDayOfWeek() == DayOfWeek.SATURDAY ||
                           currentDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                        currentDate = currentDate.plusDays(1);

                        // If we've passed the end date, circle back to start date
                        if (currentDate.isAfter(endDate)) {
                            currentDate = startDate;
                        }
                    }
                }

                ExamSlot slot = createExamSlot(subject, currentDate, isMorningSlot, i, requiredSlots);

                resourceAllocationService.allocateRoom(slot);
                resourceAllocationService.assignFaculty(slot);
                resourceAllocationService.assignInvigilators(slot);

                examSlotRepository.save(slot);
                schedule.getExamSlots().add(slot);

                // Move to next slot time
                if (isMorningSlot) {
                    isMorningSlot = false;
                } else {
                    isMorningSlot = true;
                    currentDate = currentDate.plusDays(1);

                    // If we've passed the end date, circle back to start date
                    if (currentDate.isAfter(endDate)) {
                        currentDate = startDate;
                    }
                }
            }
        }

        return examScheduleRepository.save(schedule);
    }

    private List<Subject> findSubjectsToSchedule(List<String> programIds) {
        if (programIds == null || programIds.isEmpty()) {
            return subjectRepository.findAll();
        }
        return subjectRepository.findByProgramIdIn(
            programIds.stream()
                .map(id -> {
                    try {
                        return Long.parseLong(id);
                    } catch (NumberFormatException e) {
                        // Handle non-numeric IDs (e.g., "cs", "it")
                        Program program = programRepository.findByCode(id);
                        return program != null ? program.getId() : null;
                    }
                })
                .filter(id -> id != null)
                .collect(Collectors.toList())
        );
    }

    private int calculateRequiredSlots(int totalStudents) {
        int fullSlots = totalStudents / MAX_STUDENTS_PER_SLOT;
        int remainingStudents = totalStudents % MAX_STUDENTS_PER_SLOT;

        // Any leftover students get a slot of their own, however few they are
        if (remainingStudents > 0) {
            return fullSlots + 1;
        }

        return Math.max(1, fullSlots); // At least one slot
    }

    private ExamSlot createExamSlot(Subject subject, LocalDate date, boolean isMorningSlot, int slotIndex, int totalSlots) {
        ExamSlot slot = new ExamSlot();
        slot.setExamDate(date);
        slot.setIsMorningSlot(isMorningSlot);

        LocalTime startTime = isMorningSlot ? MORNING_SLOT_START : AFTERNOON_SLOT_START;
        slot.setStartTime(startTime);
        slot.setEndTime(startTime.plusHours(EXAM_DURATION_HOURS));
        slot.setReportingTime(startTime.minusMinutes(REPORTING_TIME_MINUTES));

        slot.setSubject(subject);

        // Determine student counts for this slot
        int totalStudents = subject.getTotalStudents();
        int regularStudents = subject.getRegularStudents();
        int backlogStudents = subject.getBacklogStudents();

        int studentsPerSlot = Math.min(MAX_STUDENTS_PER_SLOT, totalStudents / totalSlots);

        if (slotIndex == totalSlots - 1) {
            // Last slot might have fewer students
            int remainingStudents = totalStudents - (studentsPerSlot * (totalSlots - 1));
            slot.setStudentCount(remainingStudents);

            // Distribute regular and backlog students proportionally for last slot
            if (totalStudents > 0) {
                int remainingRegular = regularStudents - (regularStudents / totalStudents * studentsPerSlot * (totalSlots - 1));
                int remainingBacklog = backlogStudents - (backlogStudents / totalStudents * studentsPerSlot * (totalSlots - 1));
                slot.setRegularStudentCount(remainingRegular);
                slot.setBacklogStudentCount(remainingBacklog);
            }
        } else {
            slot.setStudentCount(studentsPerSlot);

            // Distribute regular and backlog students proportionally
            if (totalStudents > 0) {
                slot.setRegularStudentCount(regularStudents * studentsPerSlot / totalStudents);
                slot.setBacklogStudentCount(backlogStudents * studentsPerSlot / totalStudents);
            }
        }

        if (slot.getBacklogStudentCount() == 0) {
            slot.setSlotType(ExamSlot.SlotType.REGULAR);
        } else if (slot.getRegularStudentCount() == 0) {
            slot.setSlotType(ExamSlot.SlotType.BACKLOG);
        } else {
            slot.setSlotType(ExamSlot.SlotType.MIXED);
        }

        if (slot.getInvigilators() == null) {
            slot.setInvigilators(new ArrayList<>());
        }

        return slot;
    }
}
