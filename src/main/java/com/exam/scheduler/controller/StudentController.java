package com.exam.scheduler.controller;

import com.exam.scheduler.model.Student;
import com.exam.scheduler.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @GetMapping("/program/{programId}")
    public ResponseEntity<List<Student>> getStudentsByProgram(@PathVariable Long programId) {
        return ResponseEntity.ok(studentService.getStudentsByProgram(programId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Student>> getStudentsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(studentService.getStudentsByStatus(status));
    }

    @GetMapping("/semester/{semester}")
    public ResponseEntity<List<Student>> getStudentsBySemester(@PathVariable int semester) {
        return ResponseEntity.ok(studentService.getStudentsBySemester(semester));
    }

    @PostMapping
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.createStudent(student));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @RequestBody Student studentDetails) {
        return ResponseEntity.ok(studentService.updateStudent(id, studentDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok(Map.of("deleted", Boolean.TRUE));
    }

    @GetMapping("/stats/total")
    public ResponseEntity<Map<String, Long>> getTotalStudents() {
        return ResponseEntity.ok(Map.of("totalStudents", studentService.countStudents()));
    }

    @GetMapping("/stats/by-program")
    public ResponseEntity<Map<String, Object>> getStudentsByProgramStats() {
        return ResponseEntity.ok(studentService.getProgramStatistics());
    }

    @GetMapping("/stats/by-semester")
    public ResponseEntity<Map<String, Object>> getStudentsBySemesterStats() {
        return ResponseEntity.ok(studentService.getSemesterStatistics());
    }
}
