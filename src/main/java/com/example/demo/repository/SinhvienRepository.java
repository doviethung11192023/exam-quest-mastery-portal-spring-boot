package com.example.demo.repository;

import com.example.demo.entity.Sinhvien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


import com.example.demo.entity.Sinhvien;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SinhvienRepository extends JpaRepository<Sinhvien, String> {
    Optional<Sinhvien> findByMaSV(String maSV);

    List<Sinhvien> findByLopMaLop(String maLop);

    List<Sinhvien> findByHoContainingOrTenContaining(String ho, String ten);
     List<Sinhvien> findByLop_MaLop(String maLop);
    
    Page<Sinhvien> findByLop_MaLop(String maLop, Pageable pageable);
    
    @Query("SELECT s FROM Sinhvien s WHERE s.lop.maLop = :maLop " +
           "AND (LOWER(s.maSV) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(s.ho) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(s.ten) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Sinhvien> searchByClassAndKeyword(String maLop, String searchTerm, Pageable pageable);
    
    @Query("SELECT s FROM Sinhvien s WHERE s.lop.maLop = :maLop " +
           "AND (LOWER(s.maSV) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(s.ho) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(s.ten) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Sinhvien> searchByClassAndKeyword(String maLop, String searchTerm);
}