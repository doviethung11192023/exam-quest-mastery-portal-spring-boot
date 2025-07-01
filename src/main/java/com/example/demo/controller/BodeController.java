package com.example.demo.controller;

import com.example.demo.dto.BodeDTO;
import com.example.demo.dto.UndoActionDTO;
import com.example.demo.service.BodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/questions")

public class BodeController {

    @Autowired
    private BodeService bodeService;

    // Lấy tất cả câu hỏi (phụ thuộc quyền)
    @GetMapping
    public ResponseEntity<List<BodeDTO>> getQuestions(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestParam(required = false) String maMH) {

        try {
            List<BodeDTO> questions;
            System.out.println("User ID: " + userId);
            System.out.println("User Role: " + userRole);
            if ("PGV".equals(userRole)) {
                // PGV có thể xem tất cả câu hỏi
                if (maMH != null && !maMH.isEmpty()) {
                    questions = bodeService.getQuestionsBySubject(maMH);
                } else {
                    questions = bodeService.getAllQuestions();
                }
            } else {
                // Giảng viên chỉ có thể xem câu hỏi của mình
                if (maMH != null && !maMH.isEmpty()) {
                    questions = bodeService.getQuestionsBySubjectAndTeacher(maMH, userId);
                } else {
                    questions = bodeService.getQuestionsByTeacher(userId);
                }
            }

            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    
    @GetMapping("/{maMH}/{cauHoi}/can-delete")
    public ResponseEntity<Boolean> canDeleteQuestion(
            @PathVariable String maMH,
            @PathVariable Integer cauHoi) {
        boolean canDelete = bodeService.canDelete(maMH, cauHoi);
        return ResponseEntity.ok(canDelete);
    }

    // Tìm kiếm câu hỏi
    @GetMapping("/search")
    public ResponseEntity<List<BodeDTO>> searchQuestions(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestParam String keyword,
            @RequestParam(required = false) String maMH) {

        try {
            List<BodeDTO> questions = bodeService.searchQuestions(keyword, maMH, userId, userRole);
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Lấy câu hỏi theo ID
    @GetMapping("/{maMH}/{cauHoi}")
    public ResponseEntity<BodeDTO> getQuestionById(
            @PathVariable String maMH,
            @PathVariable Integer cauHoi,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        try {
            BodeDTO question = bodeService.getQuestionById(maMH, cauHoi);

            if (question == null) {
                return ResponseEntity.notFound().build();
            }

            // Kiểm tra quyền truy cập
            if (!"PGV".equals(userRole) && !question.getMagv().equals(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.ok(question);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

   
    @PostMapping
    public ResponseEntity<BodeDTO> createQuestion(
            @RequestBody BodeDTO bodeDTO,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        try {
            // Add debug logs
            System.out.println("Received question: " + bodeDTO);
            System.out.println("cauHoi: " + bodeDTO.getCauHoi());
            System.out.println("User ID: " + userId);

            // Đảm bảo người tạo câu hỏi là người đăng nhập
            if (userId == null || userId.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
            }

            // Sử dụng userId từ header thay vì hardcode "HV001"
            bodeDTO.setMagv(userId.trim());

            System.out.println("Setting magv to: " + bodeDTO.getMagv());

            // Đảm bảo cauHoi là null để tự sinh ID
            bodeDTO.setCauHoi(null);

            BodeDTO createdQuestion = bodeService.createQuestion(bodeDTO, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdQuestion);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    // Cập nhật câu hỏi
    @PutMapping("/{maMH}/{cauHoi}")
    public ResponseEntity<BodeDTO> updateQuestion(
            @PathVariable String maMH,
            @PathVariable Integer cauHoi,
            @RequestBody BodeDTO bodeDTO,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        try {
            BodeDTO updatedQuestion = bodeService.updateQuestion(maMH, cauHoi, bodeDTO, userId, userRole);
            return ResponseEntity.ok(updatedQuestion);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Xóa câu hỏi
    @DeleteMapping("/{maMH}/{cauHoi}")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable String maMH,
            @PathVariable Integer cauHoi,
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {

        try {
            System.out.println("Deleting question with ID: " + maMH + ", " + cauHoi);
            System.out.println("User ID: " + userId);
            System.out.println("User Role: " + userRole);
            // Kiểm tra quyền truy cập
            bodeService.deleteQuestion(maMH, cauHoi, userId, userRole);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Hoàn tác hành động cuối cùng
    @PostMapping("/undo")
    public ResponseEntity<Object> undoAction(
            @RequestHeader(value = "X-User-Id", required = false) String userId, @RequestParam String entityType) {

        try {
            System.out.println("Undoing action for user: " + userId);
            // Check if undo is actually possible first
            if (!bodeService.canUndo(userId, entityType)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("No undo action available for this user");
            }
            Object result = bodeService.undoAction(userId, entityType);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // Kiểm tra có thể hoàn tác không
    @GetMapping("/can-undo")
    public ResponseEntity<Boolean> canUndo(
            @RequestHeader(value = "X-User-Id", required = false) String userId, @RequestParam String entityType) {

        boolean canUndo = bodeService.canUndo(userId, entityType);
        return ResponseEntity.ok(canUndo);
    }

    // Lấy thông tin hành động hoàn tác cuối cùng
    @GetMapping("/last-undo-action")
    public ResponseEntity<UndoActionDTO> getLastUndoAction(
            @RequestHeader(value = "X-User-Id", required = false) String userId, @RequestParam String entityType) {

        UndoActionDTO action = bodeService.getLastUndoAction(userId, entityType);
        if (action != null) {
            return ResponseEntity.ok(action);
        } else {
            return ResponseEntity.noContent().build();
        }
    }
}