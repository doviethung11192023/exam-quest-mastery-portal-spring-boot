package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UndoActionDTO {
    private String entityType;
    private String actionType;
    private String entityId;
    private String entityName;
    private LocalDateTime timestamp;
}