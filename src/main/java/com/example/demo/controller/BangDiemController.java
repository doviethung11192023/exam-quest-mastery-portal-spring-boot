package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.GradeRecordDTO;
import com.example.demo.service.BangDiemService;

@RestController
@RequestMapping("/grades")
public class BangDiemController {

    @Autowired
    private BangDiemService bangDiemService;

    @GetMapping
    public ResponseEntity<List<GradeRecordDTO>> getGradeReport(
            @RequestParam String classId,
            @RequestParam String subjectId,
            @RequestParam int examAttempt) {
        List<GradeRecordDTO> result = bangDiemService.getGradeRecords(classId, subjectId, examAttempt);
        return ResponseEntity.ok(result);
    }
}
