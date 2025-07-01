package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "Monhoc")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Monhoc {

    @Id
    @Column(name = "MAMH", length = 5)
    private String maMH;

    @Column(name = "TENMH", length = 40, unique = true, nullable = false)
    private String tenMH;

    // Relationships
    @OneToMany(mappedBy = "monhoc")
    private List<Bode> danhSachBoDe;

    @OneToMany(mappedBy = "monhoc")
    private List<BangDiem> danhSachBangDiem;

    @OneToMany(mappedBy = "monhoc")
    private List<GiaovienDangky> danhSachDangKy;
}