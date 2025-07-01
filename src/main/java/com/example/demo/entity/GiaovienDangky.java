package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Giaovien_Dangky")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GiaovienDangky {

    @EmbeddedId
    private GiaovienDangkyId id;

    @ManyToOne
    @JoinColumn(name = "MAGV")
    private Giaovien giaovien;

    @MapsId("maLop")
    @ManyToOne
    @JoinColumn(name = "MALOP")
    private Lop lop;

    @MapsId("maMH")
    @ManyToOne
    @JoinColumn(name = "MAMH")
    private Monhoc monhoc;

    @Column(name = "TRINHDO", length = 1)
    private String trinhDo;

    @Column(name = "NGAYTHI")
    private LocalDateTime ngayThi;

    @Column(name = "SOCAUTHI")
    private Short soCauThi;

    @Column(name = "THOIGIAN")
    private Short thoiGian;

    @PrePersist
    @PreUpdate
    public void validateData() {
        if (trinhDo != null && !"A".equals(trinhDo) && !"B".equals(trinhDo) && !"C".equals(trinhDo)) {
            throw new IllegalArgumentException("TRINHDO must be A, B or C");
        }
        if (soCauThi != null && (soCauThi < 10 || soCauThi > 100)) {
            throw new IllegalArgumentException("SOCAUTHI must be between 10 and 100");
        }
        // Validate THOIGIAN
        if (thoiGian != null && (thoiGian < 5 || thoiGian > 60)) {
            throw new IllegalArgumentException("THOIGIAN must be between 5 and 60 minutes");
        }
        // Validate NGAYTHI
        if (thoiGian != null && (thoiGian < 5 || thoiGian > 60)) {
            throw new IllegalArgumentException("THOIGIAN must be between 5 and 60 minutes");
        }
        //validate LAN
        if (id.getLan() < 1 || id.getLan() > 2) {
            throw new IllegalArgumentException("LAN must be 1 or 2");
        }
    }

   

   
}

