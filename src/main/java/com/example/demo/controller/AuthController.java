package com.example.demo.controller;

import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.entity.Giaovien;
import com.example.demo.entity.Sinhvien;
import com.example.demo.repository.GiaovienRepository;
import com.example.demo.repository.SinhvienRepository;
import com.example.demo.service.DatabaseLoginService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private GiaovienRepository giaovienRepository;

    @Autowired
    private SinhvienRepository sinhvienRepository;

    @Autowired
    private DatabaseLoginService dbLoginService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {
        System.out
                .println("Login request received: " + loginRequest.getUsername() + ", Role: " + loginRequest.getRole());
        System.out.println("=== DEBUG LOGIN ===");
        System.out.println("Username: " + loginRequest.getUsername());
        System.out.println("Password: " + loginRequest.getPassword());
        System.out.println("Role: " + loginRequest.getRole());

        LoginResponseDTO response = new LoginResponseDTO();

        try {
            // Reset to default connection first to ensure we have working credentials
            dbLoginService.resetDatabaseCredentials("sa", "11192023");
           
            // Then proceed with authentication logic
            if ("PGV".equalsIgnoreCase(loginRequest.getRole())) {
                // Check if PGV can connect to database
                Giaovien giaovien = giaovienRepository.findByMagv(loginRequest.getUsername()).orElse(null);
                if (giaovien != null) {

                    if (dbLoginService.validateDatabaseAccess(loginRequest.getUsername(), loginRequest.getPassword())) {

                        response.setAuthenticated(true);
                        response.setToken("db-token-pgv-" + System.currentTimeMillis());
                       // response.setId(loginRequest.getUsername());
                        //response.setUsername(loginRequest.getUsername());
                        response.setId(giaovien.getMagv());
                        response.setUsername(giaovien.getMagv());
                        response.setName(giaovien.getHo() + " " + giaovien.getTen());
                        //response.setName("PGV Administrator");
                        response.setRole("PGV");
    
                        // Update application's database connection with these credentials
                        dbLoginService.updateDatabaseCredentials(loginRequest.getUsername(), loginRequest.getPassword());
                        return ResponseEntity.ok(response);
                    }
                }
            } else if ("Lecturer".equalsIgnoreCase(loginRequest.getRole())) {
                // Check if teacher exists
                Giaovien giaovien = giaovienRepository.findByMagv(loginRequest.getUsername()).orElse(null);

                if (giaovien != null) {
                    // Check if teacher credentials can access database
                    if (dbLoginService.validateDatabaseAccess(loginRequest.getUsername(), loginRequest.getPassword())) {
                        response.setAuthenticated(true);
                        response.setToken("db-token-lecturer-" + System.currentTimeMillis());
                        response.setId(giaovien.getMagv());
                        response.setUsername(giaovien.getMagv());
                        response.setName(giaovien.getHo() + " " + giaovien.getTen());
                        response.setRole("Lecturer");

                        // Update application's database connection with these credentials
                        dbLoginService.updateDatabaseCredentials(loginRequest.getUsername(),
                                loginRequest.getPassword());
                        return ResponseEntity.ok(response);
                    }
                }
            } else if ("Student".equalsIgnoreCase(loginRequest.getRole())) {
                // For students, check if they exist first
                Sinhvien sinhvien = sinhvienRepository.findByMaSV(loginRequest.getUsername()).orElse(null);

                if (sinhvien != null) {
                    // Use shared student database account for all students
                    String studentDbUser = "sv";
                    String studentDbPassword = "123456";

                    if (dbLoginService.validateDatabaseAccess(studentDbUser, studentDbPassword)) {
                        // Use the common student database account
                        dbLoginService.updateDatabaseCredentials(studentDbUser, studentDbPassword);

                        // Send student information in response
                        response.setAuthenticated(true);
                        response.setToken("db-token-student-" + System.currentTimeMillis());
                        response.setId(sinhvien.getMaSV());
                        response.setUsername(sinhvien.getMaSV());
                        response.setName(sinhvien.getHo() + " " + sinhvien.getTen());
                        response.setRole("Student");
                        response.setClassId(sinhvien.getLop().getMaLop());
                        response.setClassName(sinhvien.getLop().getTenLop());
                        return ResponseEntity.ok(response);
                    }
                }
            }

            // Authentication failed
            response.setAuthenticated(false);
            response.setErrorMessage("Invalid credentials or insufficient database permissions");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Login error: " + e.getMessage());
            e.printStackTrace();

            response.setAuthenticated(false);
            response.setErrorMessage("Error during authentication: " + e.getMessage());
            return ResponseEntity.ok(response);
        }
    }
    // Add this method to your existing AuthController class

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // Reset database connection to default credentials
        dbLoginService.resetDatabaseCredentials("sa", "11192023");

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Logged out successfully"));
    }
}