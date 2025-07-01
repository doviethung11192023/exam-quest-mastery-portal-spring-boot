package com.example.demo.controller;

import com.example.demo.dto.LopDTO;
import com.example.demo.service.LopService;
import com.example.demo.service.UndoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/classes")
public class LopController {

    @Autowired
    private LopService lopService;

    @GetMapping
    public ResponseEntity<List<LopDTO>> getAllClasses() {
        return ResponseEntity.ok(lopService.getAllClasses());
    }

    @GetMapping("/{maLop}")
    public ResponseEntity<LopDTO> getClassById(@PathVariable String maLop) {
        try {
            LopDTO lopDTO = lopService.getClassById(maLop);
            return ResponseEntity.ok(lopDTO);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<LopDTO>> searchClasses(@RequestParam String keyword) {
        return ResponseEntity.ok(lopService.searchClasses(keyword));
    }

    @PostMapping
    public ResponseEntity<LopDTO> addClass(
            @RequestBody LopDTO lopDTO,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            LopDTO createdLop = lopService.addClass(lopDTO, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdLop);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{maLop}")
    public ResponseEntity<LopDTO> updateClass(
            @PathVariable String maLop,
            @RequestBody LopDTO lopDTO,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        try {
            LopDTO updatedLop = lopService.updateClass(maLop, lopDTO, userId);
            return ResponseEntity.ok(updatedLop);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/can-undo")
    public ResponseEntity<Boolean> canUndo(@RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam String entityType) {
        return ResponseEntity.ok(lopService.canUndo(userId, entityType));
    }

    // @GetMapping("/last-undo-action")
    // public ResponseEntity<UndoService.UndoActionDTO> getLastUndoAction(
    //         @RequestHeader(value = "X-User-Id", required = false) String userId) {
    //     UndoService.UndoActionDTO action = lopService.getLastUndoAction(userId);
    //     if (action != null) {
    //         return ResponseEntity.ok(action);
    //     } else {
    //         return ResponseEntity.noContent().build();
    //     }
    // }

    @PostMapping("/undo")
    public ResponseEntity<Object> undoAction(@RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestParam String entityType) {
        try {
            Object result = lopService.undoAction(userId, entityType);
            if (result != null) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.noContent().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}