package com.example.demo.service;

import com.example.demo.dto.AccountCreateDTO;
import com.example.demo.entity.Giaovien;
import com.example.demo.repository.GiaovienRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class UserAccountService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private GiaovienRepository giaovienRepository;
 /**
     * Thay đổi mật khẩu cho tài khoản đăng nhập
     * @param loginId ID đăng nhập cần đổi mật khẩu
     * @param newPassword Mật khẩu mới
     * @return Kết quả thực hiện
     */
    @Transactional
    public Map<String, Object> changePassword(String loginId, String newPassword) {
        System.out.println("Received request to change password for login ID: " + loginId);
        Map<String, Object> result = new HashMap<>();

        if (loginId == null || loginId.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "ID đăng nhập không được để trống");
            return result;
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "Mật khẩu mới không được để trống");
            return result;
        }
        System.out.println("New Password: " + newPassword);

        // Kiểm tra độ mạnh của mật khẩu
        if (!validatePasswordStrength(newPassword)) {
            result.put("success", false);
            result.put("message",
                    "Mật khẩu không đủ mạnh. Vui lòng sử dụng mật khẩu có ít nhất 6 ký tự, bao gồm chữ hoa, chữ thường và số.");
            return result;
        }

        try {
            // Gọi stored procedure để đổi mật khẩu
            executeChangePasswordProcedure(loginId, newPassword);

            result.put("success", true);
            result.put("message", "Đổi mật khẩu thành công");
            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Lỗi khi đổi mật khẩu: " + e.getMessage());
            return result;
        }
    }
    
    /**
     * Thực thi stored procedure sp_ChangeSqlLoginPassword để đổi mật khẩu
     * 
     * @param loginId     Tên đăng nhập
     * @param newPassword Mật khẩu mới
     * @throws SQLException Nếu có lỗi xảy ra
     */
    private void executeChangePasswordProcedure(String loginId, String newPassword) throws SQLException {
        Connection connection = null;
        CallableStatement callableStatement = null;
        System.out.println("Executing stored procedure to change password for login ID: " + loginId);
        try {
            connection = jdbcTemplate.getDataSource().getConnection();

            // Gọi stored procedure sp_ChangeSqlLoginPassword
            callableStatement = connection.prepareCall("{call sp_ChangeSqlLoginPassword(?, ?)}");
            callableStatement.setString(1, loginId);
            callableStatement.setString(2, newPassword);

            callableStatement.execute();
            System.out.println("Stored procedure executed successfully for login ID: " + loginId);
            System.out.println("Password changed successfully for login ID: " + loginId);
        } catch (SQLException e) {
            String errorMessage = e.getMessage();

            if (errorMessage.contains("không tồn tại")) {
                throw new RuntimeException("Tài khoản đăng nhập không tồn tại");
            } else if (errorMessage.contains("password validation")) {
                throw new RuntimeException("Mật khẩu không đạt yêu cầu độ phức tạp của SQL Server");
            } else {
                throw new RuntimeException("Lỗi khi đổi mật khẩu: " + errorMessage);
            }
        } finally {
            if (callableStatement != null) {
                callableStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }

    /**
     * Kiểm tra độ mạnh của mật khẩu
     * 
     * @param password Mật khẩu cần kiểm tra
     * @return true nếu mật khẩu đủ mạnh
     */
    private boolean validatePasswordStrength(String password) {
        // Kiểm tra độ dài
        if (password.length() < 6 || password.length() > 32) {
            return false;
        }

        // Kiểm tra có ít nhất 1 chữ hoa
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }

        // Kiểm tra có ít nhất 1 chữ thường
        if (!password.matches(".*[a-z].*")) {
            return false;
        }

        // Kiểm tra có ít nhất 1 số
        if (!password.matches(".*\\d.*")) {
            return false;
        }

        // Kiểm tra không có khoảng trắng đầu/cuối
        if (password.startsWith(" ") || password.endsWith(" ")) {
            return false;
        }

        return true;
    }
    /**
     * Creates a database account based on the role.
     * Only PGV users can create accounts.
     */
    @Transactional
    public boolean createUserAccount(AccountCreateDTO accountDTO, String creatorRole) {
        // Verify the creator has PGV role
        if (!"PGV".equals(creatorRole)) {
            throw new RuntimeException("Unauthorized: Only PGV users can create accounts");
        }

        try {
            // Create database login for the user
            executeSP_TAOLOGIN(accountDTO.getLoginId(), accountDTO.getPassword(), accountDTO.getRole());

           
            if ("Lecturer".equals(accountDTO.getRole()) || "PGV".equals(accountDTO.getRole())) {
                Optional<Giaovien> giaovienOpt = giaovienRepository.findById(accountDTO.getLoginId());
                if (giaovienOpt.isPresent()) {
                    Giaovien giaovien = giaovienOpt.get();
                    giaovien.setHasAccount(true); // Set HAS_ACCOUNT to true
                    giaovienRepository.save(giaovien);
                }
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create user account: " + e.getMessage(), e);
        }
    }

    // Add to the UserAccountService class

    /**
     * Execute the SP_TAOLOGIN stored procedure with better error handling
     */
    private void executeSP_TAOLOGIN(String loginName, String password, String role) throws SQLException {
        Connection connection = null;
        CallableStatement callableStatement = null;

        try {
            connection = jdbcTemplate.getDataSource().getConnection();
            String roleDB = "Lecturer".equals(role) ? "Lecturer" : "PGV";

            // Call SP_TAOLOGIN(LOGINNAME, PASS, USERNAME, ROLE)
            callableStatement = connection.prepareCall("{call SP_ACCOUNTCREATION( ?, ?, ?)}");

            callableStatement.setString(1, password);
            callableStatement.setString(2, loginName); // Use loginName as username
            callableStatement.setString(3, roleDB);

            callableStatement.execute();
        } catch (SQLException e) {
            // Handle specific SQL Server errors
            if (e.getMessage().contains("already exists")) {
                throw new RuntimeException("Login name already exists. Please choose a different login ID.", e);
            } else if (e.getMessage().contains("password validation")) {
                throw new RuntimeException("Password does not meet complexity requirements.", e);
            } else {
                throw new RuntimeException("Database error creating login: " + e.getMessage(), e);
            }
        } finally {
            if (callableStatement != null)
                callableStatement.close();
            if (connection != null)
                connection.close();
        }
    }
    // Add this method to your existing UserAccountService class

    public boolean deleteUserAccount(String loginId, String currentUser) {
        Connection connection = null;
        CallableStatement callableStatement = null;

        try {
            // Check for null data source
            if (jdbcTemplate.getDataSource() == null) {
                throw new IllegalStateException("Database connection is not available");
            }

            // Check if the user is a teacher/PGV and update Giaovien record
            Optional<Giaovien> giaovienOpt = giaovienRepository.findById(loginId);
            if (giaovienOpt.isPresent()) {
                Giaovien giaovien = giaovienOpt.get();
                giaovien.setHasAccount(false); // Set HAS_ACCOUNT to false
                giaovienRepository.save(giaovien);
                System.out.println("Updated account status for teacher: " + loginId);
            }

            // Execute the stored procedure to delete the login
            connection = jdbcTemplate.getDataSource().getConnection();
            callableStatement = connection.prepareCall("{call SP_XoaLoginServer(?)}");
            callableStatement.setString(1, loginId);
            callableStatement.execute();

            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting user account: " + e.getMessage());
            throw new RuntimeException("Failed to delete user account", e);
        } catch (Exception e) {
            System.err.println("Error updating teacher account status: " + e.getMessage());
            throw new RuntimeException("Failed to update account status", e);
        } finally {
            // Close resources in finally block
            try {
                if (callableStatement != null) {
                    callableStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing database resources: " + e.getMessage());
            }
        }
    }

    /**
     * Checks if a user with the given login ID exists
     */
    private boolean userExists(String loginId) {
        String sql = "SELECT COUNT(*) FROM DANGNHAP WHERE MAGV = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, loginId);
        return count != null && count > 0;
    }

    /**
     * Checks if a user is currently active in the system
     * This can be implemented based on your session tracking mechanism
     */
    private boolean isUserActive(String loginId) {
        // Here you would implement your logic to check if a user is currently active
        // This might involve checking a sessions table or an in-memory store

        // Simple example implementation:
        String sql = "SELECT COUNT(*) FROM ACTIVE_SESSIONS WHERE user_id = ? AND expiry > NOW()";
        try {
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, loginId);
            return count != null && count > 0;
        } catch (Exception e) {
            // If table doesn't exist or other error, default to assuming user is not active
            return false;
        }
    }
}