package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.BangDiem;
import com.example.demo.entity.BangDiemId;

@Repository
public interface StudentResultRepository extends JpaRepository<BangDiem, BangDiemId> {
  @Query(value = """
    SELECT
      sv.masv AS studentId,
      sv.ho + ' ' + sv.ten AS studentName,
      bd.ngaythi AS examDate,
      bd.lan AS attempt,
      q.cauhoi AS questionNumber,
      q.noidung AS questionContent,
      q.a AS answerA,
      q.b AS answerB,
      q.c AS answerC,
      q.d AS answerD,
      ct.traloi AS studentAnswer,
      q.dap_an AS correctAnswer
    FROM BangDiem bd
    JOIN Sinhvien sv ON sv.masv = bd.masv
    JOIN ChiTietBaiThi ct ON ct.masv = sv.masv AND ct.lan = bd.lan AND ct.mamh = bd.mamh
    JOIN Bode q ON q.cauhoi = ct.cauhoi AND q.mamh = ct.mamh
    WHERE sv.malop = :classId
      AND bd.mamh = :subjectId
      AND (
          (:level = 'A' AND q.trinhdo IN ('A', 'B')) OR
          (:level = 'B' AND q.trinhdo IN ('B', 'C')) OR
          (:level = 'C' AND q.trinhdo = 'C')
      )
      AND EXISTS (
        SELECT 1 FROM Giaovien_Dangky gvd
        WHERE gvd.magv = :teacherId
        AND gvd.malop = sv.malop
        AND gvd.mamh = bd.mamh
      )
    ORDER BY sv.masv, q.cauhoi
    """, nativeQuery = true)
List<Object[]> getExamResultsByFilter(
@Param("classId") String classId,
@Param("subjectId") String subjectId,
@Param("level") String level,
@Param("teacherId") String teacherId);
}