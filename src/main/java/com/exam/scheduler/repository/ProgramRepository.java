package com.exam.scheduler.repository;

import com.exam.scheduler.model.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProgramRepository extends JpaRepository<Program, Long> {
    Program findByCode(String code);
}
