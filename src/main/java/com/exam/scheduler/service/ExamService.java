package com.exam.scheduler.service;

import com.exam.scheduler.exception.ResourceNotFoundException;
import com.exam.scheduler.model.Exam;
import com.exam.scheduler.model.Subject;
import com.exam.scheduler.repository.ExamRepository;
import com.exam.scheduler.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository examRepository;
    private final SubjectRepository subjectRepository;

    public List<Exam> getAllExams() {
        return examRepository.findAll();
    }

    public Exam getExamById(Long id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exam", id));
    }

    public List<Exam> getExamsByDate(LocalDate date) {
        return examRepository.findByExamDate(date);
    }

    public List<Exam> getExamsByStatus(String status) {
        return examRepository.findByStatus(status);
    }

    public List<Exam> searchExamsBySubject(String query) {
        return examRepository.findBySubjectContaining(query);
    }

    public Exam createExam(Exam exam) {
        // If subjectEntity ID is provided but not fully loaded, fetch the complete subject
        if (exam.getSubjectEntity() != null && exam.getSubjectEntity().getId() != null) {
            Subject subject = getSubjectById(exam.getSubjectEntity().getId());
            exam.setSubjectEntity(subject);

            // Keep the legacy subject name field populated
            if (exam.getSubject() == null || exam.getSubject().isEmpty()) {
                exam.setSubject(subject.getName());
            }
        }
        return examRepository.save(exam);
    }

    public Exam updateExam(Long id, Exam examDetails) {
        Exam exam = getExamById(id);

        exam.setExamName(examDetails.getExamName());
        exam.setSubject(examDetails.getSubject());

        if (examDetails.getSubjectEntity() != null && examDetails.getSubjectEntity().getId() != null) {
            Subject subject = getSubjectById(examDetails.getSubjectEntity().getId());
            exam.setSubjectEntity(subject);

            // Keep the legacy subject name field populated if it wasn't explicitly set
            if (examDetails.getSubject() == null || examDetails.getSubject().isEmpty()) {
                exam.setSubject(subject.getName());
            }
        } else {
            exam.setSubjectEntity(null);
        }

        exam.setExamDate(examDetails.getExamDate());
        exam.setDuration(examDetails.getDuration());
        exam.setStartTime(examDetails.getStartTime());
        exam.setStatus(examDetails.getStatus());
        exam.setDescription(examDetails.getDescription());

        return examRepository.save(exam);
    }

    public void deleteExam(Long id) {
        examRepository.delete(getExamById(id));
    }

    private Subject getSubjectById(Long id) {
        return subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", id));
    }
}
