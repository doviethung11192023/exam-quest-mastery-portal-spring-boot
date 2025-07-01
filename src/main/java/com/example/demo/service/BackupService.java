// service/BackupService.java
package com.example.demo.service;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BackupService {

    @Autowired
    private DataSource dataSource;

    public Map<String, String> createBackupDevice(String databaseName) {
        Map<String, String> result = new HashMap<>();

        try (Connection conn = dataSource.getConnection()) {
            System.out.println("✅ Connected to: " + conn.getMetaData().getURL());

            CallableStatement stmt = conn.prepareCall("{call sp_CreateBackupDevice(?)}");
            stmt.setString(1, databaseName);

            System.out.println("➡️  Calling stored procedure with: " + databaseName);
            boolean hasResult = stmt.execute();
            System.out.println("📦 Stored procedure executed. Has result: " + hasResult);

            if (hasResult) {
                ResultSet rs = stmt.getResultSet();
                if (rs.next()) {
                    String status = rs.getString("Status");
                    String deviceName = rs.getString("DeviceName");
                    String path = rs.getString("Path");

                    System.out.println("✅ Status: " + status);
                    System.out.println("📁 Device Name: " + deviceName);
                    System.out.println("📂 Path: " + path);

                    result.put("status", status);
                    result.put("deviceName", deviceName);
                    result.put("path", path);
                } else {
                    System.out.println("⚠️ ResultSet is empty.");
                }
            } else {
                System.out.println("⚠️ No ResultSet returned.");
            }

        } catch (SQLException e) {
            System.err.println("❌ SQLException: " + e.getMessage());
            e.printStackTrace();
            result.put("status", "❌ SQL Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Exception: " + e.getMessage());
            e.printStackTrace();
            result.put("status", "❌ Error: " + e.getMessage());
        }

        return result;
    }

    // Thêm các phương thức mới
    public List<String> getDatabases() {
        List<String> databases = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            System.out.println("✅ Connected to DB");

            CallableStatement stmt = conn.prepareCall("{call sp_GetDatabases}");
            boolean hasResults = stmt.execute();

            if (hasResults) {
                ResultSet rs = stmt.getResultSet();
                while (rs.next()) {
                    String dbName = rs.getString("name");
                    System.out.println("📦 Found database: " + dbName);
                    databases.add(dbName);
                }
            } else {
                System.out.println("⚠️ No result returned from stored procedure.");
            }
        } catch (Exception e) {
            System.out.println("❌ Error calling sp_GetDatabases:");
            e.printStackTrace();
        }

        return databases;
    }

    public List<Map<String, String>> getBackups(String databaseName) {
        List<Map<String, String>> backups = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {
            // Gọi stored procedure hoặc query để lấy danh sách backup
            CallableStatement stmt = conn.prepareCall("{call sp_GetBackupHistory(?)}");
            stmt.setString(1, databaseName);
            boolean hasResults = stmt.execute();

            if (hasResults) {
                ResultSet rs = stmt.getResultSet();
                while (rs.next()) {
                    Map<String, String> backup = new HashMap<>();
                    backup.put("fileName", rs.getString("backup_name"));
                    backup.put("createdAt", rs.getString("backup_date"));
                    backup.put("fileSize", rs.getString("backup_size"));
                    backup.put("user", rs.getString("user_name"));
                    backups.add(backup);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return backups;
    }

    public Map<String, String> createBackup(String databaseName) {
        Map<String, String> result = new HashMap<>();

        try (Connection conn = dataSource.getConnection()) {
            System.out.println("✅ Connected to database for backup operation");

            // Xác định thư mục backup - bạn có thể thay đổi đường dẫn này theo cấu hình của
            // bạn
            String backupFolder = "C:\\Backup" + "\\" + databaseName;

            System.out.println("📂 Using backup folder: " + backupFolder);
            System.out.println("🗄️ Backing up database: " + databaseName);

            // Gọi stored procedure sp_BackupDatabase mới
            CallableStatement stmt = conn.prepareCall("{call sp_BackupDatabase(?, ?)}");
            stmt.setString(1, databaseName);
            stmt.setString(2, backupFolder);

            System.out.println("➡️ Executing backup procedure with parameters:");
            System.out.println("   - DatabaseName: " + databaseName);
            System.out.println("   - BackupFolder: " + backupFolder);

            boolean hasResults = stmt.execute();
            System.out.println("📊 Backup procedure executed. Has results: " + hasResults);

            if (hasResults) {
                ResultSet rs = stmt.getResultSet();
                if (rs.next()) {
                    String backupPath = rs.getString("BackupPath");

                    // Phân tích đường dẫn file backup để lấy tên file
                    String fileName = backupPath.substring(backupPath.lastIndexOf('\\') + 1);

                    System.out.println("✅ Backup successfully created:");
                    System.out.println("📁 Backup file: " + fileName);
                    System.out.println("🔗 Full path: " + backupPath);

                    result.put("status", "Success");
                    result.put("fileName", fileName);
                    result.put("filePath", backupPath);
                    result.put("backupDate", new java.util.Date().toString());
                    result.put("databaseName", databaseName);
                } else {
                    System.out.println("⚠️ ResultSet is empty - backup may have completed but no path returned");
                    result.put("status", "Warning: No backup path returned");
                }
            } else {
                // Kiểm tra xem có thông tin đầu ra không qua getUpdateCount
                int updateCount = stmt.getUpdateCount();
                System.out.println("⚠️ No direct ResultSet, update count: " + updateCount);

                if (updateCount >= 0) {
                    result.put("status", "Potential success, affected rows: " + updateCount);
                    result.put("fileName", databaseName + "_"
                            + new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()) + ".bak");
                    result.put("filePath", backupFolder + "\\" + result.get("fileName"));
                } else {
                    result.put("status", "Warning: No results returned and no update count");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ SQLException during backup:");
            System.err.println("   Error code: " + e.getErrorCode());
            System.err.println("   SQL state: " + e.getSQLState());
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();

            result.put("status", "❌ SQL Error: " + e.getMessage());
            result.put("errorCode", String.valueOf(e.getErrorCode()));
            result.put("sqlState", e.getSQLState());
        } catch (Exception e) {
            System.err.println("❌ General exception during backup:");
            System.err.println("   Exception type: " + e.getClass().getName());
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();

            result.put("status", "❌ Error: " + e.getMessage());
            result.put("exceptionType", e.getClass().getName());
        }

        // Log kết quả cuối cùng
        System.out.println("📋 Final backup result status: " + result.get("status"));

        return result;
    }
    // Thêm phương thức này vào BackupService.java

    public Map<String, Object> checkBackupDeviceExists(String databaseName) {
        Map<String, Object> result = new HashMap<>();

        try (Connection conn = dataSource.getConnection()) {
            System.out.println("✅ Connected to database to check backup device");
            System.out.println("🔍 Checking backup device for database: " + databaseName);

            CallableStatement stmt = conn.prepareCall("{call sp_CheckBackupDeviceExists(?)}");
            stmt.setString(1, databaseName);

            boolean hasResults = stmt.execute();
            System.out.println("📊 Check device procedure executed. Has results: " + hasResults);

            if (hasResults) {
                ResultSet rs = stmt.getResultSet();
                if (rs.next()) {
                    String status = rs.getString("Status");
                    boolean exists = rs.getBoolean("DeviceExists");
                    String deviceName = rs.getString("DeviceName");
                    String physicalPath = rs.getString("PhysicalPath");

                    System.out.println("✅ Status: " + status);
                    System.out.println("🎯 Device exists: " + exists);
                    System.out.println("📁 Device Name: " + deviceName);

                    if (exists) {
                        System.out.println("📂 Physical Path: " + physicalPath);
                    }

                    result.put("status", status);
                    result.put("exists", exists);
                    result.put("deviceName", deviceName);

                    if (physicalPath != null) {
                        result.put("physicalPath", physicalPath);
                    }
                } else {
                    System.out.println("⚠️ ResultSet is empty.");
                    result.put("status", "Error");
                    result.put("exists", false);
                    result.put("message", "No results returned from database.");
                }
            } else {
                System.out.println("⚠️ No ResultSet returned.");
                result.put("status", "Error");
                result.put("exists", false);
                result.put("message", "No results returned from procedure.");
            }
        } catch (SQLException e) {
            System.err.println("❌ SQLException during device check:");
            System.err.println("   Error code: " + e.getErrorCode());
            System.err.println("   SQL state: " + e.getSQLState());
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();

            result.put("status", "Error");
            result.put("exists", false);
            result.put("message", "SQL Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ General exception during device check:");
            System.err.println("   Exception type: " + e.getClass().getName());
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();

            result.put("status", "Error");
            result.put("exists", false);
            result.put("message", "Error: " + e.getMessage());
        }

        return result;
    }

    public Map<String, String> restoreDatabase(String databaseName, String fileName) {
        Map<String, String> result = new HashMap<>();
        System.out.println("🔄 Starting database restore for: " + databaseName);
        System.out.println("📄 Backup file: " + fileName);

        try {
            // Tạo connection riêng đến master database
            String url = "jdbc:sqlserver://localhost:1433;databaseName=master;encrypt=false";
            String user = "sa";
            String password = "11192023";

            try (Connection restoreConn = DriverManager.getConnection(url, user, password)) {
                System.out.println("✅ Connected to MASTER for restore.");

                // Gọi stored procedure từ master database
                CallableStatement stmt = restoreConn.prepareCall("{call master.dbo.sp_RestoreDatabase(?, ?)}");
                stmt.setString(1, databaseName);
                stmt.setString(2, fileName);

                boolean hasResults = stmt.execute();
                System.out.println("📊 Restore procedure executed. Has results: " + hasResults);

                if (hasResults) {
                    ResultSet rs = stmt.getResultSet();
                    if (rs.next()) {
                        result.put("status", rs.getString("Status"));
                        System.out.println("✅ Restore status: " + result.get("status"));
                        result.put("message", rs.getString("Message"));
                        System.out.println("📃 Restore message: " + result.get("message"));
                    } else {
                        System.out.println("⚠️ No results returned from stored procedure.");
                        result.put("status", "Error");
                        result.put("message", "No results returned from database.");
                    }
                } else {
                    System.out.println("⚠️ No ResultSet returned.");
                    result.put("status", "Error");
                    result.put("message", "No results returned from procedure.");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ SQLException during database restore:");
            System.err.println("   Error code: " + e.getErrorCode());
            System.err.println("   SQL state: " + e.getSQLState());
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();

            result.put("status", "Error");
            result.put("message", e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ General exception during database restore:");
            System.err.println("   Exception type: " + e.getClass().getName());
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();

            result.put("status", "Error");
            result.put("message", "Error: " + e.getMessage());
        }

        return result;
    }

 
public Map<String, String> restoreDatabaseToPointInTime(String databaseName, String fileName, String pointInTime) {
    Map<String, String> result = new HashMap<>();
    // Định dạng lại thời gian để SQL Server hiểu
    if (pointInTime.contains("T")) {
    pointInTime = pointInTime.replace("T", " ");

    // Thêm giây nếu cần
    if (pointInTime.length() == 16) { // Định dạng YYYY-MM-DD HH:MM
    pointInTime = pointInTime + ":00";
    }
    }

    System.out.println("📄 Formatted point in time: " + pointInTime);

    try {
        // Tạo 1 connection riêng chỉ để restore (chuyển sang master)
        String url = "jdbc:sqlserver://localhost:1433;databaseName=master;encrypt=false";
        String user = "sa";
        String password = "11192023";
        
        try (Connection restoreConn = DriverManager.getConnection(url, user, password)) {
            System.out.println("✅ Connected to MASTER for restore.");

            // Gọi stored procedure y như trước
            CallableStatement stmt = restoreConn.prepareCall("{call master.dbo.sp_RestoreDatabaseToPointInTime_UltimateV2(?, ?, ?)}");
            stmt.setString(1, databaseName);
            stmt.setString(2, "C:\\Backup\\" + databaseName + "\\");
            stmt.setString(3, pointInTime);

            boolean hasResults = stmt.execute();
            if (hasResults) {
                System.out.println("📊 Restore procedure executed. Has results: " + hasResults);
                ResultSet rs = stmt.getResultSet();
                if (rs.next()) {
                    result.put("status", rs.getString("Status"));
                    System.out.println("✅ Restore status: " + result.get("status"));
                    result.put("message", rs.getString("Message"));
                    System.out.println("📃 Restore message: " + result.get("message"));
                }
            }
        }
    } catch (SQLException e) {
        result.put("status", "Error");
        result.put("message", e.getMessage());
        e.printStackTrace();
    }

    return result;
}

    public Map<String, Object> checkLogBackups(String databaseName, String backupFileName) {
        Map<String, Object> result = new HashMap<>();

        try (Connection conn = dataSource.getConnection()) {
            System.out.println("✅ Connected to database to check log backups");
            System.out.println("🔍 Checking log backups for database: " + databaseName);
            System.out.println("📄 Backup file: " + backupFileName);

            // Gọi stored procedure để kiểm tra log backups
            CallableStatement stmt = conn.prepareCall("{call sp_CheckLogBackups(?, ?)}");
            stmt.setString(1, databaseName);
            stmt.setString(2, backupFileName);

            boolean hasResults = stmt.execute();
            System.out.println("📊 Check log backups procedure executed. Has results: " + hasResults);

            if (hasResults) {
                ResultSet rs = stmt.getResultSet();
                if (rs.next()) {
                    boolean hasLogs = rs.getBoolean("HasLogs");
                    String minTime = rs.getString("MinTime");
                    String maxTime = rs.getString("MaxTime");

                    System.out.println("✅ Has logs: " + hasLogs);
                    if (hasLogs) {
                        System.out.println("🕒 Min time: " + minTime);
                        System.out.println("🕒 Max time: " + maxTime);
                    }

                    result.put("hasLogs", hasLogs);
                    if (hasLogs) {
                        result.put("minTime", minTime);
                        result.put("maxTime", maxTime);
                    }
                } else {
                    System.out.println("⚠️ ResultSet is empty.");
                    result.put("hasLogs", false);
                }
            } else {
                System.out.println("⚠️ No ResultSet returned.");
                result.put("hasLogs", false);
            }
        } catch (SQLException e) {
            System.err.println("❌ SQLException during log backup check:");
            System.err.println("   Error code: " + e.getErrorCode());
            System.err.println("   SQL state: " + e.getSQLState());
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();

            result.put("hasLogs", false);
            result.put("error", "SQL Error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ General exception during log backup check:");
            System.err.println("   Exception type: " + e.getClass().getName());
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();

            result.put("hasLogs", false);
            result.put("error", "Error: " + e.getMessage());
        }

        return result;
    }

}
