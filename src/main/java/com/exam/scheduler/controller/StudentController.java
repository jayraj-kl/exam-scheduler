package com.exam.scheduler.controller;

import com.exam.scheduler.model.Student;
import com.exam.scheduler.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentRepository studentRepository;

    @GetMapping
    public ResponseEntity<List<Student>> getAllStudents() {
        return ResponseEntity.ok(studentRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found with id: " + id));
        return ResponseEntity.ok(student);
    }

    @GetMapping("/program/{programId}")
    public ResponseEntity<List<Student>> getStudentsByProgram(@PathVariable Long programId) {
        return ResponseEntity.ok(studentRepository.findByProgramId(programId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Student>> getStudentsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(studentRepository.findByStatus(status));
    }

    @GetMapping("/semester/{semester}")
    public ResponseEntity<List<Student>> getStudentsBySemester(@PathVariable int semester) {
        return ResponseEntity.ok(studentRepository.findBySemester(semester));
    }

    @PostMapping
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentRepository.save(student));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @RequestBody Student studentDetails) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found with id: " + id));
        
        student.setStudentId(studentDetails.getStudentId());
        student.setName(studentDetails.getName());
        student.setEmail(studentDetails.getEmail());
        student.setPhone(studentDetails.getPhone());
        student.setProgram(studentDetails.getProgram());
        student.setSemester(studentDetails.getSemester());
        student.setStatus(studentDetails.getStatus());
        student.setEnrollmentDate(studentDetails.getEnrollmentDate());
        student.setAddress(studentDetails.getAddress());
        
        return ResponseEntity.ok(studentRepository.save(student));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteStudent(@PathVariable Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found with id: " + id));
        
        studentRepository.delete(student);
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/stats/total")
    public ResponseEntity<Map<String, Object>> getTotalStudents() {
        Map<String, Object> response = new HashMap<>();
        long totalStudents = studentRepository.count();
        response.put("totalStudents", totalStudents);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/stats/by-program")
    public ResponseEntity<Map<String, Object>> getStudentsByProgramStats() {
        List<Student> students = studentRepository.findAll();
        
        Map<String, Long> studentsByProgram = students.stream()
            .filter(student -> student.getProgram() != null)
            .collect(Collectors.groupingBy(
                student -> student.getProgram().getName(),
                Collectors.counting()
            ));
            
        Map<String, Object> response = new HashMap<>();
        response.put("studentsByProgram", studentsByProgram);
        response.put("totalPrograms", studentsByProgram.size());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/stats/by-semester")
    public ResponseEntity<Map<String, Object>> getStudentsBySemesterStats() {
        List<Student> students = studentRepository.findAll();
        
        Map<Integer, Long> studentsBySemester = students.stream()
            .collect(Collectors.groupingBy(
                Student::getSemester,
                Collectors.counting()
            ));
            
        Map<String, Object> response = new HashMap<>();
        response.put("studentsBySemester", studentsBySemester);
        
        return ResponseEntity.ok(response);
    }
}