package com.example.demo.repository;

import com.example.demo.entity.Monhoc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonhocRepository extends JpaRepository<Monhoc, String> {

    // Tìm môn học theo mã môn học
    Monhoc findByMaMH(String maMH);

    // Tìm môn học theo mã hoặc tên (case insensitive)
    List<Monhoc> findByMaMHLikeOrTenMHLikeIgnoreCase(String maMH, String tenMH);

    // Tìm các môn học mà giáo viên đã tạo câu hỏi
    @Query("SELECT DISTINCT b.monhoc FROM Bode b WHERE b.giaovien.magv = :magv")
    List<Monhoc> findByGiaoVienCoCauHoi(@Param("magv") String magv);

    // Tìm các môn học mà giáo viên đăng ký dạy
    @Query("SELECT DISTINCT gvdk.monhoc FROM GiaovienDangky gvdk WHERE gvdk.giaovien.magv = :magv")
    List<Monhoc> findByGiaoVienDangKy(@Param("magv") String magv);
}