package com.example.demo.controller;

import com.example.demo.dto.AccountCreateDTO;
import com.example.demo.service.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private UserAccountService userAccountService;

    @PostMapping("/create")
    public ResponseEntity<?> createAccount(
            @RequestBody AccountCreateDTO accountDTO,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        System.out.println("Creating account for user: " + accountDTO.getLoginId());
        System.out.println("User role: " + userRole);

        // Default to non-PGV for safety if header is missing
        if (userRole == null) {
            userRole = "Unauthorized";
        }

        try {
            // Check authorization - only PGV can create accounts
            if (!"PGV".equals(userRole)) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "Unauthorized: Only PGV users can create accounts"));
            }

            boolean success = userAccountService.createUserAccount(accountDTO, userRole);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Account created successfully" : "Failed to create account");
            response.put("loginId", accountDTO.getLoginId());
            response.put("role", accountDTO.getRole());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/delete/{loginId}")
    public ResponseEntity<?> deleteAccount(
            @PathVariable String loginId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole,
            @RequestHeader(value = "X-Current-User", required = false) String currentUser) {

        System.out.println("Deleting account with loginId: " + loginId);
        System.out.println("Request made by user role: " + userRole);

        // Default to non-PGV for safety if header is missing
        if (userRole == null) {
            userRole = "Unauthorized";
        }

        try {
            // Check authorization - only PGV can delete accounts
            if (!"PGV".equals(userRole)) {
                return ResponseEntity.status(403).body(Map.of(
                        "success", false,
                        "message", "Unauthorized: Only PGV users can delete accounts"));
            }

            // Check if attempting to delete the currently logged in user
            if (loginId.equals(currentUser)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Cannot delete currently logged in user account"));
            }

            boolean success = userAccountService.deleteUserAccount(loginId, currentUser);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "Account deleted successfully" : "Failed to delete account");
            response.put("loginId", loginId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()));
        }
    }
}