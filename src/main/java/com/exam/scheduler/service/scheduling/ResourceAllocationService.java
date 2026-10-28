package com.exam.scheduler.service.scheduling;

import com.exam.scheduler.dto.AllocationOptions;
import com.exam.scheduler.model.ExamSchedule;
import com.exam.scheduler.model.ExamSlot;
import com.exam.scheduler.model.Faculty;
import com.exam.scheduler.model.Room;
import com.exam.scheduler.repository.ExamSlotRepository;
import com.exam.scheduler.repository.FacultyRepository;
import com.exam.scheduler.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Assigns rooms, faculty, exam heads and invigilators to exam slots.
 *
 * <p>Each public allocation method runs in its own transaction when called from
 * another bean. {@link #allocateResourcesForSchedule} intentionally calls them on
 * {@code this}, so a failure on one slot is caught without marking the whole
 * batch transaction rollback-only.
 */
@Service
@RequiredArgsConstructor
public class ResourceAllocationService {

    private final RoomRepository roomRepository;
    private final FacultyRepository facultyRepository;
    private final ExamSlotRepository examSlotRepository;

    @Transactional
    public void allocateRoom(ExamSlot slot) {
        // Find available rooms with sufficient capacity for this specific time slot
        List<Room> availableRooms = roomRepository.findAvailableRoomsWithCapacity(
            slot.getStudentCount(),
            slot.getExamDate(),
            slot.isMorningSlot()
        );

        Room selectedRoom = null;
        if (!availableRooms.isEmpty()) {
            // Find the room with capacity closest to required
            selectedRoom = availableRooms.stream()
                    .min(Comparator.comparingInt(room ->
                        Math.abs(room.getSeatingCapacity() - slot.getStudentCount())))
                    .orElse(availableRooms.get(0));
        }

        if (selectedRoom != null) {
            // Set the new room
            slot.setRoom(selectedRoom);
            if (selectedRoom.getExamSlots() == null) {
                selectedRoom.setExamSlots(new ArrayList<>());
            }
            selectedRoom.getExamSlots().add(slot);
            roomRepository.save(selectedRoom);
        } else {
            throw new RuntimeException("No suitable room available for slot with " + slot.getStudentCount() +
                " students on " + slot.getExamDate() + " " +
                (slot.isMorningSlot() ? "morning" : "afternoon"));
        }
    }

    @Transactional
    public void assignFaculty(ExamSlot slot) {
        LocalDateTime slotDateTime = LocalDateTime.of(slot.getExamDate(), slot.getStartTime());

        // First try to find faculty available specifically for this time slot
        List<Faculty> availableFaculties = facultyRepository.findAvailableFacultiesForTimeSlot(
            slot.getExamDate(), slot.getStartTime(), slot.getEndTime());

        if (availableFaculties.isEmpty()) {
            // Fallback to finding faculty who might be available (no conflicting assignments)
            availableFaculties = facultyRepository.findAvailableFacultiesForSlot(
                slotDateTime, slot.getExamDate(), slot.getStartTime());
        }

        if (!availableFaculties.isEmpty()) {
            Faculty selectedFaculty = availableFaculties.get(0);
            slot.setFaculty(selectedFaculty);

            // Update faculty workload
            selectedFaculty.setCurrentWorkload(selectedFaculty.getCurrentWorkload() + 1);
            facultyRepository.save(selectedFaculty);
        } else {
            // If still no faculty available, try to find anyone under their workload capacity
            List<Faculty> anyFaculty = facultyRepository.findAvailableFacultiesOrderByWorkload();
            if (!anyFaculty.isEmpty()) {
                Faculty selectedFaculty = anyFaculty.get(0);

                // Double check this faculty doesn't have a direct time conflict
                boolean hasConflict = selectedFaculty.getAssignedSlots().stream()
                    .anyMatch(s -> s.getExamDate().equals(slot.getExamDate()) &&
                             ((s.getStartTime().equals(slot.getStartTime()) ||
                               (s.getStartTime().isBefore(slot.getEndTime()) &&
                                s.getEndTime().isAfter(slot.getStartTime())))));

                if (!hasConflict) {
                    slot.setFaculty(selectedFaculty);
                    selectedFaculty.setCurrentWorkload(selectedFaculty.getCurrentWorkload() + 1);
                    facultyRepository.save(selectedFaculty);
                    return;
                }
            }

            throw new RuntimeException("No available faculty for slot on " + slot.getExamDate() +
                " at " + slot.getStartTime() + ". Please check faculty workload capacities and availability.");
        }
    }

    @Transactional
    public void assignInvigilators(ExamSlot slot) {
        // Assign exam head
        List<Faculty> examHeads = facultyRepository.findByIsExamHeadTrue();
        if (examHeads.isEmpty()) {
            throw new RuntimeException("No exam heads available in the system");
        }

        // Sort by workload to distribute evenly
        examHeads.sort(Comparator.comparing(Faculty::getCurrentWorkload));

        Faculty examHead = examHeads.get(0);
        slot.setExamHead(examHead);

        // Update exam head workload
        examHead.setCurrentWorkload(examHead.getCurrentWorkload() + 1);
        facultyRepository.save(examHead);

        // Assign invigilators based on student count, minimum 1
        int requiredInvigilators = Math.max(1, slot.getStudentCount() / SchedulingRules.STUDENTS_PER_INVIGILATOR);
        List<Faculty> invigilators = facultyRepository.findByIsInvigilatorTrue();

        if (invigilators.isEmpty()) {
            throw new RuntimeException("No invigilators available in the system");
        }

        // Sort by current workload to distribute evenly
        invigilators.sort(Comparator.comparing(Faculty::getCurrentWorkload));

        // Remove the exam head from potential invigilators to avoid duplication
        invigilators.removeIf(f -> f.getId().equals(examHead.getId()));

        // Get the faculty assigned to this slot to avoid duplication
        if (slot.getFaculty() != null) {
            invigilators.removeIf(f -> f.getId().equals(slot.getFaculty().getId()));
        }

        // Create a set of invigilator IDs already assigned to this slot
        List<Long> existingInvigilatorIds = slot.getInvigilators().stream()
            .map(Faculty::getId)
            .collect(Collectors.toList());

        // Remove invigilators already assigned to this slot
        invigilators.removeIf(f -> existingInvigilatorIds.contains(f.getId()));

        // Add required number of invigilators only if not already assigned
        for (int i = 0; i < Math.min(requiredInvigilators, invigilators.size()); i++) {
            Faculty invigilator = invigilators.get(i);

            // Check if invigilator is already in the list (extra safety check)
            boolean alreadyAssigned = slot.getInvigilators().stream()
                .anyMatch(inv -> inv.getId().equals(invigilator.getId()));

            if (!alreadyAssigned) {
                slot.getInvigilators().add(invigilator);

                // Update invigilator workload
                invigilator.setCurrentWorkload(invigilator.getCurrentWorkload() + 1);
                facultyRepository.save(invigilator);
            }
        }
    }

    /**
     * Fills in missing resources for every slot of {@code schedule}, skipping (and
     * logging) slots that cannot be allocated.
     *
     * @return the number of slots that were updated
     */
    @Transactional
    public int allocateResourcesForSchedule(ExamSchedule schedule, AllocationOptions options) {
        int slotsUpdated = 0;

        for (ExamSlot slot : schedule.getExamSlots()) {
            try {
                boolean updated = false;

                if (options.assignRoom() && slot.getRoom() == null) {
                    allocateRoom(slot);
                    updated = true;
                }

                if (options.assignFaculty() && slot.getFaculty() == null) {
                    assignFaculty(slot);
                    updated = true;
                }

                if (options.assignInvigilators() && (slot.getExamHead() == null || slot.getInvigilators().isEmpty())) {
                    assignInvigilators(slot);
                    updated = true;
                }

                if (updated) {
                    examSlotRepository.save(slot);
                    slotsUpdated++;
                }
            } catch (Exception e) {
                // Log the error but continue with other slots
                System.err.println("Error allocating resources for slot " + slot.getId() + ": " + e.getMessage());
            }
        }

        return slotsUpdated;
    }

    /**
     * Moves a slot to another available faculty member when {@code oldFaculty}
     * becomes unavailable, adjusting both workloads.
     */
    @Transactional
    public Faculty reassignFacultyForSlot(ExamSlot slot, Faculty oldFaculty) {
        LocalDateTime slotDateTime = LocalDateTime.of(slot.getExamDate(), slot.getStartTime());

        // Find available faculty with lowest workload, excluding the old faculty
        List<Faculty> availableFaculties = facultyRepository.findAvailableFacultiesForSlot(
            slotDateTime,
            slot.getExamDate(),
            slot.getStartTime()
        );
        availableFaculties.removeIf(f -> f.getId().equals(oldFaculty.getId()));

        if (!availableFaculties.isEmpty()) {
            Faculty newFaculty = availableFaculties.get(0);

            oldFaculty.setCurrentWorkload(Math.max(0, oldFaculty.getCurrentWorkload() - 1));
            facultyRepository.save(oldFaculty);

            newFaculty.setCurrentWorkload(newFaculty.getCurrentWorkload() + 1);
            facultyRepository.save(newFaculty);

            return newFaculty;
        }

        throw new RuntimeException("No available faculty for reassignment for slot on " + slot.getExamDate() + " at " + slot.getStartTime());
    }
}
