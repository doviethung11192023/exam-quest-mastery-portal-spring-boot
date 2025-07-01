package com.example.demo.service;

import com.example.demo.dto.MonhocDTO;
import com.example.demo.entity.Lop;
import com.example.demo.entity.Monhoc;
import com.example.demo.entity.Sinhvien;
import com.example.demo.repository.GiaovienDangkyRepository;
import com.example.demo.repository.LopRepository;
import com.example.demo.repository.MonhocRepository;
import com.example.demo.repository.SinhvienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SubjectService {

    @Autowired
    private MonhocRepository monhocRepository;

    @Autowired
    private GiaovienDangkyRepository giaovienDangkyRepository;

    @Autowired
    private SinhvienRepository sinhvienRepository;

    @Autowired
    private LopRepository lopRepository;

    /**
     * Lấy danh sách môn học mà sinh viên có thể thi
     */
    // public List<MonhocDTO> getAvailableSubjectsForStudent(String maSV, String maLop) {
    //     // Kiểm tra xem sinh viên có thuộc lớp này không
    //     Sinhvien sinhvien = sinhvienRepository.findById(maSV)
    //             .orElseThrow(() -> new RuntimeException("Không tìm thấy sinh viên"));

    //     if (!sinhvien.getLop().getMaLop().equals(maLop)) {
    //         throw new RuntimeException("Sinh viên không thuộc lớp này");
    //     }

    //     // Tìm các môn học đã được đăng ký thi cho lớp này
    //     List<String> subjectIds = giaovienDangkyRepository.findDistinctSubjectsByClass(maLop);

    //     // Lấy thông tin chi tiết của các môn học
    //     List<Monhoc> subjects = monhocRepository.findAllById(subjectIds);

    //     // Chuyển đổi sang DTO
    //     return subjects.stream()
    //             .map(this::convertToDTO)
    //             .collect(Collectors.toList());
    // }
public List<MonhocDTO> getAvailableSubjectsForStudent(String maSV, String maLop) {
        // Kiểm tra xem sinh viên có tồn tại không
        Optional<Sinhvien> sinhvienOpt = sinhvienRepository.findById(maSV);
        if (sinhvienOpt.isEmpty()) {
            throw new RuntimeException("Sinh viên không tồn tại với mã: " + maSV);
        }
        
        Sinhvien sinhvien = sinhvienOpt.get();
        
        // Kiểm tra xem sinh viên có thuộc lớp này không
        System.out.println(sinhvien.getLop().getMaLop());
        System.err.println(maLop);
        if (!sinhvien.getLop().getMaLop().trim().equals(maLop)) {
            // System.out.println("Không thuộc lớp này");
            // System.out.println(sinhvien.getLop().getMaLop().equals(maLop));
            // Trả về lỗi nếu sinh viên không thuộc lớp này
            throw new RuntimeException("Sinh viên không thuộc lớp này");
        }
        
        // Lấy các môn học đã được đăng ký thi cho lớp này
        List<String> subjectIds = giaovienDangkyRepository.findDistinctSubjectsByClass(maLop);
        
        if (subjectIds.isEmpty()) {
            // Trả về danh sách rỗng thay vì lỗi nếu không có môn học nào
            return new ArrayList<>();
        }
        
        // Lấy thông tin chi tiết của các môn học
        List<Monhoc> subjects = monhocRepository.findAllById(subjectIds);
        
        // Chuyển đổi sang DTO
        return subjects.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
    private MonhocDTO convertToDTO(Monhoc monhoc) {
        MonhocDTO dto = new MonhocDTO();
        dto.setMaMH(monhoc.getMaMH());
        dto.setTenMH(monhoc.getTenMH());
        return dto;
    }
}