package com.example.demo.controller;

import com.example.demo.dto.GiaovienDTO;
import com.example.demo.dto.UndoActionDTO;
import com.example.demo.service.GiaovienService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TestGiaovienController.java - Controller mới để test
@RestController
@RequestMapping("/public-api/giaovien")
public class TestGiaovienController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllGiaovien() {
        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(
                    "SELECT MAGV, HO, TEN, (HO + ' ' + TEN) as HOTEN, SODTLL as SODIENTHOAI, DIACHI FROM GIAOVIEN");
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
