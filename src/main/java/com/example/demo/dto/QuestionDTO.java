package com.example.demo.dto;

import java.util.Map;
import lombok.Data;

@Data
public class QuestionDTO {
    private String id;
    private String content;
    private Map<String, String> answers;
    private String correctAnswer; // Sẽ được loại bỏ khi truyền xuống client
}