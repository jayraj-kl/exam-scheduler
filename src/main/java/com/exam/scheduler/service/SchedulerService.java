package com.exam.scheduler.service;

import com.exam.scheduler.model.*;
import com.exam.scheduler.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    
    @Transactional
    public ExamSchedule generateExamSchedule(LocalDate startDate, LocalDate endDate, String scheduleName) {
        ExamSchedule schedule = new ExamSchedule();
        schedule.setName(scheduleName);
        schedule.setStartDate(startDate);
        schedule.setEndDate(endDate);
        schedule.setExamSlots(new ArrayList<>());
        
        // Get all subjects that need to be scheduled
        List<Subject> subjects = subjectRepository.findAll();
        
        // Sort subjects by total students (descending) to prioritize larger groups
        subjects.sort(Comparator.comparing(Subject::getTotalStudents).reversed());
        
        LocalDate currentDate = startDate;
        boolean isMorningSlot = true;
        
        for (Subject subject : subjects) {
            int totalStudents = subject.getTotalStudents();
            int requiredSlots = calculateRequiredSlots(totalStudents);
            
            for (int i = 0; i < requiredSlots; i++) {
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
    
    /**
     * Retrieves an exam schedule by its ID
     * 
     * @param id the schedule ID
     * @return the exam schedule, or null if not found
     */
    public ExamSchedule getScheduleById(Long id) {
        return examScheduleRepository.findById(id).orElse(null);
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
        
        return slot;
    }
    
    private void allocateRoom(ExamSlot slot) {
        // Find a suitable room based on student count
        List<Room> availableRooms = roomRepository.findAvailableRoomsWithCapacity(slot.getStudentCount());
        
        if (!availableRooms.isEmpty()) {
            // Find the room with capacity closest to required
            Room selectedRoom = availableRooms.stream()
                    .min(Comparator.comparingInt(room -> 
                        Math.abs(room.getSeatingCapacity() - slot.getStudentCount())))
                    .orElse(availableRooms.get(0));
            
            slot.setRoom(selectedRoom);
            selectedRoom.setCurrentBooking(slot);
            selectedRoom.setIsAvailable(false);
            roomRepository.save(selectedRoom);
        } else {
            throw new RuntimeException("No suitable room available for slot with " + slot.getStudentCount() + " students");
        }
    }
    
    private void assignFaculty(ExamSlot slot) {
        // Convert slot time to LocalDateTime for availability check
        LocalDateTime slotDateTime = LocalDateTime.of(slot.getExamDate(), slot.getStartTime());
        
        // Find available faculty with lowest workload
        List<Faculty> availableFaculties = facultyRepository.findAvailableFacultiesForSlot(slotDateTime);
        
        if (!availableFaculties.isEmpty()) {
            Faculty selectedFaculty = availableFaculties.get(0);
            slot.setFaculty(selectedFaculty);
            
            // Update faculty workload
            selectedFaculty.setCurrentWorkload(selectedFaculty.getCurrentWorkload() + 1);
            selectedFaculty.getAssignedSlots().add(slot);
            facultyRepository.save(selectedFaculty);
        } else {
            throw new RuntimeException("No available faculty for slot on " + slot.getExamDate() + " at " + slot.getStartTime());
        }
    }
    
    private void assignInvigilators(ExamSlot slot) {
        // Assign exam head
        List<Faculty> examHeads = facultyRepository.findByIsExamHeadTrue();
        examHeads.sort(Comparator.comparing(Faculty::getCurrentWorkload));
        
        if (!examHeads.isEmpty()) {
            Faculty examHead = examHeads.get(0);
            slot.setExamHead(examHead);
            
            // Update exam head workload
            examHead.setCurrentWorkload(examHead.getCurrentWorkload() + 1);
            facultyRepository.save(examHead);
        }
        
        // Assign invigilators based on student count
        int requiredInvigilators = Math.max(1, slot.getStudentCount() / 20); // 1 invigilator per 20 students
        List<Faculty> invigilators = facultyRepository.findByIsInvigilatorTrue();
        invigilators.sort(Comparator.comparing(Faculty::getCurrentWorkload));
        
        List<Faculty> selectedInvigilators = new ArrayList<>();
        for (int i = 0; i < Math.min(requiredInvigilators, invigilators.size()); i++) {
            Faculty invigilator = invigilators.get(i);
            selectedInvigilators.add(invigilator);
            
            // Update invigilator workload
            invigilator.setCurrentWorkload(invigilator.getCurrentWorkload() + 1);
            facultyRepository.save(invigilator);
        }
        
        slot.setInvigilators(selectedInvigilators);
    }
}