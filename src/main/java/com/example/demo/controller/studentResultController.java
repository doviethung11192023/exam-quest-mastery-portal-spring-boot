package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.StudentExamResultDTO;
import com.example.demo.service.StudentResultService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/exam-results")
public class studentResultController {

    @Autowired
    private StudentResultService examResultService;

    @GetMapping
    public ResponseEntity<?> getExamResults(
            @RequestParam String classId,
            @RequestParam String subjectId,
            @RequestParam String level,
            HttpServletRequest request) {
        String teacherId = request.getHeader("X-User-Id");; // hoặc getId()
        List<StudentExamResultDTO> results = examResultService.getExamResults(classId, subjectId, level, teacherId);
        return ResponseEntity.ok(results);
    }
}
