package com.example.demo.repository;

import com.example.demo.entity.Bode;
import com.example.demo.entity.BodeId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BodeRepository extends JpaRepository<Bode, BodeId> {
    // Thêm phương thức này vào BoDeRepository nếu có
    boolean existsByMonhocMaMH(String maMH);
    List<Bode> findAllById(BodeId id);
    
    @Query("SELECT b FROM Bode b WHERE b.id.maMH = ?1")
    List<Bode> findByIdMaMH(String maMH);
    // Removed invalid line as bodeRepository and submission are undefined in this context.
    // Tìm câu hỏi theo mã môn học
    List<Bode> findByMonhocMaMH(String maMH);

    // Tìm câu hỏi theo mã môn học và có phân trang
    Page<Bode> findByMonhocMaMH(String maMH, Pageable pageable);

    // Tìm câu hỏi theo mã giáo viên (phân quyền)
    List<Bode> findByGiaovienMagv(String magv);

    // Tìm câu hỏi theo mã giáo viên và có phân trang
    Page<Bode> findByGiaovienMagv(String magv, Pageable pageable);

    // Tìm câu hỏi theo mã môn học và mã giáo viên (phân quyền)
    List<Bode> findByMonhocMaMHAndGiaovienMagv(String maMH, String magv);

    // Thêm phương thức này vào BodeRepository

    @Query("SELECT COUNT(b) FROM Bode b WHERE b.monhoc.maMH = :maMH AND b.trinhDo = :trinhDo")
    int countQuestionsBySubjectAndLevel(@Param("maMH") String maMH, @Param("trinhDo") String trinhDo);
    // Tìm câu hỏi theo mã môn học và mã giáo viên có phân trang
    Page<Bode> findByMonhocMaMHAndGiaovienMagv(String maMH, String magv, Pageable pageable);

    // Tìm câu hỏi theo mã môn học và trình độ
    List<Bode> findByMonhocMaMHAndTrinhDo(String maMH, String trinhDo);

    // Tìm kiếm câu hỏi theo nội dung
    @Query("SELECT b FROM Bode b WHERE LOWER(b.noiDung) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Bode> searchByNoiDungContaining(@Param("keyword") String keyword);

    // Tìm kiếm câu hỏi theo nội dung và môn học
    @Query("SELECT b FROM Bode b WHERE LOWER(b.noiDung) LIKE LOWER(CONCAT('%', :keyword, '%')) AND b.monhoc.maMH = :maMH")
    List<Bode> searchByNoiDungContainingAndMonhocMaMH(@Param("keyword") String keyword, @Param("maMH") String maMH);

    // Tìm kiếm câu hỏi theo nội dung, môn học và giáo viên (phân quyền)
    @Query("SELECT b FROM Bode b WHERE LOWER(b.noiDung) LIKE LOWER(CONCAT('%', :keyword, '%')) AND b.monhoc.maMH = :maMH AND b.giaovien.magv = :magv")
    List<Bode> searchByNoiDungContainingAndMonhocMaMHAndGiaovienMagv(
            @Param("keyword") String keyword,
            @Param("maMH") String maMH,
            @Param("magv") String magv);

    // Lấy số thứ tự câu hỏi tiếp theo cho môn học
    @Query("SELECT COALESCE(MAX(b.id.cauHoi), 0) + 1 FROM Bode b WHERE b.monhoc.maMH = :maMH")
    Integer getNextQuestionNumber(@Param("maMH") String maMH);

    // Lấy câu hỏi ngẫu nhiên theo môn học và trình độ
    @Query(value = "SELECT b.* FROM Bode b WHERE b.MAMH = ?1 AND b.TRINHDO = ?2 ORDER BY NEWID() OFFSET 0 ROWS FETCH NEXT ?3 ROWS ONLY", nativeQuery = true)
    List<Bode> getRandomQuestionsBySubjectAndLevel(String maMH, String trinhDo, int limit);

    // Tìm câu hỏi theo ID (mã môn học và số thứ tự)
    Optional<Bode> findByMonhocMaMHAndIdCauHoi(String maMH, Integer cauHoi);
    
    @Query(value = "SELECT TOP(?3) * FROM BODE WHERE MAMH = ?1 AND TRINHDO = ?2 ORDER BY NEWID()", nativeQuery = true)
    List<Bode> findRandomQuestionsForExam(String maMH, String trinhDo, int count);
}