package com.exam.scheduler.service;

import com.exam.scheduler.exception.ResourceNotFoundException;
import com.exam.scheduler.model.Student;
import com.exam.scheduler.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student", id));
    }

    public List<Student> getStudentsByProgram(Long programId) {
        return studentRepository.findByProgramId(programId);
    }

    public List<Student> getStudentsByStatus(String status) {
        return studentRepository.findByStatus(status);
    }

    public List<Student> getStudentsBySemester(int semester) {
        return studentRepository.findBySemester(semester);
    }

    public Student createStudent(Student student) {
        return studentRepository.save(student);
    }

    public Student updateStudent(Long id, Student studentDetails) {
        Student student = getStudentById(id);

        student.setStudentId(studentDetails.getStudentId());
        student.setName(studentDetails.getName());
        student.setEmail(studentDetails.getEmail());
        student.setPhone(studentDetails.getPhone());
        student.setProgram(studentDetails.getProgram());
        student.setSemester(studentDetails.getSemester());
        student.setStatus(studentDetails.getStatus());
        student.setEnrollmentDate(studentDetails.getEnrollmentDate());
        student.setAddress(studentDetails.getAddress());

        return studentRepository.save(student);
    }

    public void deleteStudent(Long id) {
        studentRepository.delete(getStudentById(id));
    }

    public long countStudents() {
        return studentRepository.count();
    }

    /**
     * Student counts per program name; students without a program are left out.
     */
    public Map<String, Object> getProgramStatistics() {
        Map<String, Long> studentsByProgram = studentRepository.findAll().stream()
            .filter(student -> student.getProgram() != null)
            .collect(Collectors.groupingBy(
                student -> student.getProgram().getName(),
                Collectors.counting()
            ));

        Map<String, Object> stats = new HashMap<>();
        stats.put("studentsByProgram", studentsByProgram);
        stats.put("totalPrograms", studentsByProgram.size());
        return stats;
    }

    public Map<String, Object> getSemesterStatistics() {
        Map<Integer, Long> studentsBySemester = studentRepository.findAll().stream()
            .collect(Collectors.groupingBy(
                Student::getSemester,
                Collectors.counting()
            ));

        Map<String, Object> stats = new HashMap<>();
        stats.put("studentsBySemester", studentsBySemester);
        return stats;
    }
}
