package com.exam.scheduler.repository;

import com.exam.scheduler.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    
    List<Student> findByProgramId(Long programId);
    List<Student> findByStatus(String status);
    Student findByStudentId(String studentId);
    List<Student> findBySemester(int semester);
}