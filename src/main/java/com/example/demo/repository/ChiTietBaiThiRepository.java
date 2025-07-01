package com.example.demo.repository;

import com.example.demo.entity.ChiTietBaiThi;
import com.example.demo.entity.ChiTietBaiThiId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietBaiThiRepository extends JpaRepository<ChiTietBaiThi, ChiTietBaiThiId> {
        // Thêm phương thức này vào BaiThiRepository nếu có
        // Thay thế phương thức gây lỗi bằng query JPQL rõ ràng
        @Query("SELECT COUNT(c) > 0 FROM ChiTietBaiThi c WHERE c.id.maMH = :maMH AND c.id.cauHoi = :cauHoi")
        boolean existsByMaMHAndCauHoi(@Param("maMH") String maMH, @Param("cauHoi") Integer cauHoi);

    @Query("SELECT c FROM ChiTietBaiThi c WHERE c.id.maSV = :maSV AND c.id.maMH = :maMH AND c.id.lan = :lan")
    List<ChiTietBaiThi> findByStudentAndSubjectAndAttempt(
            @Param("maSV") String maSV,
            @Param("maMH") String maMH,
            @Param("lan") Integer lan);

    @Query("SELECT COUNT(c) FROM ChiTietBaiThi c WHERE c.id.maSV = :maSV AND c.id.maMH = :maMH AND c.id.lan = :lan AND c.traLoi = c.bode.dapAn")
    int countCorrectAnswers(
            @Param("maSV") String maSV,
            @Param("maMH") String maMH,
            @Param("lan") Integer lan);
}