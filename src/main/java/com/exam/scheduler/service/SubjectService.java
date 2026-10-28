package com.exam.scheduler.service;

import com.exam.scheduler.model.Subject;
import com.exam.scheduler.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public List<Subject> getAllSubjects() {
        return subjectRepository.findAll();
    }

    public List<Subject> getSubjectsByProgram(Long programId) {
        return subjectRepository.findByProgramId(programId);
    }
}
