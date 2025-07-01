package com.example.demo.dto;

import lombok.Data;

@Data
public class RestoreRequestDTO {
    private String databaseName;
    private String fileName;
    private String pointInTime;
}