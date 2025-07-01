package com.example.demo.service;

import com.example.demo.dto.DetailedExamResultDTO;
import com.example.demo.dto.QuestionResultDTO;
import com.example.demo.entity.BangDiem;
import com.example.demo.entity.BangDiemId;
import com.example.demo.entity.ChiTietBaiThi;
import com.example.demo.repository.BangDiemRepository;
import com.example.demo.repository.ChiTietBaiThiRepository;
import com.example.demo.repository.LopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExamResultService {

    @Autowired
    private BangDiemRepository bangDiemRepository;

    @Autowired
    private ChiTietBaiThiRepository chiTietBaiThiRepository;

    
    public DetailedExamResultDTO getExamResultDetails(String studentId, String subjectId, Integer attempt) {
        // Create the BangDiemId to find the exam result
        BangDiemId bangDiemId = new BangDiemId();
        bangDiemId.setMaSV(studentId);
        bangDiemId.setMaMH(subjectId);
        bangDiemId.setLan(attempt.shortValue());
        System.out.println("Fetching exam result for studentId: " + studentId + ", subjectId: " + subjectId + ", attempt: " + attempt);     

        // Find the exam result
        BangDiem bangDiem = bangDiemRepository.findById(bangDiemId)
                .orElseThrow(() -> new RuntimeException("Exam result not found"));

        // Get question details from ChiTietBaiThi
        List<ChiTietBaiThi> examDetails = chiTietBaiThiRepository.findByStudentAndSubjectAndAttempt(
                studentId, subjectId, attempt);
                System.out.println("Found " + examDetails.size() + " details for the exam");


        // Count correct answers
        int correctAnswers = (int) examDetails.stream()
                .filter(detail -> detail.getTraLoi() != null && detail.getTraLoi().equals(detail.getBode().getDapAn()))
                .count();

        // Map to DTOs
        List<QuestionResultDTO> questionResults = examDetails.stream()
                .map(this::mapToQuestionResultDTO)
                .collect(Collectors.toList());

        // Create and populate the result DTO
        DetailedExamResultDTO resultDTO = new DetailedExamResultDTO();
        resultDTO.setStudentId(studentId);
        resultDTO.setStudentName(bangDiem.getSinhvien().getHo() + " " + bangDiem.getSinhvien().getTen());
        resultDTO.setClassId(bangDiem.getSinhvien().getLop().getMaLop());
        resultDTO.setClassName(bangDiem.getSinhvien().getLop().getTenLop());
        resultDTO.setSubjectId(subjectId);
        resultDTO.setSubject(bangDiem.getMonhoc().getTenMH());
        resultDTO.setAttempt(attempt);
        resultDTO.setExamDate(bangDiem.getNgayThi());
        resultDTO.setScore(bangDiem.getDiem());
        resultDTO.setTotalCorrect(correctAnswers);
        resultDTO.setTotalScore(examDetails.size());
        resultDTO.setQuestions(questionResults);
        System.out.println("Mapped exam result to DTO for studentId: " + studentId);
        // Return the result DTO
        System.out.println("Returning exam result for studentId: " + studentId);
        System.out.println("Total questions: " + questionResults.size());
        System.out.println("Total correct answers: " + correctAnswers);
        System.out.println("Exam date: " + bangDiem.getNgayThi());
        System.out.println("Score: " + bangDiem.getDiem());
        System.out.println("Attempt: " + attempt);
        System.out.println("Subject: " + bangDiem.getMonhoc().getTenMH());
        System.out.println("Class: " + bangDiem.getSinhvien().getLop().getTenLop());
        System.out.println("Student name: " + bangDiem.getSinhvien().getHo() + " " + bangDiem.getSinhvien().getTen());
        System.out.println("Student ID: " + studentId);
        System.out.println("Subject ID: " + subjectId);
        System.out.println("Attempt: " + attempt);
        System.out.println("Exam date: " + bangDiem.getNgayThi());
        System.out.println("Score: " + bangDiem.getDiem());
        System.out.println("Total correct answers: " + correctAnswers);
        System.out.println("Total questions: " + examDetails.size());
        System.out.println("Returning exam result DTO for studentId: " + studentId);

        return resultDTO;
    }

    private QuestionResultDTO mapToQuestionResultDTO(ChiTietBaiThi chiTietBaiThi) {
        QuestionResultDTO dto = new QuestionResultDTO();
        dto.setQuestionNumber(chiTietBaiThi.getId().getCauHoi());
        dto.setQuestionContent(chiTietBaiThi.getBode().getNoiDung());
        dto.setDifficultyLevel(chiTietBaiThi.getBode().getTrinhDo());
        dto.setOptionA(chiTietBaiThi.getBode().getA());
        dto.setOptionB(chiTietBaiThi.getBode().getB());
        dto.setOptionC(chiTietBaiThi.getBode().getC());
        dto.setOptionD(chiTietBaiThi.getBode().getD());
        dto.setStudentAnswer(chiTietBaiThi.getTraLoi());
        dto.setCorrectAnswer(chiTietBaiThi.getBode().getDapAn());
        dto.setIsCorrect(chiTietBaiThi.getTraLoi() != null &&
                chiTietBaiThi.getTraLoi().equals(chiTietBaiThi.getBode().getDapAn()));
        return dto;
    }
}