package com.example.demo.controller;

import com.example.demo.dto.ExamInfoDTO;
import com.example.demo.dto.QuestionDTO;
import com.example.demo.dto.ExamSubmissionDTO;
import com.example.demo.dto.ExamResultDTO;
import com.example.demo.dto.ExamAttemptDTO;
import com.example.demo.dto.ExamEligibilityDTO;
import com.example.demo.service.ExamService;
import com.example.demo.service.ExamAttemptsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/exams")
public class ExamController {
    @Autowired
    private ExamService examService;

    @Autowired
    private ExamAttemptsService examAttemptsService;

    // Lấy thông tin về bài thi cho sinh viên
    @GetMapping("/student/{maSV}/class/{maLop}/subject/{maMH}/attempt/{lan}")
    public ResponseEntity<ExamInfoDTO> getExamInfo(
            @PathVariable String maSV,
            @PathVariable String maLop,
            @PathVariable String maMH,
            @PathVariable int lan) {

        ExamInfoDTO examInfo = examService.getExamInfo(maSV, maLop, maMH, lan);
        return ResponseEntity.ok(examInfo);
    }

   
    // Trong ExamController.java
    @GetMapping("/questions/test/{maLop}/{maMH}/{lan}")
    public ResponseEntity<?> getExamQuestionsForTesting(
            @PathVariable String maLop,
            @PathVariable String maMH,
            @PathVariable int lan) {
        try {
            // Chuẩn hóa dữ liệu đầu vào
            String normalizedMaLop = maLop.trim();
            String normalizedMaMH = maMH.trim();

            // Log để debug
            System.out.println("Tìm lịch thi với: maLop=[" + normalizedMaLop +
                    "], maMH=[" + normalizedMaMH + "], lan=" + lan);

            List<QuestionDTO> questions = examService.getQuestionsForTesting(normalizedMaLop, normalizedMaMH, lan);
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            // Cải thiện thông báo lỗi
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "message", "Không tìm thấy lịch thi",
                            "details", e.getMessage(),
                            "parameters", Map.of(
                                    "maLop", maLop,
                                    "maMH", maMH,
                                    "lan", lan)));
        }
    }
    @GetMapping("/questions")
    public ResponseEntity<List<QuestionDTO>> getExamQuestions(
            @RequestParam String maLop,
            @RequestParam String maMH,
            @RequestParam int lan,
            @RequestParam String maSV) {

        // Trim input parameters to remove whitespace
        String trimmedMaLop = maLop.trim();
        String trimmedMaMH = maMH.trim();
        String trimmedMaSV = maSV.trim();
        System.out.println("Trimmed maLop: " + trimmedMaLop);
        System.out.println("Trimmed maMH: " + trimmedMaMH);
        System.out.println("Trimmed maSV: " + trimmedMaSV);
        List<QuestionDTO> questions = examService.getQuestionsForExam(
                trimmedMaLop, trimmedMaMH, lan, trimmedMaSV);

        return ResponseEntity.ok(questions);
    }

    // Nộp bài thi
    @PostMapping("/submit")
    public ResponseEntity<ExamResultDTO> submitExam(@RequestBody ExamSubmissionDTO submission) {
        System.out.println("Received exam submission: " + submission);
        System.out.println("Submitting exam for student: " + submission.getMaSV());
        System.out.println("Exam submission details: " + submission);
        ExamResultDTO result = examService.submitExam(submission);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/attempts")
    public ResponseEntity<List<ExamAttemptDTO>> getExamAttempts(
            @RequestParam String classId,
            @RequestParam String subjectId) {
        List<ExamAttemptDTO> attempts = examAttemptsService.getExamAttempts(classId, subjectId);
        return ResponseEntity.ok(attempts);
    }

    // Kiểm tra sinh viên có thể thi hay không
    @GetMapping("/check")
    public ResponseEntity<ExamEligibilityDTO> checkStudentCanTakeExam(
            @RequestParam String studentId,
            @RequestParam String classId,
            @RequestParam String subjectId,
            @RequestParam int lan) {
        ExamEligibilityDTO eligibility = examAttemptsService.checkStudentCanTakeExam(
                studentId, classId, subjectId, lan);
        return ResponseEntity.ok(eligibility);
    }
}