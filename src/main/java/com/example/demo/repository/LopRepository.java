package com.example.demo.repository;

import com.example.demo.entity.Lop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LopRepository extends JpaRepository<Lop, String> {
    List<Lop> findByTenLopContainingIgnoreCase(String tenLop);

    List<Lop> findByMaLopContainingIgnoreCase(String maLop);
}