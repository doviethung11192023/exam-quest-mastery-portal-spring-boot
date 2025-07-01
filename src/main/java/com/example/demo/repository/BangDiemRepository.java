// package com.example.demo.repository;

// import com.example.demo.entity.BangDiem;
// import com.example.demo.entity.BangDiemId;
// import com.example.demo.entity.Sinhvien;

// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;
// import org.springframework.stereotype.Repository;

// import java.time.LocalDate;
// import java.util.List;
// import java.util.Optional;

// @Repository
// public interface BangDiemRepository extends JpaRepository<BangDiem, BangDiemId> {
//     // Add this method to your BangDiemRepository interface:
   
//     boolean existsBySinhvienMaSV(String maSV);
//     boolean existsByMonhocMaMH(String maMH);

//     List<BangDiem> findBySinhvienMaSV(String maSV);

//     List<BangDiem> findByMonhocMaMH(String maMH);

//     List<BangDiem> findBySinhvienMaSVAndMonhocMaMH(String maSV, String maMH);

//     @Query("SELECT AVG(b.diem) FROM BangDiem b WHERE b.monhoc.maMH = ?1 AND b.sinhvien.lop.maLop = ?2")
//     Float getAverageScoreBySubjectAndClass(String maMH, String maLop);

//     @Query("SELECT b FROM BangDiem b WHERE b.ngayThi BETWEEN ?1 AND ?2")
//     List<BangDiem> findByDateRange(LocalDate startDate, LocalDate endDate);
    
//     @Query("SELECT b FROM BangDiem b WHERE b.sinhvien = ?1 AND b.id.maMH = ?2 AND b.id.lan = ?3")
//     Optional<BangDiem> findBySinhvienAndMamhAndLan(Sinhvien sinhvien, String maMH, int lan);
//     @Query(value = """
// SELECT 
//     sv.masv AS studentId,
//     sv.ho AS lastName,
//     sv.ten AS firstName,
//     bd.diem AS score,
//     CASE
//         WHEN bd.diem >= 9 THEN 'A'
//         WHEN bd.diem >= 8 THEN 'B+'
//         WHEN bd.diem >= 7 THEN 'B'
//         WHEN bd.diem >= 6.5 THEN 'C+'
//         WHEN bd.diem >= 5.5 THEN 'C'
//         WHEN bd.diem >= 5 THEN 'D'
//         ELSE 'F'
//     END AS gradeLetter
// FROM bangdiem bd
// JOIN sinhvien sv ON sv.masv = bd.masv
// WHERE sv.malop = :classId
//   AND bd.mamh = :subjectId
//   AND bd.lan = :examAttempt
// ORDER BY sv.masv
// """, nativeQuery = true)
// List<Object[]> findGradeRecords(
//     @Param("classId") String classId,
//     @Param("subjectId") String subjectId,
//     @Param("examAttempt") int examAttempt
// );

// }
package com.example.demo.repository;

import com.example.demo.entity.BangDiem;
import com.example.demo.entity.BangDiemId;
import com.example.demo.entity.Sinhvien;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BangDiemRepository extends JpaRepository<BangDiem, BangDiemId> {
    // Add this method to your BangDiemRepository interface:

    boolean existsBySinhvienMaSV(String maSV);

    boolean existsByMonhocMaMH(String maMH);

    List<BangDiem> findBySinhvienMaSV(String maSV);

    List<BangDiem> findByMonhocMaMH(String maMH);

    List<BangDiem> findBySinhvienMaSVAndMonhocMaMH(String maSV, String maMH);

    @Query("SELECT AVG(b.diem) FROM BangDiem b WHERE b.monhoc.maMH = ?1 AND b.sinhvien.lop.maLop = ?2")
    Float getAverageScoreBySubjectAndClass(String maMH, String maLop);

    @Query("SELECT b FROM BangDiem b WHERE b.ngayThi BETWEEN ?1 AND ?2")
    List<BangDiem> findByDateRange(LocalDate startDate, LocalDate endDate);

    @Query("SELECT b FROM BangDiem b WHERE b.sinhvien = ?1 AND b.id.maMH = ?2 AND b.id.lan = ?3")
    Optional<BangDiem> findBySinhvienAndMamhAndLan(Sinhvien sinhvien, String maMH, int lan);

    @Query(value = """
            SELECT
                sv.masv AS studentId,
                sv.ho AS lastName,
                sv.ten AS firstName,
                ISNULL(bd.diem, 0) AS score,
                CASE
                    WHEN bd.diem IS NULL THEN N'Chưa thi'
                    WHEN bd.diem >= 9 THEN 'A'
                    WHEN bd.diem >= 8 THEN 'B+'
                    WHEN bd.diem >= 7 THEN 'B'
                    WHEN bd.diem >= 6.5 THEN 'C+'
                    WHEN bd.diem >= 5.5 THEN 'C'
                    WHEN bd.diem >= 5 THEN 'D'
                    ELSE 'F'
                END AS gradeLetter
                
            FROM sinhvien sv
            LEFT JOIN bangdiem bd
                ON sv.masv = bd.masv
                AND bd.mamh = :subjectId
                AND bd.lan = :examAttempt
            WHERE sv.malop = :classId
            ORDER BY sv.masv
            """, nativeQuery = true)
    List<Object[]> findGradeRecords(
            @Param("classId") String classId,
            @Param("subjectId") String subjectId,
            @Param("examAttempt") int examAttempt);
}