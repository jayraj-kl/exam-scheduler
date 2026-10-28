package com.exam.scheduler.controller;

import com.exam.scheduler.model.Faculty;
import com.exam.scheduler.service.FacultyService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.exam.scheduler.controller.ControllerErrors.withErrorPrefix;

/**
 * Faculty availability, used while scheduling exams.
 */
@RestController
@RequestMapping("/api/schedule/faculty")
@RequiredArgsConstructor
public class FacultyController {

    private final FacultyService facultyService;

    /**
     * Note: when nobody is available the 404 raised here is reported as a 500,
     * like any other unexpected error from this endpoint.
     */
    @GetMapping("/available")
    public ResponseEntity<List<Faculty>> getAvailableFaculty(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam @DateTimeFormat(pattern = "HH:mm:ss") LocalTime startTime,
            @RequestParam @DateTimeFormat(pattern = "HH:mm:ss") LocalTime endTime) {
        try {
            List<Faculty> availableFaculty = facultyService.findAvailableFaculty(date, startTime, endTime);

            if (availableFaculty.isEmpty()) {
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

    /**
     * Body: {@code {"date": "2030-01-07", "startTime": "09:00", "endTime": "12:00"}}.
     */
    @PostMapping("/{facultyId}/availability")
    public ResponseEntity<Map<String, Object>> addFacultyAvailabilitySlot(
            @PathVariable Long facultyId,
            @RequestBody Map<String, Object> availabilityData) {
        return withErrorPrefix("Error adding faculty availability slot: ", () -> {
            Faculty faculty = facultyService.getFacultyById(facultyId);

            LocalDate date = LocalDate.parse((String) availabilityData.get("date"));
            LocalTime startTime = LocalTime.parse((String) availabilityData.get("startTime"));
            LocalTime endTime = LocalTime.parse((String) availabilityData.get("endTime"));

            facultyService.addFacultyAvailability(faculty, date, startTime, endTime);

            Map<String, Object> response = new HashMap<>();
            response.put("id", faculty.getId());
            response.put("faculty", faculty);
            response.put("date", date.toString());
            response.put("startTime", startTime.toString());
            response.put("endTime", endTime.toString());

            return ResponseEntity.ok(response);
        });
    }
}
