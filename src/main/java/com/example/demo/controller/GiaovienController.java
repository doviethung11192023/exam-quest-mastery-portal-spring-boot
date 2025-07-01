package com.example.demo.controller;

import com.example.demo.dto.GiaovienDTO;
import com.example.demo.dto.UndoActionDTO;
import com.example.demo.service.GiaovienService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/giaovien")
public class GiaovienController {

    @Autowired
    private GiaovienService giaovienService;

    @GetMapping
    public ResponseEntity<List<GiaovienDTO>> getAllGiaovien(HttpServletRequest request) {
        System.out.println("Getting all giaovien");
        String userId = request.getHeader("X-User-Id");
        return ResponseEntity.ok(giaovienService.getAllGiaovien());
    }

    @GetMapping("/search")
    public ResponseEntity<List<GiaovienDTO>> searchGiaovien(
            @RequestParam String keyword,
            HttpServletRequest request) {
        System.out.println("Searching giaovien with keyword: " + keyword);
        String userId = request.getHeader("X-User-Id");
        return ResponseEntity.ok(giaovienService.searchGiaovien(keyword));
    }

    @GetMapping("/{magv}")
    public ResponseEntity<GiaovienDTO> getGiaovienById(
            @PathVariable String magv,
            HttpServletRequest request) {
        System.out.println("Getting giaovien with ID: " + magv);
        String userId = request.getHeader("X-User-Id");
        GiaovienDTO giaovien = giaovienService.getGiaovienById(magv);

        if (giaovien != null) {
            return ResponseEntity.ok(giaovien);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<GiaovienDTO> createGiaovien(
            @RequestBody GiaovienDTO giaovienDTO,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        System.out.println("Creating giaovien: " + giaovienDTO.getMagv());
        GiaovienDTO createdGiaovien = giaovienService.createGiaovien(giaovienDTO, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGiaovien);
    }

    @PutMapping("/{magv}")
    public ResponseEntity<GiaovienDTO> updateGiaovien(
            @PathVariable String magv,
            @RequestBody GiaovienDTO giaovienDTO,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        System.out.println("Updating giaovien: " + magv);
        GiaovienDTO updatedGiaovien = giaovienService.updateGiaovien(magv, giaovienDTO, userId);
        return ResponseEntity.ok(updatedGiaovien);
    }
    
    @GetMapping("/without-accounts")
    public ResponseEntity<List<GiaovienDTO>> getGiaoviensWithoutAccounts(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        // Kiểm tra quyền truy cập - chỉ PGV hoặc SA có thể xem
        if (!"PGV".equals(userRole) && !"sa".equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }

        List<GiaovienDTO> giaoviens = giaovienService.getGiaoviensWithoutAccounts();
        return ResponseEntity.ok(giaoviens);
    }

    @DeleteMapping("/{magv}")
    public ResponseEntity<Void> deleteGiaovien(
            @PathVariable String magv,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        System.out.println("Deleting giaovien: " + magv);
        giaovienService.deleteGiaovien(magv, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/undo")
    public ResponseEntity<Object> undoAction(
            @RequestHeader(value = "X-User-Id", required = false) String userId, @RequestParam String entityType) {
        System.out.println("Undoing action for user: " + userId);
        Object result = giaovienService.undoAction(userId, entityType);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/can-undo")
    public ResponseEntity<Boolean> canUndo(
            @RequestHeader(value = "X-User-Id", required = false) String userId, @RequestParam String entityType) {
        boolean canUndo = giaovienService.canUndo(userId, entityType);
        return ResponseEntity.ok(canUndo);
    }

    @GetMapping("/last-undo-action")
    public ResponseEntity<UndoActionDTO> getLastUndoAction(
            @RequestHeader(value = "X-User-Id", required = false) String userId, @RequestParam String entityType) {
        UndoActionDTO action = giaovienService.getLastUndoAction(userId, entityType);
        if (action != null) {
            return ResponseEntity.ok(action);
        } else {
            return ResponseEntity.noContent().build();
        }
    }
    
    /**
     * Kiểm tra xem giáo viên có thể xóa được không
     * 
     * @param magv Mã giáo viên cần kiểm tra
     * @return true nếu có thể xóa, false nếu không
     */
    @GetMapping("/{magv}/can-delete")
    public ResponseEntity<Boolean> canDeleteGiaovien(@PathVariable String magv) {
        System.out.println("Checking if lecturer can be deleted: " + magv);
        boolean canDelete = giaovienService.canDelete(magv);
        return ResponseEntity.ok(canDelete);
    }
}