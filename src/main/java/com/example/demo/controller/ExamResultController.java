package com.example.demo.controller;

import com.example.demo.dto.DetailedExamResultDTO;
import com.example.demo.service.ExamResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/exam-results")
//@CrossOrigin(origins = "*")
public class ExamResultController {

    @Autowired
    private ExamResultService examResultService;

    @GetMapping("/{studentId}/{subjectId}/{attempt}")
    public ResponseEntity<DetailedExamResultDTO> getExamResultDetails(
            @PathVariable String studentId,
            @PathVariable String subjectId,
            @PathVariable Integer attempt) {
            System.out.println("Fetching exam result for studentId: " + studentId + ", subjectId: " + subjectId + ", attempt: " + attempt);
        DetailedExamResultDTO result = examResultService.getExamResultDetails(studentId, subjectId, attempt);
        return ResponseEntity.ok(result);
    }
}