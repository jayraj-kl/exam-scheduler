package com.exam.scheduler.service;

import com.exam.scheduler.exception.ResourceNotFoundException;
import com.exam.scheduler.model.Faculty;
import com.exam.scheduler.repository.FacultyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FacultyService {

    private final FacultyRepository facultyRepository;

    public List<Faculty> getAllFaculty() {
        return facultyRepository.findAll();
    }

    public Faculty getFacultyById(Long id) {
        return facultyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", id));
    }

    public Faculty save(Faculty faculty) {
        return facultyRepository.save(faculty);
    }

    /**
     * Faculty under their workload capacity with no exam starting inside the window.
     *
     * @throws IllegalArgumentException if the window is incomplete, in the past or inverted
     */
    public List<Faculty> findAvailableFaculty(LocalDate date, LocalTime startTime, LocalTime endTime) {
        if (date == null || startTime == null || endTime == null) {
            throw new IllegalArgumentException("Date, start time, and end time are required");
        }

        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Cannot check availability for past dates");
        }

        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }

        return facultyRepository.findAvailableFacultiesForTimeSlot(date, startTime, endTime);
    }

    /**
     * Records the faculty member as available for each whole hour starting in
     * {@code [startTime, endTime)} on {@code date}.
     */
    @Transactional
    public void addFacultyAvailability(Faculty faculty, LocalDate date, LocalTime startTime, LocalTime endTime) {
        if (faculty == null || faculty.getId() == null) {
            throw new RuntimeException("Invalid faculty");
        }

        List<LocalDateTime> availabilitySlots = new ArrayList<>();
        LocalDateTime currentSlot = LocalDateTime.of(date, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(date, endTime);

        while (currentSlot.isBefore(endDateTime)) {
            availabilitySlots.add(currentSlot);
            currentSlot = currentSlot.plusHours(1);
        }

        if (faculty.getAvailabilitySlots() == null) {
            faculty.setAvailabilitySlots(new ArrayList<>());
        }
        faculty.getAvailabilitySlots().addAll(availabilitySlots);

        facultyRepository.save(faculty);
    }
}
