package com.example.demo.dto;

import java.util.Map;
import lombok.Data;

@Data
public class ExamSubmissionDTO {
    private String maSV;
    private String maLop;
    private String maMH;
    private int lan;
    private Map<String, String> answers; // Map của questionId -> đáp án của user
}