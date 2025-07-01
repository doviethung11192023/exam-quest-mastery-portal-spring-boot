package com.example.demo.controller;

import com.example.demo.dto.SinhvienDTO;
import com.example.demo.service.SinhvienService;
import com.example.demo.service.UndoService;
import com.example.demo.dto.UndoActionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/students")
public class SinhvienController {

    @Autowired
    private SinhvienService sinhvienService;

    @GetMapping
    public ResponseEntity<List<SinhvienDTO>> getAllStudents() {
        return ResponseEntity.ok(sinhvienService.getAllStudents());
    }

    @GetMapping("/{maSV}")
    public ResponseEntity<SinhvienDTO> getStudentById(@PathVariable String maSV) {
        try {
            SinhvienDTO sinhvienDTO = sinhvienService.getStudentById(maSV);
            return ResponseEntity.ok(sinhvienDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/class/{maLop}")
    public ResponseEntity<List<SinhvienDTO>> getStudentsByClass(@PathVariable String maLop) {
        return ResponseEntity.ok(sinhvienService.getStudentsByClass(maLop));
    }
    
    // Trong SinhvienController.java
    @GetMapping("/{maSV}/can-delete")
    public ResponseEntity<Boolean> canDeleteStudent(@PathVariable String maSV) {
        boolean canDelete = sinhvienService.canDelete(maSV);
        return ResponseEntity.ok(canDelete);
    }

    @GetMapping("/class/{maLop}/paginated")
    public ResponseEntity<Map<String, Object>> getStudentsByClassPaginated(
            @PathVariable String maLop,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Map<String, Object> response = sinhvienService.getStudentsByClassPaginated(
                maLop, search, page, size);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<SinhvienDTO>> searchStudents(
            @RequestParam String maLop,
            @RequestParam String keyword) {

        return ResponseEntity.ok(sinhvienService.searchStudents(maLop, keyword));
    }

    @PostMapping
    public ResponseEntity<SinhvienDTO> addStudent(
            @RequestBody SinhvienDTO sinhvienDTO,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            SinhvienDTO createdStudent = sinhvienService.addStudent(sinhvienDTO, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }

    @PutMapping("/{maSV}")
    public ResponseEntity<SinhvienDTO> updateStudent(
            @PathVariable String maSV,
            @RequestBody SinhvienDTO sinhvienDTO,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            SinhvienDTO updatedStudent = sinhvienService.updateStudent(maSV, sinhvienDTO, userId);
            return ResponseEntity.ok(updatedStudent);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }
    }

    @DeleteMapping("/{maSV}")
    public ResponseEntity<Void> deleteStudent(
            @PathVariable String maSV,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            sinhvienService.deleteStudent(maSV, userId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/can-undo")
    public ResponseEntity<Boolean> canUndo(@RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam String entityType) {
        return ResponseEntity.ok(sinhvienService.canUndo(userId, entityType));
    }

    @GetMapping("/last-undo-action")
    public ResponseEntity<UndoActionDTO> getLastUndoAction(
            @RequestHeader(value = "X-User-Id", required = false) String userId, @RequestParam String entityType) {
        UndoActionDTO action = sinhvienService.getLastUndoAction(userId, entityType);
        if (action != null) {
            return ResponseEntity.ok(action);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @PostMapping("/undo")
    public ResponseEntity<Object> undoAction(@RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam String entityType) {
        try {
            Object result = sinhvienService.undoAction(userId, entityType);
            if (result != null) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.noContent().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}