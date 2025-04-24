package com.exam.scheduler.service;

import com.exam.scheduler.model.*;
import com.exam.scheduler.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SchedulerService {

    private static final int MIN_STUDENTS_PER_SLOT = 30;
    private static final int MAX_STUDENTS_PER_SLOT = 35;
    private static final LocalTime MORNING_SLOT_START = LocalTime.of(9, 0);
    private static final LocalTime EVENING_SLOT_START = LocalTime.of(14, 0);
    private static final int EXAM_DURATION_HOURS = 3;
    private static final int REPORTING_TIME_MINUTES = 30;

    @Autowired
    private FacultyRepository facultyRepository;
    
    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired
    private SubjectRepository subjectRepository;
    
    @Autowired
    private ExamSlotRepository examSlotRepository;
    
    @Autowired
    private ExamScheduleRepository examScheduleRepository;
    
    @Autowired
    private ProgramRepository programRepository;
    
    public List<ExamSchedule> getAllSchedules() {
        return examScheduleRepository.findAll();
    }
    
    @Transactional
    public ExamSchedule generateExamSchedule(LocalDate startDate, LocalDate endDate, String scheduleName) {
        return generateExamSchedule(startDate, endDate, scheduleName, null, false);
    }
    
    @Transactional
    public ExamSchedule generateExamSchedule(LocalDate startDate, LocalDate endDate, String scheduleName, 
                                            List<String> programIds, Boolean includeWeekends) {
        ExamSchedule schedule = new ExamSchedule();
        schedule.setName(scheduleName);
        schedule.setStartDate(startDate);
        schedule.setEndDate(endDate);
        schedule.setExamSlots(new ArrayList<>());
        
        // Get all subjects that need to be scheduled
        List<Subject> subjects;
        
        // Filter subjects by program if programIds are provided
        if (programIds != null && !programIds.isEmpty()) {
            subjects = subjectRepository.findByProgramIdIn(
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
        } else {
            subjects = subjectRepository.findAll();
        }
        
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
                
                // Create exam slot
                ExamSlot slot = createExamSlot(subject, currentDate, isMorningSlot, i, requiredSlots);
                
                // Allocate room
                allocateRoom(slot);
                
                // Assign faculty
                assignFaculty(slot);
                
                // Assign exam head and invigilators
                assignInvigilators(slot);
                
                // Save the slot
                examSlotRepository.save(slot);
                schedule.getExamSlots().add(slot);
                
                // Move to next slot time
                if (isMorningSlot) {
                    isMorningSlot = false;
                } else {
                    isMorningSlot = true;
                    currentDate = currentDate.plusDays(1);
                    
                    // Skip to next date if we've passed the end date
                    if (currentDate.isAfter(endDate)) {
                        currentDate = startDate;
                    }
                }
            }
        }
        
        return examScheduleRepository.save(schedule);
    }
    
    private int calculateRequiredSlots(int totalStudents) {
        int fullSlots = totalStudents / MAX_STUDENTS_PER_SLOT;
        int remainingStudents = totalStudents % MAX_STUDENTS_PER_SLOT;
        
        if (remainingStudents >= MIN_STUDENTS_PER_SLOT) {
            return fullSlots + 1;
        } else if (remainingStudents > 0) {
            // If remaining students are less than minimum, we need to redistribute
            return fullSlots + 1;
        }
        
        return Math.max(1, fullSlots); // At least one slot
    }
    
    private ExamSlot createExamSlot(Subject subject, LocalDate date, boolean isMorningSlot, int slotIndex, int totalSlots) {
        ExamSlot slot = new ExamSlot();
        slot.setExamDate(date);
        slot.setIsMorningSlot(isMorningSlot);
        
        // Set start and end times
        LocalTime startTime = isMorningSlot ? MORNING_SLOT_START : EVENING_SLOT_START;
        slot.setStartTime(startTime);
        slot.setEndTime(startTime.plusHours(EXAM_DURATION_HOURS));
        slot.setReportingTime(startTime.minusMinutes(REPORTING_TIME_MINUTES));
        
        // Set subject
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
        
        // Set slot type
        if (slot.getBacklogStudentCount() == 0) {
            slot.setSlotType(ExamSlot.SlotType.REGULAR);
        } else if (slot.getRegularStudentCount() == 0) {
            slot.setSlotType(ExamSlot.SlotType.BACKLOG);
        } else {
            slot.setSlotType(ExamSlot.SlotType.MIXED);
        }
        
        // Initialize invigilators list
        if (slot.getInvigilators() == null) {
            slot.setInvigilators(new ArrayList<>());
        }
        
        return slot;
    }
    
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
        
        // Assign invigilators based on student count
        // Rule: 1 invigilator for every 20 students, minimum 1
        int requiredInvigilators = Math.max(1, slot.getStudentCount() / 20);
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
    
    @Transactional
    public int allocateResourcesForSchedule(Long scheduleId) {
        ExamSchedule schedule = getScheduleById(scheduleId);
        int slotsUpdated = 0;
        
        for (ExamSlot slot : schedule.getExamSlots()) {
            try {
                // Skip slots that already have all resources allocated
                if (slot.getRoom() != null && slot.getFaculty() != null && 
                    slot.getExamHead() != null && !slot.getInvigilators().isEmpty()) {
                    continue;
                }
                
                // Allocate resources for this slot
                if (slot.getRoom() == null) {
                    allocateRoom(slot);
                }
                
                if (slot.getFaculty() == null) {
                    assignFaculty(slot);
                }
                
                if (slot.getExamHead() == null || slot.getInvigilators().isEmpty()) {
                    assignInvigilators(slot);
                }
                
                examSlotRepository.save(slot);
                slotsUpdated++;
            } catch (Exception e) {
                // Log the error but continue with other slots
                System.err.println("Error allocating resources for slot " + slot.getId() + ": " + e.getMessage());
            }
        }
        
        return slotsUpdated;
    }

    @Transactional
    public int allocateResourcesForSchedule(Long scheduleId, boolean assignRoom, boolean assignFaculty, boolean assignInvigilators) {
        ExamSchedule schedule = getScheduleById(scheduleId);
        int slotsUpdated = 0;
        
        for (ExamSlot slot : schedule.getExamSlots()) {
            try {
                boolean updated = false;
                
                // Allocate resources for this slot based on parameters
                if (assignRoom && slot.getRoom() == null) {
                    allocateRoom(slot);
                    updated = true;
                }
                
                if (assignFaculty && slot.getFaculty() == null) {
                    assignFaculty(slot);
                    updated = true;
                }
                
                if (assignInvigilators && (slot.getExamHead() == null || slot.getInvigilators().isEmpty())) {
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
    
    @Transactional
    public void addFacultyAvailability(Faculty faculty, LocalDate date, LocalTime startTime, LocalTime endTime) {
        // Check if faculty exists
        if (faculty == null || faculty.getId() == null) {
            throw new RuntimeException("Invalid faculty");
        }
        
        // Add availability slots to faculty
        List<LocalDateTime> availabilitySlots = new ArrayList<>();
        
        // Generate a slot for each hour within the range
        LocalDateTime currentSlot = LocalDateTime.of(date, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(date, endTime);
        
        while (currentSlot.isBefore(endDateTime)) {
            availabilitySlots.add(currentSlot);
            currentSlot = currentSlot.plusHours(1);
        }
        
        // If the faculty's availability slots list is null, initialize it
        if (faculty.getAvailabilitySlots() == null) {
            faculty.setAvailabilitySlots(new ArrayList<>());
        }
        
        // Add all new slots
        faculty.getAvailabilitySlots().addAll(availabilitySlots);
        
        // Update the faculty
        facultyRepository.save(faculty);
    }
    
    // Method to reassign faculty if they become unavailable
    @Transactional
    public Faculty reassignFacultyForSlot(ExamSlot slot, Faculty oldFaculty) {
        // Convert slot time to LocalDateTime for availability check
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
            
            // Update old faculty workload
            oldFaculty.setCurrentWorkload(Math.max(0, oldFaculty.getCurrentWorkload() - 1));
            facultyRepository.save(oldFaculty);
            
            // Update new faculty workload
            newFaculty.setCurrentWorkload(newFaculty.getCurrentWorkload() + 1);
            facultyRepository.save(newFaculty);
            
            return newFaculty;
        }
        
        throw new RuntimeException("No available faculty for reassignment for slot on " + slot.getExamDate() + " at " + slot.getStartTime());
    }

    public ExamSchedule getScheduleById(Long id) {
        return examScheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found with id: " + id));
    }
}