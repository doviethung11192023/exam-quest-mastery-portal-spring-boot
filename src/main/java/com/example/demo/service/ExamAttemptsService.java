package com.example.demo.service;

import com.example.demo.dto.ExamAttemptDTO;
import com.example.demo.dto.ExamEligibilityDTO;
import com.example.demo.dto.ExamInfoDTO;
import com.example.demo.entity.BangDiem;
import com.example.demo.entity.GiaovienDangky;
import com.example.demo.entity.Sinhvien;
import com.example.demo.repository.BangDiemRepository;
import com.example.demo.repository.GiaovienDangkyRepository;
import com.example.demo.repository.SinhvienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExamAttemptsService {

    @Autowired
    private GiaovienDangkyRepository giaovienDangkyRepository;

    @Autowired
    private BangDiemRepository bangDiemRepository;

    @Autowired
    private SinhvienRepository sinhvienRepository;

    /**
     * Lấy danh sách các lần thi cho một lớp và môn học
     */
    public List<ExamAttemptDTO> getExamAttempts(String maLop, String maMH) {
        List<GiaovienDangky> dangkys = giaovienDangkyRepository.findByMaLopAndMaMH(maLop, maMH);

        return dangkys.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Kiểm tra xem sinh viên có thể tham gia kỳ thi hay không
     */
    public ExamEligibilityDTO checkStudentCanTakeExam(String maSV, String maLop, String maMH, int lan) {
        ExamEligibilityDTO result = new ExamEligibilityDTO();

        try {
            // Kiểm tra sinh viên có tồn tại không và có thuộc lớp không
            Sinhvien sinhvien = sinhvienRepository.findById(maSV)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên"));

            if (!sinhvien.getLop().getMaLop().equals(maLop)) {
                result.setCanTake(false);
                result.setMessage("Sinh viên không thuộc lớp này");
                return result;
            }

            // Kiểm tra xem có lịch thi cho môn học và lớp này không
            GiaovienDangky dangky = giaovienDangkyRepository.findByMalopAndMamhAndLan(maLop, maMH, (short) lan)
                    .orElseThrow(() -> new RuntimeException("Không có lịch thi cho môn học này"));

            // Kiểm tra xem sinh viên đã thi lần này chưa
            Optional<BangDiem> existingScore = bangDiemRepository.findBySinhvienAndMamhAndLan(sinhvien, maMH, lan);
            if (existingScore.isPresent()) {
                result.setCanTake(false);
                result.setMessage("Sinh viên đã thi lần này rồi");
                return result;
            }

            // Kiểm tra thời gian thi
            // Nếu ngày thi đã qua, không được thi nữa
            if (dangky.getNgayThi().isBefore(java.time.LocalDateTime.now().minusHours(1))) {
                result.setCanTake(false);
                result.setMessage("Thời gian thi đã qua");
                return result;
            }

            // Nếu chưa đến ngày thi, không được thi
            if (dangky.getNgayThi().isAfter(java.time.LocalDateTime.now().plusHours(1))) {
                result.setCanTake(false);
                result.setMessage("Chưa đến thời gian thi");
                return result;
            }

            // Nếu tất cả điều kiện đều thỏa mãn, sinh viên có thể thi
            result.setCanTake(true);

            // Tạo thông tin đề thi
            ExamInfoDTO examInfo = new ExamInfoDTO();
            examInfo.setMaLop(maLop);
            examInfo.setMaMH(maMH);
            examInfo.setLan(lan);
            examInfo.setTrinhDo(dangky.getTrinhDo());
            examInfo.setSoCauThi(dangky.getSoCauThi());
            examInfo.setThoiGianThi(dangky.getThoiGian());
            examInfo.setNgayThi(java.sql.Timestamp.valueOf(dangky.getNgayThi()));
            examInfo.setDaThiRoi(false);

            result.setExamInfo(examInfo);

        } catch (Exception e) {
            result.setCanTake(false);
            result.setMessage("Lỗi: " + e.getMessage());
        }

        return result;
    }

    private ExamAttemptDTO convertToDTO(GiaovienDangky dangky) {
        ExamAttemptDTO dto = new ExamAttemptDTO();
        dto.setLan(dangky.getId().getLan());
        dto.setNgayThi(dangky.getNgayThi());
        return dto;
    }
}