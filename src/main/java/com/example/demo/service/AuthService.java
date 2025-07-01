package com.example.demo.service;

import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.entity.Giaovien;
import com.example.demo.entity.Sinhvien;
import com.example.demo.repository.GiaovienRepository;
import com.example.demo.repository.SinhvienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private GiaovienRepository giaovienRepository;

    @Autowired
    private SinhvienRepository sinhvienRepository;

    // Mock implementation - in production replace with proper authentication
    public LoginResponseDTO login(String username, String password, String role) {
        LoginResponseDTO response = new LoginResponseDTO();

        if ("PGV".equalsIgnoreCase(role)) {
            // Check if user is PGV
            if ("admin".equals(username) && "password".equals(password)) {
                response.setAuthenticated(true);
                response.setToken("mock-pgv-token");
                response.setId("ADMIN");
                response.setUsername(username);
                response.setName("Administrator");
                response.setRole("PGV");
                return response;
            }
        } else if ("Lecturer".equalsIgnoreCase(role)) {
            // Check if user is lecturer
            Giaovien giaovien = giaovienRepository.findByMagv(username).orElse(null);
            if (giaovien != null && "password".equals(password)) {
                response.setAuthenticated(true);
                response.setToken("mock-lecturer-token");
                response.setId(giaovien.getMagv());
                response.setUsername(giaovien.getMagv());
                response.setName(giaovien.getHo() + " " + giaovien.getTen());
                response.setRole("Lecturer");
                return response;
            }
        } else if ("Student".equalsIgnoreCase(role)) {
            // Check if user is student
            Sinhvien sinhvien = sinhvienRepository.findByMaSV(username).orElse(null);
            if (sinhvien != null && "password".equals(password)) {
                response.setAuthenticated(true);
                response.setToken("mock-student-token");
                response.setId(sinhvien.getMaSV());
                response.setUsername(sinhvien.getMaSV());
                response.setName(sinhvien.getHo() + " " + sinhvien.getTen());
                response.setRole("Student");
                response.setClassId(sinhvien.getLop().getMaLop());
                response.setClassName(sinhvien.getLop().getTenLop());
                return response;
            }
        }

        // Authentication failed
        response.setAuthenticated(false);
        response.setErrorMessage("Invalid credentials");
        return response;
    }
}