package com.example.demo.controller;

import com.example.demo.dto.MonhocDTO;
import com.example.demo.dto.UndoActionDTO;
import com.example.demo.service.MonhocService;
import com.example.demo.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/subjects")
public class MonhocController {

    @Autowired
    private MonhocService monhocService;
    @Autowired
    private SubjectService subjectService;

    // Get all subjects
    @GetMapping
    public ResponseEntity<List<MonhocDTO>> getAllMonhoc(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        List<MonhocDTO> subjects = monhocService.findAll();
        return ResponseEntity.ok(subjects);
    }

    // Lấy danh sách môn học mà sinh viên có thể thi
    // @GetMapping("/student/{maSV}/class/{maLop}")
    // public ResponseEntity<List<MonhocDTO>> getAvailableSubjectsForStudent(
    //         @PathVariable String maSV,
    //         @PathVariable String maLop) {
    //     List<MonhocDTO> subjects = subjectService.getAvailableSubjectsForStudent(maSV, maLop);
    //     return ResponseEntity.ok(subjects);
    // }
// Lấy danh sách môn học mà sinh viên có thể thi
@GetMapping("/student/{maSV}/class/{maLop}")
public ResponseEntity<?> getAvailableSubjectsForStudent(
        @PathVariable String maSV,
        @PathVariable String maLop) {
    try {
        // Trim maSV và maLop để loại bỏ các khoảng trắng không mong muốn
        String trimmedMaSV = maSV.trim();
        String trimmedMaLop = maLop.trim();
        System.out.println("Trimmed maSV: " + trimmedMaSV);
        System.out.println("Trimmed maLop: " + trimmedMaLop);
        List<MonhocDTO> subjects = subjectService.getAvailableSubjectsForStudent(trimmedMaSV, trimmedMaLop);
        return ResponseEntity.ok(subjects);
    } catch (RuntimeException e) {
        // Trả về response lỗi có ý nghĩa với mã HTTP phù hợp
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", e.getMessage());

        // Xác định HTTP status code dựa trên loại lỗi
        if (e.getMessage().contains("không tồn tại") || e.getMessage().contains("không thuộc lớp")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}

// Thêm endpoint này vào MonhocController
@GetMapping("/{maMH}/can-delete")
public ResponseEntity<Boolean> canDeleteSubject(
        @PathVariable String maMH,
        @RequestHeader(value = "X-User-Id", required = false) String userId,
        @RequestHeader(value = "X-User-Role", required = false) String userRole) {
System.out.println("User ID: " + userId);
    // Nếu cần kiểm tra quyền (tùy chọn)
   

    boolean canDelete = monhocService.canDelete(maMH);
    return ResponseEntity.ok(canDelete);
}
    // Get a specific subject by ID
    @GetMapping("/{maMH}")
    public ResponseEntity<MonhocDTO> getMonhocById(
            @PathVariable String maMH,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        MonhocDTO subject = monhocService.findById(maMH);
        return ResponseEntity.ok(subject);
    }

    // Create a new subject
    @PostMapping
    public ResponseEntity<MonhocDTO> createMonhoc(
            @RequestBody MonhocDTO monhocDTO,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        System.out.println("User ID: " + userId);
        System.out.println("User Role: " + userRole);
        // Only PGV (Phòng Giáo Vụ) can create subjects
        if (!"PGV".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        MonhocDTO created = monhocService.create(monhocDTO, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Update an existing subject
    @PutMapping("/{maMH}")
    public ResponseEntity<MonhocDTO> updateMonhoc(
            @PathVariable String maMH,
            @RequestBody MonhocDTO monhocDTO,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        // Only PGV can update subjects
        if (!"PGV".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        monhocDTO.setMaMH(maMH); // Ensure ID in path matches object
        MonhocDTO updated = monhocService.update(monhocDTO, userId);
        return ResponseEntity.ok(updated);
    }

    // Delete a subject
    @DeleteMapping("/{maMH}")
    public ResponseEntity<Void> deleteMonhoc(
            @PathVariable String maMH,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        // Only PGV can delete subjects
        if (!"PGV".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        monhocService.delete(maMH, userId);
        return ResponseEntity.noContent().build();
    }

    // Search subjects by name or code
    @GetMapping("/search")
    public ResponseEntity<List<MonhocDTO>> searchMonhoc(
            @RequestParam String keyword,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        List<MonhocDTO> results = monhocService.search(keyword);
        return ResponseEntity.ok(results);
    }

    // Undo the last action
    @PostMapping("/undo")
    public ResponseEntity<UndoActionDTO> undoAction(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole, @RequestParam String entityType) {

        // Only PGV can undo actions
        if (!"PGV".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        UndoActionDTO result = monhocService.undo(userId, entityType);
        if (result == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }

    // Check if undo is available
    @GetMapping("/can-undo")
    public ResponseEntity<Boolean> canUndo(
            @RequestHeader(value = "X-User-Id", required = false) String userId, @RequestParam String entityType) {
        boolean canUndo = monhocService.canUndo(userId, entityType);
        return ResponseEntity.ok(canUndo);
    }

    // Get the last undo action available
    @GetMapping("/last-undo-action")
    public ResponseEntity<UndoActionDTO> getLastUndoAction(
            @RequestHeader(value = "X-User-Id", required = false) String userId, @RequestParam String entityType) {
        UndoActionDTO action = monhocService.getLastUndoAction(userId, entityType);
        if (action == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(action);
    }
}