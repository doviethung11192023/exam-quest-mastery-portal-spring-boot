package com.example.demo.service;

import com.example.demo.dto.GiaovienDTO;
import com.example.demo.dto.UndoActionDTO;
import com.example.demo.entity.Giaovien;
import com.example.demo.repository.GiaovienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GiaovienService {

    @Autowired
    private GiaovienRepository giaovienRepository;

    @Autowired
    private UndoService undoService;

    // DTO conversion methods
    private GiaovienDTO convertToDTO(Giaovien giaovien) {
        if (giaovien == null)
            return null;

        GiaovienDTO dto = new GiaovienDTO();
        dto.setMagv(giaovien.getMagv());
        dto.setHo(giaovien.getHo());
        dto.setTen(giaovien.getTen());
        dto.setHoTen(giaovien.getHo() + " " + giaovien.getTen());
        dto.setSoDienThoai(giaovien.getSoDienThoai());
        dto.setDiaChi(giaovien.getDiaChi());
        dto.setTrangThai(giaovien.getTrangThai());
        dto.setHasAccount(giaovien.getHasAccount());
        return dto;
    }

    private Giaovien convertToEntity(GiaovienDTO dto) {
        Giaovien entity = new Giaovien();
        entity.setMagv(dto.getMagv());
        entity.setHo(dto.getHo());
        entity.setTen(dto.getTen());
        entity.setSoDienThoai(dto.getSoDienThoai());
        entity.setDiaChi(dto.getDiaChi());
        entity.setTrangThai(dto.getTrangThai() != null ? dto.getTrangThai() : true);
        return entity;
    }

    @Transactional(readOnly = true)
    public List<GiaovienDTO> getAllGiaovien() {
        try {
            List<Giaovien> giaoviens = giaovienRepository.findAllActive();
            return giaoviens.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error in getAllGiaovien: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Kiểm tra xem giáo viên có thể xóa được không
     * 
     * @param magv Mã giáo viên cần kiểm tra
     * @return true nếu giáo viên có thể xóa, false nếu không
     */
    @Transactional(readOnly = true)
    public boolean canDelete(String magv) {
        try {
            // Kiểm tra xem giáo viên có tồn tại không
            Optional<Giaovien> giaovienOpt = giaovienRepository.findById(magv);
            if (giaovienOpt.isEmpty()) {
                return false;
            }

            Giaovien giaovien = giaovienOpt.get();

            // Kiểm tra xem giáo viên có bộ đề không
            if (giaovien.getDanhSachBoDe() != null && !giaovien.getDanhSachBoDe().isEmpty()) {
                return false;
            }

            // Kiểm tra xem giáo viên có đăng ký (lớp học) không
            if (giaovien.getDanhSachDangKy() != null && !giaovien.getDanhSachDangKy().isEmpty()) {
                return false;
            }

            // Nếu không có dữ liệu liên quan, có thể xóa
            return true;
        } catch (Exception e) {
            System.err.println("Error in canDelete: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Transactional(readOnly = true)
    public List<GiaovienDTO> searchGiaovien(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return getAllGiaovien();
            }

            List<Giaovien> giaoviens = giaovienRepository.searchGiaovien(keyword);
            return giaoviens.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error in searchGiaovien: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Transactional(readOnly = true)
    public GiaovienDTO getGiaovienById(String magv) {
        try {
            Optional<Giaovien> giaovienOpt = giaovienRepository.findById(magv);
            return giaovienOpt.map(this::convertToDTO).orElse(null);
        } catch (Exception e) {
            System.err.println("Error in getGiaovienById: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Transactional
    public GiaovienDTO createGiaovien(GiaovienDTO giaovienDTO, String currentUser) {
        try {
            // Validate input
            if (giaovienDTO.getMagv() == null || giaovienDTO.getHo() == null || giaovienDTO.getTen() == null) {
                throw new IllegalArgumentException("Required fields (MAGV, HO, TEN) cannot be null");
            }

            // Check if ID already exists
            if (giaovienRepository.existsById(giaovienDTO.getMagv())) {
                throw new IllegalArgumentException("Lecturer with ID " + giaovienDTO.getMagv() + " already exists");
            }

            // Truncate too long fields to prevent DB errors
            if (giaovienDTO.getSoDienThoai() != null && giaovienDTO.getSoDienThoai().length() > 15) {
                giaovienDTO.setSoDienThoai(giaovienDTO.getSoDienThoai().substring(0, 15));
            }

            if (giaovienDTO.getDiaChi() != null && giaovienDTO.getDiaChi().length() > 50) {
                giaovienDTO.setDiaChi(giaovienDTO.getDiaChi().substring(0, 50));
            }

            // Set default status to active
            giaovienDTO.setTrangThai(true);

            // Convert to entity and save
            Giaovien giaovien = convertToEntity(giaovienDTO);
            Giaovien savedGiaovien = giaovienRepository.save(giaovien);
            GiaovienDTO savedDTO = convertToDTO(savedGiaovien);

            // Save undo action
            final String magv = giaovienDTO.getMagv();
            // undoService.pushUndoAction(currentUser, new UndoService.UndoAction(
            //         "GIAOVIEN",
            //         "INSERT",
            //         magv,
            //         null,
            //         savedDTO,
            //         () -> {
            //             try {
            //                 giaovienRepository.deleteById(magv);
            //                 return true;
            //             } catch (Exception e) {
            //                 e.printStackTrace();
            //                 return false;
            //             }
            //         }));
            undoService.pushUndoAction(
                    currentUser,
                    "GIAOVIEN", // entityType
                    new UndoService.UndoAction(
                            "GIAOVIEN", // entityType
                            "INSERT", // actionType
                            magv, // entityId
                            savedDTO.getHoTen() != null ? savedDTO.getHoTen()
                                    : (savedDTO.getHo() + " " + savedDTO.getTen()), // entityName
                            () -> { // undoFunction
                                try {
                                    giaovienRepository.deleteById(magv);
                                    return true;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return false;
                                }
                            })          );

            return savedDTO;
        } catch (Exception e) {
            System.err.println("Error in createGiaovien: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    public GiaovienDTO updateGiaovien(String magv, GiaovienDTO giaovienDTO, String currentUser) {
        try {
            // Validate existence
            Optional<Giaovien> giaovienOpt = giaovienRepository.findById(magv);
            if (giaovienOpt.isEmpty()) {
                throw new IllegalArgumentException("Lecturer with ID " + magv + " not found");
            }

            // Save original state for undo
            Giaovien originalGiaovien = giaovienOpt.get();
            GiaovienDTO originalDTO = convertToDTO(originalGiaovien);

            // Truncate too long fields to prevent DB errors
            if (giaovienDTO.getSoDienThoai() != null && giaovienDTO.getSoDienThoai().length() > 15) {
                giaovienDTO.setSoDienThoai(giaovienDTO.getSoDienThoai().substring(0, 15));
            }

            if (giaovienDTO.getDiaChi() != null && giaovienDTO.getDiaChi().length() > 50) {
                giaovienDTO.setDiaChi(giaovienDTO.getDiaChi().substring(0, 50));
            }

            // Update entity fields but keep ID the same
            giaovienDTO.setMagv(magv);
            Giaovien giaovien = convertToEntity(giaovienDTO);

            // Ensure relationships are maintained
            giaovien.setDanhSachBoDe(originalGiaovien.getDanhSachBoDe());
            giaovien.setDanhSachDangKy(originalGiaovien.getDanhSachDangKy());

            // Save updated entity
            Giaovien updatedGiaovien = giaovienRepository.save(giaovien);
            GiaovienDTO updatedDTO = convertToDTO(updatedGiaovien);

            // Save undo action
            final GiaovienDTO finalOriginalDTO = originalDTO;
            // undoService.pushUndoAction(currentUser, new UndoService.UndoAction(
            //         "GIAOVIEN",
            //         "UPDATE",
            //         magv,
            //         originalDTO,
            //         updatedDTO,
            //         () -> {
            //             try {
            //                 // Convert back to entity
            //                 Giaovien revertGiaovien = convertToEntity(finalOriginalDTO);

            //                 // Ensure relationships are maintained during reversion
            //                 Optional<Giaovien> currentOpt = giaovienRepository.findById(magv);
            //                 if (currentOpt.isPresent()) {
            //                     Giaovien currentGiaovien = currentOpt.get();
            //                     revertGiaovien.setDanhSachBoDe(currentGiaovien.getDanhSachBoDe());
            //                     revertGiaovien.setDanhSachDangKy(currentGiaovien.getDanhSachDangKy());
            //                 }

            //                 // Save reverted entity
            //                 Giaovien revertedGiaovien = giaovienRepository.save(revertGiaovien);
            //                 return convertToDTO(revertedGiaovien);
            //             } catch (Exception e) {
            //                 e.printStackTrace();
            //                 return null;
            //             }
            //         }));
            undoService.pushUndoAction(
                    currentUser,
                    "GIAOVIEN", // entityType
                    new UndoService.UndoAction(
                            "GIAOVIEN", // entityType
                            "UPDATE", // actionType
                            magv, // entityId
                            originalDTO.getHoTen() != null ? originalDTO.getHoTen()
                                    : (originalDTO.getHo() + " " + originalDTO.getTen()), // entityName
                            () -> { // undoFunction
                                try {
                                    // Convert back to entity
                                    Giaovien revertGiaovien = convertToEntity(finalOriginalDTO);

                                    // Ensure relationships are maintained during reversion
                                    Optional<Giaovien> currentOpt = giaovienRepository.findById(magv);
                                    if (currentOpt.isPresent()) {
                                        Giaovien currentGiaovien = currentOpt.get();
                                        revertGiaovien.setDanhSachBoDe(currentGiaovien.getDanhSachBoDe());
                                        revertGiaovien.setDanhSachDangKy(currentGiaovien.getDanhSachDangKy());
                                    }

                                    // Save reverted entity
                                    Giaovien revertedGiaovien = giaovienRepository.save(revertGiaovien);
                                    return convertToDTO(revertedGiaovien);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return null;
                                }
                            }));

            return updatedDTO;
        } catch (Exception e) {
            System.err.println("Error in updateGiaovien: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    public void deleteGiaovien(String magv, String currentUser) {
        try {
            // Verify existence
            Optional<Giaovien> giaovienOpt = giaovienRepository.findById(magv);
            if (giaovienOpt.isEmpty()) {
                throw new IllegalArgumentException("Lecturer with ID " + magv + " not found");
            }

            Giaovien giaovien = giaovienOpt.get();
            GiaovienDTO originalDTO = convertToDTO(giaovien);

            // Check if can be deleted (soft delete by setting status)
            if (giaovien.getDanhSachBoDe() != null && !giaovien.getDanhSachBoDe().isEmpty()) {
                throw new IllegalArgumentException("Cannot delete lecturer with ID " + magv
                        + " because they have associated question sets");
            }

            if (giaovien.getDanhSachDangKy() != null && !giaovien.getDanhSachDangKy().isEmpty()) {
                throw new IllegalArgumentException("Cannot delete lecturer with ID " + magv
                        + " because they have associated registrations");
            }

            // Soft delete by setting status to false
            giaovien.setTrangThai(false);
            giaovienRepository.save(giaovien);

            // Save undo action
            // undoService.pushUndoAction(currentUser, new UndoService.UndoAction(
            //         "GIAOVIEN",
            //         "DELETE",
            //         magv,
            //         originalDTO,
            //         null,
            //         () -> {
            //             try {
            //                 Optional<Giaovien> deletedOpt = giaovienRepository.findById(magv);
            //                 if (deletedOpt.isPresent()) {
            //                     Giaovien deletedGiaovien = deletedOpt.get();
            //                     deletedGiaovien.setTrangThai(true);
            //                     Giaovien restoredGiaovien = giaovienRepository.save(deletedGiaovien);
            //                     return convertToDTO(restoredGiaovien);
            //                 }
            //                 return null;
            //             } catch (Exception e) {
            //                 e.printStackTrace();
            //                 return null;
            //             }
            //         }));
            undoService.pushUndoAction(
                    currentUser,
                    "GIAOVIEN", // entityType
                    new UndoService.UndoAction(
                            "GIAOVIEN", // entityType
                            "DELETE", // actionType
                            magv, // entityId
                            originalDTO.getHoTen() != null ? originalDTO.getHoTen()
                                    : (originalDTO.getHo() + " " + originalDTO.getTen()), // entityName
                            () -> { // undoFunction
                                try {
                                    Optional<Giaovien> deletedOpt = giaovienRepository.findById(magv);
                                    if (deletedOpt.isPresent()) {
                                        Giaovien deletedGiaovien = deletedOpt.get();
                                        deletedGiaovien.setTrangThai(true);
                                        Giaovien restoredGiaovien = giaovienRepository.save(deletedGiaovien);
                                        return convertToDTO(restoredGiaovien);
                                    }
                                    return null;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return null;
                                }
                            }));
        } catch (Exception e) {
            System.err.println("Error in deleteGiaovien: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Lấy danh sách giáo viên chưa có tài khoản
     * 
     * @return List<GiaovienDTO> danh sách các GiaovienDTO chưa có tài khoản
     */
    public List<GiaovienDTO> getGiaoviensWithoutAccounts() {
        List<Giaovien> giaoviens = giaovienRepository.findGiaoviensWithoutAccounts();
        return giaoviens.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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
        return undoService.canUndo(currentUser, role);
    }

    public UndoActionDTO getLastUndoAction(String currentUser,String role) {
        return undoService.getLastUndoAction(currentUser,role);
    }
}