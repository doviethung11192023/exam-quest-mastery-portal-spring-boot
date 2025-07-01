package com.example.demo.service;

import com.example.demo.dto.LopDTO;
import com.example.demo.entity.Lop;
import com.example.demo.repository.LopRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LopService {

    @Autowired
    private LopRepository lopRepository;

    @Autowired
    private UndoService undoService;

    @Transactional
    public List<LopDTO> getAllClasses() {
        return lopRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public LopDTO getClassById(String maLop) {
        Lop lop = lopRepository.findById(maLop)
                .orElseThrow(() -> new RuntimeException("Lớp không tồn tại: " + maLop));
        return convertToDTO(lop);
    }

    @Transactional
    public List<LopDTO> searchClasses(String keyword) {
        List<Lop> results = lopRepository.findByMaLopContainingIgnoreCase(keyword);
        results.addAll(lopRepository.findByTenLopContainingIgnoreCase(keyword));
        return results.stream()
                .distinct()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public LopDTO addClass(LopDTO lopDTO, String currentUser) {
        // Kiểm tra trùng lặp
        if (lopRepository.existsById(lopDTO.getMaLop())) {
            throw new RuntimeException("Mã lớp đã tồn tại: " + lopDTO.getMaLop());
        }

        Lop newLop = new Lop();
        newLop.setMaLop(lopDTO.getMaLop());
        newLop.setTenLop(lopDTO.getTenLop());

        Lop savedLop = lopRepository.save(newLop);

        // Lưu action để undo
        // undoService.pushUndoAction(currentUser, new UndoService.UndoAction(
        //         "LOP",
        //         "ADD",
        //         savedLop.getMaLop(),
        //         savedLop.getTenLop(),
        //         null,
        //         savedLop,
        //         () -> {
        //             lopRepository.deleteById(savedLop.getMaLop());
        //             return null;
        //         }));

        return convertToDTO(savedLop);
    }

    @Transactional
    public LopDTO updateClass(String maLop, LopDTO lopDTO, String currentUser) {
        Lop existingLop = lopRepository.findById(maLop)
                .orElseThrow(() -> new RuntimeException("Lớp không tồn tại: " + maLop));

        // Tạo bản sao cho undo
        Lop originalLop = new Lop();
        originalLop.setMaLop(existingLop.getMaLop());
        originalLop.setTenLop(existingLop.getTenLop());

        existingLop.setTenLop(lopDTO.getTenLop());

        Lop updatedLop = lopRepository.save(existingLop);

        // Lưu action để undo
        // undoService.pushUndoAction(currentUser, new UndoService.UndoAction(
        //         "LOP",
        //         "EDIT",
        //         updatedLop.getMaLop(),
        //         updatedLop.getTenLop(),
        //         originalLop,
        //         updatedLop,
        //         () -> {
        //             Lop revertLop = lopRepository.findById(originalLop.getMaLop())
        //                     .orElse(null);
        //             if (revertLop != null) {
        //                 revertLop.setTenLop(originalLop.getTenLop());
        //                 lopRepository.save(revertLop);
        //                 return convertToDTO(revertLop);
        //             }
        //             return null;
        //         }));

        return convertToDTO(updatedLop);
    }

    public boolean canUndo(String userId,String role) {
        return undoService.canUndo(userId,role);
    }

    public Object undoAction(String userId,String role) {
        return undoService.undoLastAction(userId,role);
    }

    // public UndoService.UndoActionDTO getLastUndoAction(String userId) {
    //     return undoService.getLastUndoAction(userId);
    // }

    private LopDTO convertToDTO(Lop lop) {
        if (lop == null)
            return null;

        LopDTO dto = new LopDTO();
        dto.setMaLop(lop.getMaLop());
        dto.setTenLop(lop.getTenLop());
        return dto;
    }
}