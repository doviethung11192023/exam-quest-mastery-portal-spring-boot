package com.example.demo.service;

import com.example.demo.dto.BodeDTO;
import com.example.demo.dto.UndoActionDTO;
import com.example.demo.entity.Bode;
import com.example.demo.entity.BodeId;
import com.example.demo.entity.ChiTietBaiThi;
import com.example.demo.entity.Giaovien;
import com.example.demo.entity.Monhoc;
import com.example.demo.repository.BodeRepository;
import com.example.demo.repository.ChiTietBaiThiRepository;
import com.example.demo.repository.GiaovienRepository;
import com.example.demo.repository.MonhocRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BodeService {

    @Autowired
    private BodeRepository bodeRepository;

    @Autowired
    private MonhocRepository monhocRepository;

    @Autowired
    private GiaovienRepository giaovienRepository;

    @Autowired
    private UndoService undoService;

    @Autowired
    private ChiTietBaiThiRepository chiTietBaiThiRepository;

    public boolean canDelete(String maMH, Integer cauHoi) {
        // Kiểm tra xem câu hỏi có tồn tại trong bất kỳ đề thi nào không
        boolean isUsedInExam = chiTietBaiThiRepository.existsByMaMHAndCauHoi(maMH, cauHoi);

        // Có thể xóa nếu không được sử dụng trong bất kỳ đề thi nào
        return !isUsedInExam;
    }
    // Chuyển đổi Entity sang DTO
    private BodeDTO convertToDTO(Bode bode) {
        if (bode == null)
            return null;

        BodeDTO dto = new BodeDTO();
        dto.setMaMH(bode.getMonhoc().getMaMH());
        dto.setTenMH(bode.getMonhoc().getTenMH());
        dto.setCauHoi(bode.getId().getCauHoi());
        dto.setTrinhDo(bode.getTrinhDo());
        dto.setNoiDung(bode.getNoiDung());
        dto.setA(bode.getA());
        dto.setB(bode.getB());
        dto.setC(bode.getC());
        dto.setD(bode.getD());
        dto.setDapAn(bode.getDapAn());

        if (bode.getGiaovien() != null) {
            dto.setMagv(bode.getGiaovien().getMagv());
            dto.setTenGiangVien(bode.getGiaovien().getHo() + " " + bode.getGiaovien().getTen());
        }

        return dto;
    }

    // Chuyển đổi DTO sang Entity
    private Bode convertToEntity(BodeDTO dto) throws Exception {
        Bode entity = new Bode();

        // Tạo BodeId
        BodeId bodeId = new BodeId();
        bodeId.setMaMH(dto.getMaMH());
        // bodeId.setCauHoi(dto.getCauHoi());
        entity.setId(bodeId);

        // Thiết lập các thuộc tính
        entity.setTrinhDo(dto.getTrinhDo());
        entity.setNoiDung(dto.getNoiDung());
        entity.setA(dto.getA());
        entity.setB(dto.getB());
        entity.setC(dto.getC());
        entity.setD(dto.getD());
        entity.setDapAn(dto.getDapAn());

        // Thiết lập môn học
        Optional<Monhoc> monhocOpt = monhocRepository.findById(dto.getMaMH());
        if (monhocOpt.isEmpty()) {
            throw new IllegalArgumentException("Môn học không tồn tại: " + dto.getMaMH());
        }
        entity.setMonhoc(monhocOpt.get());

        // Thiết lập giáo viên
        if (dto.getMagv() != null && !dto.getMagv().isEmpty()) {
            Optional<Giaovien> giaovienOpt = giaovienRepository.findById(dto.getMagv());
            if (giaovienOpt.isEmpty()) {
                throw new IllegalArgumentException("Giáo viên không tồn tại: " + dto.getMagv());
            }
            entity.setGiaovien(giaovienOpt.get());
        }

        return entity;
    }

    // Lấy tất cả câu hỏi (Dành cho PGV)
    @Transactional(readOnly = true)
    public List<BodeDTO> getAllQuestions() {
        try {
            List<Bode> questions = bodeRepository.findAll();
            return questions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Lấy câu hỏi theo môn học (Dành cho PGV)
    @Transactional(readOnly = true)
    public List<BodeDTO> getQuestionsBySubject(String maMH) {
        try {
            List<Bode> questions = bodeRepository.findByMonhocMaMH(maMH);
            return questions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Lấy câu hỏi theo giáo viên (Phân quyền)
    @Transactional(readOnly = true)
    public List<BodeDTO> getQuestionsByTeacher(String magv) {
        try {
            List<Bode> questions = bodeRepository.findByGiaovienMagv(magv);
            return questions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Lấy câu hỏi theo môn học và giáo viên (Phân quyền)
    @Transactional(readOnly = true)
    public List<BodeDTO> getQuestionsBySubjectAndTeacher(String maMH, String magv) {
        try {
            List<Bode> questions = bodeRepository.findByMonhocMaMHAndGiaovienMagv(maMH, magv);
            return questions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Lấy câu hỏi theo môn học, trình độ
    @Transactional(readOnly = true)
    public List<BodeDTO> getQuestionsBySubjectAndLevel(String maMH, String trinhDo) {
        try {
            List<Bode> questions = bodeRepository.findByMonhocMaMHAndTrinhDo(maMH, trinhDo);
            return questions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Tìm câu hỏi theo từ khóa
    @Transactional(readOnly = true)
    public List<BodeDTO> searchQuestions(String keyword, String maMH, String magv, String role) {
        try {
            List<Bode> questions;

            if (role.equals("PGV")) {
                // PGV có thể tìm kiếm tất cả câu hỏi hoặc theo môn học
                if (maMH != null && !maMH.isEmpty()) {
                    questions = bodeRepository.searchByNoiDungContainingAndMonhocMaMH(keyword, maMH);
                } else {
                    questions = bodeRepository.searchByNoiDungContaining(keyword);
                }
            } else {
                // Giảng viên chỉ có thể tìm câu hỏi của mình
                if (maMH != null && !maMH.isEmpty()) {
                    questions = bodeRepository.searchByNoiDungContainingAndMonhocMaMHAndGiaovienMagv(keyword, maMH,
                            magv);
                } else {
                    questions = bodeRepository.findByGiaovienMagv(magv);
                }
            }

            return questions.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    // Lấy câu hỏi theo ID
    @Transactional(readOnly = true)
    public BodeDTO getQuestionById(String maMH, Integer cauHoi) {
        try {
            BodeId id = new BodeId(maMH, cauHoi);
            Optional<Bode> bodeOpt = bodeRepository.findById(id);
            return bodeOpt.map(this::convertToDTO).orElse(null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Tạo câu hỏi mới
    // @Transactional
    // public BodeDTO createQuestion(BodeDTO bodeDTO, String currentUser) {
    //     try {
    //         // Kiểm tra đầu vào
    //         if (bodeDTO.getMaMH() == null || bodeDTO.getNoiDung() == null || bodeDTO.getDapAn() == null) {
    //             throw new IllegalArgumentException("Các trường bắt buộc không được để trống");
    //         }

    //         // Kiểm tra môn học
    //         Optional<Monhoc> monhocOpt = monhocRepository.findById(bodeDTO.getMaMH());
    //         if (monhocOpt.isEmpty()) {
    //             throw new IllegalArgumentException("Môn học không tồn tại: " + bodeDTO.getMaMH());
    //         }

    //         // Kiểm tra giáo viên
    //         Optional<Giaovien> giaovienOpt = giaovienRepository.findById(bodeDTO.getMagv());
    //         if (giaovienOpt.isEmpty()) {
    //             throw new IllegalArgumentException("Giáo viên không tồn tại: " + bodeDTO.getMagv());
    //         }

    //         // Validate đáp án
    //         if (!bodeDTO.getDapAn().matches("[A-D]")) {
    //             throw new IllegalArgumentException("Đáp án phải là A, B, C hoặc D");
    //         }

    //         // Validate trình độ
    //         if (!bodeDTO.getTrinhDo().matches("[A-C]")) {
    //             throw new IllegalArgumentException("Trình độ phải là A, B hoặc C");
    //         }

    //         // Tạo số thứ tự câu hỏi mới nếu chưa có
    //         // if (bodeDTO.getCauHoi() == null) {
    //         //     bodeDTO.setCauHoi(bodeRepository.getNextQuestionNumber(bodeDTO.getMaMH()));
    //         // }

    //         // Kiểm tra xem câu hỏi đã tồn tại chưa
    //         BodeId id = new BodeId(bodeDTO.getMaMH(), bodeDTO.getCauHoi());
    //         if (bodeRepository.existsById(id)) {
    //             throw new IllegalArgumentException("Câu hỏi đã tồn tại trong môn học này");
    //         }

    //         // Chuyển đổi và lưu entity
    //         Bode bode = convertToEntity(bodeDTO);
    //         Bode savedBode = bodeRepository.save(bode);
    //         BodeDTO savedDTO = convertToDTO(savedBode);

    //         // Lưu hành động undo
    //         final BodeId bodeId = id;
    //         undoService.pushUndoAction(currentUser, new UndoService.UndoAction(
    //                 "BODE",
    //                 "INSERT",
    //                 bodeDTO.getMaMH() + "-" + bodeDTO.getCauHoi(),
    //                 null,
    //                 savedBode,
    //                 () -> {
    //                     try {
    //                         bodeRepository.deleteById(bodeId);
    //                         return true;
    //                     } catch (Exception e) {
    //                         e.printStackTrace();
    //                         return false;
    //                     }
    //                 }));

    //         return savedDTO;
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         throw new RuntimeException("Lỗi khi tạo câu hỏi: " + e.getMessage());
    //     }
    // }
@Autowired
private EntityManagerFactory entityManagerFactory;

/**
 * Phương thức chuyên biệt để tạo mới câu hỏi mà không chèn giá trị vào cột identity
 * @param bode đối tượng Bode với thông tin đầy đủ (trừ cauHoi)
 * @return đối tượng Bode đã được lưu với cauHoi được sinh tự động
 */
private Bode insertBodeWithoutIdentity(Bode bode) {
    EntityManager entityManager = entityManagerFactory.createEntityManager();
    entityManager.getTransaction().begin();
    
    try {
        // 1. Lấy maMH từ bode
        String maMH = bode.getMonhoc().getMaMH();
        
        // 2. Tạo câu lệnh SQL không chứa cột identity
        String sql = "INSERT INTO Bode (MAMH, TRINHDO, NOIDUNG, A, B, C, D, DAP_AN, MAGV) " +
                     "VALUES (:maMH, :trinhDo, :noiDung, :a, :b, :c, :d, :dapAn, :magv)";
        
        // 3. Set các tham số
        jakarta.persistence.Query query = entityManager.createNativeQuery(sql);
        query.setParameter("maMH", maMH);
        query.setParameter("trinhDo", bode.getTrinhDo());
        query.setParameter("noiDung", bode.getNoiDung());
        query.setParameter("a", bode.getA());
        query.setParameter("b", bode.getB());
        query.setParameter("c", bode.getC());
        query.setParameter("d", bode.getD());
        query.setParameter("dapAn", bode.getDapAn());
        query.setParameter("magv", bode.getGiaovien().getMagv());
        
        // 4. Thực thi lệnh insert
        query.executeUpdate();
        
        // 5. Lấy giá trị cauHoi vừa được tạo
        String getIdSql = "SELECT MAX(cauhoi) FROM Bode WHERE MAMH = :maMH";
        jakarta.persistence.Query getIdQuery = entityManager.createNativeQuery(getIdSql);
        getIdQuery.setParameter("maMH", maMH);
        Integer cauHoi = (Integer) getIdQuery.getSingleResult();
        
        // 6. Commit transaction
        entityManager.getTransaction().commit();
        
        // 7. Đọc lại đối tượng đầy đủ từ database
        BodeId bodeId = new BodeId(maMH, cauHoi);
        return bodeRepository.findById(bodeId).orElseThrow(() -> 
            new RuntimeException("Không thể tìm thấy câu hỏi sau khi tạo"));
            
    } catch (Exception e) {
        // Rollback transaction nếu có lỗi
        if (entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().rollback();
        }
        throw new RuntimeException("Lỗi khi insert câu hỏi: " + e.getMessage(), e);
    } finally {
        // Đóng entityManager
        if (entityManager.isOpen()) {
            entityManager.close();
        }
    }
}
    // @Transactional
    // public BodeDTO createQuestion(BodeDTO bodeDTO, String currentUser) {
    //     try {
    //         // Kiểm tra đầu vào
    //         if (bodeDTO.getMaMH() == null || bodeDTO.getNoiDung() == null || bodeDTO.getDapAn() == null) {
    //             throw new IllegalArgumentException("Các trường bắt buộc không được để trống");
    //         }

    //         // Kiểm tra môn học
    //         Optional<Monhoc> monhocOpt = monhocRepository.findById(bodeDTO.getMaMH());
    //         if (monhocOpt.isEmpty()) {
    //             throw new IllegalArgumentException("Môn học không tồn tại: " + bodeDTO.getMaMH());
    //         }

    //         // Kiểm tra giáo viên
    //         Optional<Giaovien> giaovienOpt = giaovienRepository.findById(bodeDTO.getMagv());
    //         if (giaovienOpt.isEmpty()) {
    //             throw new IllegalArgumentException("Giáo viên không tồn tại: " + bodeDTO.getMagv());
    //         }

    //         // Validate đáp án
    //         if (!bodeDTO.getDapAn().matches("[A-D]")) {
    //             throw new IllegalArgumentException("Đáp án phải là A, B, C hoặc D");
    //         }

    //         // Validate trình độ
    //         if (!bodeDTO.getTrinhDo().matches("[A-C]")) {
    //             throw new IllegalArgumentException("Trình độ phải là A, B hoặc C");
    //         }

    //         // QUAN TRỌNG: KHÔNG đặt giá trị cho cauHoi - để SQL Server tự tạo
    //         // Comment hoặc xóa đoạn code sau
    //         // if (bodeDTO.getCauHoi() == null) {
    //         // bodeDTO.setCauHoi(bodeRepository.getNextQuestionNumber(bodeDTO.getMaMH()));
    //         // }

    //         // Để SQL Server tạo ID tự động, không cần kiểm tra câu hỏi tồn tại
    //         // Chỉ cần đảm bảo thông tin cần thiết
    //         Bode bode = new Bode();
    //         // BodeId bodeId = new BodeId();
    //         // bodeId.setMaMH(bodeDTO.getMaMH());
    //         // KHÔNG đặt cauHoi ở đây
    //         // bodeId.setCauHoi(bodeDTO.getCauHoi());
    //         // bode.setId(bodeId); // ID sẽ được cập nhật sau khi save
    //         bode.setMaMH(bodeDTO.getMaMH());
    //         // Thiết lập các thuộc tính khác
    //         bode.setNoiDung(bodeDTO.getNoiDung());
    //         bode.setA(bodeDTO.getA());
    //         bode.setB(bodeDTO.getB());
    //         bode.setC(bodeDTO.getC());
    //         bode.setD(bodeDTO.getD());
    //         bode.setDapAn(bodeDTO.getDapAn());
    //         bode.setTrinhDo(bodeDTO.getTrinhDo());
    //         bode.setGiaovien(giaovienOpt.get());
    //         bode.setMonhoc(monhocOpt.get());

    //         // Lưu entity (SQL Server sẽ tạo cauHoi tự động)
    //         Bode savedBode = bodeRepository.save(bode);
    //         BodeDTO savedDTO = convertToDTO(savedBode);

    //         // Lưu hành động undo với ID đã được tạo
    //         final BodeId finalBodeId = savedBode.getId();
    //         undoService.pushUndoAction(currentUser, new UndoService.UndoAction(
    //                 "BODE",
    //                 "INSERT",
    //                 savedDTO.getMaMH() + "-" + savedDTO.getCauHoi(),
    //                 null,
    //                 savedBode,
    //                 () -> {
    //                     try {
    //                         bodeRepository.deleteById(finalBodeId);
    //                         return true;
    //                     } catch (Exception e) {
    //                         e.printStackTrace();
    //                         return false;
    //                     }
    //                 }));

    //         return savedDTO;
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         throw new RuntimeException("Lỗi khi tạo câu hỏi: " + e.getMessage(), e);
    //     }
    // }
    @Transactional
    public BodeDTO createQuestion(BodeDTO bodeDTO, String currentUser) {
        try {
            // Kiểm tra đầu vào
            if (bodeDTO.getMaMH() == null || bodeDTO.getNoiDung() == null || bodeDTO.getDapAn() == null) {
                throw new IllegalArgumentException("Các trường bắt buộc không được để trống");
            }

            // Kiểm tra môn học
            Optional<Monhoc> monhocOpt = monhocRepository.findById(bodeDTO.getMaMH());
            if (monhocOpt.isEmpty()) {
                throw new IllegalArgumentException("Môn học không tồn tại: " + bodeDTO.getMaMH());
            }

            // Kiểm tra giáo viên
            Optional<Giaovien> giaovienOpt = giaovienRepository.findById(bodeDTO.getMagv());
            if (giaovienOpt.isEmpty()) {
                throw new IllegalArgumentException("Giáo viên không tồn tại: " + bodeDTO.getMagv());
            }

            // Validate đáp án và trình độ
            if (!bodeDTO.getDapAn().matches("[A-D]")) {
                throw new IllegalArgumentException("Đáp án phải là A, B, C hoặc D");
            }

            if (!bodeDTO.getTrinhDo().matches("[A-C]")) {
                throw new IllegalArgumentException("Trình độ phải là A, B hoặc C");
            }

            // Chuẩn bị đối tượng Bode với đầy đủ thông tin
            Bode bode = new Bode();
            bode.setNoiDung(bodeDTO.getNoiDung());
            bode.setA(bodeDTO.getA());
            bode.setB(bodeDTO.getB());
            bode.setC(bodeDTO.getC());
            bode.setD(bodeDTO.getD());
            bode.setDapAn(bodeDTO.getDapAn());
            bode.setTrinhDo(bodeDTO.getTrinhDo());
            bode.setGiaovien(giaovienOpt.get());
            bode.setMonhoc(monhocOpt.get());

            // Sử dụng phương thức chuyên biệt để insert
            Bode savedBode = insertBodeWithoutIdentity(bode);
            BodeDTO savedDTO = convertToDTO(savedBode);

            // Lưu hành động undo với ID đã được tạo
            //final BodeId finalBodeId = savedBode.getId();
            // undoService.pushUndoAction(currentUser, new UndoService.UndoAction(
            //         "BODE",
            //         "INSERT",
            //         savedDTO.getMaMH() + "-" + savedDTO.getCauHoi(),
            //         null,
            //         savedBode,
            //         () -> {
            //             try {
            //                 bodeRepository.deleteById(finalBodeId);
            //                 return true;
            //             } catch (Exception e) {
            //                 e.printStackTrace();
            //                 return false;
            //             }
            //         }));
            final BodeId finalBodeId = savedBode.getId();
            undoService.pushUndoAction(
                    currentUser,
                    "BODE", // entityType
                    new UndoService.UndoAction(
                            "BODE", // entityType
                            "INSERT", // actionType
                            savedDTO.getMaMH() + "-" + savedDTO.getCauHoi(), // entityId
                            savedDTO.getNoiDung(), // entityName - sử dụng nội dung câu hỏi thay vì null
                            () -> { // undoFunction
                                try {
                                    bodeRepository.deleteById(finalBodeId);
                                    return true;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return false;
                                }
                            }));

            return savedDTO;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi tạo câu hỏi: " + e.getMessage(), e);
        }
    }
    // Cập nhật câu hỏi
    @Transactional
    public BodeDTO updateQuestion(String maMH, Integer cauHoi, BodeDTO bodeDTO, String currentUser, String role) {
        try {
            // Kiểm tra câu hỏi tồn tại
            BodeId bodeId = new BodeId(maMH, cauHoi);
            Optional<Bode> bodeOpt = bodeRepository.findById(bodeId);

            if (bodeOpt.isEmpty()) {
                throw new IllegalArgumentException("Không tìm thấy câu hỏi");
            }

            Bode existingBode = bodeOpt.get();

            // Kiểm tra quyền: chỉ PGV hoặc giáo viên tạo câu hỏi mới có quyền sửa
            String creatorId = existingBode.getGiaovien().getMagv();
            if (!role.equals("PGV") && !bodeDTO.getMagv().equals(creatorId)) {
                throw new IllegalArgumentException("Bạn không có quyền cập nhật câu hỏi này");
            }

            // Lưu trạng thái ban đầu để hoàn tác
            Bode originalBode = cloneBode(existingBode);

            // Validate đáp án
            if (!bodeDTO.getDapAn().matches("[A-D]")) {
                throw new IllegalArgumentException("Đáp án phải là A, B, C hoặc D");
            }

            // Validate trình độ
            if (!bodeDTO.getTrinhDo().matches("[A-C]")) {
                throw new IllegalArgumentException("Trình độ phải là A, B hoặc C");
            }

            // Cập nhật các trường
            existingBode.setTrinhDo(bodeDTO.getTrinhDo());
            existingBode.setNoiDung(bodeDTO.getNoiDung());
            existingBode.setA(bodeDTO.getA());
            existingBode.setB(bodeDTO.getB());
            existingBode.setC(bodeDTO.getC());
            existingBode.setD(bodeDTO.getD());
            existingBode.setDapAn(bodeDTO.getDapAn());

            // Lưu entity
            Bode updatedBode = bodeRepository.save(existingBode);
            BodeDTO updatedDTO = convertToDTO(updatedBode);

            // Lưu hành động undo
            // final Bode finalOriginalBode = originalBode;
            // undoService.pushUndoAction(currentUser, new UndoService.UndoAction(
            //         "BODE",
            //         "UPDATE",
            //         maMH + "-" + cauHoi,
            //         originalBode,
            //         updatedBode,
            //         () -> {
            //             try {
            //                 // Lưu lại trạng thái ban đầu
            //                 Bode revertedBode = bodeRepository.save(finalOriginalBode);
            //                 return convertToDTO(revertedBode);
            //             } catch (Exception e) {
            //                 e.printStackTrace();
            //                 return null;
            //             }
            //         }));
            final Bode finalOriginalBode = originalBode;
            undoService.pushUndoAction(
                    currentUser,
                    "BODE", // entityType
                    new UndoService.UndoAction(
                            "BODE", // entityType
                            "UPDATE", // actionType
                            maMH + "-" + cauHoi, // entityId
                            originalBode.getNoiDung(), // entityName - sử dụng nội dung câu hỏi thay vì truyền toàn bộ
                                                       // đối tượng
                            () -> { // undoFunction
                                try {
                                    // Lưu lại trạng thái ban đầu
                                    Bode revertedBode = bodeRepository.save(finalOriginalBode);
                                    return convertToDTO(revertedBode);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return null;
                                }
                            })          );
            return updatedDTO;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi cập nhật câu hỏi: " + e.getMessage());
        }
    }

    private Bode cloneBode(Bode source) {
        Bode clone = new Bode();
        BodeId bodeId = new BodeId(source.getId().getMaMH(), source.getId().getCauHoi());
        clone.setId(bodeId);
        clone.setTrinhDo(source.getTrinhDo());
        clone.setNoiDung(source.getNoiDung());
        clone.setA(source.getA());
        clone.setB(source.getB());
        clone.setC(source.getC());
        clone.setD(source.getD());
        clone.setDapAn(source.getDapAn());
        clone.setMonhoc(source.getMonhoc());
        clone.setGiaovien(source.getGiaovien());
        return clone;
}
    // Xóa câu hỏi
    @Transactional
    public void deleteQuestion(String maMH, Integer cauHoi, String currentUser, String role) {
        try {
            // Kiểm tra câu hỏi tồn tại
            System.out.println("Attempting to delete question: " + maMH + "-" + cauHoi);
            BodeId bodeId = new BodeId(maMH, cauHoi);
            Optional<Bode> bodeOpt = bodeRepository.findById(bodeId);
            //System.out.println("Bode Opt: " + bodeOpt.get());
            System.out.println("Found question: " + bodeOpt.isPresent());
            if (bodeOpt.isEmpty()) {
                throw new IllegalArgumentException("Không tìm thấy câu hỏi");
            }

            Bode existingBode = bodeOpt.get();
            System.out.println("Deleting question: " + maMH + "-" + cauHoi);
            System.out.println(role + " - " + existingBode.getGiaovien().getMagv() + " - " + currentUser);
            // Kiểm tra quyền: chỉ PGV hoặc giáo viên tạo câu hỏi mới có quyền xóa
            String creatorId = existingBode.getGiaovien().getMagv();
            System.out.println("Creator ID: " + creatorId);
            System.out.println("Current User: " + currentUser);
            // if (!role.equals("PGV") && !currentUser.equals(creatorId)) {
            //     throw new IllegalArgumentException("Bạn không có quyền xóa câu hỏi này");
            // }

            // Lưu trạng thái ban đầu để hoàn tác
            final Bode deletedBode = cloneBode(existingBode);

            // Xóa câu hỏi
            bodeRepository.deleteById(bodeId);

            // Lưu hành động undo
//                    undoService.pushUndoAction(currentUser, new UndoService.UndoAction(
//         "BODE",
//         "DELETE",
//         maMH + "-" + cauHoi,
//         deletedBode,
//         null,
//         () -> {
//             java.sql.Connection connection = null;
//             java.sql.Statement stmt = null;
            
//             try {
//                 // Lấy thông tin từ deletedBode
//                 String innerMaMH = deletedBode.getId().getMaMH();
//                 int innerCauHoi = deletedBode.getId().getCauHoi();
//                 String trinhDo = deletedBode.getTrinhDo();
//                 String noiDung = escapeSql(deletedBode.getNoiDung());
//                 String a = escapeSql(deletedBode.getA());
//                 String b = escapeSql(deletedBode.getB());
//                 String c = escapeSql(deletedBode.getC());
//                 String d = escapeSql(deletedBode.getD());
//                 String dapAn = deletedBode.getDapAn();
//                 String maGv = deletedBode.getGiaovien().getMagv();
                
//                 // Lấy kết nối trực tiếp từ EntityManager
//                 connection = entityManagerFactory.createEntityManager()
//                         .unwrap(org.hibernate.Session.class)
//                         .doReturningWork(conn -> conn);
                
//                 // Tắt auto-commit
//                 connection.setAutoCommit(false);
                
//                 // Tạo statement
//                 stmt = connection.createStatement();
                
//                 // Bật IDENTITY_INSERT
//                 System.out.println("Bật IDENTITY_INSERT ON");
//                 stmt.execute("SET IDENTITY_INSERT Bode ON");
                
//                 // Tạo câu lệnh INSERT trực tiếp (để tránh vấn đề với PreparedStatement)
//                 String insertSQL = String.format(
//                     "INSERT INTO Bode (MAMH, CAUHOI, TRINHDO, NOIDUNG, A, B, C, D, DAP_AN, MAGV) " +
//                     "VALUES ('%s', %d, '%s', N'%s', N'%s', N'%s', N'%s', N'%s', '%s', '%s')",
//                     maMH, cauHoi, trinhDo, noiDung, a, b, c, d, dapAn, maGv
//                 );
                
//                 // Thực thi INSERT
//                 System.out.println("Thực thi: " + insertSQL);
//                 int rowsAffected = stmt.executeUpdate(insertSQL);
//                 System.out.println("Rows affected: " + rowsAffected);
                
//                 // Tắt IDENTITY_INSERT
//                 stmt.execute("SET IDENTITY_INSERT Bode OFF");
                
//                 // Commit transaction
//                 connection.commit();
//                 System.out.println("Commit transaction thành công");
                
//                 // Tìm câu hỏi đã khôi phục
//                 BodeId restoredBodeId = new BodeId(maMH, cauHoi);
//                 Bode restored = bodeRepository.findById(restoredBodeId).orElse(null);
                
//                 return restored != null ? convertToDTO(restored) : null;
//             } catch (Exception e) {
//                 // Log lỗi
//                 e.printStackTrace();
//                 System.err.println("Lỗi khi khôi phục câu hỏi: " + e.getMessage());
                
//                 // Rollback transaction nếu có lỗi
//                 if (connection != null) {
//                     try {
//                         connection.rollback();
//                         System.out.println("Transaction rollback");
//                     } catch (SQLException ex) {
//                         ex.printStackTrace();
//                     }
//                 }
                
//                 return null;
//             } finally {
//                 // Đóng tài nguyên
//                 if (stmt != null) {
//                     try {
//                         stmt.close();
//                     } catch (SQLException e) {
//                         e.printStackTrace();
//                     }
//                 }
                
//                 if (connection != null) {
//                     try {
//                         // Khôi phục auto-commit
//                         connection.setAutoCommit(true);
//                         connection.close();
//                     } catch (SQLException e) {
//                         e.printStackTrace();
//                     }
//                 }
//             }
//         }
// ));
undoService.pushUndoAction(
        currentUser,
        "BODE", // entityType
        new UndoService.UndoAction(
                "BODE", // entityType
                "DELETE", // actionType
                maMH + "-" + cauHoi, // entityId
                deletedBode.getNoiDung(), // entityName - sử dụng nội dung câu hỏi
                () -> { // undoFunction
                    java.sql.Connection connection = null;
                    java.sql.Statement stmt = null;

                    try {
                        // Lấy thông tin từ deletedBode
                        String innerMaMH = deletedBode.getId().getMaMH();
                        int innerCauHoi = deletedBode.getId().getCauHoi();
                        String trinhDo = deletedBode.getTrinhDo();
                        String noiDung = escapeSql(deletedBode.getNoiDung());
                        String a = escapeSql(deletedBode.getA());
                        String b = escapeSql(deletedBode.getB());
                        String c = escapeSql(deletedBode.getC());
                        String d = escapeSql(deletedBode.getD());
                        String dapAn = deletedBode.getDapAn();
                        String maGv = deletedBode.getGiaovien().getMagv();

                        // Lấy kết nối trực tiếp từ EntityManager
                        connection = entityManagerFactory.createEntityManager()
                                .unwrap(org.hibernate.Session.class)
                                .doReturningWork(conn -> conn);

                        // Tắt auto-commit
                        connection.setAutoCommit(false);

                        // Tạo statement
                        stmt = connection.createStatement();

                        // Bật IDENTITY_INSERT
                        System.out.println("Bật IDENTITY_INSERT ON");
                        stmt.execute("SET IDENTITY_INSERT Bode ON");

                        // Tạo câu lệnh INSERT trực tiếp (để tránh vấn đề với PreparedStatement)
                        String insertSQL = String.format(
                                "INSERT INTO Bode (MAMH, CAUHOI, TRINHDO, NOIDUNG, A, B, C, D, DAP_AN, MAGV) " +
                                        "VALUES ('%s', %d, '%s', N'%s', N'%s', N'%s', N'%s', N'%s', '%s', '%s')",
                                maMH, cauHoi, trinhDo, noiDung, a, b, c, d, dapAn, maGv);

                        // Thực thi INSERT
                        System.out.println("Thực thi: " + insertSQL);
                        int rowsAffected = stmt.executeUpdate(insertSQL);
                        System.out.println("Rows affected: " + rowsAffected);

                        // Tắt IDENTITY_INSERT
                        stmt.execute("SET IDENTITY_INSERT Bode OFF");

                        // Commit transaction
                        connection.commit();
                        System.out.println("Commit transaction thành công");

                        // Tìm câu hỏi đã khôi phục
                        BodeId restoredBodeId = new BodeId(maMH, cauHoi);
                        Bode restored = bodeRepository.findById(restoredBodeId).orElse(null);

                        return restored != null ? convertToDTO(restored) : null;
                    } catch (Exception e) {
                        // Log lỗi
                        e.printStackTrace();
                        System.err.println("Lỗi khi khôi phục câu hỏi: " + e.getMessage());

                        // Rollback transaction nếu có lỗi
                        if (connection != null) {
                            try {
                                connection.rollback();
                                System.out.println("Transaction rollback");
                            } catch (SQLException ex) {
                                ex.printStackTrace();
                            }
                        }

                        return null;
                    } finally {
                        // Đóng tài nguyên
                        if (stmt != null) {
                            try {
                                stmt.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }

                        if (connection != null) {
                            try {
                                // Khôi phục auto-commit
                                connection.setAutoCommit(true);
                                connection.close();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }));

// Helper method to escape SQL strings

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi xóa câu hỏi: " + e.getMessage());
        }
    }

    // Hoàn tác hành động cuối cùng
    @Transactional
    public Object undoAction(String currentUser, String role) {
        try {
            System.err.println("Undoing action for user: " + currentUser);
            System.out.println("Can undo: " + undoService.canUndo(currentUser, role));
            return undoService.undoLastAction(currentUser, role);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi hoàn tác: " + e.getMessage());
        }
    }

    // Kiểm tra có thể hoàn tác không
    public boolean canUndo(String currentUser, String role) {
        return undoService.canUndo(currentUser, role);
    }

    // Lấy thông tin hành động hoàn tác cuối cùng
    public UndoActionDTO getLastUndoAction(String currentUser, String role) {
        return undoService.getLastUndoAction(currentUser, role);
    }
    
    private String escapeSql(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("'", "''");
    }
}