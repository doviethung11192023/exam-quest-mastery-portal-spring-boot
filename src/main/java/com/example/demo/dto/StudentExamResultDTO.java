package com.example.demo.dto;

import java.util.List;

public class StudentExamResultDTO {

    private String studentId;
    private String studentName;
    private String examDate;
    private String attempt;
    private List<ExamDetailDTO> details;

    // Constructors
    public StudentExamResultDTO() {
    }

    public StudentExamResultDTO(String studentId, String studentName, String examDate, String attempt,
            List<ExamDetailDTO> details) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.examDate = examDate;
        this.attempt = attempt;
        this.details = details;
    }

    // Getters and Setters
    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getExamDate() {
        return examDate;
    }

    public void setExamDate(String examDate) {
        this.examDate = examDate;
    }

    public String getAttempt() {
        return attempt;
    }

    public void setAttempt(String attempt) {
        this.attempt = attempt;
    }

    public List<ExamDetailDTO> getDetails() {
        return details;
    }

    public void setDetails(List<ExamDetailDTO> details) {
        this.details = details;
    }
}
