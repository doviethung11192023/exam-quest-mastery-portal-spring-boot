package com.example.demo.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ExamAttemptDTO {
    private int lan;
    private LocalDateTime ngayThi;
}