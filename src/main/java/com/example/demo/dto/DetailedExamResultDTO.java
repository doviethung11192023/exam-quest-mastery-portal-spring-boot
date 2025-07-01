package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetailedExamResultDTO {
    private String studentId;
    private String studentName;
    private String classId;
    private String className;
    private String subjectId;
    private String subject;
    private Integer attempt;
    private LocalDate examDate;
    private Float score;
    private Integer totalCorrect;
    private Integer totalScore;
    private List<QuestionResultDTO> questions;
}