package com.example.demo.dto;

import lombok.Data;

@Data
public class ExamResultDTO {
    private double score;
    private int totalQuestions;
    private int correctAnswers;
    private int answeredQuestions;
}