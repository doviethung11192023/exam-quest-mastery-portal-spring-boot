package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResultDTO {
    private Integer questionNumber;
    private String questionContent;
    private String difficultyLevel;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String studentAnswer;
    private String correctAnswer;
    private Boolean isCorrect;
}