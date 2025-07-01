package com.example.demo.dto;

import lombok.Data;

@Data
public class ExamEligibilityDTO {
    private boolean canTake;
    private String message;
    private ExamInfoDTO examInfo;
}