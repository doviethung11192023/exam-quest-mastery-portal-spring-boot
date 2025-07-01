package com.example.demo.service;

import com.example.demo.dto.GiaovienDangkyDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.BodeRepository;
import com.example.demo.repository.GiaovienDangkyRepository;
import com.example.demo.repository.GiaovienRepository;
import com.example.demo.repository.LopRepository;
import com.example.demo.repository.MonhocRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GiaovienDangkyService {

    @Autowired
    private GiaovienDangkyRepository giaovienDangkyRepository;

    @Autowired
    private GiaovienRepository giaovienRepository;

    @Autowired
    private LopRepository lopRepository;

    @Autowired
    private MonhocRepository monhocRepository;

    @Autowired
    private BodeRepository bodeRepository;

    @Autowired
    private UndoService undoService;
    
    /**
     * Lấy danh sách lớp mà giáo viên đã đăng ký dạy
     * 
     * @param magv Mã giáo viên
     * @return Danh sách các lớp
     */
    public List<Lop> getClassesByTeacher(String magv) {
        return giaovienDangkyRepository.findClassesByTeacher(magv);
    }

    /**
     * Lấy danh sách môn học mà giáo viên đã đăng ký dạy
     * 
     * @param magv Mã giáo viên
     * @return Danh sách các môn học
     */
    public List<Monhoc> getSubjectsByTeacher(String magv) {
        return giaovienDangkyRepository.findSubjectsByTeacher(magv);
    }
    /**
     * Đăng ký lịch thi mới
     */
    @Transactional
    public GiaovienDangkyDTO registerExam(GiaovienDangkyDTO examDTO, String currentUser) {
        // Tìm kiếm giáo viên, lớp và môn học
        Giaovien giaovien = giaovienRepository.findById(examDTO.getMagv())
                .orElseThrow(() -> new RuntimeException("Giáo viên không tồn tại"));

        Lop lop = lopRepository.findById(examDTO.getMaLop())
                .orElseThrow(() -> new RuntimeException("Lớp không tồn tại"));

        Monhoc monhoc = monhocRepository.findById(examDTO.getMaMH())
                .orElseThrow(() -> new RuntimeException("Môn học không tồn tại"));

        // Kiểm tra xem đã có đăng ký trùng chưa
        if (giaovienDangkyRepository.countByLopAndMonHocAndLan(
                examDTO.getMaLop(), examDTO.getMaMH(), examDTO.getLan()) > 0) {
            throw new RuntimeException("Đăng ký cho lớp, môn học và lần thi này đã tồn tại");
        }

        
        int questionCount = bodeRepository.countQuestionsBySubjectAndLevel(
                examDTO.getMaMH(), examDTO.getTrinhDo());

        int requiredQuestions = examDTO.getSoCauThi();
        int mainLevelMinQuestions = (int) Math.ceil(requiredQuestions * 0.7); // 70% số câu hỏi cần thiết

        if (questionCount < requiredQuestions) {
            String lowerLevel = getLowerLevel(examDTO.getTrinhDo());

            // Nếu không có trình độ thấp hơn hoặc câu hỏi chính không đạt 70%
            if (lowerLevel == null || questionCount < mainLevelMinQuestions) {
                String errorMsg = lowerLevel == null
                        ? "Không đủ câu hỏi cho bài thi trình độ " + examDTO.getTrinhDo() + ". Yêu cầu: "
                                + requiredQuestions + ", Hiện có: " + questionCount
                        : "Không đủ câu hỏi ở trình độ " + examDTO.getTrinhDo() + " (cần tối thiểu "
                                + mainLevelMinQuestions + " câu, hiện có " + questionCount + " câu)";

                throw new RuntimeException(errorMsg);
            }

            // Lấy số câu hỏi ở trình độ thấp hơn
            int additionalQuestions = bodeRepository.countQuestionsBySubjectAndLevel(
                    examDTO.getMaMH(), lowerLevel);

            int totalAvailable = questionCount + additionalQuestions;

            if (totalAvailable < requiredQuestions) {
                throw new RuntimeException("Không đủ câu hỏi cho bài thi. Yêu cầu: " +
                        requiredQuestions + ", Hiện có: " + totalAvailable +
                        " (" + examDTO.getTrinhDo() + ": " + questionCount + ", " +
                        lowerLevel + ": " + additionalQuestions + ")");
            }

            System.out.println("Đủ câu hỏi khi kết hợp trình độ " + examDTO.getTrinhDo() +
                    " và " + lowerLevel + ": " + totalAvailable);
        }
        // Tạo ID cho đăng ký
        GiaovienDangkyId id = new GiaovienDangkyId(
                examDTO.getMaLop(), examDTO.getMaMH(), examDTO.getLan());

        // Tạo entity đăng ký
        GiaovienDangky giaovienDangky = new GiaovienDangky();
        giaovienDangky.setId(id);
        giaovienDangky.setGiaovien(giaovien);
        giaovienDangky.setLop(lop);
        giaovienDangky.setMonhoc(monhoc);
        giaovienDangky.setTrinhDo(examDTO.getTrinhDo());
        giaovienDangky.setNgayThi(examDTO.getNgayThi());
        giaovienDangky.setSoCauThi(examDTO.getSoCauThi());
        giaovienDangky.setThoiGian(examDTO.getThoiGian());

        // Lưu đăng ký
        GiaovienDangky savedGiaovienDangky = giaovienDangkyRepository.save(giaovienDangky);

        // Thêm hành động vào undo stack
        // undoService.pushUndoAction(currentUser, new UndoService.UndoAction(
        //         "GIAOVIEN_DANGKY",
        //         "ADD",
        //         examDTO.getMaLop() + "-" + examDTO.getMaMH() + "-" + examDTO.getLan(),
        //         null,
        //         toDTO(savedGiaovienDangky),
        //         () -> {
        //             try {
        //                 giaovienDangkyRepository.deleteById(id);
        //                 return null;
        //             } catch (Exception e) {
        //                 e.printStackTrace();
        //                 return null;
        //             }
        //         }));
        undoService.pushUndoAction(
                currentUser,
                "GIAOVIEN_DANGKY", // entityType
                new UndoService.UndoAction(
                        "GIAOVIEN_DANGKY", // entityType
                        "ADD", // actionType (giữ nguyên "ADD" thay vì "INSERT" để đảm bảo tương thích với code
                               // hiện tại)
                        examDTO.getMaLop() + "-" + examDTO.getMaMH() + "-" + examDTO.getLan(), // entityId
                        "Đăng ký: " + examDTO.getMaLop() + " - " + examDTO.getMaMH(), // entityName - sử dụng thông tin
                                                                                      // lớp và môn học
                        () -> { // undoFunction
                            try {
                                giaovienDangkyRepository.deleteById(id);
                                return true; // Trả về true để xác nhận thành công
                            } catch (Exception e) {
                                e.printStackTrace();
                                return false; // Trả về false để xác nhận thất bại
                            }
                        }));

        return toDTO(savedGiaovienDangky);
    }

    /**
     * Cập nhật đăng ký lịch thi
     */
    @Transactional
    public GiaovienDangkyDTO updateExam(GiaovienDangkyDTO examDTO, String currentUser) {
        // Tạo ID để tìm kiếm
        GiaovienDangkyId id = new GiaovienDangkyId(
                examDTO.getMaLop(), examDTO.getMaMH(), examDTO.getLan());

        // Tìm kiếm đăng ký cần cập nhật
        GiaovienDangky existingGiaovienDangky = giaovienDangkyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đăng ký thi không tồn tại"));

        // Lưu trạng thái trước khi cập nhật cho undo
        GiaovienDangkyDTO oldState = toDTO(existingGiaovienDangky);

        // Tìm kiếm giáo viên mới nếu có thay đổi
        if (!existingGiaovienDangky.getGiaovien().getMagv().equals(examDTO.getMagv())) {
            Giaovien giaovien = giaovienRepository.findById(examDTO.getMagv())
                    .orElseThrow(() -> new RuntimeException("Giáo viên không tồn tại"));
            existingGiaovienDangky.setGiaovien(giaovien);
        }

        // Kiểm tra số câu hỏi có đủ không
        // int questionCount = bodeRepository.countQuestionsBySubjectAndLevel(
        //         examDTO.getMaMH(), examDTO.getTrinhDo());

        // if (questionCount < examDTO.getSoCauThi()) {
        //     throw new RuntimeException("Không đủ câu hỏi cho bài thi. Yêu cầu: " +
        //             examDTO.getSoCauThi() + ", Hiện có: " + questionCount);
        // }
        // Thay thế đoạn code kiểm tra số câu hỏi hiện tại trong updateExam()
      // Kiểm tra số câu hỏi có đủ không
        // Gọi hàm kiểm tra câu hỏi trước khi đăng ký
        validateQuestionCount(examDTO.getMaMH(), examDTO.getTrinhDo(), examDTO.getSoCauThi());

        // Cập nhật thông tin
        existingGiaovienDangky.setTrinhDo(examDTO.getTrinhDo());
        existingGiaovienDangky.setNgayThi(examDTO.getNgayThi());
        existingGiaovienDangky.setSoCauThi(examDTO.getSoCauThi());
        existingGiaovienDangky.setThoiGian(examDTO.getThoiGian());

        // Lưu thay đổi
        GiaovienDangky updatedGiaovienDangky = giaovienDangkyRepository.save(existingGiaovienDangky);

        // Thêm hành động vào undo stack
        // undoService.pushUndoAction(currentUser, new UndoService.UndoAction(
        //         "GIAOVIEN_DANGKY",
        //         "EDIT",
        //         examDTO.getMaLop() + "-" + examDTO.getMaMH() + "-" + examDTO.getLan(),
        //         oldState,
        //         toDTO(updatedGiaovienDangky),
        //         () -> {
        //             try {
        //                 // Khôi phục trạng thái cũ
        //                 GiaovienDangky gvdk = giaovienDangkyRepository.findById(id).orElse(null);
        //                 if (gvdk != null) {
        //                     Giaovien oldGiaovien = giaovienRepository.findById(oldState.getMagv()).orElse(null);
        //                     if (oldGiaovien != null) {
        //                         gvdk.setGiaovien(oldGiaovien);
        //                     }
        //                     gvdk.setTrinhDo(oldState.getTrinhDo());
        //                     gvdk.setNgayThi(oldState.getNgayThi());
        //                     gvdk.setSoCauThi(oldState.getSoCauThi());
        //                     gvdk.setThoiGian(oldState.getThoiGian());

        //                     giaovienDangkyRepository.save(gvdk);
        //                     return toDTO(gvdk);
        //                 }
        //                 return null;
        //             } catch (Exception e) {
        //                 e.printStackTrace();
        //                 return null;
        //             }
        //         }));
        undoService.pushUndoAction(
                currentUser,
                "GIAOVIEN_DANGKY", // entityType
                new UndoService.UndoAction(
                        "GIAOVIEN_DANGKY", // entityType
                        "EDIT", // actionType
                        examDTO.getMaLop() + "-" + examDTO.getMaMH() + "-" + examDTO.getLan(), // entityId
                        "Đăng ký thi lớp " + examDTO.getMaLop() + ", môn " + examDTO.getMaMH(), // entityName
                        () -> { // undoFunction
                            try {
                                // Khôi phục trạng thái cũ
                                GiaovienDangky gvdk = giaovienDangkyRepository.findById(id).orElse(null);
                                if (gvdk != null) {
                                    Giaovien oldGiaovien = giaovienRepository.findById(oldState.getMagv()).orElse(null);
                                    if (oldGiaovien != null) {
                                        gvdk.setGiaovien(oldGiaovien);
                                    }
                                    gvdk.setTrinhDo(oldState.getTrinhDo());
                                    gvdk.setNgayThi(oldState.getNgayThi());
                                    gvdk.setSoCauThi(oldState.getSoCauThi());
                                    gvdk.setThoiGian(oldState.getThoiGian());

                                    giaovienDangkyRepository.save(gvdk);
                                    return toDTO(gvdk);
                                }
                                return null;
                            } catch (Exception e) {
                                e.printStackTrace();
                                return null;
                            }
                        }));

        return toDTO(updatedGiaovienDangky);
    }

    // Thêm phương thức hỗ trợ để xác định trình độ thấp hơn
    // Sửa phương thức hỗ trợ để xác định trình độ thấp hơn đúng theo logic yêu cầu
    private String getLowerLevel(String currentLevel) {
        switch (currentLevel) {
            case "A":
                return "B"; // A có thể lấy thêm từ B (A là cao nhất)
            case "B":
                return "C"; // B có thể lấy thêm từ C
            case "C":
                return null; // C là thấp nhất, không có trình độ nào thấp hơn
            default:
                return null;
        }
    }

    // Kiểm tra số câu hỏi có đủ không (dùng cho cả registerExam và updateExam)
    private void validateQuestionCount(String maMH, String trinhDo, int soCauThi) {
        int questionCount = bodeRepository.countQuestionsBySubjectAndLevel(maMH, trinhDo);
        int requiredQuestions = soCauThi;
        int mainLevelMinQuestions = (int) Math.ceil(requiredQuestions * 0.7); // 70% số câu hỏi cần thiết

        if (questionCount < requiredQuestions) {
            // Trường hợp trình độ C không đủ câu hỏi: báo lỗi ngay
            if ("C".equals(trinhDo)) {
                throw new RuntimeException("Không đủ câu hỏi cho bài thi trình độ C. Yêu cầu: " +
                        requiredQuestions + ", Hiện có: " + questionCount);
            }

            // Lấy trình độ thấp hơn
            String lowerLevel = getLowerLevel(trinhDo);

            // Nếu không đạt tối thiểu 70% câu hỏi ở trình độ chính
            if (questionCount < mainLevelMinQuestions) {
                throw new RuntimeException("Không đủ câu hỏi ở trình độ " + trinhDo + " (cần tối thiểu "
                        + mainLevelMinQuestions + " câu, hiện có " + questionCount + " câu)");
            }

            // Kiểm tra số câu hỏi ở trình độ thấp hơn
            int additionalQuestions = bodeRepository.countQuestionsBySubjectAndLevel(maMH, lowerLevel);
            int totalAvailable = questionCount + additionalQuestions;

            if (totalAvailable < requiredQuestions) {
                throw new RuntimeException("Không đủ câu hỏi cho bài thi. Yêu cầu: " +
                        requiredQuestions + ", Hiện có: " + totalAvailable +
                        " (" + trinhDo + ": " + questionCount + ", " +
                        lowerLevel + ": " + additionalQuestions + ")");
            }

            System.out.println("Đủ câu hỏi khi kết hợp trình độ " + trinhDo +
                    " và " + lowerLevel + ": " + totalAvailable);
        }
    }
    /**
     * Xóa đăng ký lịch thi
     */
    @Transactional
    public void deleteExam(String maLop, String maMH, Short lan, String currentUser) {
        GiaovienDangkyId id = new GiaovienDangkyId(maLop, maMH, lan);

        // Tìm kiếm đăng ký cần xóa
        GiaovienDangky existingGiaovienDangky = giaovienDangkyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đăng ký thi không tồn tại"));

        // Lưu trạng thái cho undo
        GiaovienDangkyDTO deletedState = toDTO(existingGiaovienDangky);

        // Xóa đăng ký
        giaovienDangkyRepository.deleteById(id);

        // Thêm hành động vào undo stack
        // undoService.pushUndoAction(currentUser, new UndoService.UndoAction(
        //         "GIAOVIEN_DANGKY",
        //         "DELETE",
        //         maLop + "-" + maMH + "-" + lan,
        //         deletedState,
        //         null,
        //         () -> {
        //             try {
        //                 // Tạo lại đăng ký đã xóa
        //                 Giaovien giaovien = giaovienRepository.findById(deletedState.getMagv()).orElse(null);
        //                 Lop lop = lopRepository.findById(deletedState.getMaLop()).orElse(null);
        //                 Monhoc monhoc = monhocRepository.findById(deletedState.getMaMH()).orElse(null);

        //                 if (giaovien != null && lop != null && monhoc != null) {
        //                     GiaovienDangky newGvdk = new GiaovienDangky();
        //                     newGvdk.setId(id);
        //                     newGvdk.setGiaovien(giaovien);
        //                     newGvdk.setLop(lop);
        //                     newGvdk.setMonhoc(monhoc);
        //                     newGvdk.setTrinhDo(deletedState.getTrinhDo());
        //                     newGvdk.setNgayThi(deletedState.getNgayThi());
        //                     newGvdk.setSoCauThi(deletedState.getSoCauThi());
        //                     newGvdk.setThoiGian(deletedState.getThoiGian());

        //                     giaovienDangkyRepository.save(newGvdk);
        //                     return toDTO(newGvdk);
        //                 }
        //                 return null;
        //             } catch (Exception e) {
        //                 e.printStackTrace();
        //                 return null;
        //             }
        //         }));
        undoService.pushUndoAction(
                currentUser,
                "GIAOVIEN_DANGKY", // entityType
                new UndoService.UndoAction(
                        "GIAOVIEN_DANGKY", // entityType
                        "DELETE", // actionType
                        maLop + "-" + maMH + "-" + lan, // entityId
                        "Đăng ký thi lớp " + maLop + ", môn " + maMH + " (giáo viên " + deletedState.getMagv() + ")", // entityName
                        () -> { // undoFunction
                            try {
                                // Tạo lại đăng ký đã xóa
                                Giaovien giaovien = giaovienRepository.findById(deletedState.getMagv()).orElse(null);
                                Lop lop = lopRepository.findById(deletedState.getMaLop()).orElse(null);
                                Monhoc monhoc = monhocRepository.findById(deletedState.getMaMH()).orElse(null);

                                if (giaovien != null && lop != null && monhoc != null) {
                                    GiaovienDangky newGvdk = new GiaovienDangky();
                                    newGvdk.setId(id);
                                    newGvdk.setGiaovien(giaovien);
                                    newGvdk.setLop(lop);
                                    newGvdk.setMonhoc(monhoc);
                                    newGvdk.setTrinhDo(deletedState.getTrinhDo());
                                    newGvdk.setNgayThi(deletedState.getNgayThi());
                                    newGvdk.setSoCauThi(deletedState.getSoCauThi());
                                    newGvdk.setThoiGian(deletedState.getThoiGian());

                                    giaovienDangkyRepository.save(newGvdk);
                                    return toDTO(newGvdk);
                                }
                                return null;
                            } catch (Exception e) {
                                e.printStackTrace();
                                return null;
                            }
                        }));
    }

    /**
     * Lấy số câu hỏi trong bộ đề theo môn học và trình độ
     */
    public int getQuestionCount(String maMH, String trinhDo) {
        return bodeRepository.countQuestionsBySubjectAndLevel(maMH, trinhDo);
    }

    /**
     * Tìm kiếm đăng ký thi
     */
    public List<GiaovienDangkyDTO> searchExams(String maLop, String maMH, String maGV) {
        List<GiaovienDangky> exams = giaovienDangkyRepository.searchExams(
                maLop.isEmpty() ? null : maLop,
                maMH.isEmpty() ? null : maMH,
                maGV.isEmpty() ? null : maGV);

        return exams.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Lấy tất cả đăng ký thi của một giáo viên
     */
    /**
     * Lấy tất cả đăng ký thi của một giáo viên
     */
    public List<GiaovienDangkyDTO> getExamsByTeacher(String maGV) {
        // Sửa tên phương thức để khớp với Repository đã sửa
        List<GiaovienDangky> exams = giaovienDangkyRepository.findByGiaovien_Magv(maGV);
        return exams.stream().map(this::toDTO).collect(Collectors.toList());
    }

    /**
     * Chuyển đổi entity sang DTO
     */
    private GiaovienDangkyDTO toDTO(GiaovienDangky entity) {
        return new GiaovienDangkyDTO(
                entity.getId().getMaLop(),
                entity.getLop().getTenLop(),
                entity.getId().getMaMH(),
                entity.getMonhoc().getTenMH(),
                entity.getId().getLan(),
                entity.getGiaovien().getMagv(),
                entity.getGiaovien().getHo() + " " + entity.getGiaovien().getTen(),
                entity.getTrinhDo(),
                entity.getNgayThi(),
                entity.getSoCauThi(),
                entity.getThoiGian());
    }
}