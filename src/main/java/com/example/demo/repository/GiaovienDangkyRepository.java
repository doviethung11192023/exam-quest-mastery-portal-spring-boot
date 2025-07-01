package com.example.demo.repository;

import com.example.demo.entity.GiaovienDangky;
import com.example.demo.entity.GiaovienDangkyId;
import com.example.demo.entity.Lop;
import com.example.demo.entity.Monhoc;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GiaovienDangkyRepository extends JpaRepository<GiaovienDangky, GiaovienDangkyId> {
        // Thêm 2 phương thức này vào interface của repository
@Query("SELECT DISTINCT l FROM Lop l JOIN GiaovienDangky gvd ON gvd.lop.maLop = l.maLop WHERE gvd.giaovien.magv = :magv")
List<Lop> findClassesByTeacher(@Param("magv") String magv);

@Query("SELECT DISTINCT mh FROM Monhoc mh JOIN GiaovienDangky gvd ON gvd.monhoc.maMH = mh.maMH WHERE gvd.giaovien.magv = :magv")
List<Monhoc> findSubjectsByTeacher(@Param("magv") String magv);
        // Thêm phương thức này vào GiaovienDangkyRepository
        boolean existsByMonhocMaMH(String maMH);
        // @Query("SELECT g FROM GiaovienDangky g WHERE g.malop = ?1 AND g.mamh = ?2 AND
        // g.lan = ?3")
        // Optional<GiaovienDangky> findByMalopAndMamhAndLan(String maLop, String maMH,
        // int lan);

        // Sửa từ "MaGV" thành "magv" để khớp với tên thuộc tính trong entity Giaovien
        List<GiaovienDangky> findByGiaovien_Magv(String magv);

        @Query("SELECT gvdk FROM GiaovienDangky gvdk WHERE gvdk.lop.maLop = :maLop AND gvdk.monhoc.maMH = :maMH AND gvdk.id.lan = :lan")
        Optional<GiaovienDangky> findByMalopAndMamhAndLan(@Param("maLop") String maLop, @Param("maMH") String maMH,
                        @Param("lan") Short lan);

        @Query("SELECT COUNT(gvdk) FROM GiaovienDangky gvdk WHERE gvdk.lop.maLop = :maLop AND gvdk.monhoc.maMH = :maMH AND gvdk.id.lan = :lan")
        int countByLopAndMonHocAndLan(@Param("maLop") String maLop, @Param("maMH") String maMH,
                        @Param("lan") Short lan);

        @Query("SELECT gvdk FROM GiaovienDangky gvdk " +
                        "JOIN FETCH gvdk.lop " +
                        "JOIN FETCH gvdk.monhoc " +
                        "JOIN FETCH gvdk.giaovien " +
                        "WHERE (:maLop IS NULL OR gvdk.lop.maLop = :maLop) " +
                        "AND (:maMH IS NULL OR gvdk.monhoc.maMH = :maMH) " +
                        "AND (:maGV IS NULL OR gvdk.giaovien.magv = :maGV)")
        List<GiaovienDangky> searchExams(
                        @Param("maLop") String maLop,
                        @Param("maMH") String maMH,
                        @Param("maGV") String maGV);

        // Sửa từ malop -> maLop và mamh -> maMH để khớp với tên thuộc tính trong entity
        @Query("SELECT g FROM GiaovienDangky g WHERE g.lop.maLop = ?1 AND g.monhoc.maMH = ?2 AND g.id.lan = ?3")
        Optional<GiaovienDangky> findByMaLopAndMaMHAndLan(String maLop, String maMH, short lan);

        @Query("SELECT g FROM GiaovienDangky g WHERE g.lop.maLop = ?1 AND g.monhoc.maMH = ?2")
        List<GiaovienDangky> findByMaLopAndMaMH(String maLop, String maMH);

        @Query("SELECT DISTINCT g.monhoc.maMH FROM GiaovienDangky g WHERE g.lop.maLop = ?1")
        List<String> findDistinctSubjectsByClass(String maLop);
}