package com.example.demo.controller;

import com.example.demo.service.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserAccountService userAccountService;

    // Các endpoints hiện có...

    /**
     * Endpoint đổi mật khẩu cho người dùng
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        System.out.println("Received request to change password: " + request);
        System.out.println("Request body: " + request.toString());
        String userId = request.get("userId");
        String newPassword = request.get("newPassword");
        System.out.println("User ID: " + userId);
        System.out.println("New Password: " + newPassword);

        Map<String, Object> result = userAccountService.changePassword(userId, newPassword);

        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Endpoint kiểm tra tài khoản tồn tại (để validate trước khi đổi mật khẩu)
     */
    // @GetMapping("/check/{userId}")
    // public ResponseEntity<?> checkUserExists(@PathVariable String userId) {
    //     boolean exists = userAccountService.checkUserExists(userId);
    //     return ResponseEntity.ok(Map.of("exists", exists));
    // }
}