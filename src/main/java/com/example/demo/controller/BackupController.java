// controller/BackupController.java
package com.example.demo.controller;

import com.example.demo.dto.BackupDeviceRequestDTO;
import com.example.demo.dto.RestoreRequestDTO;
import com.example.demo.service.BackupService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/backup")
public class BackupController {

    @Autowired
    private BackupService backupService;

    @PostMapping("/create-device")
    public ResponseEntity<Map<String, String>> createBackupDevice(@RequestBody BackupDeviceRequestDTO request) {
        Map<String, String> result = backupService.createBackupDevice(request.getDatabaseName());
        return ResponseEntity.ok(result);
    }
    
    // Thêm các endpoint mới
    @GetMapping("/databases")
    public ResponseEntity<List<String>> getDatabases() {
        List<String> databases = backupService.getDatabases();
        return ResponseEntity.ok(databases);
    }

    @GetMapping("/list")
    public ResponseEntity<List<Map<String, String>>> getBackups(@RequestParam String databaseName) {
        List<Map<String, String>> backups = backupService.getBackups(databaseName);
        return ResponseEntity.ok(backups);
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, String>> createBackup(@RequestBody BackupDeviceRequestDTO request) {
        Map<String, String> result = backupService.createBackup(request.getDatabaseName());
        return ResponseEntity.ok(result);
    }


    @PostMapping("/restore")
    public ResponseEntity<Map<String, String>> restoreDatabase(@RequestBody RestoreRequestDTO request) {
        Map<String, String> result;
        if (request.getPointInTime() != null && !request.getPointInTime().isEmpty()) {
            result = backupService.restoreDatabaseToPointInTime(
                    request.getDatabaseName(),
                    request.getFileName(),
                    request.getPointInTime());
        } else {
            result = backupService.restoreDatabase(
                    request.getDatabaseName(),
                    request.getFileName());
        }
        return ResponseEntity.ok(result);
    }
    // Thêm endpoint này vào BackupController.java

@GetMapping("/check-device")
public ResponseEntity<?> checkBackupDeviceExists(@RequestParam String database) {
    Map<String, Object> result = backupService.checkBackupDeviceExists(database);
    
    // Nếu device không tồn tại, trả về 404 với thông tin chi tiết
    if (result.containsKey("exists") && result.get("exists").equals(false)) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of(
                "exists", false,
                "deviceName", result.get("deviceName"),
                "message", "No backup device found for database: " + database
            ));
    }
    
    // Nếu có lỗi khác, trả về lỗi 500
    if (result.containsKey("status") && result.get("status").equals("Error")) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of(
                "error", "Failed to check backup device",
                "message", result.getOrDefault("message", "Unknown error")
            ));
    }
    
    // Nếu tìm thấy device, trả về 200 với thông tin
    return ResponseEntity.ok(Map.of(
        "exists", true,
        "deviceName", result.get("deviceName"),
        "physicalPath", result.getOrDefault("physicalPath", "")
    ));
}

@GetMapping("/logs")
public ResponseEntity<Map<String, Object>> checkLogBackups(
        @RequestParam String databaseName,
        @RequestParam String backupFileName) {
    // Kiểm tra xem databaseName và backupFileName có hợp lệ không
    System.out.println("Checking logs for database: " + databaseName + ", backup file: " + backupFileName);

    Map<String, Object> result = backupService.checkLogBackups(databaseName, backupFileName);

    // Nếu có lỗi, trả về thông tin lỗi nhưng vẫn giữ status 200 để frontend có thể
    // xử lý
    if (result.containsKey("error")) {
        return ResponseEntity.ok(Map.of(
                "hasLogs", false,
                "error", result.get("error")));
    }

    return ResponseEntity.ok(result);
}
}
