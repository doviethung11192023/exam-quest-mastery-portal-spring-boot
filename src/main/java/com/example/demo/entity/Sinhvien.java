package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "Sinhvien")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sinhvien {

    @Id
    @Column(name = "MASV", length = 8)
    private String maSV;

    @Column(name = "HO", length = 40)
    private String ho;

    @Column(name = "TEN", length = 10)
    private String ten;

    @Column(name = "NGAYSINH")
    private LocalDate ngaySinh;

    @Column(name = "DIACHI", length = 100)
    private String diaChi;

    @ManyToOne
    @JoinColumn(name = "MALOP")
    private Lop lop;

    // Relationships
    @OneToMany(mappedBy = "sinhvien", cascade = CascadeType.ALL)
    private List<BangDiem> danhSachBangDiem;

    // Helper method to get full name
    @Transient
    public String getHoTen() {
        return this.ho + " " + this.ten;
    }
}