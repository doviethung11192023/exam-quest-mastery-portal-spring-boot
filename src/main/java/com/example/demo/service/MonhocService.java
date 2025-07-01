package com.example.demo.service;

import com.example.demo.dto.MonhocDTO;
import com.example.demo.dto.UndoActionDTO;
import com.example.demo.entity.ChiTietBaiThi;
import com.example.demo.entity.Monhoc;
import com.example.demo.repository.BangDiemRepository;
import com.example.demo.repository.BodeRepository;
import com.example.demo.repository.ChiTietBaiThiRepository;
import com.example.demo.repository.GiaovienDangkyRepository;
import com.example.demo.repository.MonhocRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MonhocService {

    @Autowired
    private MonhocRepository monhocRepository;

    @Autowired
    private UndoService undoService;

    @Autowired
    private GiaovienDangkyRepository giaovienDangkyRepository;

    @Autowired
    private BodeRepository boDeRepository;

    @Autowired
    private ChiTietBaiThiRepository baiThiRepository;

    @Autowired
    private BangDiemRepository bangDiemRepository;

    // Find all subjects
    public List<MonhocDTO> findAll() {
        List<Monhoc> subjects = monhocRepository.findAll();
        return subjects.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Find subject by ID
    public MonhocDTO findById(String maMH) {
        Optional<Monhoc> subject = monhocRepository.findById(maMH);
        return subject.map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Subject not found with ID: " + maMH));
    }

    // Create a new subject
    @Transactional
    public MonhocDTO create(MonhocDTO monhocDTO, String userId) {
        // Check if code already exists
        if (monhocRepository.findById(monhocDTO.getMaMH()).isPresent()) {
            throw new RuntimeException("Subject with code " + monhocDTO.getMaMH() + " already exists");
        }

        // Convert DTO to entity
        Monhoc monhoc = new Monhoc();
        monhoc.setMaMH(monhocDTO.getMaMH());
        monhoc.setTenMH(monhocDTO.getTenMH());

        // Save the entity
        Monhoc savedMonhoc = monhocRepository.save(monhoc);

        // Register undo action
        // undoService.pushUndoAction(userId,
        //         new UndoService.UndoAction(
        //                 "MONHOC",
        //                 "INSERT",
        //                 savedMonhoc.getMaMH(),
        //                 null, // No original state since this is a new subject
        //                 savedMonhoc,
        //                 () -> {
        //                     monhocRepository.deleteById(savedMonhoc.getMaMH());
        //                     return savedMonhoc;
        //                 }));
        undoService.pushUndoAction(
                userId,
                "MONHOC", // entityType
                new UndoService.UndoAction(
                        "MONHOC", // entityType
                        "INSERT", // actionType
                        savedMonhoc.getMaMH(), // entityId
                        savedMonhoc.getTenMH(), // entityName - sử dụng tên môn học làm tên entity
                        () -> { // undoFunction
                            try {
                                monhocRepository.deleteById(savedMonhoc.getMaMH());
                                return savedMonhoc;
                            } catch (Exception e) {
                                e.printStackTrace();
                                return null;
                            }
                        }));
        return convertToDTO(savedMonhoc);
    }

    // Update an existing subject
    @Transactional
    public MonhocDTO update(MonhocDTO monhocDTO, String userId) {
        // Find existing subject
        Monhoc existingMonhoc = monhocRepository.findById(monhocDTO.getMaMH())
                .orElseThrow(() -> new RuntimeException("Subject not found with ID: " + monhocDTO.getMaMH()));

        // Save original state for undo
        Monhoc originalState = new Monhoc();
        originalState.setMaMH(existingMonhoc.getMaMH());
        originalState.setTenMH(existingMonhoc.getTenMH());

        // Update fields
        existingMonhoc.setTenMH(monhocDTO.getTenMH());

        // Save updated entity
        Monhoc updatedMonhoc = monhocRepository.save(existingMonhoc);

        // Register undo action
        // undoService.pushUndoAction(userId,
        //         new UndoService.UndoAction(
        //                 "MONHOC",
        //                 "UPDATE",
        //                 updatedMonhoc.getMaMH(),
        //                 originalState,
        //                 updatedMonhoc,
        //                 () -> {
        //                     monhocRepository.save(originalState);
        //                     return originalState;
        //                 }));
        undoService.pushUndoAction(
                userId,
                "MONHOC", // entityType
                new UndoService.UndoAction(
                        "MONHOC", // entityType
                        "UPDATE", // actionType
                        updatedMonhoc.getMaMH(), // entityId
                        originalState.getTenMH(), // entityName - sử dụng tên môn học ban đầu làm tên entity
                        () -> { // undoFunction
                            try {
                                Monhoc restoredMonhoc = monhocRepository.save(originalState);
                                return restoredMonhoc;
                            } catch (Exception e) {
                                e.printStackTrace();
                                return null;
                            }
                        }));

        return convertToDTO(updatedMonhoc);
    }

    // Delete a subject
    @Transactional
    public void delete(String maMH, String userId) {
        // Find existing subject
        Monhoc existingMonhoc = monhocRepository.findById(maMH)
                .orElseThrow(() -> new RuntimeException("Subject not found with ID: " + maMH));

        // Save original state for undo
        Monhoc originalState = new Monhoc();
        originalState.setMaMH(existingMonhoc.getMaMH());
        originalState.setTenMH(existingMonhoc.getTenMH());

        // Delete the entity
        monhocRepository.deleteById(maMH);

        // Register undo action
        // undoService.pushUndoAction(userId,
        //         new UndoService.UndoAction(
        //                 "MONHOC",
        //                 "DELETE",
        //                 maMH,
        //                 originalState,
        //                 null, // No new state since the subject was deleted
        //                 () -> {
        //                     monhocRepository.save(originalState);
        //                     return originalState;
        //                 }));
        undoService.pushUndoAction(
                userId,
                "MONHOC", // entityType
                new UndoService.UndoAction(
                        "MONHOC", // entityType
                        "DELETE", // actionType
                        maMH, // entityId
                        originalState.getTenMH(), // entityName - sử dụng tên môn học bị xóa làm tên entity
                        () -> { // undoFunction
                            try {
                                Monhoc restoredMonhoc = monhocRepository.save(originalState);
                                return restoredMonhoc;
                            } catch (Exception e) {
                                e.printStackTrace();
                                return null;
                            }
                        }));
    }

    // Search subjects by name or code
    public List<MonhocDTO> search(String keyword) {
        String searchTerm = "%" + keyword.toLowerCase() + "%";
        List<Monhoc> subjects = monhocRepository.findByMaMHLikeOrTenMHLikeIgnoreCase(searchTerm, searchTerm);
        return subjects.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Thêm phương thức này vào MonhocService
    /**
     * Kiểm tra xem một môn học có thể xóa được không
     * 
     * @param maMH Mã môn học cần kiểm tra
     * @return true nếu môn học có thể xóa, false nếu không
     */
    public boolean canDelete(String maMH) {
        // Kiểm tra xem môn học có tồn tại không
        if (!monhocRepository.existsById(maMH)) {
            return false;
        }

        // Kiểm tra xem môn học có được sử dụng trong bảng GiaoVienDangKy không
        boolean usedInGVDK = giaovienDangkyRepository.existsByMonhocMaMH(maMH);
        if (usedInGVDK) {
            System.out.println("Môn học " + maMH + " đang được sử dụng trong bảng GiaoVienDangKy.");
            return false;
        }

        // Kiểm tra xem môn học có được sử dụng trong bảng BoDe không
        boolean usedInBoDe = boDeRepository.existsByMonhocMaMH(maMH);
        if (usedInBoDe) {
            System.out.println("Môn học " + maMH + " đang được sử dụng trong bảng BoDe.");
            return false;
        }

        // Kiểm tra xem môn học có được sử dụng trong bảng BaiThi không
        boolean usedInBaiThi = bangDiemRepository.existsByMonhocMaMH(maMH);
        if (usedInBaiThi) {
            System.out.println("Môn học " + maMH + " đang được sử dụng trong bảng BangDiem.");
            return false;
        }

        // Nếu không có ràng buộc nào, có thể xóa
        return true;
    }
    // Undo the last action
    @Transactional
    public UndoActionDTO undo(String userId,String role) {
        try {
            Object result = undoService.undoLastAction(userId,role);
            if (result != null) {
                return undoService.getLastUndoAction(userId,role);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Error undoing last action: " + e.getMessage(), e);
        }
    }

    // Check if undo is available
    public boolean canUndo(String userId,String role) {
        return undoService.canUndo(userId,role);
    }

    // Get the last undo action available
    public UndoActionDTO getLastUndoAction(String userId,String role) {
        return undoService.getLastUndoAction(userId,role);
    }

    // Helper method to convert Entity to DTO
    private MonhocDTO convertToDTO(Monhoc monhoc) {
        MonhocDTO dto = new MonhocDTO();
        dto.setMaMH(monhoc.getMaMH());
        dto.setTenMH(monhoc.getTenMH());
        return dto;
    }
}