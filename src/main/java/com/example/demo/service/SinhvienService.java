package com.example.demo.service;

import com.example.demo.dto.SinhvienDTO;
import com.example.demo.dto.UndoActionDTO;
import com.example.demo.entity.Lop;
import com.example.demo.entity.Sinhvien;
import com.example.demo.repository.BangDiemRepository;
import com.example.demo.repository.LopRepository;
import com.example.demo.repository.SinhvienRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.PersistenceUnit;
import jakarta.persistence.EntityManagerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


   
@Service
public class SinhvienService {
    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private SinhvienRepository sinhvienRepository;

    @Autowired
    private LopRepository lopRepository;

    @Autowired
    private UndoService undoService;

    @Autowired 
    private BangDiemRepository bangDiemRepository;
    
    // Trong SinhvienService.java
    /**
     * Kiểm tra xem sinh viên có thể xóa được không
     * 
     * @param maSV Mã sinh viên cần kiểm tra
     * @return true nếu sinh viên có thể xóa, false nếu không
     */
    public boolean canDelete(String maSV) {
        // Kiểm tra xem sinh viên có tồn tại không
        if (!sinhvienRepository.existsById(maSV)) {
            return false;
        }

        // Kiểm tra xem sinh viên có dữ liệu liên quan không
        // Ví dụ: kiểm tra bảng bài thi
        boolean hasExams = bangDiemRepository.existsBySinhvienMaSV(maSV);
        if (hasExams) {
            return false;
        }

        // Kiểm tra bảng khác nếu cần
        // boolean hasOtherRelatedData = otherRepository.existsBySinhvienMaSV(maSV);

        // Nếu không có dữ liệu liên quan, có thể xóa
        return true;
    }

    @Transactional(readOnly = true)
    public List<SinhvienDTO> getAllStudents() {
        return sinhvienRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SinhvienDTO> getStudentsByClass(String maLop) {
        return sinhvienRepository.findByLop_MaLop(maLop)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getStudentsByClassPaginated(String maLop, String searchTerm, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Sinhvien> studentPage;

        if (searchTerm != null && !searchTerm.isEmpty()) {
            studentPage = sinhvienRepository.searchByClassAndKeyword(maLop, searchTerm, pageable);
        } else {
            studentPage = sinhvienRepository.findByLop_MaLop(maLop, pageable);
        }

        List<SinhvienDTO> studentDTOs = studentPage.getContent()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("students", studentDTOs);
        response.put("currentPage", studentPage.getNumber());
        response.put("totalItems", studentPage.getTotalElements());
        response.put("totalPages", studentPage.getTotalPages());

        return response;
    }

    @Transactional(readOnly = true)
    public SinhvienDTO getStudentById(String maSV) {
        Sinhvien sinhvien = sinhvienRepository.findById(maSV)
                .orElseThrow(() -> new RuntimeException("Sinh viên không tồn tại: " + maSV));
        return convertToDTO(sinhvien);
    }

    @Transactional(readOnly = true)
    public List<SinhvienDTO> searchStudents(String maLop, String searchTerm) {
        return sinhvienRepository.searchByClassAndKeyword(maLop, searchTerm)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public SinhvienDTO addStudent(SinhvienDTO sinhvienDTO, String currentUser) {
        // Kiểm tra trùng lặp
        if (sinhvienRepository.existsById(sinhvienDTO.getMaSV())) {
            throw new RuntimeException("Mã sinh viên đã tồn tại: " + sinhvienDTO.getMaSV());
        }

        // Kiểm tra lớp tồn tại
        Lop lop = lopRepository.findById(sinhvienDTO.getMaLop())
                .orElseThrow(() -> new RuntimeException("Lớp không tồn tại: " + sinhvienDTO.getMaLop()));

        Sinhvien newSinhvien = new Sinhvien();
        newSinhvien.setMaSV(sinhvienDTO.getMaSV());
        newSinhvien.setHo(extractHo(sinhvienDTO.getHoTen()));
        newSinhvien.setTen(extractTen(sinhvienDTO.getHoTen()));
        newSinhvien.setNgaySinh(sinhvienDTO.getNgaySinh());
        newSinhvien.setDiaChi(sinhvienDTO.getDiaChi());
        newSinhvien.setLop(lop);

        Sinhvien savedSinhvien = sinhvienRepository.save(newSinhvien);

        // Lưu action để undo
        // undoService.pushUndoAction(currentUser, new UndoService.UndoAction(
        //         "SINHVIEN",
        //         "INSERT",
        //         savedSinhvien.getMaSV(),             
        //         null,
        //         savedSinhvien,
        //         () -> {
        //             EntityManager em = entityManagerFactory.createEntityManager();
        //             EntityTransaction tx = em.getTransaction();
        //              tx.begin();
            
        //     // Xóa chi tiết bài thi trước (nếu có)
        //     Query deleteChiTietQuery = em.createNativeQuery(
        //         "DELETE FROM ChiTietBaiThi WHERE MASV = :maSV");
        //     deleteChiTietQuery.setParameter("maSV", savedSinhvien.getMaSV());
        //     deleteChiTietQuery.executeUpdate();
            
        //     // Xóa bảng điểm (thử các tên bảng khác nhau)
           
                
           
        //             Query deleteBangDiemQuery = em.createNativeQuery(
        //                 "DELETE FROM BANGDIEM WHERE MASV = :maSV");
        //             deleteBangDiemQuery.setParameter("maSV", savedSinhvien.getMaSV());
        //             deleteBangDiemQuery.executeUpdate();
                
            
        //     tx.commit();
        
        
        // // Sau khi xóa dữ liệu liên quan, xóa sinh viên
        // sinhvienRepository.deleteById(savedSinhvien.getMaSV());
        
     
        //             sinhvienRepository.deleteById(savedSinhvien.getMaSV());
        //             return null;
               

        //         }));
        undoService.pushUndoAction(
                currentUser,
                "SINHVIEN", // entityType
                new UndoService.UndoAction(
                        "SINHVIEN", // entityType
                        "INSERT", // actionType
                        savedSinhvien.getMaSV(), // entityId
                        savedSinhvien.getHoTen(), // entityName - sử dụng họ tên sinh viên làm tên entity
                        () -> { // undoFunction
                            try {
                                EntityManager em = entityManagerFactory.createEntityManager();
                                EntityTransaction tx = em.getTransaction();
                                try {
                                    tx.begin();

                                    // Xóa chi tiết bài thi trước (nếu có)
                                    Query deleteChiTietQuery = em.createNativeQuery(
                                            "DELETE FROM ChiTietBaiThi WHERE MASV = :maSV");
                                    deleteChiTietQuery.setParameter("maSV", savedSinhvien.getMaSV());
                                    deleteChiTietQuery.executeUpdate();

                                    // Xóa bảng điểm
                                    Query deleteBangDiemQuery = em.createNativeQuery(
                                            "DELETE FROM BANGDIEM WHERE MASV = :maSV");
                                    deleteBangDiemQuery.setParameter("maSV", savedSinhvien.getMaSV());
                                    deleteBangDiemQuery.executeUpdate();

                                    tx.commit();
                                } catch (Exception e) {
                                    if (tx.isActive()) {
                                        tx.rollback();
                                    }
                                    throw e;
                                } finally {
                                    em.close();
                                }

                                // Sau khi xóa dữ liệu liên quan, xóa sinh viên
                                sinhvienRepository.deleteById(savedSinhvien.getMaSV());
                                return true;
                            } catch (Exception e) {
                                e.printStackTrace();
                                return false;
                            }
                        }));
                return convertToDTO(savedSinhvien);
    }

    @Transactional
    public SinhvienDTO updateStudent(String maSV, SinhvienDTO sinhvienDTO, String currentUser) {
        Sinhvien existingSinhvien = sinhvienRepository.findById(maSV)
                .orElseThrow(() -> new RuntimeException("Sinh viên không tồn tại: " + maSV));

        // Tạo bản sao cho undo
        Sinhvien originalSinhvien = cloneSinhvien(existingSinhvien);

        // Kiểm tra lớp tồn tại nếu thay đổi
        Lop lop = existingSinhvien.getLop();
        if (!existingSinhvien.getLop().getMaLop().equals(sinhvienDTO.getMaLop())) {
            lop = lopRepository.findById(sinhvienDTO.getMaLop())
                    .orElseThrow(() -> new RuntimeException("Lớp không tồn tại: " + sinhvienDTO.getMaLop()));
        }

        existingSinhvien.setHo(extractHo(sinhvienDTO.getHoTen()));
        existingSinhvien.setTen(extractTen(sinhvienDTO.getHoTen()));
        existingSinhvien.setNgaySinh(sinhvienDTO.getNgaySinh());
        existingSinhvien.setDiaChi(sinhvienDTO.getDiaChi());
        existingSinhvien.setLop(lop);

        Sinhvien updatedSinhvien = sinhvienRepository.save(existingSinhvien);

        // Lưu action để undo
        // undoService.pushUndoAction(currentUser, new UndoService.UndoAction(
        //         "SINHVIEN",
        //         "EDIT",
        //         updatedSinhvien.getMaSV(),
        //         originalSinhvien,
        //         updatedSinhvien,
        //         () -> {
        //             Sinhvien revertSinhvien = sinhvienRepository.findById(originalSinhvien.getMaSV())
        //                     .orElse(null);
        //             if (revertSinhvien != null) {
        //                 revertSinhvien.setHo(originalSinhvien.getHo());
        //                 revertSinhvien.setTen(originalSinhvien.getTen());
        //                 revertSinhvien.setNgaySinh(originalSinhvien.getNgaySinh());
        //                 revertSinhvien.setDiaChi(originalSinhvien.getDiaChi());

        //                 // Lấy lại lớp gốc
        //                 Lop originalLop = lopRepository.findById(originalSinhvien.getLop().getMaLop()).orElse(null);
        //                 if (originalLop != null) {
        //                     revertSinhvien.setLop(originalLop);
        //                 }

        //                 sinhvienRepository.save(revertSinhvien);
        //                 return convertToDTO(revertSinhvien);
        //             }
        //             return null;
        //         }));
        undoService.pushUndoAction(
                currentUser,
                "SINHVIEN", // entityType
                new UndoService.UndoAction(
                        "SINHVIEN", // entityType
                        "EDIT", // actionType (giữ nguyên "EDIT" thay vì "UPDATE" để đảm bảo tương thích với
                                // code hiện tại)
                        updatedSinhvien.getMaSV(), // entityId
                        originalSinhvien.getHoTen(), // entityName - sử dụng họ tên ban đầu của sinh viên
                        () -> { // undoFunction
                            try {
                                Sinhvien revertSinhvien = sinhvienRepository.findById(originalSinhvien.getMaSV())
                                        .orElse(null);
                                if (revertSinhvien != null) {
                                    revertSinhvien.setHo(originalSinhvien.getHo());
                                    revertSinhvien.setTen(originalSinhvien.getTen());
                                    revertSinhvien.setNgaySinh(originalSinhvien.getNgaySinh());
                                    revertSinhvien.setDiaChi(originalSinhvien.getDiaChi());

                                    // Lấy lại lớp gốc
                                    Lop originalLop = lopRepository.findById(originalSinhvien.getLop().getMaLop())
                                            .orElse(null);
                                    if (originalLop != null) {
                                        revertSinhvien.setLop(originalLop);
                                    }

                                    Sinhvien savedSinhvien = sinhvienRepository.save(revertSinhvien);
                                    return convertToDTO(savedSinhvien);
                                }
                                return null;
                            } catch (Exception e) {
                                e.printStackTrace();
                                return null;
                            }
                        })      );
        return convertToDTO(updatedSinhvien);
    }

    // @Transactional
    // public void deleteStudent(String maSV, String currentUser) {
    //     try{

    //         Sinhvien sinhvien = sinhvienRepository.findById(maSV)
    //                 .orElseThrow(() -> new RuntimeException("Sinh viên không tồn tại: " + maSV));

    //         // Tạo bản sao cho undo
    //         Sinhvien deletedSinhvien = cloneSinhvien(sinhvien);
    //         String lopId = deletedSinhvien.getLop().getMaLop();
    //         System.out.println("Lớp của sinh viên: " + lopId);
    //         // Kiểm tra lớp tồn tại
    //         sinhvienRepository.deleteById(maSV);
    //         System.out.println("Đã xóa sinh viên: " + maSV);
    //         System.out.println("Lớp của sinh viên: " + lopId);
    //         System.out.println("Trạng thái xóa:" + sinhvienRepository.existsById(maSV));
    //     }
    //     catch (Exception e) {

    //     e.printStackTrace();
    //     throw new RuntimeException("Lỗi khi xóa sinh viên: " + e.getMessage());
    //     }

    //     // Lưu action để undo
    //     // undoService.pushUndoAction(currentUser, new UndoService.UndoAction(
    //     //         "SINHVIEN",
    //     //         "DELETE",
    //     //         deletedSinhvien.getMaSV(),
    //     //         deletedSinhvien.getHoTen(),
    //     //         deletedSinhvien,
    //     //         null,
    //     //         () -> {
    //     //             // Kiểm tra lớp còn tồn tại không
    //     //             Lop lop = lopRepository.findById(lopId).orElse(null);
    //     //             if (lop == null) {
    //     //                 return null; // Nếu lớp không tồn tại thì không thể khôi phục
    //     //             }

    //     //             // Tạo mới sinh viên với thông tin cũ
    //     //             Sinhvien newSinhvien = new Sinhvien();
    //     //             newSinhvien.setMaSV(deletedSinhvien.getMaSV());
    //     //             newSinhvien.setHo(deletedSinhvien.getHo());
    //     //             newSinhvien.setTen(deletedSinhvien.getTen());
    //     //             newSinhvien.setNgaySinh(deletedSinhvien.getNgaySinh());
    //     //             newSinhvien.setDiaChi(deletedSinhvien.getDiaChi());
    //     //             newSinhvien.setLop(lop);

    //     //             Sinhvien restoredSinhvien = sinhvienRepository.save(newSinhvien);
    //     //             return convertToDTO(restoredSinhvien);
    //     //         }));
    // }
@Transactional
public void deleteStudent(String maSV, String currentUser) {
    try {
        Sinhvien sinhvien = sinhvienRepository.findById(maSV)
                .orElseThrow(() -> new RuntimeException("Sinh viên không tồn tại: " + maSV));
        
        // Tạo bản sao cho undo
        Sinhvien deletedSinhvien = cloneSinhvien(sinhvien);
        String lopId = deletedSinhvien.getLop().getMaLop();
        System.out.println("Lớp của sinh viên: " + lopId);
        
        // Lưu dữ liệu liên quan trước khi xóa
        EntityManager em = entityManagerFactory.createEntityManager();
        
        // 1. Lưu dữ liệu bảng điểm - chỉ định chính xác các cột theo entity BangDiem
        List<Map<String, Object>> bangDiemData = new ArrayList<>();
        try {
            Query queryBangDiem = em.createNativeQuery(
                "SELECT bd.MASV, bd.MAMH, bd.LAN, bd.NGAYTHI, bd.DIEM FROM BANGDIEM bd WHERE bd.MASV = :maSV");
            queryBangDiem.setParameter("maSV", maSV);
            List<Object[]> bangDiemResults = queryBangDiem.getResultList();
            
            for (Object[] row : bangDiemResults) {
                Map<String, Object> record = new HashMap<>();
                record.put("MASV", row[0]);
                record.put("MAMH", row[1]);
                record.put("LAN", row[2]);
                record.put("NGAYTHI", row[3]);
                record.put("DIEM", row[4]);
                bangDiemData.add(record);
            }
            System.out.println("Đã lưu " + bangDiemData.size() + " bản ghi bảng điểm để khôi phục sau này");
        } catch (Exception e) {
            System.out.println("Không thể đọc dữ liệu từ bảng điểm: " + e.getMessage());
        }
        
        // 2. Lưu dữ liệu chi tiết bài thi - chỉ định chính xác các cột theo entity ChiTietBaiThi
        List<Map<String, Object>> chiTietBaiThiData = new ArrayList<>();
        try {
            Query queryChiTiet = em.createNativeQuery(
                "SELECT ct.MASV, ct.MAMH, ct.LAN, ct.CAUHOI, ct.TRALOI FROM ChiTietBaiThi ct WHERE ct.MASV = :maSV");
            queryChiTiet.setParameter("maSV", maSV);
            List<Object[]> chiTietResults = queryChiTiet.getResultList();
            
            for (Object[] row : chiTietResults) {
                Map<String, Object> record = new HashMap<>();
                record.put("MASV", row[0]);
                record.put("MAMH", row[1]);
                record.put("LAN", row[2]);
                record.put("CAUHOI", row[3]);
                record.put("TRALOI", row[4]);
                chiTietBaiThiData.add(record);
            }
            System.out.println("Đã lưu " + chiTietBaiThiData.size() + " bản ghi chi tiết bài thi để khôi phục sau này");
        } catch (Exception e) {
            System.out.println("Không thể đọc dữ liệu từ bảng chi tiết bài thi: " + e.getMessage());
        }
        
        // Tiến hành xóa dữ liệu
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            
            // Xóa chi tiết bài thi trước (nếu có)
            Query deleteChiTietQuery = em.createNativeQuery(
                "DELETE FROM ChiTietBaiThi WHERE MASV = :maSV");
            deleteChiTietQuery.setParameter("maSV", maSV);
            int chiTietDeleted = deleteChiTietQuery.executeUpdate();
            System.out.println("Đã xóa " + chiTietDeleted + " bản ghi chi tiết bài thi");
            
            // Xóa bảng điểm
            Query deleteBangDiemQuery = em.createNativeQuery(
                "DELETE FROM BANGDIEM WHERE MASV = :maSV");
            deleteBangDiemQuery.setParameter("maSV", maSV);
            int bangDiemDeleted = deleteBangDiemQuery.executeUpdate();
            System.out.println("Đã xóa " + bangDiemDeleted + " bản ghi bảng điểm");
            
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Lỗi khi xóa dữ liệu liên quan: " + e.getMessage());
        } finally {
            em.close();
        }
        
        // Sau khi xóa dữ liệu liên quan, xóa sinh viên
        sinhvienRepository.deleteById(maSV);
        
        System.out.println("Đã xóa sinh viên: " + maSV);
        System.out.println("Trạng thái xóa: " + !sinhvienRepository.existsById(maSV));
        
        // Lưu action để undo, bao gồm cả dữ liệu liên quan
        // undoService.pushUndoAction(currentUser, new UndoService.UndoAction(
        //         "SINHVIEN",
        //         "DELETE",
        //         deletedSinhvien.getMaSV(),
        //         deletedSinhvien,
        //         null,
        //         () -> {
        //             try {
        //                 // 1. Kiểm tra lớp còn tồn tại không
        //                 Lop lop = lopRepository.findById(lopId)
        //                         .orElseThrow(() -> new RuntimeException("Lớp không tồn tại: " + lopId));
                        
        //                 // 2. Tạo mới sinh viên với thông tin cũ
        //                 Sinhvien newSinhvien = new Sinhvien();
        //                 newSinhvien.setMaSV(deletedSinhvien.getMaSV());
        //                 newSinhvien.setHo(deletedSinhvien.getHo());
        //                 newSinhvien.setTen(deletedSinhvien.getTen());
        //                 newSinhvien.setNgaySinh(deletedSinhvien.getNgaySinh());
        //                 newSinhvien.setDiaChi(deletedSinhvien.getDiaChi());
        //                 newSinhvien.setLop(lop);
                        
        //                 System.out.println("Đang khôi phục sinh viên: " + newSinhvien.getMaSV());
        //                 Sinhvien restoredSinhvien = sinhvienRepository.save(newSinhvien);
                        
        //                 // 3. Khôi phục dữ liệu bảng điểm
        //                 if (!bangDiemData.isEmpty()) {
        //                     EntityManager emRestore = entityManagerFactory.createEntityManager();
        //                     EntityTransaction txRestore = emRestore.getTransaction();
        //                     try {
        //                         txRestore.begin();
                                
        //                         for (Map<String, Object> record : bangDiemData) {
        //                             // Sử dụng tên bảng BANGDIEM như đã khai báo trong entity
        //                             String sql = "INSERT INTO BANGDIEM (MASV, MAMH, LAN, NGAYTHI, DIEM) VALUES (?, ?, ?, ?, ?)";
                                    
        //                             Query insertQuery = emRestore.createNativeQuery(sql);
        //                             insertQuery.setParameter(1, record.get("MASV"));
        //                             insertQuery.setParameter(2, record.get("MAMH"));
        //                             insertQuery.setParameter(3, record.get("LAN"));
        //                             insertQuery.setParameter(4, record.get("NGAYTHI"));
        //                             insertQuery.setParameter(5, record.get("DIEM"));
        //                             insertQuery.executeUpdate();
        //                         }
                                
        //                         System.out.println("Đã khôi phục " + bangDiemData.size() + " bản ghi bảng điểm");
        //                         txRestore.commit();
        //                     } catch (Exception e) {
        //                         if (txRestore != null && txRestore.isActive()) {
        //                             txRestore.rollback();
        //                         }
        //                         System.err.println("Lỗi khi khôi phục dữ liệu bảng điểm: " + e.getMessage());
        //                         e.printStackTrace();
        //                     } finally {
        //                         emRestore.close();
        //                     }
        //                 }
                        
        //                 // 4. Khôi phục dữ liệu chi tiết bài thi - sử dụng các trường chính xác
        //                 if (!chiTietBaiThiData.isEmpty()) {
        //                     EntityManager emRestore = entityManagerFactory.createEntityManager();
        //                     EntityTransaction txRestore = emRestore.getTransaction();
        //                     try {
        //                         txRestore.begin();
                                
        //                         for (Map<String, Object> record : chiTietBaiThiData) {
        //                             String sql = "INSERT INTO ChiTietBaiThi (MASV, MAMH, LAN, CAUHOI, TRALOI) VALUES (?, ?, ?, ?, ?)";
                                    
        //                             Query insertQuery = emRestore.createNativeQuery(sql);
        //                             insertQuery.setParameter(1, record.get("MASV"));
        //                             insertQuery.setParameter(2, record.get("MAMH"));
        //                             insertQuery.setParameter(3, record.get("LAN"));
        //                             insertQuery.setParameter(4, record.get("CAUHOI"));
        //                             insertQuery.setParameter(5, record.get("TRALOI"));
        //                             insertQuery.executeUpdate();
        //                         }
                                
        //                         System.out.println("Đã khôi phục " + chiTietBaiThiData.size() + " bản ghi chi tiết bài thi");
        //                         txRestore.commit();
        //                     } catch (Exception e) {
        //                         if (txRestore != null && txRestore.isActive()) {
        //                             txRestore.rollback();
        //                         }
        //                         System.err.println("Lỗi khi khôi phục dữ liệu chi tiết bài thi: " + e.getMessage());
        //                         e.printStackTrace();
        //                     } finally {
        //                         emRestore.close();
        //                     }
        //                 }
                        
        //                 System.out.println("Đã khôi phục sinh viên và dữ liệu liên quan thành công!");
        //                 return convertToDTO(restoredSinhvien);
        //             } catch (Exception e) {
        //                 System.err.println("Lỗi khi khôi phục sinh viên: " + e.getMessage());
        //                 e.printStackTrace();
        //                 return null;
        //             }
        //         }
        // ));
        undoService.pushUndoAction(
                currentUser,
                "SINHVIEN", // entityType
                new UndoService.UndoAction(
                        "SINHVIEN", // entityType
                        "DELETE", // actionType
                        deletedSinhvien.getMaSV(), // entityId
                        deletedSinhvien.getHoTen(), // entityName - sử dụng họ tên của sinh viên bị xóa
                        () -> { // undoFunction
                            try {
                                // 1. Kiểm tra lớp còn tồn tại không
                                Lop lop = lopRepository.findById(lopId)
                                        .orElseThrow(() -> new RuntimeException("Lớp không tồn tại: " + lopId));

                                // 2. Tạo mới sinh viên với thông tin cũ
                                Sinhvien newSinhvien = new Sinhvien();
                                newSinhvien.setMaSV(deletedSinhvien.getMaSV());
                                newSinhvien.setHo(deletedSinhvien.getHo());
                                newSinhvien.setTen(deletedSinhvien.getTen());
                                newSinhvien.setNgaySinh(deletedSinhvien.getNgaySinh());
                                newSinhvien.setDiaChi(deletedSinhvien.getDiaChi());
                                newSinhvien.setLop(lop);

                                System.out.println("Đang khôi phục sinh viên: " + newSinhvien.getMaSV());
                                Sinhvien restoredSinhvien = sinhvienRepository.save(newSinhvien);

                                // 3. Khôi phục dữ liệu bảng điểm
                                if (!bangDiemData.isEmpty()) {
                                    EntityManager emRestore = entityManagerFactory.createEntityManager();
                                    EntityTransaction txRestore = emRestore.getTransaction();
                                    try {
                                        txRestore.begin();

                                        for (Map<String, Object> record : bangDiemData) {
                                            // Sử dụng tên bảng BANGDIEM như đã khai báo trong entity
                                            String sql = "INSERT INTO BANGDIEM (MASV, MAMH, LAN, NGAYTHI, DIEM) VALUES (?, ?, ?, ?, ?)";

                                            Query insertQuery = emRestore.createNativeQuery(sql);
                                            insertQuery.setParameter(1, record.get("MASV"));
                                            insertQuery.setParameter(2, record.get("MAMH"));
                                            insertQuery.setParameter(3, record.get("LAN"));
                                            insertQuery.setParameter(4, record.get("NGAYTHI"));
                                            insertQuery.setParameter(5, record.get("DIEM"));
                                            insertQuery.executeUpdate();
                                        }

                                        System.out
                                                .println("Đã khôi phục " + bangDiemData.size() + " bản ghi bảng điểm");
                                        txRestore.commit();
                                    } catch (Exception e) {
                                        if (txRestore != null && txRestore.isActive()) {
                                            txRestore.rollback();
                                        }
                                        System.err.println("Lỗi khi khôi phục dữ liệu bảng điểm: " + e.getMessage());
                                        e.printStackTrace();
                                    } finally {
                                        emRestore.close();
                                    }
                                }

                                // 4. Khôi phục dữ liệu chi tiết bài thi - sử dụng các trường chính xác
                                if (!chiTietBaiThiData.isEmpty()) {
                                    EntityManager emRestore = entityManagerFactory.createEntityManager();
                                    EntityTransaction txRestore = emRestore.getTransaction();
                                    try {
                                        txRestore.begin();

                                        for (Map<String, Object> record : chiTietBaiThiData) {
                                            String sql = "INSERT INTO ChiTietBaiThi (MASV, MAMH, LAN, CAUHOI, TRALOI) VALUES (?, ?, ?, ?, ?)";

                                            Query insertQuery = emRestore.createNativeQuery(sql);
                                            insertQuery.setParameter(1, record.get("MASV"));
                                            insertQuery.setParameter(2, record.get("MAMH"));
                                            insertQuery.setParameter(3, record.get("LAN"));
                                            insertQuery.setParameter(4, record.get("CAUHOI"));
                                            insertQuery.setParameter(5, record.get("TRALOI"));
                                            insertQuery.executeUpdate();
                                        }

                                        System.out.println("Đã khôi phục " + chiTietBaiThiData.size()
                                                + " bản ghi chi tiết bài thi");
                                        txRestore.commit();
                                    } catch (Exception e) {
                                        if (txRestore != null && txRestore.isActive()) {
                                            txRestore.rollback();
                                        }
                                        System.err.println(
                                                "Lỗi khi khôi phục dữ liệu chi tiết bài thi: " + e.getMessage());
                                        e.printStackTrace();
                                    } finally {
                                        emRestore.close();
                                    }
                                }

                                System.out.println("Đã khôi phục sinh viên và dữ liệu liên quan thành công!");
                                return convertToDTO(restoredSinhvien);
                            } catch (Exception e) {
                                System.err.println("Lỗi khi khôi phục sinh viên: " + e.getMessage());
                                e.printStackTrace();
                                return null;
                            }
                        }));
    } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Lỗi khi xóa sinh viên: " + e.getMessage());
    }
}

    private SinhvienDTO convertToDTO(Sinhvien sinhvien) {
        if (sinhvien == null)
            return null;

        SinhvienDTO dto = new SinhvienDTO();
        dto.setMaSV(sinhvien.getMaSV());
        dto.setHo(sinhvien.getHo());
        dto.setTen(sinhvien.getTen());
        dto.setHoTen(sinhvien.getHoTen());
        dto.setNgaySinh(sinhvien.getNgaySinh());
        dto.setDiaChi(sinhvien.getDiaChi());
        dto.setMaLop(sinhvien.getLop().getMaLop());
        dto.setTenLop(sinhvien.getLop().getTenLop());
        return dto;
    }

    private Sinhvien cloneSinhvien(Sinhvien source) {
        Sinhvien clone = new Sinhvien();
        clone.setMaSV(source.getMaSV());
        clone.setHo(source.getHo());
        clone.setTen(source.getTen());
        clone.setNgaySinh(source.getNgaySinh());
        clone.setDiaChi(source.getDiaChi());
        clone.setLop(source.getLop());
        return clone;
    }

    private String extractHo(String hoTen) {
        if (hoTen == null || hoTen.isEmpty()) {
            return "";
        }

        int lastSpaceIndex = hoTen.lastIndexOf(" ");
        if (lastSpaceIndex > 0) {
            return hoTen.substring(0, lastSpaceIndex).trim();
        }

        return "";
    }

    private String extractTen(String hoTen) {
        if (hoTen == null || hoTen.isEmpty()) {
            return "";
        }

        int lastSpaceIndex = hoTen.lastIndexOf(" ");
        if (lastSpaceIndex > 0) {
            return hoTen.substring(lastSpaceIndex + 1).trim();
        }

        return hoTen.trim();
    }
    
    @Transactional
    public Object undoAction(String currentUser,String role) {
        try {
            return undoService.undoLastAction(currentUser,role);
        } catch (Exception e) {
            System.err.println("Error in undoAction: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public boolean canUndo(String currentUser,String role) {
        return undoService.canUndo(currentUser,role);
    }
    public UndoActionDTO getLastUndoAction(String currentUser,String role) {
        return undoService.getLastUndoAction(currentUser,role);
    }

}