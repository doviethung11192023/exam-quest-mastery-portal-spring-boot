package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "Lop")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Lop {

    @Id
    @Column(name = "MALOP", length = 8)
    private String maLop;

    @Column(name = "TENLOP", length = 40, unique = true, nullable = false)
    private String tenLop;

    // Relationships
    @OneToMany(mappedBy = "lop", cascade = CascadeType.ALL)
    private List<Sinhvien> danhSachSinhVien;

    @OneToMany(mappedBy = "lop", cascade = CascadeType.ALL)
    private List<GiaovienDangky> danhSachDangKy;
}