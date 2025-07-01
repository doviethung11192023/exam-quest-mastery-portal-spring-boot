package com.example.demo.dto;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private boolean authenticated;
    private String token;
    private String id;
    private String username;
    private String name;
    private String role;
    private String classId;
    private String className;
    private String errorMessage;
}