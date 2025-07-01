package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "BANGDIEM", schema = "dbo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BangDiem {

    @EmbeddedId
    private BangDiemId id;

    @MapsId("maSV")
    @ManyToOne
    @JoinColumn(name = "MASV")
    private Sinhvien sinhvien;

    @MapsId("maMH")
    @ManyToOne
    @JoinColumn(name = "MAMH")
    private Monhoc monhoc;

    @Column(name = "NGAYTHI")
    private LocalDate ngayThi;

    @Column(name = "DIEM")
    private Float diem;

    @OneToMany(mappedBy = "bangDiem", cascade = CascadeType.ALL)
    private List<ChiTietBaiThi> danhSachChiTietBaiThi;

    @PrePersist
    @PreUpdate
    public void validateData() {
        // Validate DIEM
        if (diem != null && (diem < 0 || diem > 10)) {
            throw new IllegalArgumentException("DIEM must be between 0 and 10");
        }

        // Validate LAN
        if (id != null && (id.getLan() < 1 || id.getLan() > 2)) {
            throw new IllegalArgumentException("LAN must be 1 or 2");
        }
    }
}