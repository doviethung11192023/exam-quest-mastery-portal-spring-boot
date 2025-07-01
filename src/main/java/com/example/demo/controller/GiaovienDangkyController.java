package com.example.demo.controller;

import com.example.demo.dto.GiaovienDangkyDTO;
import com.example.demo.dto.ApiResponse;
import com.example.demo.service.GiaovienDangkyService;
import com.example.demo.entity.Lop;
import com.example.demo.entity.Monhoc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/exam-registrations")
public class GiaovienDangkyController {

    @Autowired
    private GiaovienDangkyService giaovienDangkyService;

    /**
     * Đăng ký lịch thi mới
     */
    @PostMapping
    public ResponseEntity<?> registerExam(
            @RequestBody GiaovienDangkyDTO examDTO,
            @RequestHeader("X-User-Id") String currentUser) {
                System.out.println(examDTO.getNgayThi());
        try {
            GiaovienDangkyDTO createdExam = giaovienDangkyService.registerExam(examDTO, currentUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new ApiResponse<>(true, "Đăng ký lịch thi thành công", createdExam));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Cập nhật đăng ký lịch thi
     */
    @PutMapping
    public ResponseEntity<?> updateExam(
            @RequestBody GiaovienDangkyDTO examDTO,
            @RequestHeader("X-User-Id") String currentUser) {
        try {
            GiaovienDangkyDTO updatedExam = giaovienDangkyService.updateExam(examDTO, currentUser);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Cập nhật lịch thi thành công", updatedExam));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Xóa đăng ký lịch thi
     */
    @DeleteMapping("/{maLop}/{maMH}/{lan}")
    public ResponseEntity<?> deleteExam(
            @PathVariable String maLop,
            @PathVariable String maMH,
            @PathVariable Short lan,
            @RequestHeader("X-User-Id") String currentUser) {
        try {
            giaovienDangkyService.deleteExam(maLop, maMH, lan, currentUser);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Xóa lịch thi thành công", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Tìm kiếm đăng ký thi
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchExams(
            @RequestParam(required = false, defaultValue = "") String maLop,
            @RequestParam(required = false, defaultValue = "") String maMH,
            @RequestParam(required = false, defaultValue = "") String maGV) {
        try {
            List<GiaovienDangkyDTO> exams = giaovienDangkyService.searchExams(maLop, maMH, maGV);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Tìm kiếm thành công", exams));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Lấy đăng ký thi của giáo viên
     */
    @GetMapping("/teacher/{maGV}")
    public ResponseEntity<?> getExamsByTeacher(@PathVariable String maGV) {
        try {
            List<GiaovienDangkyDTO> exams = giaovienDangkyService.getExamsByTeacher(maGV);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Lấy danh sách thành công", exams));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Kiểm tra số câu hỏi
     */
    @GetMapping("/question-count")
    public ResponseEntity<?> getQuestionCount(
            @RequestParam String maMH,
            @RequestParam String trinhDo) {
        try {
            int count = giaovienDangkyService.getQuestionCount(maMH, trinhDo);
            Map<String, Object> response = new HashMap<>();
            response.put("count", count);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Lấy số câu hỏi thành công", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Lấy danh sách lớp mà giáo viên đã đăng ký dạy
     * 
     * @param maGV Mã giáo viên
     * @return Danh sách các lớp đã đăng ký
     */
    @GetMapping("/teacher/{maGV}/classes")
    public ResponseEntity<?> getClassesByTeacher(@PathVariable String maGV) {
        try {
            System.out.println("Fetching classes for teacher: " + maGV);
            List<Lop> classes = giaovienDangkyService.getClassesByTeacher(maGV);

            // Chuyển đổi thành dạng đơn giản hơn để trả về
            List<Map<String, String>> result = classes.stream()
                    .map(lop -> {
                        Map<String, String> classMap = new HashMap<>();
                        classMap.put("maLop", lop.getMaLop());
                        classMap.put("tenLop", lop.getTenLop());
                        return classMap;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Lấy danh sách lớp thành công", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    /**
     * Lấy danh sách môn học mà giáo viên đã đăng ký dạy
     * 
     * @param maGV Mã giáo viên
     * @return Danh sách các môn học đã đăng ký
     */
    @GetMapping("/teacher/{maGV}/subjects")
    public ResponseEntity<?> getSubjectsByTeacher(@PathVariable String maGV) {
        try {
            System.out.println("Fetching subjects for teacher: " + maGV);
            List<Monhoc> subjects = giaovienDangkyService.getSubjectsByTeacher(maGV);

            // Chuyển đổi thành dạng đơn giản hơn để trả về
            List<Map<String, String>> result = subjects.stream()
                    .map(monhoc -> {
                        Map<String, String> subjectMap = new HashMap<>();
                        subjectMap.put("maMH", monhoc.getMaMH());
                        subjectMap.put("tenMH", monhoc.getTenMH());
                        return subjectMap;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(
                    new ApiResponse<>(true, "Lấy danh sách môn học thành công", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, e.getMessage(), null));
        }
    }
}