package com.example.demo.repository;

import com.example.demo.entity.Giaovien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GiaovienRepository extends JpaRepository<Giaovien, String> {
    Optional<Giaovien> findByMagv(String magv);
    //Thêm các phương thức goi stored procedure ở đây nếu cần
   @Query("SELECT g FROM Giaovien g WHERE g.trangThai = true OR g.trangThai IS NULL ORDER BY g.ten, g.ho")
    List<Giaovien> findAllActive();
    
    @Query("SELECT g FROM Giaovien g WHERE (g.trangThai = true OR g.trangThai IS NULL) AND " +
           "(LOWER(g.magv) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.ho) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.ten) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(g.soDienThoai) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Giaovien> searchGiaovien(String keyword);
    
    // Lấy danh sách giáo viên chưa có tài khoản
    @Query("SELECT g FROM Giaovien g WHERE g.hasAccount = false OR g.hasAccount IS NULL")
    List<Giaovien> findGiaoviensWithoutAccounts();
}